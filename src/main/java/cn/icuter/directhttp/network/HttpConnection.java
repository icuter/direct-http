package cn.icuter.directhttp.network;

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
    private boolean supportKeepAlive;
    private int connTimeout;
    private int readTimeout;

    HttpConnection(String host, int port) {
        clientSocket = new Socket();
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
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

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isSupportKeepAlive() {
        return supportKeepAlive;
    }

    public void setSupportKeepAlive(boolean supportKeepAlive) {
        this.supportKeepAlive = supportKeepAlive;
    }

    public OutputStream getOutputStream() throws IOException {
        return clientSocket.getOutputStream();
    }

    public InputStream getInputStream() throws IOException {
        return clientSocket.getInputStream();
    }

    public int getConnTimeout() {
        return connTimeout;
    }

    public void setConnTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public boolean isClosed() {
        return clientSocket.isClosed();
    }

    @Override
    public void close() throws IOException {
        clientSocket.close();
    }
}
