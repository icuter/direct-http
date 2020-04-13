package cn.icuter.directhttp.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class IOUtils {

    public static byte[] readLine(InputStream in) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (int b = in.read(); b != -1; b = in.read()) {
                if (b != '\r' && b != '\n') {
                    out.write(b);
                }
                if (b == '\n') {
                    break;
                }
            }
            return out.toByteArray();
        }
    }

    public static byte[] readAllBytes(InputStream src) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        readBytesTo(src, out, 16384); // 16KB
        return out.toByteArray();
    }

    public static void readBytesTo(InputStream src, OutputStream target) throws IOException {
        readBytesTo(src, target, 16384); // 16KB
    }

    public static void readBytesTo(InputStream src, OutputStream target, int bufferSize) throws IOException {
        byte[] bytes = new byte[bufferSize];
        for (int n = src.read(bytes); n > 0; n = src.read(bytes)) {
            target.write(bytes, 0, n);
        }
    }

    public static void tryCleanupRemaining(InputStream in) throws IOException {
        int remaining = in.available();
        if (remaining <= 0) {
            return;
        }
        byte[] buffer = new byte[remaining];
        int n = buffer.length;
        int read;
        do {
            read = in.read(buffer);
        } while (read > 0 && (n -= read) > 0);
    }
}
