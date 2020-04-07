package cn.icuter.directhttp.transport;

import java.io.InputStream;
import java.util.Map;

/**
 * @author edward
 * @since 2020-04-07
 */
public interface Part {
    boolean isMultipart();
    InputStream content();
    Map<String, String> header();
}
