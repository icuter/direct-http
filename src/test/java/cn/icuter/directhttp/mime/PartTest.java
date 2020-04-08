package cn.icuter.directhttp.mime;

import cn.icuter.directhttp.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PartTest {

    private static final String SRC = "abc 123 我爱吃鱼 \r\n";

    @Test
    public void testTextPart() throws Exception {
        Part part = new TextPart(SRC);
        part.header().put("Content-Disposition", "form-data; name=\"myTextField\"; filename=demo.txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        part.writeTo(out);

        assertPartBody(out.toByteArray());
    }

    @Test
    public void testFilePart() throws Exception {
        Path path = Files.createTempFile("direct-http-file-part", "");
        Files.write(path, StringUtils.encodeAsUTF8(SRC), StandardOpenOption.WRITE);
        Part part = new FilePart(path.toFile());
        part.header().put("Content-Disposition", "form-data; name=\"myTextField\"; filename=demo.txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        part.writeTo(out);

        assertPartBody(out.toByteArray());
    }

    @Test
    public void testStreamPart() throws Exception {
        Path path = Files.createTempFile("direct-http-file-part", "");
        Files.write(path, StringUtils.encodeAsUTF8(SRC), StandardOpenOption.WRITE);

        Part part = new StreamPart(Files.newInputStream(path, StandardOpenOption.READ));
        part.header().put("Content-Disposition", "form-data; name=\"myTextField\"; filename=demo.txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        part.writeTo(out);

        assertPartBody(out.toByteArray());
    }

    private void assertPartBody(byte[] bytes) {
        Assert.assertEquals("Content-Disposition: form-data; name=\"myTextField\"; filename=demo.txt\r\n"
                + "\r\n" + SRC, StringUtils.decodeAsUTF8(bytes));
    }

    @Test
    public void testMultipart() throws Exception {
        Multipart subMulti = new Multipart()
                .addPart(new TextPart(SRC))
                .addPart(new TextPart(SRC));
        Multipart multipart = new Multipart()
                .addPart(new TextPart(SRC))
                .addPart(new TextPart(SRC))
                .addPart(subMulti);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        multipart.writeTo(out);

        String expected = "--" + multipart.getBoundary() + "\r\n"
                + "\r\n"
                + SRC + "\r\n"
                + "--" + multipart.getBoundary() + "\r\n"
                + "\r\n"
                + SRC + "\r\n"
                + "--" + multipart.getBoundary() + "\r\n"
                + "Content-Type: " + subMulti.toContentType() + "\r\n"
                + "\r\n"
                + "--" + subMulti.getBoundary() + "\r\n"
                + "\r\n"
                + SRC + "\r\n"
                + "--" + subMulti.getBoundary() + "\r\n"
                + "\r\n"
                + SRC + "\r\n"
                + "--" + subMulti.getBoundary() + "--\r\n"
                + "--" + multipart.getBoundary() + "--\r\n";
        Assert.assertEquals("multipart/mixed; boundary=" + multipart.getBoundary(), multipart.toContentType());
        Assert.assertEquals(expected, StringUtils.decodeAsUTF8(out.toByteArray()));
    }
}
