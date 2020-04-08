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
        writeHeaderTo(out);
        writeBodyTo(out);
    }

    @Override
    public long length() {
        return headerLength() + bodyLength();
    }

    protected void writeHeaderTo(OutputStream out) throws Exception {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String header = entry.getKey() + ": " + entry.getValue();
            out.write(StringUtils.encodeAsISO(header));
            out.write(CRLF);
        }
        out.write(CRLF);
    }
    public abstract void writeBodyTo(OutputStream out) throws Exception;

    protected long headerLength() {
        long headerLen = 0;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String header = entry.getKey() + ": " + entry.getValue();
            headerLen += StringUtils.encodeAsISO(header).length + 2;
        }
        return headerLen + 2;
    }

    public abstract long bodyLength();
}
