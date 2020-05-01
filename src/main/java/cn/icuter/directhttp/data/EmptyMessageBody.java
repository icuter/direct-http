package cn.icuter.directhttp.data;

import java.io.IOException;
import java.io.OutputStream;

public class EmptyMessageBody implements MessageBody {
    @Override
    public void writeTo(OutputStream out) throws IOException {
    }
    @Override
    public long contentLength() {
        return 0;
    }
}
