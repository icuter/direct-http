package cn.icuter.directhttp.transport;

import cn.icuter.directhttp.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author edward
 * @since 2020-04-12
 */
public class TextMessageBody implements MessageBody {
    final String text;
    final Charset encodingCharset;

    public TextMessageBody(String text) {
        this(text, StandardCharsets.UTF_8);
    }

    public TextMessageBody(String text, Charset encodingCharset) {
        this.text = text;
        this.encodingCharset = encodingCharset;
    }

    @Override
    public void readFrom(InputStream in) throws IOException {

    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(StringUtils.encodeAs(text, encodingCharset));
    }
}
