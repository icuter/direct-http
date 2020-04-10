package cn.icuter.directhttp.transport;

import cn.icuter.directhttp.mime.BodyPart;
import cn.icuter.directhttp.mime.Multipart;
import cn.icuter.directhttp.mime.Part;
import cn.icuter.directhttp.utils.HeaderUtils;
import cn.icuter.directhttp.utils.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Http spec https://www.w3.org/Protocols/HTTP/1.1/rfc2616.pdf
 *
 * <pre>
 *   Request = Request-Line
 *             *(( general-header
 *             | request-header
 *             | entity-header ) CRLF)
 *             CRLF
 *             [ message-body ]
 * </pre>
 */
public class HttpRequestMessage {
    private static final String SP = " ";
    private static final String CRLF = "\r\n";
    private static final String HEADER_KV_SEPARATOR = ":" + SP;
    private String host;
    private String requestURI;
    private Map<String, Object> headers = new LinkedHashMap<>();

    /**
     * Cookie spec
     *
     * @see "http://www.ietf.org/rfc/rfc2109.txt"
     * @see "https://www.ietf.org/rfc/rfc2965.txt"
     */
    private List<HttpCookie> cookieList = new LinkedList<>();
    private String method = "GET";
    private Charset contentCharset = StandardCharsets.UTF_8;
    private byte[] content = new byte[0];
    private String version = "HTTP/1.1";
    private String userAgent = "direct-http";
    /** Content-Type for multipart/form-data */
    private Multipart multipart;

    public HttpRequestMessage() {
    }

    public void writeTo(OutputStream out) throws IOException {
        StringBuilder resultBuilder = new StringBuilder();
        // request start line
        buildRequestLine(resultBuilder);
        addNewLine(resultBuilder);

        // request headers
        buildHeaders(resultBuilder);
        addNewLine(resultBuilder);

        // write request line and headers
        out.write(StringUtils.encodeAsISO(resultBuilder.toString()));
        if (multipart != null) {
            multipart.writeTo(out);
        } else if (content.length > 0) {
            out.write(content);
        }
    }

    public String format() {
        StringBuilder resultBuilder = new StringBuilder();
        // request start line
        buildRequestLine(resultBuilder);
        addNewLine(resultBuilder);

        // request headers
        buildHeaders(resultBuilder);
        addNewLine(resultBuilder);

        // request content
        buildRequestContent(resultBuilder);
        return resultBuilder.toString();
    }

    private void buildHeaders(StringBuilder resultBuilder) {
        buildRequestHost(resultBuilder);
        buildRequestContentLength(resultBuilder);
        buildRequestUserAgent(resultBuilder);
        buildRequestHeaders(resultBuilder);
        buildRequestCookies(resultBuilder);
    }

    private void addNewLine(StringBuilder builder) {
        builder.append(CRLF);
    }

    private void buildRequestLine(StringBuilder builder) {
        builder.append(method).append(SP).append(requestURI).append(SP).append(version);
    }

    private void buildRequestHost(StringBuilder builder) {
        addNewLine(builder.append("host").append(HEADER_KV_SEPARATOR).append(host));
    }

    private void buildRequestUserAgent(StringBuilder resultBuilder) {
        addNewLine(resultBuilder.append("user-agent").append(HEADER_KV_SEPARATOR).append(userAgent));
    }

    private void buildRequestContentLength(StringBuilder resultBuilder) {
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
            if (content.length > 0) {
                throw new IllegalArgumentException("Method of GET / HEAD should NOT request message body!");
            }
            return;
        }
        long contentLength = multipart != null ? multipart.length() : content.length;
        addNewLine(resultBuilder.append("content-length").append(HEADER_KV_SEPARATOR).append(contentLength));
    }

    private void buildRequestHeaders(StringBuilder builder) {
        boolean hasConnectionHeader = false;
        for (Map.Entry<String, Object> header : headers.entrySet()) {
            String headerName = header.getKey().toLowerCase();
            if (!headerName.equals("cookie")) {
                String val = header.getValue() == null ? "" : String.valueOf(header.getValue());
                addNewLine(builder.append(headerName).append(HEADER_KV_SEPARATOR).append(val));
            }
            if (headerName.equals("connection")) {
                hasConnectionHeader = true;
            }
        }
        if (!hasConnectionHeader) {
            addNewLine(builder.append("connection").append(HEADER_KV_SEPARATOR).append("keep-alive"));
        }
    }

    private void buildRequestCookies(StringBuilder builder) {
        if (cookieList == null || cookieList.isEmpty()) {
            return;
        }
        builder.append("cookie:").append(SP);
        for (int i = 0; i < cookieList.size() - 1; i++) {
            HttpCookie cookie = cookieList.get(i);
            builder.append(cookie.toString()).append(";").append(SP);
        }
        addNewLine(builder.append(cookieList.get(cookieList.size() - 1).toString()));
    }

    private void buildRequestContent(StringBuilder builder) {
        if (multipart != null) {
            builder.append("... Multipart Message Body ...");
            return;
        }
        String value = (String) headers.getOrDefault("content-type", "");
        String charset = HeaderUtils.findHeaderParamValue(value, "charset", contentCharset.name());
        builder.append(StringUtils.decodeAs(content, Charset.forName(charset)));
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public void setContentCharset(String charsetName) {
        this.contentCharset = Charset.forName(charsetName);
    }

    public void setContentCharset(Charset charset) {
        this.contentCharset = charset;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void addHeader(String key, Object val) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("Header name must not be null or empty!");
        }
        headers.put(key, val);
    }

    public void setMultipart(Multipart multipart) {
        Objects.requireNonNull(multipart, "Multipart must NOT be null!");
        this.multipart = multipart;
        addHeader("Content-Type", multipart.toContentType());
    }

    public void addBodyPart(BodyPart part) {
        if (this.multipart == null) {
            this.multipart = new Multipart("form-data");
        }
        this.multipart.addBodyPart(part);
    }

    public Multipart getMultipart() {
        return multipart;
    }

    @Override
    public String toString() {
        return format();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public void addCookie(HttpCookie cookie) {
        this.cookieList.add(cookie);
    }

    public void addCookie(String name, Object value) {
        String strValue = value == null ? "" : String.valueOf(value);
        addCookie(cookieOfVersion0(name, strValue));
    }

    private HttpCookie cookieOfVersion0(String name, String value) {
        HttpCookie cookie = new HttpCookie(name, value);
        cookie.setVersion(0);
        return cookie;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        Objects.requireNonNull(content);
        this.content = content;
    }
}
