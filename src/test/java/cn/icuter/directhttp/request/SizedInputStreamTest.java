package cn.icuter.directhttp.request;

import cn.icuter.directhttp.request.SizedInputStream;
import cn.icuter.directhttp.utils.IOUtils;
import cn.icuter.directhttp.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SizedInputStreamTest {

    @Test
    public void testSized() throws IOException {
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(data())) {
            testSizedStream(byteIn, "1234567890", 10);
            testSizedStream(byteIn, "abcdefghij", 10);
            testSizedStream(byteIn, "我爱吃鱼", 12);
            testSizedStream(byteIn, "!@#$%^", 10);

            Assert.assertEquals(0, byteIn.available());
        }
    }

    private void testSizedStream(InputStream in, String expected, int size) throws IOException {
        SizedInputStream sized = new SizedInputStream(in, size);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.readBytesTo(sized, out);
        Assert.assertEquals(0, sized.available());
        Assert.assertEquals(expected, StringUtils.decodeAsUTF8(out.toByteArray()));
    }

    private byte[] data() {
        return StringUtils.encodeAsUTF8("1234567890abcdefghij我爱吃鱼!@#$%^");
    }
}
