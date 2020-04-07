package cn.icuter.directhttp.transport;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Content-Type: Multipart/mixed
 *
 * https://tools.ietf.org/html/rfc2046#section-5.1.1
 *
 * @author edward
 * @since 2020-04-06
 */
public class Multipart implements Part {
    private String boundary;
    private String subType;
    private List<MimePart> parts;

    @Override
    public boolean isMultipart() {
        return true;
    }

    @Override
    public InputStream content() {
        return null;
    }

    @Override
    public Map<String, String> header() {
        return null;
    }
}
