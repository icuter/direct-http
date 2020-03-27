package cn.icuter.directhttp.transport;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpResponseMessageTest {

    @Test
    public void testContentLength() throws IOException {
        byte[] content = "Hello Direct Http 超轻量的 HTTP 框架".getBytes(StandardCharsets.UTF_8);
        byte[] header = ("HTTP/1.1 200 OK\r\n"
                + "Test-Header: Test-Header-Value\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "content-length: " + content.length + "\r\n"
                + "\r\n")
                .getBytes(StandardCharsets.ISO_8859_1);
        byte[] data = new byte[header.length + content.length];
        System.arraycopy(header, 0, data, 0, header.length);
        System.arraycopy(content, 0, data, header.length, content.length);

        ByteArrayInputStream in = new ByteArrayInputStream(data);
        HttpResponseMessage responseMessage = HttpResponseMessage.loadFromStream(in);
        Assert.assertEquals("HTTP/1.1 200 OK", responseMessage.getStatusLine());
        Assert.assertEquals("Test-Header-Value", String.valueOf(responseMessage.getHeaders().get("test-header")));
        Assert.assertEquals("text/plain; charset=UTF-8", String.valueOf(responseMessage.getHeaders().get("content-type")));
        Assert.assertEquals(content.length, responseMessage.getContentLength());
        Assert.assertEquals(new String(content, StandardCharsets.UTF_8), responseMessage.getMessageBody());
    }

    @Test
    public void testDefaultContentEncoding() throws IOException {
        byte[] content;
        byte[] header;
        byte[] data;// default content charset UTF-8
        content = "Hello Direct Http 超轻量的 HTTP 框架".getBytes(StandardCharsets.UTF_8);
        header = ("HTTP/1.1 200 OK\r\n"
                + "Test-Header: Test-Header-Value\r\n"
                + "Content-Type: text/plain\r\n"
                + "content-length: " + content.length + "\r\n"
                + "\r\n")
                .getBytes(StandardCharsets.ISO_8859_1);
        data = new byte[header.length + content.length];
        System.arraycopy(header, 0, data, 0, header.length);
        System.arraycopy(content, 0, data, header.length, content.length);

        ByteArrayInputStream in = new ByteArrayInputStream(data);
        HttpResponseMessage responseMessage = HttpResponseMessage.loadFromStream(in);
        Assert.assertEquals("HTTP/1.1 200 OK", responseMessage.getStatusLine());
        Assert.assertEquals("Test-Header-Value", String.valueOf(responseMessage.getHeaders().get("test-header")));
        Assert.assertEquals("text/plain", String.valueOf(responseMessage.getHeaders().get("content-type")));
        Assert.assertEquals(content.length, responseMessage.getContentLength());
        Assert.assertEquals(new String(content, StandardCharsets.UTF_8), responseMessage.getMessageBody());
    }

    @Test
    public void testNoContent() throws IOException {
        byte[] header;// content length is 0
        header = ("HTTP/1.1 200 OK\r\n"
                + "Test-Header: Test-Header-Value\r\n"
                + "Content-Type: text/plain\r\n"
                + "\r\n")
                .getBytes(StandardCharsets.ISO_8859_1);

        ByteArrayInputStream in = new ByteArrayInputStream(header);
        HttpResponseMessage responseMessage = HttpResponseMessage.loadFromStream(in);
        Assert.assertEquals("HTTP/1.1 200 OK", responseMessage.getStatusLine());
        Assert.assertEquals("Test-Header-Value", String.valueOf(responseMessage.getHeaders().get("test-header")));
        Assert.assertEquals("text/plain", String.valueOf(responseMessage.getHeaders().get("content-type")));
        Assert.assertFalse(responseMessage.getHeaders().containsKey("content-length"));
        Assert.assertEquals(0, responseMessage.getContentLength());
        Assert.assertEquals("", responseMessage.getMessageBody());
    }

    @Test
    public void testNoContentAndNoFinalCRLF() throws IOException {
        byte[] header;// content length is 0 and without final CRLF
        header = ("HTTP/1.1 200 OK\r\n"
                + "Test-Header: Test-Header-Value\r\n"
                + "Content-Type: text/plain\r\n"
                ).getBytes(StandardCharsets.ISO_8859_1);

        ByteArrayInputStream in = new ByteArrayInputStream(header);
        HttpResponseMessage responseMessage = HttpResponseMessage.loadFromStream(in);
        Assert.assertEquals("HTTP/1.1 200 OK", responseMessage.getStatusLine());
        Assert.assertEquals("Test-Header-Value", String.valueOf(responseMessage.getHeaders().get("test-header")));
        Assert.assertEquals("text/plain", String.valueOf(responseMessage.getHeaders().get("content-type")));
        Assert.assertFalse(responseMessage.getHeaders().containsKey("content-length"));
        Assert.assertEquals(0, responseMessage.getContentLength());
        Assert.assertEquals("", responseMessage.getMessageBody());
    }

    @Test
    public void testNoContentAndNoHeaderCRLF() throws IOException {
        byte[] header;// content length is 0 and without final Header CRLF
        header = ("HTTP/1.1 200 OK\r\n"
                + "Test-Header: Test-Header-Value\r\n"
                + "Content-Type: text/plain"
        ).getBytes(StandardCharsets.ISO_8859_1);

        ByteArrayInputStream in = new ByteArrayInputStream(header);
        HttpResponseMessage responseMessage = HttpResponseMessage.loadFromStream(in);
        Assert.assertEquals("HTTP/1.1 200 OK", responseMessage.getStatusLine());
        Assert.assertEquals("Test-Header-Value", String.valueOf(responseMessage.getHeaders().get("test-header")));
        Assert.assertEquals("text/plain", String.valueOf(responseMessage.getHeaders().get("content-type")));
        Assert.assertFalse(responseMessage.getHeaders().containsKey("content-length"));
        Assert.assertEquals(0, responseMessage.getContentLength());
        Assert.assertEquals("", responseMessage.getMessageBody());
    }
}
