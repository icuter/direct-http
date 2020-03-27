package cn.icuter.directhttp;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Request implements Closeable {
    private final String host;
    private Parameter parameter = new Parameter();
    private Config config = new Config();

    public Request(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public Response post(String relativePath) {
        parameter.method = "POST";
        return sendRequest(relativePath);
    }
    public Response get(String relativePath) {
        parameter.method = "GET";
        return sendRequest(relativePath);
    }
    public Response put(String relativePath) {
        parameter.method = "PUT";
        return sendRequest(relativePath);
    }
    public Response patch(String relativePath) {
        parameter.method = "PATCH";
        return sendRequest(relativePath);
    }

    public Response sendRequest(String relativePath) {
        resetParameter();
        return null;
    }

    private void resetParameter() {
        parameter = new Parameter();
    }

    public Request charset(String charset) {
        parameter.charset = charset;
        return this;
    }

    public Request method(String method) {
        parameter.method = method;
        return this;
    }

    public Request body(String body) {
        parameter.body = body;
        return this;
    }

    public Request compression(String compressionType) {
        config.compressionType = compressionType;
        return this;
    }

    public Request header(String name, Object value) {
        parameter.headers.put(name, value);
        return this;
    }

    public void close() throws IOException {
        // TODO
    }

    class Config {
        int connectTimeout = 10000; // default 10s
        int readTimeout = 30000; // default 30s
        String compressionType = "deflate";
    }
    class Parameter {
        final Map<String, Object> headers = new HashMap<>();
        final Map<String, Object> cookies = new HashMap<>();
        final Map<String, Object> queryStrings = new HashMap<>();
        final Map<String, Object> formFields = new HashMap<>();
        final Map<String, Object> multiparts = new HashMap<>();
        String body;
        String method = "GET";
        String charset = "UTF-8";
    }
}
