package cn.icuter.directhttp.data;

import cn.icuter.directhttp.mime.Multipart;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author edward
 * @since 2020-04-12
 */
public class MultipartMessageBody implements MessageBody {
    private Multipart multipart;

    public MultipartMessageBody(Multipart multipart) {
        this.multipart = multipart;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        multipart.writeTo(out);
    }

    @Override
    public long contentLength() {
        return multipart.length();
    }
}
