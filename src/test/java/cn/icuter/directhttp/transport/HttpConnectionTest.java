package cn.icuter.directhttp.transport;

import cn.icuter.directhttp.mock.MockHttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpConnectionTest {
    private static MockHttpServer server;
    private static final int PORT = 9999;
    @BeforeClass
    public static void startUpServer() throws IOException {
        server = MockHttpServer.newLocalHttpServer(PORT);
        server.start();
    }

    @Test
    public void testFixedLength() throws IOException {
        HttpRequestMessage request = new HttpRequestMessage();
        request.setHost("localhost");
        request.setRequestURI("/mock/stdout");
        request.addHeader("Content-Type", "text/plain; charset=UTF-8");
        request.setMethod("POST");
        request.setContent("Hello World 我爱吃鱼".getBytes(StandardCharsets.UTF_8));

        try (HttpConnection connection = HttpConnection.newHttp("localhost", PORT)) {
            request.writeTo(connection.getOutputStream());
            HttpResponseMessage response = HttpResponseMessage.loadFromStream(connection.getInputStream());
            System.out.println(response.getHeaders());
            System.out.println(response.getMessageBody());
        }
    }
    @Test
    public void testChunk() throws IOException {
        HttpRequestMessage request = new HttpRequestMessage();
        request.setHost("localhost");
        request.setRequestURI("/mock/chunk");

        try (HttpConnection connection = HttpConnection.newHttp("localhost", PORT)) {
            request.writeTo(connection.getOutputStream());
            HttpResponseMessage response = HttpResponseMessage.loadFromStream(connection.getInputStream());
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                InputStream in = response.getInputStream();
                byte[] bytes = new byte[20480];
                for (int n = in.read(bytes); n > 0; n = in.read(bytes)) {
                    out.write(bytes, 0, n);
                }
                System.out.println(new String(out.toByteArray(), StandardCharsets.UTF_8));
            }
        }
    }

    @AfterClass
    public static void shutdownServer() {
        if (server != null) {
            server.close();
        }
    }
}
