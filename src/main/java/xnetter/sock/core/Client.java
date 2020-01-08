package xnetter.sock.core;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xnetter.utils.TimeUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.ScheduledFuture;

/**
 * TCP客户端的抽象类
 * @author majikang
 * @create 2019-12-05
 */
public abstract class Client extends Manager {
	protected static final Logger logger = LoggerFactory.getLogger(Client.class);
	
    private boolean hasStart;
    private boolean hasClose;
    
	private Handler handler;
	private final AtomicLong sessionId;
	private final Bootstrap bootstrap;
	private final EventLoopGroup workGroup;
	private ScheduledFuture<?> keepAliveFuture;
	
    protected Client(Conf conf) {
    	this(conf, Dispatcher.Factory.DEFAULT.create(conf.procPackageName), 
    			Coder.Factory.DEFAULT, Handler.Factory.DEFAULT);
    }
    
    public Client(Conf conf, Dispatcher<?> dispatcher, 
    		Coder.Factory coderFactory, Handler.Factory handlerFactory) {
    	super(conf, dispatcher, coderFactory, handlerFactory);
		
		this.hasStart = false;
		this.hasClose = false;
        this.bootstrap = new Bootstrap();
        this.workGroup = new NioEventLoopGroup();
        this.sessionId = new AtomicLong();
	}
    	
    public void start() {
        synchronized (this) {
            if (hasStart) {
                return;
            }
            
            hasStart = true;  
            hasClose = false;
            
            ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                	 ChannelConfig cc = ch.config();
                     cc.setOption(ChannelOption.TCP_NODELAY, conf.noDelay);
                     cc.setOption(ChannelOption.SO_KEEPALIVE, conf.keepAlive);
                     cc.setOption(ChannelOption.SO_SNDBUF, conf.socketSendBuff);
                     cc.setOption(ChannelOption.SO_RCVBUF, conf.socketRecvBuff);
                     cc.setOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535));
                     
                     Handler handler = handlerFactory.create(sessionId.incrementAndGet(), Client.this);
                     
                     ChannelPipeline pipeline = ch.pipeline();
                     pipeline.addLast("coder", coderFactory.create(Client.this, handler, conf.msgPackageName));
                     pipeline.addLast("handler", handler);
                }
        	};
        	
            bootstrap.group(workGroup).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, conf.noDelay)
                    .option(ChannelOption.SO_SNDBUF, conf.socketSendBuff)
                    .option(ChannelOption.SO_RCVBUF, conf.socketRecvBuff)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(initializer);
            
            doConnect();

            // 是否保持心跳
            if (conf.keepAlive) {
                long sendInterval = Math.max(conf.expireTime / 2, 1);
                keepAliveFuture = workGroup.scheduleWithFixedDelay(this::doKeepAlive, 
                		sendInterval, sendInterval, TimeUnit.SECONDS);
            }
        }
    }
    
    private void doConnect() {
    	synchronized (this) {
            if (!hasClose) {
		    	logger.info("do connect address={}:{}", conf.ip, conf.port);
			    bootstrap.connect(conf.ip, conf.port);
            }
    	}
    }
    
    @Override
    protected void onConnect(Handler handler) {
    	logger.info("connect to {}", handler.toString());
    	
        synchronized (this) {
            if (hasClose) {
            	handler.close();
            } else {
                if (this.handler != null) {
                	this.handler.close();
                	this.handler = null;
                }
                
                this.handler = handler;
            }
        }
    }

    @Override
    protected void onClose(Handler handler) {
    	logger.info("close from {}", handler.toString());
       
        synchronized (this) {
        	// 断开的不是原来建立连接的
            if (this.handler != null && this.handler != handler) {
                return;
            }
            
            if (this.handler != null) {
                this.handler = null;
            }

            // 是否断开重连
            if (conf.reconnrect && !hasClose) {
            	workGroup.schedule(this::doConnect, 
            			conf.reconnectInterval, TimeUnit.SECONDS);
            }
        }
    }
    
    @Override
    protected void onExcept(Handler handler, Throwable cause) {
    	logger.info("except from {}", handler.toString());
    }

    public <T> boolean send(T msg) {
        final Handler h = handler;
        try {
            return h != null && h.send(msg);
        } catch (Exception e) {
            return false;
        }
    }

    public <T> boolean send(Collection<T> msgs) {
        final Handler h = handler;
        try {
            return h != null && h.send(msgs);
        } catch (Exception e) {
            return false;
        }
    }
    
    public void close() {
        synchronized (this) {
            if (!hasStart || hasClose) {
                return;
            }

            if (keepAliveFuture != null) {
                keepAliveFuture.cancel(false);
                keepAliveFuture = null;
            }
            
            hasStart = false;
            hasClose = true;
            if (handler != null) {
            	handler.close();
            	handler = null;
            }
        }
    }
    
    private void doKeepAlive() {
        long now = TimeUtil.nowWithMilli();
        final Handler h = handler;
        
        if (h != null) {
            if (h.isExpired(now)) {
            	h.close();
            } else {
            	h.sendKeepAlive();
            }
        }
    }
}
