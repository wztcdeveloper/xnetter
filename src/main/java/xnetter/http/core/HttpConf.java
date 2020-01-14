package xnetter.http.core;

import io.netty.handler.codec.http.multipart.DiskFileUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HttpConf {
    public final int port;
    public final boolean sslEnabled;
    public final String ksPath;
    public final String ksPassword;
    public final String certPassword;

    public List<String> downloadDirs;

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
        this.downloadDirs = new ArrayList<>();
    }

    /**
     * 上传文件保存路径，可以是绝对路径，也可以是相对路径
     * 相对路径是相对项目根目录的路径
     * @param uploadDir
     */
    public void setUploadDir(String uploadDir) throws IOException {
        File dir = new File(uploadDir);
        if (dir.exists() && dir.isDirectory()) {
            DiskFileUpload.baseDirectory = uploadDir;
        } else {
            String newDir = new File("").getCanonicalPath() + uploadDir;

            dir = new File(newDir);
            if (dir.exists() && dir.isDirectory()) {
                DiskFileUpload.baseDirectory = newDir;
            } else {
                throw new FileNotFoundException(String.format("Directory not found: \"%s\" or \"%s\"",
                        uploadDir, newDir));
            }
        }
    }

    /**
     * 下载文件的路径，只能是相对路径
     * 相对路径是相对项目根目录的路径
     * @param downloadDirs
     */
    public void addDownloadDir(String... downloadDirs) {
        Collections.addAll(this.downloadDirs, downloadDirs);
    }

    public boolean isDownloadDir(String path) {
        return downloadDirs.contains(path);
    }
}
