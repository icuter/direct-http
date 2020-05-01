package cn.icuter.directhttp.response;

import cn.icuter.directhttp.response.ChunkedInputStream;
import cn.icuter.directhttp.response.HttpResponseMessage;
import cn.icuter.directhttp.utils.IOUtils;
import cn.icuter.directhttp.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ChunkedInputStreamTest {

    private byte[] chunkBody1 = "123456\n789".getBytes(StandardCharsets.UTF_8);
    private byte[] chunkBody2 = "谁谁谁坎坎坷\n坷来了来了".getBytes(StandardCharsets.UTF_8);
    private byte[] chunkBody3 = "abc\nDEFG".getBytes(StandardCharsets.UTF_8);
    private byte[] chunkBody4 = "The\nEND".getBytes(StandardCharsets.UTF_8);

    @Test
    public void testWithTrailer() throws IOException {
        byte[] data = createDataBytes(null,"x-date: 2020-03-27 12:05:58");
        ChunkedInputStream in = ChunkedInputStream.of(new ByteArrayInputStream(data));
        in.setHasTrailerResponseHeader();
        in.responseMessage = new HttpResponseMessage();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.readBytesTo(in, out);

        assertChunkBody(out);
        Assert.assertEquals(0, in.available());

        int contentLength = Integer.parseInt(
                (String) in.responseMessage.getHeaders().getOrDefault("content-length", "0"));
        Assert.assertEquals(in.getContentLength(), contentLength);
        Assert.assertEquals("2020-03-27 12:05:58", in.responseMessage.getHeaders().get("x-date"));
    }

    @Test
    public void testNoTrailer() throws IOException {
        byte[] data = createDataBytes(null);
        InputStream in = ChunkedInputStream.of(new ByteArrayInputStream(data));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int b = in.read(); b > 0; b = in.read()) {
            out.write(b);
        }

        assertChunkBody(out);
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testExistsInvalidEnding() throws IOException {
        byte[] data = createDataBytes(new byte[]{97, 98, 99});
        InputStream in = ChunkedInputStream.of(new ByteArrayInputStream(data));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int b = in.read(); b > 0; b = in.read()) {
            out.write(b);
        }

        assertChunkBody(out);
        Assert.assertEquals(0, in.available());
    }

    private void assertChunkBody(ByteArrayOutputStream out) {
        Assert.assertEquals(StringUtils.decodeAsUTF8(chunkBody1)
                        + StringUtils.decodeAsUTF8(chunkBody2)
                        + StringUtils.decodeAsUTF8(chunkBody3)
                        + StringUtils.decodeAsUTF8(chunkBody4),
                StringUtils.decodeAsUTF8(out.toByteArray()));
    }

    private byte[] createDataBytes(byte[] invalidBytes, String... trailers) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeChunkData(out, "; ex-param1=ex-value1", chunkBody1);
        writeChunkData(out, " ;ex-param2=ex-value2", chunkBody2);
        writeChunkData(out, "; ", chunkBody3);
        writeChunkData(out, "", chunkBody4);
        // last chunk
        out.write(new byte[] {'0','\r','\n'});
        if (trailers != null) {
            for (String trailer : trailers) {
               out.write(trailer.getBytes(StandardCharsets.ISO_8859_1));
               out.write(new byte[]{'\r', '\n'});
            }
        }
        // final CRLF
        out.write(new byte[] {'\r', '\n'});
        if (invalidBytes != null) {
            out.write(invalidBytes);
        }
        return out.toByteArray();
    }

    private void writeChunkData(ByteArrayOutputStream dataStream,
                                String chunkSizeExtension, byte[] chunkBody) throws IOException {
        Objects.requireNonNull(chunkSizeExtension);

        dataStream.write(
                (Integer.toHexString(chunkBody.length) + chunkSizeExtension + "\r\n")
                        .getBytes(StandardCharsets.ISO_8859_1));
        dataStream.write(chunkBody);
        dataStream.write(new byte[]{'\r', '\n'});
    }
}
