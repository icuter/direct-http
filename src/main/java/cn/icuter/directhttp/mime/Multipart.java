package cn.icuter.directhttp.mime;

import cn.icuter.directhttp.utils.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Content-Type: Multipart/mixed
 *
 * https://tools.ietf.org/html/rfc2046#section-5.1.1
 *
 * @author edward
 * @since 2020-04-06
 */
public class Multipart implements Part {

    /** multipart/form-data refer to the spec of <a href="https://tools.ietf.org/html/rfc1867">rfc1867</a> */
    public static Multipart FORM_DATA = new Multipart("form-data");
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
        this.boundaryBytes = boundary.getBytes(StandardCharsets.ISO_8859_1);
    }

    public Multipart(String boundary, String subType) {
        this.boundary = boundary;
        this.subType = subType;
        this.boundaryBytes = boundary.getBytes(StandardCharsets.ISO_8859_1);
    }

    public Multipart addPart(Part part) {
        parts.add(part);
        if (part.isMultipart()) {
            ((Multipart) part).setParent(this);
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

    public void setParent(Multipart parent) {
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
    public Map<String, String> header() {
        return null;
    }

    @Override
    public void writeTo(OutputStream out) throws Exception {
        if (parent != null) {
            out.write(StringUtils.encodeAsISO("Content-Type: " + toContentType()));
            out.write(CRLF);
            out.write(CRLF);
        }
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
