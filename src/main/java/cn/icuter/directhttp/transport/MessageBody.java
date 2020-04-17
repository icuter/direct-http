package cn.icuter.directhttp.transport;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author edward
 * @since 2020-04-11
 */
public interface MessageBody {

    /**
     * Writes data to the Http Connection Stream for finishing the http request
     *
     * @param out the OutputStream of the Http Connection
     * @throws IOException if OutputStream has been closed or unstable network
     */
    void writeTo(OutputStream out) throws IOException;

    /**
     * Indicates the message body length, actually,
     * message body length is measured to non-fixed or fixed.
     * <i>Content-Length</i> header is for fixed body length request and
     * <i>Transfer-Encoding</i> is for non-fixed body length request.
     * If exists <i>Transfer-Encoding</i> header, content length will be ignored.
     *
     * @return message body length <br/>
     *         0 means message body NOT provided <br/>
     */
    long contentLength();
}
