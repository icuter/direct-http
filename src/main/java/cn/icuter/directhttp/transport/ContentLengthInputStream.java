package cn.icuter.directhttp.transport;

import cn.icuter.directhttp.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ContentLengthInputStream extends FilterInputStream {

    private int remainingContentLength;
    private volatile boolean closed;

    private ContentLengthInputStream(InputStream in, int contentLength) {
        super(in);
        if (!(in instanceof BufferedInputStream) && contentLength > 0) {
            this.in = new BufferedInputStream(in, contentLength);
        }
    }

    public static ContentLengthInputStream of(InputStream in) throws IOException {
        return of(in, in.available());
    }

    public static ContentLengthInputStream of(InputStream in, int contentLength) throws IOException {
        ContentLengthInputStream contentLengthIn = new ContentLengthInputStream(in, contentLength);
        contentLengthIn.remainingContentLength = contentLength;
        return contentLengthIn;
    }

    @Override
    public int read() throws IOException {
        if (closed) {
            throw new IOException("Stream closed!");
        }
        if (remainingContentLength <= 0) {
            IOUtils.tryCleanupRemaining(in);
            return -1;
        }
        int b = super.read();
        if (b != -1) {
            remainingContentLength--;
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (closed) {
            throw new IOException("Stream closed!");
        }
        if (remainingContentLength <= 0) {
            IOUtils.tryCleanupRemaining(in);
            return -1;
        }
        int n = super.read(b, off, len);
        if (n > 0) {
            remainingContentLength -= n;
        }
        return n;
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }
}
