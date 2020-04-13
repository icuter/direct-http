package cn.icuter.directhttp.transport;

import cn.icuter.directhttp.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author edward
 * @since 2020-04-12
 */
public class BinaryMessageBody implements MessageBody {
    byte[] bytes;

    public BinaryMessageBody(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public void readFrom(InputStream in) throws IOException {
        bytes = IOUtils.readAllBytes(in);
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(bytes);
    }
}
