package cn.icuter.directhttp;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class CustomSSLSocketFactory extends SSLSocketFactory {
    private javax.net.ssl.SSLSocketFactory impl;

    private CustomSSLSocketFactory() {
        try {
            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("SSL");
            sslContext.init(null, initTrustManager(), null);
            this.impl = sslContext.getSocketFactory();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    javax.net.ssl.TrustManager[] initTrustManager() {
        return new javax.net.ssl.TrustManager[]{new javax.net.ssl.X509TrustManager() {

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) {}

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) {}
        }};
    }


    protected Socket configSocket(Socket socket) {
        if (socket instanceof javax.net.ssl.SSLSocket)
        // CSOFF: LineLength|LeftCurly
        // noinspection SpellCheckingInspection
        {
            // Disable TLS for Sun Java security package issue: http://bugs.sun.com/view_bug.do?bug_id=4815023
            // 'bad_record_mac' error for self signed cert
            // workaround is from http://bugs.java.com/view_bug.do?bug_id=4639763

            // 2015-07-02: disabled this workaround by "William Leung" <lwr@coremail.cn>
            // ((javax.net.ssl.SSLSocket) socket).setEnabledProtocols(new String[]{"SSLv2Hello", "SSLv3"});

            // Check CM-21907, this workaround would failed in jdk 1.8.0_31
                /* == StackTraces ==
                    javax.net.ssl.SSLHandshakeException: No appropriate protocol (protocol is disabled or cipher suites are inappropriate)
                        at sun.security.ssl.Handshaker.activate(Handshaker.java:503)
                        at sun.security.ssl.SSLSocketImpl.kickstartHandshake(SSLSocketImpl.java:1470)
                        at sun.security.ssl.SSLSocketImpl.performInitialHandshake(SSLSocketImpl.java:1339)
                        at sun.security.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:1391)
                        at sun.security.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:1375)
                        at sun.net.www.protocol.https.HttpsClient.afterConnect(HttpsClient.java:563)
                        at sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.connect(AbstractDelegateHttpsURLConnection.java:185)
                        at sun.net.www.protocol.https.HttpsURLConnectionImpl.connect(HttpsURLConnectionImpl.java:153)
                 */
        }
        // CSON: LineLength|LeftCurly
        return socket;
    }

    public static javax.net.ssl.SSLSocketFactory getDefault() {
        return new CustomSSLSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return impl.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return impl.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return configSocket(impl.createSocket(socket, host, port, autoClose));
    }

    @Override
    public Socket createSocket() throws IOException {
        return configSocket(impl.createSocket());
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return configSocket(impl.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return configSocket(impl.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return configSocket(impl.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress host, int port, InetAddress localHost, int localPort) throws IOException {
        return configSocket(impl.createSocket(host, port, localHost, localPort));
    }

}
