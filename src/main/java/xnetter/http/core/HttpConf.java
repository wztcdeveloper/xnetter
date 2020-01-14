package xnetter.http.core;

import io.netty.handler.codec.http.multipart.DiskFileUpload;

public final class HttpConf {
    public final int port;
    public final boolean sslEnabled;
    public final String ksPath;
    public final String ksPassword;
    public final String certPassword;

    public String downloadDir;

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

    /**
     * 下载文件的路径
     * @param downloadDir
     */
    public void setDownloadDir(String downloadDir) {
        this.downloadDir = downloadDir;
    }

    /**
     * 上传文件保存路径
     * @param uploadDir
     */
    public void setUploadDir(String uploadDir) {
        DiskFileUpload.baseDirectory = uploadDir;
    }
}
