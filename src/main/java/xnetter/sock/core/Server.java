package xnetter.sock.core;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xnetter.utils.TimeUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.ScheduledFuture;

/**
 * TCP服务端的抽象类
 * @author majikang
 * @create 2019-12-05
 */
public abstract class Server extends Manager {

	protected static final Logger logger = LoggerFactory.getLogger(Server.class);
	
	private final AtomicLong sessionId;
    private Channel serverChannel;
    private ScheduledFuture<?> keepAliveFuture;
    private Map<Long, Handler> handlers = new ConcurrentHashMap<>();
    
    protected Server(Conf conf) {
    	this(conf, Dispatcher.Factory.DEFAULT.create(conf.actionPackageName),
    			Coder.Factory.DEFAULT, Handler.Factory.DEFAULT);
    }
    
    public Server(Conf conf, Dispatcher<?> dispatcher, 
    		Coder.Factory coderFactory, Handler.Factory handlerFactory) {
    	super(conf, dispatcher, coderFactory, handlerFactory);
    	
        this.sessionId = new AtomicLong();
    }

    public void start() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();
        
    
    	ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
            	 ChannelConfig cc = ch.config();
                 cc.setOption(ChannelOption.TCP_NODELAY, conf.tcpNoDelay);
                 cc.setOption(ChannelOption.SO_KEEPALIVE, conf.keepAlive);
                 cc.setOption(ChannelOption.SO_SNDBUF, conf.socketSendBuff);
                 cc.setOption(ChannelOption.SO_RCVBUF, conf.socketRecvBuff);
                 cc.setOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535));
                 
                 Handler handler = handlerFactory.create(sessionId.incrementAndGet(), Server.this);
                 
                 ChannelPipeline pipeline = ch.pipeline();
                 pipeline.addLast("coder", coderFactory.create(Server.this, handler, conf.msgPackageName));
                 pipeline.addLast("handler", handler);
            }
    	};
    	
    	bootstrap.group(boss, work)
        	.channel(NioServerSocketChannel.class)
        	.option(ChannelOption.SO_BACKLOG, conf.backlog)
        	.childHandler(initializer); 
    	
        // 服务器绑定端口监听
    	serverChannel = bootstrap.bind(conf.ip, conf.port).sync().channel();
        logger.info("Netty-tcp server is listening on {}:{}", conf.ip, conf.port);

        // 是否保持心跳
        if (conf.keepAlive) {
            long sendInterval = Math.max(conf.expireTime / 2, 1);
            keepAliveFuture = work.scheduleWithFixedDelay(this::doKeepAlive, 
            		sendInterval, sendInterval, TimeUnit.SECONDS);
        }
    }
    
    public void close() {
    	if (serverChannel != null) {
            serverChannel.close();
            serverChannel = null;
    	}
    	
        if (keepAliveFuture != null) {
            keepAliveFuture.cancel(false);
            keepAliveFuture = null;
        }
    }
   
	@Override
	protected void onConnect(Handler handler) {
		// TODO Auto-generated method stub
		logger.info("connect from {}", handler.toString());
		
		handlers.put(handler.getSid(), handler);
	}

	@Override
	protected void onClose(Handler handler) {
		// TODO Auto-generated method stub
		logger.info("close from {}", handler.toString());
		
		handlers.remove(handler.getSid());
	}
	
    @Override
    protected void onExcept(Handler handler, Throwable cause) {
    	logger.info("except from {}, reason: {}", handler.toString(), cause.getMessage());
    }
    
	public <T> boolean send(long sid, T msg) {
    	Handler h = handlers.get(sid);
        return h != null && h.send(msg);
	}
	
	public <T> boolean send(long sid, Collection<T> msgs) {
    	Handler h = handlers.get(sid);
        return h != null && h.send(msgs);
	}
	
	public <T> void broadcast(T msg) {
		handlers.values().forEach(s -> s.send(msg));
	}
	
	public <T> void broadcast(Collection<T> msgs) {
		handlers.values().forEach(s -> s.send(msgs));
	}
	
    private void doKeepAlive() {
        long now = TimeUtil.nowWithMilli();
        for (Handler handler : handlers.values()) {
            if (handler.isExpired(now)) {
            	handler.close();
            } else {
            	handler.sendKeepAlive();
            }
        }
    }
}
