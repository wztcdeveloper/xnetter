package xnetter.http.core;


import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import xnetter.http.ssl.AuthFactory;
import xnetter.http.ssl.SslFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 启动HTTP服务器
 * @author majikang
* @create 2019-11-05
 */
public final class HttpServer {
	private static Logger logger = LoggerFactory.getLogger(HttpServer.class);

	public final HttpConf conf;
    public final HttpRouter router;
	private ServerChannel serverChannel;
    private SslFactory sslFactory;
    private List<HttpFilter> filters;

    /**
     * 监听端口port, 并且自动扫描和注册actionPackages下的所有Action
     * @param port 监听端口
     * @param actionPackages Action的包路径
     */
	public HttpServer(final int port, final String... actionPackages)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IOException {
	     this(port, new HttpRouter(actionPackages));
	}
    public HttpServer(final HttpConf conf, final String... actionPackages)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IOException {
        this(conf, new HttpRouter(actionPackages));
    }

    public HttpServer(final int port, final HttpRouter router) {
        this(new HttpConf(port), router);
    }

    public HttpServer(final HttpConf conf, final HttpRouter router) {
        this.conf = conf;
        this.router = router;
        this.filters = new ArrayList<>();

        if (this.conf.sslEnabled) {
            this.sslFactory = new AuthFactory(conf.ksPath,
                    conf.ksPassword, conf.certPassword);
        }
    }

    public void start() throws InterruptedException {
    	ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
            	  ChannelPipeline ph = ch.pipeline();
            	  // 把单个HTTP请求转为FullHttpRequest或FullHttpResponse
                  ph.addLast("encoder", new HttpResponseEncoder());
                  ph.addLast("decoder", new HttpRequestDecoder());
                  ph.addLast("aggregator", new HttpObjectAggregator(10*1024*1024));
                  // 所有的HTTP请求，都由HttpHandler来处理
                  ph.addLast("dispatcher",  new HttpHandler(HttpServer.this));

                  if (sslFactory != null) {
                      // 如果使用https，则必须放到第一位
                      ph.addFirst("sslHandler", new SslHandler(sslFactory.newEngine(ch)));
                  }
            }
    	};

        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();
        bootstrap.group(boss, work)
        	.channel(NioServerSocketChannel.class)
        	.childHandler(initializer);
    	
        // 服务器绑定端口监听
        serverChannel = (ServerChannel)bootstrap.bind(conf.port).sync().channel();
        logger.info("Netty-http server is listening on port: {}", conf.port);
    }
    
    public void close() {
    	if (serverChannel != null) {
            serverChannel.close();
            serverChannel = null;
    	}
    }

    public List<HttpFilter> getFilters() {
	    return this.filters;
    }

    public void registFilter(HttpFilter... filters) {
        for (HttpFilter filter : filters) {
            if (!this.filters.contains(filter)) {
                this.filters.add(filter);
            }
        }
    }

    public void unregistFilter(HttpFilter... filters) {
        for (HttpFilter filter : filters) {
            this.filters.remove(filter);
        }
    }
}
