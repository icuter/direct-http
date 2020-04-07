package cn.icuter.directhttp.mime;

import cn.icuter.directhttp.utils.StringUtils;

import java.io.OutputStream;

public class TextPart extends BodyPart {
    private final String text;

    public TextPart(String text) {
        this.text = text;
    }
    @Override
    public void writeBodyTo(OutputStream out) throws Exception {
        // get charset from ContentType parameters (default UTF-8)
        out.write(StringUtils.encodeAsUTF8(text));
    }
}
