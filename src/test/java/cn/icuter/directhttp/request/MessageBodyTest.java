package cn.icuter.directhttp.request;

import cn.icuter.directhttp.mime.Multipart;
import cn.icuter.directhttp.mime.TextPart;
import cn.icuter.directhttp.request.MessageBody;
import cn.icuter.directhttp.request.MessageBodyFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MessageBodyTest {
    public static final String SRC = "我 Love 吃鱼";
    @Test
    public void testTextMessageBody() throws IOException {
        byte[] bytes = SRC.getBytes(StandardCharsets.UTF_8);
        MessageBody messageBody = MessageBodyFactory.text(SRC);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        messageBody.writeTo(out);
        Assert.assertEquals(bytes.length, messageBody.contentLength());
        Assert.assertArrayEquals(bytes, out.toByteArray());

        bytes = SRC.getBytes("GBK");
        messageBody = MessageBodyFactory.text(SRC, "GBK");
        out = new ByteArrayOutputStream();
        messageBody.writeTo(out);
        Assert.assertEquals(bytes.length, messageBody.contentLength());
        Assert.assertArrayEquals(bytes, out.toByteArray());
    }

    @Test
    public void testBinaryMessageBody() throws IOException {
        byte[] bytes = SRC.getBytes(StandardCharsets.UTF_8);
        MessageBody messageBody = MessageBodyFactory.binary(bytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        messageBody.writeTo(out);
        Assert.assertEquals(bytes.length, messageBody.contentLength());
        Assert.assertArrayEquals(bytes, out.toByteArray());

        int len = bytes.length / 2;
        messageBody = MessageBodyFactory.binary(bytes, len);
        out = new ByteArrayOutputStream();
        messageBody.writeTo(out);
        byte[] data = new byte[len];
        System.arraycopy(bytes, 0, data, 0, len);
        Assert.assertEquals(len, messageBody.contentLength());
        Assert.assertArrayEquals(data, out.toByteArray());
    }

    @Test
    public void testInputStreamMessageBody() throws IOException {
        byte[] bytes = SRC.getBytes(StandardCharsets.UTF_8);
        MessageBody messageBody = MessageBodyFactory.stream(new ByteArrayInputStream(bytes));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        messageBody.writeTo(out);
        Assert.assertEquals(messageBody.contentLength(), bytes.length);
        Assert.assertArrayEquals(bytes, out.toByteArray());

        messageBody = MessageBodyFactory.stream(new ByteArrayInputStream(bytes), bytes.length);
        out = new ByteArrayOutputStream();
        messageBody.writeTo(out);
        Assert.assertEquals(messageBody.contentLength(), bytes.length);
        Assert.assertArrayEquals(bytes, out.toByteArray());
    }

    @Test
    public void testMultipartMessageBody() throws IOException {
        Multipart multipart = new Multipart();
        multipart.addBodyPart(new TextPart(SRC));
        MessageBody messageBody = MessageBodyFactory.multipart(multipart);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        multipart.writeTo(out);
        byte[] data1 = out.toByteArray();

        out = new ByteArrayOutputStream();
        messageBody.writeTo(out);
        byte[] data2 = out.toByteArray();
        Assert.assertEquals(multipart.length(), messageBody.contentLength());
        Assert.assertArrayEquals(data1, data2);
    }
}
