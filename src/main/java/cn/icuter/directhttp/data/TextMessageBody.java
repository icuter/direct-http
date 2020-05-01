package cn.icuter.directhttp.data;

import cn.icuter.directhttp.utils.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author edward
 * @since 2020-04-12
 */
public class TextMessageBody implements MessageBody {
    private MessageBody binary;

    public TextMessageBody(String text) {
        this(text, "UTF-8");
    }

    public TextMessageBody(String text, String charset) {
        this.binary = new BinaryMessageBody(StringUtils.encodeAs(text, Charset.forName(charset)));
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        binary.writeTo(out);
    }

    @Override
    public long contentLength() {
        return binary.contentLength();
    }
}
