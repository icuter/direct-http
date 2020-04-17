package cn.icuter.directhttp.transport;

import cn.icuter.directhttp.mime.Multipart;

import java.io.InputStream;

public class MessageBodyFactory {
    private static final MessageBody INSTANCE = new EmptyMessageBody();

    private MessageBodyFactory() {
    }
    public static MessageBody empty() {
        return INSTANCE;
    }

    public static MessageBody binary(byte[] data) {
        return new BinaryMessageBody(data);
    }

    public static MessageBody binary(byte[] data, long length) {
        return new BinaryMessageBody(data, length);
    }

    public static MessageBody text(String text) {
        return new TextMessageBody(text);
    }

    public static MessageBody text(String text, String charset) {
        return new TextMessageBody(text, charset);
    }

    public static MessageBody multipart(Multipart multipart) {
        return new MultipartMessageBody(multipart);
    }

    public static MessageBody stream(InputStream in) {
        return new InputStreamMessageBody(in);
    }
}
