package xnetter.http.ssl;

import io.netty.channel.socket.SocketChannel;

import javax.net.ssl.SSLEngine;

/**
 * 负责生成SSLEngine来完成SSL加密
 * @author majikang
 * @create 2020-01-16
 */
public abstract class SslFactory {

    public SSLEngine newEngine(SocketChannel ch) {
        return this.newEngine(ch, false);
    }

    public abstract SSLEngine newEngine(SocketChannel ch, boolean client);
}
