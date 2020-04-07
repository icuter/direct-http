package cn.icuter.directhttp.mime;

import cn.icuter.directhttp.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PartTest {

    public static final String SRC = "abc 123 我爱吃鱼 \r\n";

    @Test
    public void testTextPart() throws Exception {
        Part part = new TextPart(SRC);
        part.header().put("Content-Disposition", "form-data; name=\"myTextField\"");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        part.writeTo(out);

        String partEntry = "Content-Disposition: form-data; name=\"myTextField\"\r\n"
                + "\r\n" + SRC;
        Assert.assertEquals(partEntry, StringUtils.decodeAsUTF8(out.toByteArray()));
    }

    @Test
    public void testFilePart() throws Exception {
        Path path = Files.createTempFile("direct-http-file-part", "");
        Files.write(path, StringUtils.encodeAsUTF8(SRC), StandardOpenOption.WRITE);
        Part part = new FilePart(path.toFile());
        part.header().put("Content-Disposition", "form-data; name=\"myTextField\"; filename=demo.txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        part.writeTo(out);

        String partEntry = "Content-Disposition: form-data; name=\"myTextField\"; filename=demo.txt\r\n"
                + "\r\n" + SRC;
        Assert.assertEquals(partEntry, StringUtils.decodeAsUTF8(out.toByteArray()));
    }
}
