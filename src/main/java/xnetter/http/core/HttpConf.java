package xnetter.http.core;

public final class HttpConf {
    public final int port;
    public final boolean sslEnabled;
    public final String keystorePath;
    public final String keystorePassword;
    public final String certificatePassword;

    public HttpConf(int port) {
        this(port, false, "", "", "");
    }

    public HttpConf(int port, boolean sslEnabled, String keystorePath,
                    String keystorePassword, String certificatePassword) {
        this.port = port;
        this.sslEnabled = sslEnabled;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.certificatePassword = certificatePassword;
    }
}
