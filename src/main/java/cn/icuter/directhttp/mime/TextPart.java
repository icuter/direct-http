package cn.icuter.directhttp.mime;

import cn.icuter.directhttp.utils.HeaderUtils;
import cn.icuter.directhttp.utils.StringUtils;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TextPart extends BodyPart {
    private final String text;

    public TextPart(String text) {
        this.text = text;
    }

    @Override
    public void writeBodyTo(OutputStream out) throws Exception {
        out.write(StringUtils.encodeAs(text, Charset.forName(getCharset())));
    }

    @Override
    public long bodyLength() {
        return StringUtils.encodeAs(text, Charset.forName(getCharset())).length;
    }

    private String getCharset() {
        String contentType = header().getOrDefault("Content-Type", "");
        return HeaderUtils.findHeaderParamValue(contentType, "charset", StandardCharsets.UTF_8.name());
    }
}
