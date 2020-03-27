package cn.icuter.directhttp;

import cn.icuter.directhttp.utils.SSLUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JdkHttpConnectionRequest implements AutoCloseable {

    private static final String RESPONSE_HEADER_SET_COOKIE = "Set-Cookie";
    private static final String RESPONSE_HEADER_CONTENT_TYPE = "Content-Type";
    private static final Pattern PATTERN_COOKIE = Pattern.compile("^([^=]*)=(.*);");
    private static final Pattern PATTERN_CHARSET = Pattern.compile("^.+;\\s*charset=\"?([^\"]*)\"?", Pattern.CASE_INSENSITIVE);
    private static final String LINE_FEED = "\r\n";

    private String url;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, Object> formFields = new HashMap<>();
    private Map<String, Object> queryParams = new HashMap<>();
    private String content;
    private String method = "GET";
    private String charset = "UTF-8";
    private int connectTimeout = 5000;
    private int readTimeout = 10000;

    private HttpURLConnection conn = null;
    private Map<String, String> responseHeader = new HashMap<>();
    private Map<String, String> responseCookie = new HashMap<>();
    private InputStream responseInputStream;

    public String sendRequest() throws IOException {
        String buildUrl = buildUrl(url);
        URL requestUrl = new URL(buildUrl);

        conn = (HttpURLConnection) requestUrl.openConnection();

        acceptAllHttps(conn);       // 信任所有 https 证书
        setRequestProperty(conn);   // 设置 RequestProperty
        doSettingAndConnect(conn);  // 设置属性并且尝试链接
        writeRequestBody(conn);     // 设置表单参数或请求内容
        finish(conn);               // 完成请求

        // set result
        setResponseHeader(conn);
        setResponseCookie(responseHeader);
        return getResponseResult(conn);
    }

    private void setResponseHeader(HttpURLConnection conn) {
        Map<String, List<String>> headers = conn.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String key = entry.getKey();
            List<String> val = entry.getValue();
            if (key != null && key.length() > 0) {
                responseHeader.put(key, val.size() > 0 ? join(val, LINE_FEED) : "");
            }
        }
    }

    String join(Collection<String> collection, String joinChar) {
        StringBuilder result = new StringBuilder();
        for (String item : collection) {
            result.append(joinChar).append(item);
        }
        String joinStr = result.toString();
        return joinStr.length() > 0 ? joinStr.replaceFirst(joinChar, "") : joinStr;
    }


    private void setResponseCookie(Map<String, String> responseHeader) {
        String cookieStr = responseHeader.get(RESPONSE_HEADER_SET_COOKIE);
        if (cookieStr == null || cookieStr.length() <= 0) {
            return;
        }
        String[] cookieArr = cookieStr.split(LINE_FEED);
        for (String cookie : cookieArr) {
            Matcher matcher = PATTERN_COOKIE.matcher(cookie);
            if (matcher.find()) {
                String key = matcher.group(1);
                String val = matcher.group(2);
                responseCookie.put(key, val);
            }
        }
    }

    private String buildUrl(String url) throws UnsupportedEncodingException {
        String queryString = paramsToUrl(queryParams);
        if (queryString != null && queryString.length() > 0) {
            url += url.contains("?") ? ("&" + queryString) : ("?" + queryString);
        }
        if ("GET".equalsIgnoreCase(method)) {
            String formString = paramsToUrl(formFields);
            if (formString != null && formString.length() > 0) {
                return url.contains("?") ? (url + "&" + formString) : (url + "?" + formString);
            }
        }
        return url;
    }

    private void acceptAllHttps(HttpURLConnection conn) {
        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection) conn).setSSLSocketFactory(SSLUtils.getDummySSLSocketFactory());
            ((HttpsURLConnection) conn).setHostnameVerifier(SSLUtils.getDummyHostnameVerifier());
        }
    }

    private void doSettingAndConnect(HttpURLConnection conn) throws IOException {
        boolean isNotGetMethod = !"GET".equalsIgnoreCase(method);

        conn.setRequestMethod(method);
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(isNotGetMethod);
        conn.connect();
    }

    private void writeRequestBody(HttpURLConnection conn) throws IOException {
        if ("GET".equalsIgnoreCase(method)) { // get 请求没有 body
            return;
        }
        String requestBody = null;
        if (!formFields.isEmpty()) {
            requestBody = paramsToUrl(formFields);
        } else if (content != null && content.length() > 0) {
            requestBody = content;
        }
        if (requestBody != null && requestBody.length() > 0) {
            OutputStream requestStream = conn.getOutputStream();
            requestStream.write(requestBody.getBytes(charset));
        }
    }

    private void finish(HttpURLConnection conn) throws IOException {
        if (conn.getDoOutput()) {
            conn.getOutputStream().flush();
        }
    }

    private String getResponseResult(HttpURLConnection conn) throws IOException {
        InputStream in;
        // Http OK
        if (200 <= conn.getResponseCode() && conn.getResponseCode() <= 299) {
            in = conn.getInputStream();
        } else {
            in = conn.getErrorStream();
        }
        if (in == null) {
            return "http status code: " + conn.getResponseCode() + ", message: " + conn.getResponseMessage();
        }
        String responseContentType = responseHeader.get(RESPONSE_HEADER_CONTENT_TYPE);
        String charsetInContentType = parseCharsetFromContentType(responseContentType, charset);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[16384];
            for (int n = in.read(buffer); n != -1; n = in.read(buffer)) {
                out.write(buffer, 0, n);
            }
            return new String(out.toByteArray(), charsetInContentType);
        }
    }

    private void setResponseInputStream(HttpURLConnection conn) throws IOException {
        if (200 <= conn.getResponseCode() && conn.getResponseCode() <= 299) {
            this.responseInputStream = conn.getInputStream();
        } else {
            this.responseInputStream = conn.getErrorStream();
        }
    }

    @Override
    public void close() throws Exception {
        if (conn != null) {
            conn.disconnect();
        }
    }

    private String parseCharsetFromContentType(String responseContentType, String defCharset) {
        if (responseContentType == null || responseContentType.length() <= 0) {
            return defCharset;
        }
        Matcher matcher = PATTERN_CHARSET.matcher(responseContentType);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return defCharset;
    }

    public Map<String, String> getResponseHeader() {
        return responseHeader;
    }

    public Map<String, String> getResponseCookie() {
        return responseCookie;
    }

    /**
     * 返回格式：key1=value1&key2=value2
     *
     * @return String
     */
    private String paramsToUrl(Map<String, Object> params) throws UnsupportedEncodingException {
        StringBuilder urlParams = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String encodedValue = "";
            if (entry.getValue() != null) {
                encodedValue = URLEncoder.encode(String.valueOf(entry.getValue()), charset);
            }
            urlParams.append(entry.getKey()).append("=").append(encodedValue).append("&");
        }
        return urlParams.toString().length() > 0 ? urlParams.toString().replaceFirst("&$", "") : urlParams.toString();
    }

    public void addFormField(String key, Object value) {
        formFields.put(key, value);
    }

    public void queryString(String key, Object value) {
        queryParams.put(key, value);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    private void setRequestProperty(HttpURLConnection conn) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            String headerValue = entry.getValue();
            if (entry.getKey().toLowerCase().startsWith("content-type")) {
                if (!headerValue.toLowerCase().contains("charset")) {
                    headerValue = headerValue.replaceFirst(";\\s*$", "") + "; charset=" + charset;
                }
            }
            conn.setRequestProperty(entry.getKey(), headerValue);
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setFormFields(Map<String, Object> formFields) {
        this.formFields = formFields;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}
