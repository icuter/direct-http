package cn.icuter.directhttp.mock;

import cn.icuter.directhttp.data.TestData;
import cn.icuter.directhttp.utils.IOUtils;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

public class MockHttpServer extends HttpServer implements Closeable {
    private final HttpServer server;

    private MockHttpServer(HttpServer server) {
        this.server = server;
        registerContexts();
    }

    private void registerContexts() {
        server.createContext("/mock/stdout", exchange -> {
            try {
                InputStream in = exchange.getRequestBody();
                int contentLen = in.available();

                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
                exchange.getResponseHeaders().add("Connection", "keep-alive");

                exchange.sendResponseHeaders(200, contentLen);
                try (OutputStream out = exchange.getResponseBody()) {
                    IOUtils.readBytesTo(in, out, contentLen);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        });
        server.createContext("/mock/chunk", exchange -> {
            try {

                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.getResponseHeaders().add("Connection", "keep-alive");

                exchange.sendResponseHeaders(200, 0);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(TestData.mediumHtmlData());
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        });
        server.createContext("/mock/chunk/conn/close", exchange -> {
            try {
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.getResponseHeaders().add("Connection", "close");

                exchange.sendResponseHeaders(200, 0);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(TestData.mediumHtmlData());
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        });
        server.createContext("/mock/conn/close", exchange -> {
            try {
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.getResponseHeaders().add("Connection", "close");

                exchange.sendResponseHeaders(204, -1);
                exchange.getResponseBody().close();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        });
    }


    public static MockHttpServer newLocalHttpServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        return new MockHttpServer(server);
    }

    @Override
    public void bind(InetSocketAddress inetSocketAddress, int i) throws IOException {
        server.bind(inetSocketAddress, i);
    }

    @Override
    public void start() {
        server.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setExecutor(Executor executor) {
        server.setExecutor(executor);
    }

    @Override
    public Executor getExecutor() {
        return server.getExecutor();
    }

    @Override
    public void stop(int i) {
        server.stop(i);
    }

    @Override
    public HttpContext createContext(String s, HttpHandler httpHandler) {
        return server.createContext(s, httpHandler);
    }

    @Override
    public HttpContext createContext(String s) {
        return server.createContext(s);
    }

    @Override
    public void removeContext(String s) throws IllegalArgumentException {
        server.removeContext(s);
    }

    @Override
    public void removeContext(HttpContext httpContext) {
        server.removeContext(httpContext);
    }

    @Override
    public InetSocketAddress getAddress() {
        return server.getAddress();
    }

    @Override
    public void close() {
        stop(0);
    }
}
