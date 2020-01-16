package xnetter.http.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xnetter.http.ssl.AuthFactory;
import xnetter.http.ssl.SslFactory;
import xnetter.http.ssl.TrustFactory;
import xnetter.utils.KeyValue;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public final class HttpClient {
    private static Logger logger = LoggerFactory.getLogger(HttpClient.class);
    private static final String CONNECTION_KEEP_ALIVE = "keep-alive";
    private static final String CONNECTION_CLOSE = "close";

    public static abstract class Handler extends ChannelInboundHandlerAdapter {
        public abstract void onRecv(String content);

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            logger.debug("recv from {}", ctx.channel().remoteAddress().toString());
            logger.debug("recv content: {}", msg);

            if (msg instanceof FullHttpResponse) {
                ByteBuf buf = ((FullHttpResponse)msg).content();
                onRecv(buf.toString(CharsetUtil.UTF_8));
                buf.release();
            } else if(msg instanceof HttpContent) {
                ByteBuf buf = ((HttpContent)msg).content();
                onRecv(buf.toString(CharsetUtil.UTF_8));
                buf.release();
            }
        }
    }

    private final Handler handler;
    private final boolean sslEnable;
    private final String ksPath;
    private final String ksPassword;

    private String host;
    private int port;
    public Channel channel;

    private SslFactory sslFactory;

    public HttpClient(Handler handler)
            throws NoSuchAlgorithmException, KeyManagementException {
        this(handler, false);
    }

    public HttpClient(Handler handler, boolean sslEnable)
            throws NoSuchAlgorithmException, KeyManagementException {
        this(handler, sslEnable, null, null);
    }

    /**
     * @param ksPath 证书路径
     * @param ksPassword 证书密码
     */
    public HttpClient(Handler handler, boolean sslEnable, String ksPath, String ksPassword)
            throws KeyManagementException, NoSuchAlgorithmException {
        this.handler = handler;
        this.sslEnable = sslEnable;
        this.ksPath = ksPath;
        this.ksPassword = ksPassword;
        this.sslFactory = makeSslFactory();
    }

    public HttpClient(HttpClient client)
            throws NoSuchAlgorithmException, KeyManagementException {
        this(client.handler, client.sslEnable, client.ksPath, client.ksPassword);
    }

    private SslFactory makeSslFactory()
            throws KeyManagementException, NoSuchAlgorithmException {
        SslFactory factory = null;
        if (this.sslEnable) {
            if (StringUtils.isNotEmpty(ksPath) && StringUtils.isNotEmpty(ksPassword)) {
                factory = new AuthFactory(ksPath, ksPassword);
            } else {
                factory = new TrustFactory();
            }
        }
        return factory;
    }

    public void start(String host, int port) throws InterruptedException {
        this.host = host;
        this.port = port;

        ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline ph = ch.pipeline();
                ph.addLast("encoder", new HttpClientCodec());
                ph.addLast("aggregator", new HttpObjectAggregator(10*1024*1024));
                // 所有的HTTP请求，都由HttpHandler来处理
                ph.addLast("dispatcher",  handler);

                if (sslFactory != null) {
                    // 如果使用https，则必须放到第一位
                    ph.addFirst("sslHandler", new SslHandler(sslFactory.newEngine(ch, true)));
                }
            }
        };

        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup boss = new NioEventLoopGroup();
        bootstrap.group(boss)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(initializer);

        // 连接服务器地址
        channel = bootstrap.connect(host, port).sync().channel();
        logger.info("Netty-http client is connecting to {}:{}", host, port);
    }

    public void get(String url)
            throws UnsupportedEncodingException,
            MalformedURLException, InterruptedException,
            NoSuchAlgorithmException, KeyManagementException {
        request(url, HttpMethod.GET, null);
    }

    public void post(String url, String content)
            throws UnsupportedEncodingException,
            MalformedURLException, InterruptedException,
            NoSuchAlgorithmException, KeyManagementException {
        request(url, HttpMethod.POST, content);
    }

    public void request(String url, HttpMethod method, String content)
            throws UnsupportedEncodingException,
            MalformedURLException, InterruptedException,
            KeyManagementException, NoSuchAlgorithmException {
        KeyValue<String, Integer> address = getAddress(new URL(url));

        HttpClient client = this;
        if (StringUtils.isEmpty(this.host) || client.channel == null) {
            client.start(address.getKey(), address.getValue());
        } else if (!address.equals(this.host, this.port)) {
            // 构建新的HttpClient来请求
            client = new HttpClient(this);
            client.start(address.getKey(), address.getValue());
        } else if (!client.channel.isActive()) {
            client.channel.connect(new InetSocketAddress(client.host, client.port));
        }

        client.doRequest(url, method, content);
    }

    private void doRequest(String url, HttpMethod method, String content)
            throws UnsupportedEncodingException, MalformedURLException {
        FullHttpRequest request = null;
        if (StringUtils.isEmpty(content)) {
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, url);
        } else {
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, url,
                    Unpooled.wrappedBuffer(content.getBytes("UTF-8")));
        }

        // 构建http请求
        request.headers().set(HttpHeaderNames.HOST, new URL(url).getHost());
        request.headers().set(HttpHeaderNames.CONNECTION, CONNECTION_KEEP_ALIVE);
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        // 发送http请求
        channel.writeAndFlush(request).addListener(future -> {
            if (future.isSuccess()) {
                logger.debug("request success, url={}", url);
            } else {
                logger.error(future.cause().getMessage(), future.cause());
            }
        });;
    }

    public void close() {
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }

    private KeyValue<String, Integer> getAddress(URL url) {
        String host = url.getHost();
        int port = url.getPort();
        if(port == -1 && url.getProtocol().equalsIgnoreCase("http")) {
            port = 80;
        }
        if(port == -1 && url.getProtocol().equalsIgnoreCase("https")) {
            port = 443;
        }

        return new KeyValue<>(host, port);
    }


}
