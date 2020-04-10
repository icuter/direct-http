package cn.icuter.directhttp.mime;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

/**
 * "multipart/mixed" refer to the spec of <a href="https://tools.ietf.org/html/rfc2046#section-5.1.1">rfc2046#section-5.1.1</a>
 * <p />
 * "multipart/form-data" refer to the spec of <a href="https://tools.ietf.org/html/rfc1867">rfc1867</a>
 *
 * <pre>
 * multipart-body := [preamble CRLF]
 *               dash-boundary transport-padding CRLF
 *               body-part *encapsulation
 *               close-delimiter transport-padding
 *               [CRLF epilogue]
 * </pre>
 *
 * Multipart does not have MIME-part-headers that is a http message body.
 *
 * @author edward
 * @since 2020-04-06
 */
public class Multipart implements Part {

    private final MultiBodyPart multiBodyPart;

    public Multipart() {
        this("mixed");
    }

    public Multipart(String subType) {
        multiBodyPart = new MultiBodyPart(subType);
    }

    public Multipart(String subType, String boundary) {
        multiBodyPart = new MultiBodyPart(subType, boundary);
    }

    public Multipart addBodyPart(BodyPart part) {
        multiBodyPart.addBodyPart(part);
        return this;
    }

    @Override
    public boolean isMultipart() {
        return true;
    }

    @Override
    public long length() {
        return multiBodyPart.bodyLength() + CRLF.length;
    }

    @Override
    public Map<String, String> header() {
        return Collections.emptyMap();
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        multiBodyPart.writeBodyTo(out);
        out.write(CRLF);
    }

    @Override
    public String toString() {
        return toContentType();
    }

    public String getBoundary() {
        return multiBodyPart.getBoundary();
    }

    public String getSubType() {
        return multiBodyPart.getSubType();
    }

    public String toContentType() {
        return multiBodyPart.toContentType();
    }
}
