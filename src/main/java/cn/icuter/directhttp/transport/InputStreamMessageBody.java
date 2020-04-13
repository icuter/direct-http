package cn.icuter.directhttp.transport;

import cn.icuter.directhttp.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * @author edward
 * @since 2020-04-12
 */
public class InputStreamMessageBody implements MessageBody {
    InputStream in;

    public InputStreamMessageBody() {
    }

    public InputStreamMessageBody(InputStream in) {
        this.in = in;
    }

    @Override
    public void readFrom(InputStream in) throws IOException {
        this.in = in;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        Objects.requireNonNull(in, "Input stream must NOT be null");

        IOUtils.readBytesTo(in, out);
    }
}
