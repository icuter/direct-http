package cn.icuter.directhttp.mime;

import java.io.OutputStream;
import java.util.Map;

/**
 * @author edward
 * @since 2020-04-07
 */
public interface Part {
    byte[] CRLF = new byte[] {'\r', '\n'};

    boolean isMultipart();
    Map<String, String> header();
    void writeTo(OutputStream out) throws Exception;
}
