package cn.icuter.directhttp.transport;

import cn.icuter.directhttp.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * For reading chunked http response message body, and non fixed length specification,
 * please refer to <a href="https://tools.ietf.org/html/rfc2616#section-3.6">rfc2616#section-3.6</a>.
 *
 * <br />
 *
 * Decoding Chunked Response Algorithm,
 * refer to <a href="https://tools.ietf.org/html/rfc7230#page-38">rfc7230#page-38</a>.
 * <pre>
 * 4.1.3.  Decoding Chunked
 *
 *    A process for decoding the chunked transfer coding can be represented
 *    in pseudo-code as:
 *
 *      length := 0
 *      read chunk-size, chunk-ext (if any), and CRLF
 *      while (chunk-size > 0) {
 *         read chunk-data and CRLF
 *         append chunk-data to decoded-body
 *         length := length + chunk-size
 *         read chunk-size, chunk-ext (if any), and CRLF
 *      }
 *      read trailer field
 *      while (trailer field is not empty) {
 *         if (trailer field is allowed to be sent in a trailer) {
 *             append trailer field to existing header fields
 *         }
 *         read trailer-field
 *      }
 *      Content-Length := length
 *      Remove "chunked" from Transfer-Encoding
 *      Remove Trailer from existing header fields
 * </pre>
 *
 * @author edward leejan
 */
public class ChunkedInputStream extends FilterInputStream {

    enum ReadingState {
        /** reading chunk-size that indicates the chunk-data size */
        SIZE,
        /** reading chunk-data */
        DATA,
        /** found the ending characters of "0\r\n" and read the chunk trailer */
        TRAILER,
        /** finished reading */
        DONE
    }

    private ReadingState state = ReadingState.SIZE;
    private int remainingChunkDataSize;
    private int contentLength;
    private volatile boolean closed;
    private boolean eos; // end of stream

    /**
     *  Request must contains "TE: trailers" header to indicate that chunk-trailer is acceptable
     *  <a href="https://tools.ietf.org/html/rfc7230#page-37">rfc7230 Chunked Trailer Part</a>
     *
     *  <blockquote>
     *  Unless the request includes a TE header field indicating "trailers"
     *  is acceptable, as described in Section 4.3, a server SHOULD NOT
     *  generate trailer fields that it believes are necessary for the user
     *  agent to receive.  Without a TE containing "trailers", the server
     *  ought to assume that the trailer fields might be silently discarded
     *  along the path to the user agent.  This requirement allows
     *  intermediaries to forward a de-chunked message to an HTTP/1.0
     *  recipient without buffering the entire response.
     *  </blockquote>
     */
    private boolean hasTrailerResponseHeader;
    private byte[] temp = new byte[1];

    /** Relate to the Http Response Message */
    HttpResponseMessage responseMessage;

    private ChunkedInputStream(InputStream in) {
        super(in);
        if (!(in instanceof BufferedInputStream)) {
            this.in = new BufferedInputStream(in);
        }
    }

    public static ChunkedInputStream of(InputStream in) {
        return new ChunkedInputStream(in);
    }

    @Override
    public int read() throws IOException {
        int read = read(temp, 0, 1);
        // why temp[0] & 0xff ?
        //   for read() spec, we have to return positive number if data found,
        //   but UTF-8 encoding character contains negative byte value.
        //   Such as Chinese Character "帅" saving byte array with [-27, -72, -123],
        //   so -27 & 0xff will return 229 to satisfy.
        //   The spec describes "The value byte is returned as an int in the range 0 to 255"
        //   and OutputStream writing ((byte) 229) equals to OutputStream writing -27
        return read <= 0 ? -1 : temp[0] & 0xff;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (closed) {
            throw new IOException("Stream closed!");
        }
        if (eos) {
            return -1;
        }
        if (!prepareChunkDataReading()) {
            return -1;
        }

        int read = in.read(b, off, Math.min(len, remainingChunkDataSize));
        if (read > 0) {
            remainingChunkDataSize -= read;
        } else {
            eos = true;
        }
        return read;
    }

    private boolean prepareChunkDataReading() throws IOException {
        if (state == ReadingState.DATA && remainingChunkDataSize == 0) {
            // the chunk body ending line
            readCRLF();

            state = ReadingState.SIZE;
        }
        if (state == ReadingState.SIZE) {
            // read chunk size representing the chunk body size
            readChunkSize();

            state = ReadingState.DATA;
            if (remainingChunkDataSize == 0) {
                state = ReadingState.TRAILER;
            }
        }
        if (state == ReadingState.TRAILER) {
            // read all trailer data and put into Http Response Header
            readTrailerToResponseHeader();
            setContentLength();
            IOUtils.tryCleanupRemaining(in);

            state = ReadingState.DONE;
        }
        // return false if ReadingState is DONE
        // return true if ReadingState is NOT DONE
        return state != ReadingState.DONE;
    }

    private void readChunkSize() throws IOException {
        byte[] chunkSizeBytes = IOUtils.readLine(in);
        if (chunkSizeBytes.length <= 0) {
            throw new IllegalStateException("Illegal chunked http response message body !");
        }
        // read the line of chunk-size, but ignore the chunk-extensions
        // last-chunk = 1*("0") [ chunk-extension ] CRLF
        remainingChunkDataSize = getChunkDataSize(chunkSizeBytes);
        contentLength += remainingChunkDataSize;
    }

    private void readTrailerToResponseHeader() throws IOException {
        for (byte[] bytes = IOUtils.readLine(in); bytes.length > 0; bytes = IOUtils.readLine(in)) {
            if (responseMessage == null || !hasTrailerResponseHeader) {
                continue;
            }
            String header = new String(bytes, StandardCharsets.ISO_8859_1);
            // TODO inefficiently parse http response header line
            int colonIndex = header.indexOf(":");
            if (colonIndex >= 0) {
                responseMessage.getHeaders().put(header.substring(0, colonIndex).trim().toLowerCase(),
                        header.substring(colonIndex + 1).trim());
            } else {
                responseMessage.getHeaders().put(header, "");
            }
        }
    }

    private void setContentLength() {
        if (responseMessage != null) {
            responseMessage.getHeaders().put("content-length", String.valueOf(contentLength));
        }
    }

    private int getChunkDataSize(byte[] chunkSizeBytes) {
        for (int i = 0; i < chunkSizeBytes.length; i++) {
            if (chunkSizeBytes[i] == ' ' || chunkSizeBytes[i] == ';') {
                return Integer.parseInt(
                        new String(chunkSizeBytes, 0, i, StandardCharsets.ISO_8859_1), 16);
            }
        }
        return Integer.parseInt(
                new String(chunkSizeBytes, StandardCharsets.ISO_8859_1), 16);
    }

    int getContentLength() {
        return contentLength;
    }

    private void readCRLF() throws IOException {
        int b = in.read();
        if (b != '\r') {
            throw new IOException("Invalid chunk-data end char \"" + b + "\" !");
        }
        b = in.read();
        if (b != '\n') {
            throw new IOException("Invalid chunk-data body end char \"" + b + "\" !");
        }
    }

    void setHasTrailerResponseHeader() {
        hasTrailerResponseHeader = true;
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }
}
