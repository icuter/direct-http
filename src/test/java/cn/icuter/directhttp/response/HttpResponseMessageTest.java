package cn.icuter.directhttp.response;

import cn.icuter.directhttp.data.TestData;
import cn.icuter.directhttp.response.HttpResponseMessage;
import cn.icuter.directhttp.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

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
        Assert.assertEquals(StringUtils.decodeAsUTF8(content), responseMessage.getMessageBody());
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
        Assert.assertEquals(StringUtils.decodeAsUTF8(content), responseMessage.getMessageBody());
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

    @Test
    public void testSetCookieResponse() throws IOException {
        byte[] header;// content length is 0 and without final Header CRLF
        header = ("HTTP/1.1 200 OK\r\n"
                + "Test-Header: Test-Header-Value\r\n"
                + "Set-Cookie: SID=31d4d96e407aad42; Path=/; Secure; HttpOnly\r\n"
                + "Set-Cookie: lang=en-US; Path=/; Domain=example.com\r\n"
                + "Set-Cookie2: server=nginx; Path=/, view=MONTH; Domain=directhttp.com\r\n"
                + "Set-Cookie2: server2=apache; Path=/, view2=DAY; Domain=directhttp.com\r\n"
                + "Content-Type: text/plain\r\n" +
                "\r\n"
        ).getBytes(StandardCharsets.ISO_8859_1);

        ByteArrayInputStream in = new ByteArrayInputStream(header);
        HttpResponseMessage responseMessage = HttpResponseMessage.loadFromStream(in);
        Assert.assertEquals("HTTP/1.1 200 OK", responseMessage.getStatusLine());
        Assert.assertEquals("Test-Header-Value", String.valueOf(responseMessage.getHeaders().get("test-header")));
        Assert.assertEquals("text/plain", String.valueOf(responseMessage.getHeaders().get("content-type")));
        Assert.assertFalse(responseMessage.getHeaders().containsKey("content-length"));
        Assert.assertEquals(0, responseMessage.getContentLength());
        Assert.assertEquals("", responseMessage.getMessageBody());

        Map<String, Object> cookieMap = responseMessage.getCookies();
        // Set-Cookie
        HttpCookie sidCookie = (HttpCookie) cookieMap.get("SID");
        Assert.assertEquals("31d4d96e407aad42", sidCookie.getValue());
        Assert.assertEquals("/", sidCookie.getPath());
        Assert.assertTrue(sidCookie.getSecure());
        Assert.assertTrue(sidCookie.isHttpOnly());
        HttpCookie langCookie = (HttpCookie) cookieMap.get("lang");
        Assert.assertEquals("en-US", langCookie.getValue());
        Assert.assertEquals("/", langCookie.getPath());
        Assert.assertEquals("example.com", langCookie.getDomain());
        Assert.assertFalse(langCookie.getSecure());
        Assert.assertFalse(langCookie.isHttpOnly());

        // Set-Cookie2
        HttpCookie serverCookie = (HttpCookie) cookieMap.get("server");
        Assert.assertEquals("nginx", serverCookie.getValue());
        Assert.assertEquals("/", serverCookie.getPath());
        HttpCookie viewCookie = (HttpCookie) cookieMap.get("view");
        Assert.assertEquals("MONTH", viewCookie.getValue());
        Assert.assertEquals("directhttp.com", viewCookie.getDomain());
        HttpCookie server2Cookie = (HttpCookie) cookieMap.get("server2");
        Assert.assertEquals("apache", server2Cookie.getValue());
        Assert.assertEquals("/", server2Cookie.getPath());
        HttpCookie view2Cookie = (HttpCookie) cookieMap.get("view2");
        Assert.assertEquals("DAY", view2Cookie.getValue());
        Assert.assertEquals("directhttp.com", view2Cookie.getDomain());
    }

    @Test
    public void testGZipContent() throws IOException {
        testContentEncoding("gzip");
    }

    @Test
    public void testDeflateContent() throws IOException {
        testContentEncoding("deflate");
    }

    @Test
    public void testChunkedGZipContent() throws IOException {
        // TODO ChunkedOutputStream
    }

    private void testContentEncoding(String encoding) throws IOException {
        byte[] content = TestData.smallHtmlData();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (OutputStream out = getEncodingOutputStream(byteOut, encoding)) {
            out.write(content);
        }
        byte[] encodedContent = byteOut.toByteArray();
        byte[] header = ("HTTP/1.1 200 OK\r\n"
                + "Test-Header: Test-Header-Value\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "Content-Encoding: " + encoding + "\r\n"
                + "content-length: " + encodedContent.length + "\r\n"
                + "\r\n")
                .getBytes(StandardCharsets.ISO_8859_1);
        byte[] data = new byte[header.length + encodedContent.length];
        System.arraycopy(header, 0, data, 0, header.length);
        System.arraycopy(encodedContent, 0, data, header.length, encodedContent.length);

        HttpResponseMessage response = HttpResponseMessage.loadFromStream(new ByteArrayInputStream(data));
        Assert.assertEquals(StringUtils.decodeAsUTF8(content), response.getMessageBody());
    }

    private OutputStream getEncodingOutputStream(ByteArrayOutputStream byteOut, String encoding) throws IOException {
        if ("gzip".equalsIgnoreCase(encoding)) {
            return new GZIPOutputStream(byteOut);
        } else if ("deflate".equalsIgnoreCase(encoding)) {
            return new DeflaterOutputStream(byteOut);
        }
        throw new UnsupportedEncodingException("Invalid encoding: " + encoding);
    }
}
