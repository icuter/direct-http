package cn.icuter.directhttp;

public class Direct {

    public static Request newRequest(String host) {
        return new Request(host);
    }

    public static Request newRequest(String scheme, String ipOrDomain) {
        return newRequest(scheme + "://" + ipOrDomain);
    }
}
