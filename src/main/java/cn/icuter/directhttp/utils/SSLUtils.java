package cn.icuter.directhttp.utils;

import cn.icuter.directhttp.CustomSSLSocketFactory;

public class SSLUtils {
    public static javax.net.ssl.SSLSocketFactory getDummySSLSocketFactory() {
        return CustomSSLSocketFactory.getDefault();
    }

    public static javax.net.ssl.HostnameVerifier getDummyHostnameVerifier() {
        return (hostName, sslSession) -> true;
    }

}
