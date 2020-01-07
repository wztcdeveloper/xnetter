package xnetter.sock.udp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import xnetter.sock.core.Coder;
import xnetter.sock.core.Dispatcher;
import xnetter.sock.core.Handler;
import xnetter.sock.core.Manager;

/**
 * UDP服务器，监听一个端口，等待接受数据
 * @author majikang
 * @create 2019-11-05
 */
public abstract class UdpServer extends Manager {
	protected static final Logger logger = LoggerFactory.getLogger(UdpServer.class);

	private Channel serverChannel;
	
    protected UdpServer(Conf conf) {
    	this(conf, Dispatcher.Factory.DEFAULT.create(conf.procPackageName), 
    			Coder.Factory.DEFAULT, UdpHandler.DEFAULT);
    }
    
    public UdpServer(Conf conf, Dispatcher<?> dispatcher, 
    		Coder.Factory coderFactory, Handler.Factory handlerFactory) {
    	super(conf, dispatcher, coderFactory, handlerFactory);
    }
	
    public void start() throws InterruptedException {
		Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        
    	ChannelInitializer<NioDatagramChannel> initializer = new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel ch) throws Exception {
            	 ChannelConfig cc = ch.config();
                 cc.setOption(ChannelOption.SO_SNDBUF, conf.socketSendBuff);
                 cc.setOption(ChannelOption.SO_RCVBUF, conf.socketRecvBuff);
                 cc.setOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535));
                 
                 ChannelPipeline pipeline = ch.pipeline();
                 pipeline.addLast("handler", new RecvHandler(UdpServer.this));
                 //pipeline.addLast("handler", handlerFactory.create(0, UdpServer.this));
            }
    	};
    	
    	bootstrap.group(group)
        	.channel(NioDatagramChannel.class)
        	.option(ChannelOption.SO_BROADCAST, true)
        	.handler(initializer); 
    	
        // 服务器绑定端口监听
    	serverChannel = bootstrap.bind(conf.ip, conf.port).sync().channel();
        logger.info("Netty-udp server is listening on {}:{}", conf.ip, conf.port);
	}
    
    public void close() {
    	if (serverChannel != null) {
    		serverChannel.close();
    		serverChannel = null;
    	}
    }
    

}
