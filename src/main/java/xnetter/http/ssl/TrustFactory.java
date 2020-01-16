package xnetter.http.ssl;

import io.netty.channel.socket.SocketChannel;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * SSL加密，不用证书
 * @author majikang
 * @create 2020-01-16
 */
public final class TrustFactory extends SslFactory {
    private final SSLContext sslContext;

    public TrustFactory() throws NoSuchAlgorithmException, KeyManagementException {
        sslContext = makeContext();
    }

     private SSLContext makeContext() throws NoSuchAlgorithmException, KeyManagementException {
         TrustManager[] trustAllCerts = new TrustManager[] {
                 new X509TrustManager() {
                     @Override
                     public X509Certificate[] getAcceptedIssuers() {
                         return new X509Certificate[0];
                     }

                     @Override
                     public void checkClientTrusted(X509Certificate[] certs, String authType) {
                     }

                     @Override
                     public void checkServerTrusted(X509Certificate[] certs, String authType) {
                     }
                 }};

         // Ignore differences between given hostname and certificate hostname
         SSLContext ctx = SSLContext.getInstance("SSL");
         ctx.init(null, trustAllCerts, new SecureRandom());
         HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
         HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        return ctx;
    }

    @Override
    public SSLEngine newEngine(SocketChannel ch, boolean client) {
        SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(client);
        sslEngine.setNeedClientAuth(false);
        return sslEngine;
    }
}
