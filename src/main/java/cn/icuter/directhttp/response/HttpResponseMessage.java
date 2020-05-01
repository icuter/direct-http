package cn.icuter.directhttp.response;

import cn.icuter.directhttp.utils.HeaderUtils;
import cn.icuter.directhttp.utils.IOUtils;
import cn.icuter.directhttp.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * <pre>
 *   Response = Status-Line
 *              *(( general-header
 *              | response-header
 *              | entity-header ) CRLF)
 *              CRLF
 *              [ message-body ]
 *  </pre>
 */
public class HttpResponseMessage {
    private String statusLine;
    private Map<String, Object> headers = new HashMap<>();
    private Map<String, Object> cookies = new HashMap<>();
    private InputStream messageBodyStream;
    private String messageBody;
    int contentLength;
    private boolean chunkBody;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static HttpResponseMessage loadFromStream(InputStream in) throws IOException {
        HttpResponseMessage response = new HttpResponseMessage();
        response.statusLine = StringUtils.decodeAsISO(IOUtils.readLine(in));
        for (byte[] line = IOUtils.readLine(in); line.length > 0; line = IOUtils.readLine(in)) {
            String header = StringUtils.decodeAsISO(line);
            // TODO inefficiently parse http response header line
            int colonIndex = header.indexOf(":");
            if (colonIndex >= 0) {
                String headerName = header.substring(0, colonIndex).trim().toLowerCase();
                if (headerName.equals("set-cookie") || headerName.equals("set-cookie2")) {
                    List<HttpCookie> cookieList = HttpCookie.parse(header);
                    for (HttpCookie cookie : cookieList) {
                        response.cookies.put(cookie.getName(), cookie);
                    }
                } else {
                    response.headers.put(headerName, header.substring(colonIndex + 1).trim());
                }
            } else {
                response.headers.put(header, "");
            }
        }
        String connection = (String) response.headers.getOrDefault("connection", "keep-alvie");
        boolean connClose = "close".equalsIgnoreCase(connection);
        // non fixed content length response, especially zip attachment content response
        // TODO compatible with transfer-encoding: gzip, chunked
        if (response.headers.containsKey("transfer-encoding")) {
            ChunkedInputStream chunkedIn = ChunkedInputStream.of(in);
            if (response.headers.containsKey("trailer")) {
                chunkedIn.setHasTrailerResponseHeader();
            }
            if (connClose) {
                chunkedIn.setCloseReadAll();
            }
            // bi-association
            chunkedIn.responseMessage = response;
            response.messageBodyStream = chunkedIn;
            response.chunkBody = true;
        } else {
            response.contentLength = Integer.parseInt(
                    (String) response.headers.getOrDefault("content-length", "0"));
            ContentLengthInputStream clIn = ContentLengthInputStream.of(in);
            if (connClose) {
                clIn.setCloseReadAll();
            }
            if (response.contentLength == 0 && connClose) {
                IOUtils.tryCleanupRemaining(in);
                in.close();
            }
            response.messageBodyStream = clIn;
        }
        if (response.headers.containsKey("content-encoding")) {
            String contentEnc = (String) response.headers.get("content-encoding");
            if ("gzip".equalsIgnoreCase(contentEnc)) {
                response.messageBodyStream = new GZIPInputStream(response.messageBodyStream);
            } else if ("deflate".equalsIgnoreCase(contentEnc)) {
                response.messageBodyStream = new InflaterInputStream(response.messageBodyStream);
            } else {
                throw new UnsupportedEncodingException("Unacceptable Content-Encoding \"" + contentEnc + "\"");
            }
        }
        return response;
    }

    public String getMessageBody() throws IOException {
        if (messageBody != null) {
            return messageBody;
        }
        if (!chunkBody && contentLength == 0) {
            return (messageBody = "");
        }
        int bufferSize = contentLength > 0 ? contentLength : 16384;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(bufferSize)) {
            IOUtils.readBytesTo(messageBodyStream, out, bufferSize);
            byte[] bytes = out.toByteArray();
            /*
            * Compatible with content encoding with gzip and deflate
            if (bytes.length != contentLength) {
                throw new IllegalStateException("The http response header of Content-Length incorrect, content-length="
                        + contentLength + ", but actually message body length is " + bytes.length);
            }*/
            String contentType = (String) headers.getOrDefault("content-type", "text/plain");
            String contentCharset = HeaderUtils.findHeaderParamValue(contentType, "charset", DEFAULT_CHARSET.name());
            return (messageBody = StringUtils.decodeAs(bytes, Charset.forName(contentCharset)));
        }
    }

    public int getContentLength() {
        return contentLength;
    }

    public Map<String, Object> getCookies() {
        return cookies;
    }

    public String getStatusLine() {
        return statusLine;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public InputStream getMessageBodyStream() {
        return messageBodyStream;
    }
}
