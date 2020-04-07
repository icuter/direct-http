package cn.icuter.directhttp.data;

import cn.icuter.directhttp.mock.MockHttpServer;
import cn.icuter.directhttp.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author edward
 * @since 2020-04-05
 */
public class TestData {

    public static byte[] smallHtmlData() throws IOException {
        return getFileBytes("small-test.html");
    }

    public static byte[] mediumHtmlData() throws IOException {
        return getFileBytes("medium-test.html");
    }

    public static String md5Hash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return Base64.getEncoder().encodeToString(digest.digest(data));
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private static byte[] getFileBytes(String filename) throws IOException {
        InputStream fileIn = MockHttpServer.class.getResourceAsStream("/" + filename);
        try (BufferedInputStream in = new BufferedInputStream(fileIn)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.readBytesTo(in, out);
            return out.toByteArray();
        }
    }
}
