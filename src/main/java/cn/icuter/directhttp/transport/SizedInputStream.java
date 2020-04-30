package cn.icuter.directhttp.transport;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SizedInputStream extends FilterInputStream {

    private boolean eof;
    private int remainingSize;
    private byte[] buffer = new byte[1]; // temp byte buffer

    public SizedInputStream(InputStream in, int maxReadSize) {
        super(in);
        this.remainingSize = maxReadSize;
    }

    protected SizedInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        if (read(buffer, 0, buffer.length) > 0) {
            return buffer[0] & 0xFF;
        }
        return -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (eof || remainingSize == 0) {
            return -1;
        }
        len = Math.min(remainingSize, len);
        int n = super.read(b, off, len);

        remainingSize = n > 0 ? remainingSize - n : 0;

        if (remainingSize == 0) {
            eof = true;
        }
        return n;
    }

    @Override
    public void close() throws IOException {
        if (!eof) {
            eof = true;
        }
    }

    @Override
    public int available() throws IOException {
        return remainingSize;
    }
}
