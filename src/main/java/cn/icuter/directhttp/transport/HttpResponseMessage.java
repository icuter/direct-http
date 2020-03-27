package cn.icuter.directhttp.transport;

import cn.icuter.directhttp.utils.HeaderUtils;
import cn.icuter.directhttp.utils.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
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
    private Map<String, Object> headers = new LinkedHashMap<>();
    private InputStream inputStream;
    private String messageBody;
    private int contentLength;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static HttpResponseMessage loadFromStream(InputStream in) throws IOException {
        HttpResponseMessage responseMessage = new HttpResponseMessage();
        responseMessage.statusLine = new String(IOUtils.readLine(in), StandardCharsets.ISO_8859_1);
        for (byte[] headerLine = IOUtils.readLine(in); headerLine.length > 0; headerLine = IOUtils.readLine(in)) {
            String header = new String(headerLine, StandardCharsets.ISO_8859_1);
            // TODO inefficiently parse http response header line
            // TODO to parse Set-Cookie / Set-Cookie2
            int colonIndex = header.indexOf(":");
            if (colonIndex >= 0) {
                responseMessage.headers.put(header.substring(0, colonIndex).trim().toLowerCase(),
                                header.substring(colonIndex + 1).trim());
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
            responseMessage.inputStream = chunkedIn;
            return responseMessage;
        }
        responseMessage.contentLength = Integer.parseInt(
                (String) responseMessage.headers.getOrDefault("content-length", "0"));
        responseMessage.inputStream = ContentLengthInputStream.of(in);
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
            byte[] buffer = new byte[contentLength];
            for (int n = inputStream.read(buffer); n > 0; n = inputStream.read(buffer)) {
                out.write(buffer, 0, n);
            }
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

    public String getStatusLine() {
        return statusLine;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
