package cn.icuter.directhttp.mime;

import cn.icuter.directhttp.utils.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;

public class StreamPart extends BodyPart {
    private final InputStream in;

    public StreamPart(InputStream in) {
        this.in = in;
    }

    @Override
    public void writeBodyTo(OutputStream out) throws Exception {
        IOUtils.readBytesTo(in, out);
    }
}
