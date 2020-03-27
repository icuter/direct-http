package cn.icuter.directhttp.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
