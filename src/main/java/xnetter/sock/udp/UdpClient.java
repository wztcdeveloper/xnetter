package xnetter.sock.udp;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xnetter.sock.core.Coder;
import xnetter.sock.core.Dispatcher;
import xnetter.sock.core.Handler;
import xnetter.sock.core.Manager;

/**
 * UDP客户端，绑定任意端口，主动发送数据
 * @author majikang
 * @create 2019-11-05
 */
public abstract class UdpClient extends Manager {
	protected static final Logger logger = LoggerFactory.getLogger(UdpClient.class);

	private static final ThreadLocal<ByteBuf> byteBuf = ThreadLocal.withInitial(() -> Unpooled.buffer(65535));
	
	private final Coder coder;
	private final AtomicLong sessionId;
	
	private Channel channel;
	
    protected UdpClient(Conf conf) {
    	this(conf, Dispatcher.Factory.DEFAULT.create(conf.procPackageName), 
    			Coder.Factory.DEFAULT, UdpHandler.DEFAULT);
    }
    
    public UdpClient(Conf conf, Dispatcher<?> dispatcher, 
    		Coder.Factory coderFactory, Handler.Factory handlerFactory) {
    	super(conf, dispatcher, coderFactory, handlerFactory);
    	
    	this.sessionId = new AtomicLong();
    	coder = coderFactory.create(this, null, conf.msgPackageName);
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
                pipeline.addLast("handler", handlerFactory.create(sessionId.incrementAndGet(), UdpClient.this));
        	}
    	};
    	
    	bootstrap.group(group)
        	.channel(NioDatagramChannel.class)
        	.option(ChannelOption.SO_BROADCAST, true)
        	.handler(initializer); 
    	
        // 服务器绑定端口监听
    	channel = bootstrap.bind(0).sync().channel();
        logger.info("Netty-udp client is connect to {}:{}", conf.ip, conf.port);
	}
	
	public <T> boolean send(T msg) {
		try {
			byteBuf.get().clear();
			coder.doEncode(msg, byteBuf.get());
			return send(byteBuf.get());
		} catch (Exception e) {
			return false;
		}
	}
	
	public <T> boolean send(Collection<T> msgs) {
		try {
			byteBuf.get().clear();
			for (T msg : msgs) {
				coder.doEncode(msg, byteBuf.get());
			}
			return send(byteBuf.get());
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean send(ByteBuf byteBuf) throws InterruptedException {
		if (channel !=  null) {
			channel.writeAndFlush(new DatagramPacket(
					Unpooled.copiedBuffer(byteBuf),
	                new InetSocketAddress(conf.ip, conf.port))).sync();
			return true;
		} 
		return false;
	}
}
