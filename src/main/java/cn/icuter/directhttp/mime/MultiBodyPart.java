package cn.icuter.directhttp.mime;

import cn.icuter.directhttp.utils.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * MultiBodyPart is different to Multipart, MultiBodyPart is one of body-part that designed by <a href="https://tools.ietf.org/html/rfc2046#section-5.1.1">rfc2046#section-5.1.1</a>
 * and contains MIME-part-headers.
 * <p />
 *
 * @author edward
 * @since 2020-04-09
 */
public class MultiBodyPart extends BodyPart {
    private List<BodyPart> bodyParts = new LinkedList<>();
    private byte[] boundaryBytes;
    private String boundary;
    private String subType;

    public MultiBodyPart() {
        this("mixed");
    }

    public MultiBodyPart(String subType) {
        init(subType, nextBoundary());
    }

    public MultiBodyPart(String subType, String boundary) {
        if (StringUtils.isBlank(boundary)) {
            throw new IllegalArgumentException("boundary must NOT be null or empty!");
        }
        init(subType, boundary);
    }

    private void init(String subType, String boundary) {
        this.boundary = boundary;
        this.subType = subType;
        this.boundaryBytes = StringUtils.encodeAsISO(boundary);
        header().put("Content-Type", toContentType());
    }

    private String nextBoundary() {
        return Integer.toUnsignedString(ThreadLocalRandom.current().nextInt(), 32)
                + Long.toString(System.currentTimeMillis(), 32);
    }

    public String toContentType() {
        return "multipart/" + subType + "; boundary=" + boundary;
    }

    public void addBodyPart(BodyPart bodyPart) {
        bodyParts.add(bodyPart);
    }

    @Override
    public boolean isMultipart() {
        return true;
    }

    @Override
    public void writeBodyTo(OutputStream out) throws IOException {
        for (BodyPart part : bodyParts) {
            writeBodyBoundaryLine(out);
            part.writeTo(out);
            out.write(CRLF);
        }
        writeEndBoundaryLine(out);
    }

    private void writeBodyBoundaryLine(OutputStream out) throws IOException {
        out.write(BOUNDARY_EXTENSION);
        out.write(boundaryBytes);
        out.write(CRLF);
    }

    private void writeEndBoundaryLine(OutputStream out) throws IOException {
        out.write(BOUNDARY_EXTENSION);
        out.write(boundaryBytes);
        out.write(BOUNDARY_EXTENSION);
    }

    public String getBoundary() {
        return boundary;
    }

    public String getSubType() {
        return subType;
    }

    @Override
    public long bodyLength() {
        long length = 0;
        for (BodyPart part : bodyParts) {
            length += BOUNDARY_EXTENSION.length + boundaryBytes.length /* body boundary length */
                    + CRLF.length
                    + part.length()                                    /* part body length (contains part headers) */
                    + CRLF.length;
        }
        return length
                + BOUNDARY_EXTENSION.length + boundaryBytes.length /* body boundary length */
                + BOUNDARY_EXTENSION.length;                       /* ending boundary length */
    }
}
