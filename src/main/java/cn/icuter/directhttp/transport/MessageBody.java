package cn.icuter.directhttp.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author edward
 * @since 2020-04-11
 */
public interface MessageBody {
    void readFrom(InputStream in) throws IOException;
    void writeTo(OutputStream out) throws IOException;
}
