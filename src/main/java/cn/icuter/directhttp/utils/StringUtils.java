package cn.icuter.directhttp.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class StringUtils {

    public static byte[] encodeAs(String src, Charset charset) {
        return src.getBytes(charset);
    }

    public static byte[] encodeAsUTF8(String src) {
        return encodeAs(src, StandardCharsets.UTF_8);
    }

    public static byte[] encodeAsISO(String src) {
        return encodeAs(src, StandardCharsets.ISO_8859_1);
    }

    public static String decodeAs(byte[] bytes, Charset charset) {
        return new String(bytes, charset);
    }

    public static String decodeAsUTF8(byte[] bytes) {
        return decodeAs(bytes, StandardCharsets.UTF_8);
    }

    public static String decodeAsISO(byte[] bytes) {
        return decodeAs(bytes, StandardCharsets.ISO_8859_1);
    }

}
