package cn.icuter.directhttp.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class IOUtilsTest {
    @Test
    public void testReadLine() throws IOException {
        String s = "abc 1235 我爱吃榴莲\r\n";
        ByteArrayInputStream in = new ByteArrayInputStream(StringUtils.encodeAsUTF8(s));
        byte[] line = IOUtils.readLine(in);

        Assert.assertEquals(StringUtils.decodeAsUTF8(line), s.substring(0, s.length() - 2));
    }
}
