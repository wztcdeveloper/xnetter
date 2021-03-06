package xnetter.http.ssl;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;

/**
 * SSL加密，必须使用证书
 * @author majikang
 * @create 2020-01-16
 */
public final class AuthFactory extends SslFactory {
    private static Logger logger = LoggerFactory.getLogger(AuthFactory.class);

    public final String ksPath;
    public final String ksPassword;
    public final String certPassword;
    private SslContext sslContext = null;

    public AuthFactory(String ksPath, String ksPassword) {
        this(ksPath, ksPassword, "");
    }

    public AuthFactory(String ksPath, String ksPassword, String certPassword) {
        this.ksPath = ksPath;
        this.ksPassword = ksPassword;
        this.certPassword = certPassword;
        this.init(StringUtils.isEmpty(certPassword));
    }

    @Override
    public SSLEngine newEngine(SocketChannel ch, boolean client) {
        SSLEngine sslEngine = sslContext.newEngine(ch.alloc());
        sslEngine.setUseClientMode(client);
        sslEngine.setNeedClientAuth(client && StringUtils.isNotEmpty(ksPath) && StringUtils.isNotEmpty(ksPassword));
        return sslEngine;
    }

    private void init(boolean client) {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }

        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(getKeyStoreStream(), getKeyStorePassword());

            if (client) {
                TrustManagerFactory tf = TrustManagerFactory.getInstance(algorithm);
                tf.init(ks);
                sslContext = SslContextBuilder.forClient().trustManager(tf).build();
            } else {
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
                kmf.init(ks, getCertificatePassword());
                sslContext = SslContextBuilder.forServer(kmf).build();;
            }
        } catch (Exception e) {
            logger.error("初始化server SSL失败", e);
        }
    }

    private InputStream getKeyStoreStream() {
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(ksPath);
        } catch (FileNotFoundException e) {
            logger.error("读取密钥文件失败", e);
        }
        return inStream;
    }

    /**
     * 获取密钥密码(证书别名密码)
     * @return
     */
    private char[] getKeyStorePassword() {
        return ksPassword.toCharArray();
    }

    /**
     * 获取安全证书密码
     * @return
     */
    private char[] getCertificatePassword() {
        return certPassword.toCharArray();
    }
}
