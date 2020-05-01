package cn.icuter.directhttp.response;

import cn.icuter.directhttp.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ContentLengthInputStream extends FilterInputStream {

    private int remainingContentLength;
    private boolean closed;
    private boolean closeReadAll;

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
            if (closeReadAll) {
                in.close();
            }
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
            if (closeReadAll) {
                in.close();
            }
            return -1;
        }
        len = Math.min(remainingContentLength, len);
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

    @Override
    public int available() throws IOException {
        return Math.max(remainingContentLength, 0);
    }

    public void setCloseReadAll() {
        this.closeReadAll = true;
    }
}
