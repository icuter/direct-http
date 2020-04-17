package cn.icuter.directhttp.transport;

import cn.icuter.directhttp.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class HttpRequestMessageTest {

    @Test
    public void testFormat() {
        byte[] content = "{'name':'Edward','framework':'direct-http',comment:'超轻量'}".getBytes(StandardCharsets.UTF_8);
        // Builder + Composite
        HttpRequestMessage requestMessage = new HttpRequestMessage();
        requestMessage.setHost("direct-http.com");
        requestMessage.setMethod("POST");
        requestMessage.setRequestURI("/icuter/api");
        requestMessage.addCookie("Test-Cookie-1", "Test-Cookie-Value1");
        requestMessage.addCookie("Test-Cookie-2", "Test-Cookie-Value2");
        requestMessage.addHeader("Test-Header", "Test-Header-Value");
        requestMessage.setMessageBody(MessageBodyFactory.binary(content));

        Assert.assertEquals(
                "POST /icuter/api HTTP/1.1\r\n" +
                        "host: direct-http.com\r\n" +
                        "content-length: " + content.length + "\r\n" +
                        "user-agent: direct-http\r\n" +
                        "test-header: Test-Header-Value\r\n" +
                        "connection: keep-alive\r\n" +
                        "cookie: Test-Cookie-1=Test-Cookie-Value1; Test-Cookie-2=Test-Cookie-Value2\r\n" +
                        "\r\n" +
                        StringUtils.decodeAsUTF8(content),
                requestMessage.format());
    }

    @Test
    public void testNoContent() {
        // Builder + Composite
        HttpRequestMessage requestMessage = new HttpRequestMessage();
        requestMessage.setHost("direct-http.com");
        requestMessage.setMethod("GET");
        requestMessage.setRequestURI("/icuter/api");
        requestMessage.addCookie("Test-Cookie-1", "Test-Cookie-Value1");
        requestMessage.addCookie("Test-Cookie-2", "Test-Cookie-Value2");
        requestMessage.addHeader("Test-Header", "Test-Header-Value");
        requestMessage.addHeader("Content-Type", "application/json");

        Assert.assertEquals(
                "GET /icuter/api HTTP/1.1\r\n" +
                        "host: direct-http.com\r\n" +
                        "user-agent: direct-http\r\n" +
                        "test-header: Test-Header-Value\r\n" +
                        "content-type: application/json\r\n" +
                        "connection: keep-alive\r\n" +
                        "cookie: Test-Cookie-1=Test-Cookie-Value1; Test-Cookie-2=Test-Cookie-Value2\r\n" +
                        "\r\n",
                requestMessage.format());
    }
}
