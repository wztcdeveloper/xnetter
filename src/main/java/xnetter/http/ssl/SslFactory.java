package xnetter.http.ssl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xnetter.http.core.HttpConf;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;

public class SslFactory {
    private static Logger logger = LoggerFactory.getLogger(SslFactory.class);
    private static final String PROTOCOL = "SSLv3";

    private final HttpConf conf;
    private SSLContext sslContext = null;

    public SslFactory(HttpConf conf) {
        this.conf = conf;
        this.init();
    }

    public SSLEngine createSSLEngine() {
        SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(false);
        sslEngine.setNeedClientAuth(false);
        return sslEngine ;
    }

    private void init() {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }

        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(getKeyStoreStream(), getKeyStorePassword());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, getCertificatePassword());

            sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(kmf.getKeyManagers(), null, null);
        } catch (Exception e) {
            logger.error("初始化server SSL失败", e);
        }
    }

    private InputStream getKeyStoreStream() {
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(conf.keystorePath);
        } catch (FileNotFoundException e) {
            logger.error("读取密钥文件失败", e);
        }
        return inStream;
    }

    /**
     * 获取安全证书密码
     * @return
     */
    private char[] getCertificatePassword() {
        return conf.certificatePassword.toCharArray();
    }

    /**
     * 获取密钥密码(证书别名密码)
     * @return
     */
    private char[] getKeyStorePassword() {
        return conf.keystorePassword.toCharArray();
    }
}
