package cn.icuter.directhttp.network;

import cn.icuter.directhttp.data.TestData;
import cn.icuter.directhttp.mock.MockHttpServer;
import cn.icuter.directhttp.request.HttpRequestMessage;
import cn.icuter.directhttp.data.MessageBodyFactory;
import cn.icuter.directhttp.response.HttpResponseMessage;
import cn.icuter.directhttp.utils.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HttpConnectionTest {
    private static MockHttpServer server;
    public static final int PORT = 9999;

    @BeforeClass
    public static void startUpServer() throws IOException {
        server = MockHttpServer.newLocalHttpServer(PORT);
        server.start();
    }

    @Test
    public void testContentLengthResponse() throws IOException {
        try (HttpConnection connection = HttpConnectionFactory.newHttp("localhost", PORT)) {
            testStdout(connection);
            testStdout(connection);
            testStdout(connection);
        }
    }

    private void testStdout(HttpConnection connection) throws IOException {
        String content = "Hello World 我爱吃鱼";
        HttpRequestMessage request = new HttpRequestMessage();
        request.setHost("localhost");
        request.setRequestURI("/mock/stdout");
        request.setMethod("POST");
        request.setMessageBody(MessageBodyFactory.text(content));
        request.writeTo(connection.getOutputStream());
        HttpResponseMessage response = HttpResponseMessage.loadFromStream(connection.getInputStream());

        Assert.assertFalse(response.getHeaders().isEmpty());
        Assert.assertEquals(content, response.getMessageBody());
    }

    @Test
    public void testChunkedResponse() throws IOException {
        String dataHash = TestData.md5Hash(TestData.mediumHtmlData());
        try (HttpConnection connection = HttpConnectionFactory.newHttp("localhost", PORT)) {
            testChunked(connection, dataHash);
            testChunked(connection, dataHash);
            testChunked(connection, dataHash);
        }
    }

    @Test
    public void testChunkedCloseResponse() throws IOException {
        String dataHash = TestData.md5Hash(TestData.mediumHtmlData());
        try (HttpConnection connection = HttpConnectionFactory.newHttp("localhost", PORT)) {
            HttpRequestMessage request = new HttpRequestMessage();
            request.setHost("localhost");
            request.setRequestURI("/mock/chunk/conn/close");
            request.writeTo(connection.getOutputStream());

            HttpResponseMessage response = HttpResponseMessage.loadFromStream(connection.getInputStream());
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                InputStream in = response.getMessageBodyStream();
                IOUtils.readBytesTo(in, out);

                Assert.assertEquals(dataHash, TestData.md5Hash(out.toByteArray()));
            }
            Assert.assertTrue(connection.isClosed());
        }
    }

    @Test
    public void testNoContentCloseResponse() throws IOException {
        HttpRequestMessage request = new HttpRequestMessage();
        request.setHost("localhost");
        request.setRequestURI("/mock/conn/close");
        try (HttpConnection connection = HttpConnectionFactory.newHttp("localhost", PORT)) {
            request.writeTo(connection.getOutputStream());
            HttpResponseMessage responseMessage = HttpResponseMessage.loadFromStream(connection.getInputStream());

            Assert.assertEquals("close", responseMessage.getHeaders().get("connection"));
            Assert.assertTrue(connection.isClosed());
        }
    }

    private void testChunked(HttpConnection connection, String expectHash) throws IOException {
        HttpRequestMessage request = new HttpRequestMessage();
        request.setHost("localhost");
        request.setRequestURI("/mock/chunk");
        request.writeTo(connection.getOutputStream());
        HttpResponseMessage response = HttpResponseMessage.loadFromStream(connection.getInputStream());
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            InputStream in = response.getMessageBodyStream();
            IOUtils.readBytesTo(in, out);

            Assert.assertEquals(expectHash, TestData.md5Hash(out.toByteArray()));
        }
    }

    @AfterClass
    public static void shutdownServer() {
        if (server != null) {
            server.close();
        }
    }
}
