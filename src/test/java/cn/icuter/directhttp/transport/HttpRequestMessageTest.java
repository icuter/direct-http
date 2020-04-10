package cn.icuter.directhttp.transport;

import cn.icuter.directhttp.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

public class HttpRequestMessageTest {

    @Test
    public void testFormat() {
        byte[] content = "{'name':'Edward','framework':'direct-http',comment:'超轻量'}".getBytes(StandardCharsets.UTF_8);
        // Builder + Composite
        HttpRequestMessage requestPacket = new HttpRequestMessage();
        requestPacket.setHost("direct-http.com");
        requestPacket.setMethod("POST");
        requestPacket.setRequestURI("/icuter/api");
        requestPacket.addCookie("Test-Cookie-1", "Test-Cookie-Value1");
        requestPacket.addCookie("Test-Cookie-2", "Test-Cookie-Value2");
        requestPacket.addHeader("Test-Header", "Test-Header-Value");
        requestPacket.addHeader("Content-Type", "application/json");
        requestPacket.setMessageBodyStream(new ByteArrayInputStream(content));

        Assert.assertEquals(
                "POST /icuter/api HTTP/1.1\r\n" +
                        "host: direct-http.com\r\n" +
                        "content-length: " + content.length + "\r\n" +
                        "user-agent: direct-http\r\n" +
                        "test-header: Test-Header-Value\r\n" +
                        "content-type: application/json\r\n" +
                        "connection: keep-alive\r\n" +
                        "cookie: Test-Cookie-1=Test-Cookie-Value1; Test-Cookie-2=Test-Cookie-Value2\r\n" +
                        "\r\n" +
                        StringUtils.decodeAsUTF8(content),
                requestPacket.format());
    }

    @Test
    public void testNoContent() {
        // Builder + Composite
        HttpRequestMessage requestPacket = new HttpRequestMessage();
        requestPacket.setHost("direct-http.com");
        requestPacket.setMethod("GET");
        requestPacket.setRequestURI("/icuter/api");
        requestPacket.addCookie("Test-Cookie-1", "Test-Cookie-Value1");
        requestPacket.addCookie("Test-Cookie-2", "Test-Cookie-Value2");
        requestPacket.addHeader("Test-Header", "Test-Header-Value");
        requestPacket.addHeader("Content-Type", "application/json");

        Assert.assertEquals(
                "GET /icuter/api HTTP/1.1\r\n" +
                        "host: direct-http.com\r\n" +
                        "user-agent: direct-http\r\n" +
                        "test-header: Test-Header-Value\r\n" +
                        "content-type: application/json\r\n" +
                        "connection: keep-alive\r\n" +
                        "cookie: Test-Cookie-1=Test-Cookie-Value1; Test-Cookie-2=Test-Cookie-Value2\r\n" +
                        "\r\n",
                requestPacket.format());
    }
}
