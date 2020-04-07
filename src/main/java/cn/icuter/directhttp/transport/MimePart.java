package cn.icuter.directhttp.transport;

import java.io.InputStream;
import java.util.Map;

/**
 * @author edward
 * @since 2020-04-06
 */
public class MimePart implements Part {
    private Map<String, String> headers;
    private InputStream in;

    @Override
    public boolean isMultipart() {
        return false;
    }

    @Override
    public InputStream content() {
        return in;
    }

    @Override
    public Map<String, String> header() {
        return headers;
    }
}
