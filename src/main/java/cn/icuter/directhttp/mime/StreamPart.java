package cn.icuter.directhttp.mime;

import cn.icuter.directhttp.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamPart extends BodyPart {
    private final InputStream in;
    private long available;

    public StreamPart(InputStream in) {
        this.in = in;
        try {
            this.available = in.available();
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid InputStream");
        }
    }

    @Override
    public void writeBodyTo(OutputStream out) throws Exception {
        IOUtils.readBytesTo(in, out);
    }

    @Override
    public long bodyLength() {
        return available;
    }
}
