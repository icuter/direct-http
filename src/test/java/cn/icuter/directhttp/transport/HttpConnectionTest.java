package cn.icuter.directhttp.transport;

import cn.icuter.directhttp.mock.MockHttpServer;
import cn.icuter.directhttp.utils.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HttpConnectionTest {
    private static MockHttpServer server;
    private static final int PORT = 9999;

    @BeforeClass
    public static void startUpServer() throws IOException {
        server = MockHttpServer.newLocalHttpServer(PORT);
        server.start();
    }

    @Test
    public void testContentLengthResponse() throws IOException {
        String content = "Hello World 我爱吃鱼";
        HttpRequestMessage request = new HttpRequestMessage();
        request.setHost("localhost");
        request.setRequestURI("/mock/stdout");
        request.addHeader("Content-Type", "text/plain; charset=UTF-8");
        request.setMethod("POST");
        request.setContent(content.getBytes(StandardCharsets.UTF_8));

        try (HttpConnection connection = HttpConnectionFactory.newHttp("localhost", PORT)) {
            testStdout(request, connection, content);
            testStdout(request, connection, content);
            testStdout(request, connection, content);
        }
    }

    private void testStdout(HttpRequestMessage request, HttpConnection connection, String expect) throws IOException {
        request.writeTo(connection.getOutputStream());
        HttpResponseMessage response = HttpResponseMessage.loadFromStream(connection.getInputStream());
        Assert.assertEquals(expect, response.getMessageBody());
    }

    @Test
    public void testChunkedResponse() throws IOException {
        HttpRequestMessage request = new HttpRequestMessage();
        request.setHost("localhost");
        request.setRequestURI("/mock/chunk");
        String dataHash = md5Hash(MockHttpServer.data());
        try (HttpConnection connection = HttpConnectionFactory.newHttp("localhost", PORT)) {
            testChunked(request, connection, dataHash);
            testChunked(request, connection, dataHash);
            testChunked(request, connection, dataHash);
        }
    }

    private void testChunked(HttpRequestMessage request, HttpConnection connection, String expectHash) throws IOException {
        request.writeTo(connection.getOutputStream());
        HttpResponseMessage response = HttpResponseMessage.loadFromStream(connection.getInputStream());
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            InputStream in = response.getMessageBodyStream();
            IOUtils.readBytesTo(in, out);
            Assert.assertEquals(expectHash, md5Hash(out.toByteArray()));
        }
    }

    private String md5Hash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return Base64.getEncoder().encodeToString(digest.digest(data));
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    @AfterClass
    public static void shutdownServer() {
        if (server != null) {
            server.close();
        }
    }
}
