package cn.icuter.directhttp.transport;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * <ol>
 *     <li>Plain Socket</li>
 *     <li>SSL Socket</li>
 *     <li>Socket Proxy</li>
 * </ol>
 *
 * @author Edward LeeJan
 */
public class HttpConnection implements Closeable {
    private final Socket clientSocket;
    private final String host;
    private final int port;

    HttpConnection(String host, int port) {
        clientSocket = new Socket();
        this.host = host;
        this.port = port;
    }

    public void connect(int connTimeout, int readTimeout) throws IOException {
        clientSocket.connect(new InetSocketAddress(host, port), connTimeout);
        clientSocket.setSoTimeout(readTimeout);
        clientSocket.setTcpNoDelay(true);
    }

    public void write(byte[] data) throws IOException {
        clientSocket.getOutputStream().write(data);
    }

    public int read(byte[] buffer) throws IOException {
        return clientSocket.getInputStream().read(buffer);
    }

    public OutputStream getOutputStream() throws IOException {
        return clientSocket.getOutputStream();
    }

    public InputStream getInputStream() throws IOException {
        return clientSocket.getInputStream();
    }

    @Override
    public void close() throws IOException {
        clientSocket.close();
    }
}
