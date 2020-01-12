package xnetter.http.core;

public final class HttpConf {
    public final int port;
    public final boolean sslEnabled;
    public final String ksPath;
    public final String ksPassword;
    public final String certPassword;

    public HttpConf(int port) {
        this(port, false, "", "", "");
    }

    public HttpConf(int port, boolean sslEnabled, String ksPath,
                    String ksPassword, String certPassword) {
        this.port = port;
        this.sslEnabled = sslEnabled;
        this.ksPath = ksPath;
        this.ksPassword = ksPassword;
        this.certPassword = certPassword;
    }
}
