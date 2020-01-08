package xnetter.http.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
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

import java.io.IOException;

/**
 * 启动HTTP/HTTPS服务器
 * @author majikang
 */
public final class HttpServer {
	private static Logger logger = LoggerFactory.getLogger(HttpServer.class);

	private final int port;
	private final HttpRouter router;
	private ServerChannel serverChannel;

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

    public HttpServer(final int port, final HttpRouter router) {
        this.port = port;
        this.router = router;
    }

    public void start() throws InterruptedException {
    	ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();
        
    	ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
            	  ChannelPipeline ph = ch.pipeline();
            	  // 把单个HTTP/HTTPS请求转为FullHttpRequest或FullHttpResponse
                  ph.addLast("encoder", new HttpResponseEncoder());
                  ph.addLast("decoder", new HttpRequestDecoder());
                  ph.addLast("aggregator", new HttpObjectAggregator(10*1024*1024));
                  // 所有的HTTP/HTTPS请求，都由HttpHandler来处理
                  ph.addLast("dispatcher",  new HttpHandler(port, router));
            }
    	};
    	
        bootstrap.group(boss, work)
        	.channel(NioServerSocketChannel.class)
        	.childHandler(initializer);
    	
        // 服务器绑定端口监听
        serverChannel = (ServerChannel)bootstrap.bind(port).sync().channel();
        logger.info("Netty-http server is listening on port: {}", port);
    }
    
    public void close() {
    	if (serverChannel != null) {
            serverChannel.close();
            serverChannel = null;
    	}
    }
}
