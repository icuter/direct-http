package cn.icuter.directhttp.mime;

import cn.icuter.directhttp.utils.StringUtils;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class BodyPart implements Part {
    private Map<String, String> headers = new HashMap<>();

    @Override
    public boolean isMultipart() {
        return false;
    }

    @Override
    public Map<String, String> header() {
        return headers;
    }

    @Override
    public void writeTo(OutputStream out) throws Exception {
        // write header to output stream
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String header = entry.getKey() + ": " + entry.getValue();
            out.write(StringUtils.encodeAsISO(header));
            out.write(CRLF);
        }
        out.write(CRLF);
        writeBodyTo(out);
    }

    public abstract void writeBodyTo(OutputStream out) throws Exception;
}
