package cn.icuter.directhttp.transport;

import cn.icuter.directhttp.mime.Multipart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author edward
 * @since 2020-04-12
 */
public class MultipartMessageBody implements MessageBody {
    Multipart multipart;

    public MultipartMessageBody(Multipart multipart) {
        this.multipart = multipart;
    }

    @Override
    public void readFrom(InputStream in) throws IOException {
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        multipart.writeTo(out);
    }
}
