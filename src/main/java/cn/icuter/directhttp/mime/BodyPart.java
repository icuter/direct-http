package cn.icuter.directhttp.mime;

import cn.icuter.directhttp.utils.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <i>BodyPart</i> refer to the spec from <a href="https://tools.ietf.org/html/rfc2046#section-5.1.1">rfc2046#section-5.1.1</a>
 * <p />
 * <pre>
 * body-part := MIME-part-headers [CRLF *OCTET]
 *              ; Lines in a body-part must not start
 *              ; with the specified dash-boundary and
 *              ; the delimiter must not appear anywhere
 *              ; in the body part.  Note that the
 *              ; semantics of a body-part differ from
 *              ; the semantics of a message, as
 *              ; described in the text.
 * </pre>
 * Body-part contains MIME-part-headers and it's message body
 *
 * @author edward leejan
 * @since 2020-04-07
 */
public abstract class BodyPart implements Part {
    private Map<String, String> mimePartHeaders = new LinkedHashMap<>();

    @Override
    public boolean isMultipart() {
        return false;
    }

    @Override
    public Map<String, String> header() {
        return mimePartHeaders;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        writeHeaderTo(out);
        writeBodyTo(out);
    }

    @Override
    public long length() {
        return headerLength() + bodyLength();
    }

    protected void writeHeaderTo(OutputStream out) throws IOException {
        for (Map.Entry<String, String> entry : mimePartHeaders.entrySet()) {
            String header = entry.getKey() + ": " + entry.getValue();
            out.write(StringUtils.encodeAsISO(header));
            out.write(CRLF);
        }
        out.write(CRLF);
    }
    public abstract void writeBodyTo(OutputStream out) throws IOException;

    protected long headerLength() {
        long headerLen = 0;
        for (Map.Entry<String, String> entry : mimePartHeaders.entrySet()) {
            String header = entry.getKey() + ": " + entry.getValue();
            headerLen += StringUtils.encodeAsISO(header).length + CRLF.length;
        }
        return headerLen + CRLF.length;
    }

    public abstract long bodyLength();
}
