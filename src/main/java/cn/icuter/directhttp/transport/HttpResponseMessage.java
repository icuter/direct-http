package cn.icuter.directhttp.transport;

import cn.icuter.directhttp.utils.HeaderUtils;
import cn.icuter.directhttp.utils.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *   Response = Status-Line
 *              *(( general-header
 *              | response-header
 *              | entity-header ) CRLF)
 *              CRLF
 *              [ message-body ]
 *  </pre>
 */
public class HttpResponseMessage {
    private String statusLine;
    private Map<String, Object> headers = new HashMap<>();
    private Map<String, Object> cookies = new HashMap<>();
    private InputStream messageBodyStream;
    private String messageBody;
    private int contentLength;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static HttpResponseMessage loadFromStream(InputStream in) throws IOException {
        HttpResponseMessage responseMessage = new HttpResponseMessage();
        responseMessage.statusLine = new String(IOUtils.readLine(in), StandardCharsets.ISO_8859_1);
        for (byte[] line = IOUtils.readLine(in); line.length > 0; line = IOUtils.readLine(in)) {
            String header = new String(line, StandardCharsets.ISO_8859_1);
            // TODO inefficiently parse http response header line
            int colonIndex = header.indexOf(":");
            if (colonIndex >= 0) {
                String headerName = header.substring(0, colonIndex).trim().toLowerCase();
                if (headerName.equals("set-cookie") || headerName.equals("set-cookie2")) {
                    List<HttpCookie> cookieList = HttpCookie.parse(header);
                    for (HttpCookie cookie : cookieList) {
                        responseMessage.cookies.put(cookie.getName(), cookie);
                    }
                } else {
                    responseMessage.headers.put(headerName, header.substring(colonIndex + 1).trim());
                }
            } else {
                responseMessage.headers.put(header, "");
            }
        }
        // non fixed content length response, especially zip attachment content response
        // TODO compatible with transfer-encoding: gzip, chunked
        if (responseMessage.headers.containsKey("transfer-encoding")) {
            ChunkedInputStream chunkedIn = ChunkedInputStream.of(in);
            if (responseMessage.headers.containsKey("trailer")) {
                chunkedIn.setHasTrailerResponseHeader();
            }
            // bi-association
            chunkedIn.responseMessage = responseMessage;
            responseMessage.messageBodyStream = chunkedIn;
            return responseMessage;
        }
        responseMessage.contentLength = Integer.parseInt(
                (String) responseMessage.headers.getOrDefault("content-length", "0"));
        responseMessage.messageBodyStream = ContentLengthInputStream.of(in);
        return responseMessage;
    }

    public String getMessageBody() throws IOException {
        if (messageBody != null) {
            return messageBody;
        }
        if (contentLength == 0) {
            return (messageBody = "");
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(contentLength)) {
            IOUtils.readBytesTo(messageBodyStream, out, contentLength);
            byte[] bytes = out.toByteArray();
            if (bytes.length != contentLength) {
                throw new IllegalStateException("The http response header of Content-Length incorrect, content-length="
                        + contentLength + ", but actually message body length is " + bytes.length);
            }
            String contentType = (String) headers.getOrDefault("content-type", "text/plain");
            String contentCharset = HeaderUtils.findHeaderParamValue(contentType, "charset", DEFAULT_CHARSET.name());
            return (messageBody = new String(bytes, contentCharset));
        }
    }

    public int getContentLength() {
        return contentLength;
    }

    public Map<String, Object> getCookies() {
        return cookies;
    }

    public String getStatusLine() {
        return statusLine;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public InputStream getMessageBodyStream() {
        return messageBodyStream;
    }
}
