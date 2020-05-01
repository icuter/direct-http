package cn.icuter.directhttp.data;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author edward
 * @since 2020-04-12
 */
public class BinaryMessageBody implements MessageBody {
    private byte[] bytes;
    private long length;

    public BinaryMessageBody(byte[] bytes) {
        this(bytes, bytes.length);
    }

    public BinaryMessageBody(byte[] bytes, long length) {
        this.bytes = bytes;
        if (length < 0) {
            throw new IllegalArgumentException("the length (" + length + ") must be positive");
        }
        this.length = length;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(bytes, 0, (int) length);
    }

    @Override
    public long contentLength() {
        return length;
    }
}
