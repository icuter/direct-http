package cn.icuter.directhttp.transport;

import java.io.IOException;

public class HttpConnectionFactory {

    public static HttpConnection newHttp(String host, int port) throws IOException {
        HttpConnection connection = new HttpConnection(host, port);
        connection.connect(3_000, 15_000);
        return connection;
    }

    public static HttpConnection newHttp(String host) throws IOException {
        HttpConnection connection = new HttpConnection(host, 80);
        connection.connect(3_000, 15_000);
        return connection;
    }
}
