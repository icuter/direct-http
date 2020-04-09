package cn.icuter.directhttp.mime;

import cn.icuter.directhttp.utils.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * "multipart/mixed" refer to the spec of <a href="https://tools.ietf.org/html/rfc2046#section-5.1.1">rfc2046#section-5.1.1</a>
 * <p />
 * "multipart/form-data" refer to the spec of <a href="https://tools.ietf.org/html/rfc1867">rfc1867</a>
 *
 * @author edward
 * @since 2020-04-06
 */
public class Multipart extends BodyPart {

    private static final byte[] BOUNDARY_EXTENSION = new byte[] {'-', '-'};

    private Multipart parent;
    private final String boundary;
    private byte[] boundaryBytes;
    private final String subType;
    private List<Part> parts = new LinkedList<>();

    public Multipart() {
        this("mixed");
    }

    public Multipart(String subType) {
        this.boundary = nextBoundary();
        this.subType = subType;
        this.boundaryBytes = StringUtils.encodeAsISO(boundary);
    }

    public Multipart(String boundary, String subType) {
        if (StringUtils.isBlank(boundary)) {
            throw new IllegalArgumentException("boundary must NOT be null or empty!");
        }
        this.boundary = boundary;
        this.subType = subType;
        this.boundaryBytes = StringUtils.encodeAsISO(this.boundary);
    }

    public Multipart addPart(Part part) {
        parts.add(part);
        if (part.isMultipart()) {
            Multipart multipart = (Multipart) part;
            multipart.setParent(this);
            multipart.header().put("Content-Type", multipart.toContentType());
        }
        return this;
    }

    public String getBoundary() {
        return boundary;
    }

    public String getSubType() {
        return subType;
    }

    public List<Part> getParts() {
        return parts;
    }

    public String toContentType() {
        return "multipart/" + subType + "; boundary=" + boundary;
    }

    private void setParent(Multipart parent) {
        this.parent = parent;
    }

    private String nextBoundary() {
        return Integer.toUnsignedString(ThreadLocalRandom.current().nextInt(), 32)
                + Long.toString(System.currentTimeMillis(), 32);
    }

    @Override
    public String toString() {
        return toContentType();
    }

    @Override
    public boolean isMultipart() {
        return true;
    }

    @Override
    public void writeHeaderTo(OutputStream out) throws Exception {
        if (parent != null) {
            super.writeHeaderTo(out);
        }
    }

    @Override
    public void writeBodyTo(OutputStream out) throws Exception {
        for (Part part : parts) {
            writeBodyBoundaryLine(out);
            part.writeTo(out);
            // Multipart contains CRLF ending characters
            if (!part.isMultipart()) {
                out.write(CRLF);
            }
        }
        writeEndBoundaryLine(out);
    }

    @Override
    protected long headerLength() {
        if (parent != null) {
            return super.headerLength();
        }
        return 0;
    }

    @Override
    public long bodyLength() {
        long length = 0;
        for (Part part : parts) {
            length += BOUNDARY_EXTENSION.length + boundaryBytes.length /* body boundary length */
                    + CRLF.length
                    + part.length();                                   /* part body length (contains part headers) */

            if (!part.isMultipart()) {
                length += CRLF.length;
            }
        }
        return length
                + BOUNDARY_EXTENSION.length + boundaryBytes.length /* body boundary length */
                + BOUNDARY_EXTENSION.length                        /* ending boundary length */
                + CRLF.length;
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
        out.write(CRLF);
    }
}
