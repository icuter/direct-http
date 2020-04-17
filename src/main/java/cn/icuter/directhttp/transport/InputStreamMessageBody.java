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
    private InputStream in;
    private long size;

    public InputStreamMessageBody(InputStream in) {
        this(in, IOUtils.avaiable(in));
    }

    public InputStreamMessageBody(InputStream in, int size) {
        this.in = in;
        if (size <= 0) {
            throw new IllegalArgumentException("the size (" + size + ") must be positive");
        }
        this.size = size;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        Objects.requireNonNull(in, "Input stream must NOT be null");

        IOUtils.readBytesTo(in, out);
    }

    @Override
    public long contentLength() {
        return size;
    }
}
