package xnetter.sock.core;

import java.net.InetSocketAddress;
import java.util.Collection;

import xnetter.sock.core.Manager.Conf;
import xnetter.sock.protocol.ProtocolHandler;
import xnetter.utils.TimeUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * 处理器, 每个Handler对应一个建立的连接
 * @author majikang
 * @create 2019-11-05
 */
public abstract class Handler extends ChannelDuplexHandler {
	
	public interface Factory {
		Handler create(long sid, Manager manager);

		public static final Handler.Factory DEFAULT = new Handler.Factory() {
			@Override
			public Handler create(long sid, Manager manager) {
				return new ProtocolHandler(sid, manager);
			}
		};
	}

	public final long sid;
	public final Conf conf;
	public final Manager manager;
	
	private String remoteAddress;
	private volatile long lastKeepAliveTime;
	protected volatile ChannelHandlerContext context;
	
	public Handler(long sid, Manager manager) {
		this.sid = sid;
		this.conf = manager.conf;
		this.manager = manager;
		
		this.lastKeepAliveTime = TimeUtil.nowWithMilli();
	}

	public final Conf getConf() {
		return this.conf;
	}

	public final long getSid() {
		return this.sid;
	}

	// 收到KeepAlive协议，需要调用该函数
	public final void recvKeepAlive(long time) {
		this.lastKeepAliveTime = time;
	}

	public final boolean isExpired(long now) {
		return now > lastKeepAliveTime + conf.expireTime * 1000L;
	}

	public abstract void sendKeepAlive();
	
	// 收到其他协议，会调用onRecv
	public abstract void onRecv(ChannelHandlerContext ctx, Object msg);
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.context = ctx;
		setRemoteAddress(ctx);
		
		manager.onConnect(this);
		manager.onAddHandler(this);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) {
		this.context = null;
		
		manager.onClose(this);
		manager.onDelHandler(this);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		this.context = ctx;
		setRemoteAddress(ctx);
		
		onRecv(ctx, msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) 
			throws Exception {
		this.context = null;
		
		manager.onExcept(this, cause);
	}
	
	public <T> boolean send(T msg) {
		try {
			if (context != null && context.channel().isActive()) {
				context.writeAndFlush(msg);
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public <T> boolean send(Collection<T> msgs) {
		try {
			if (context != null && context.channel().isActive()) {
				for (T msg : msgs) {
					context.write(msg);
				}
				context.flush();
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public final void close() {
		if (context != null && context.channel().isActive()) {
			context.close();
		}
	}

	public String toString() {
		return String.format("sid=%d[remote=%s]", sid, getRemoteAddress());
	}
	
	protected String getRemoteAddress() {
		if (remoteAddress != null) {
			return remoteAddress;
		} else {
			return String.format("%s:%d", manager.conf.ip, manager.conf.port);
		}
	}
	
	private void setRemoteAddress(ChannelHandlerContext ctx) {
		if (context != null) {
			InetSocketAddress isa = (InetSocketAddress) context.channel().remoteAddress();
			if (isa != null) {
				remoteAddress = String.format("%s:%d", isa.getAddress().getHostAddress(), isa.getPort());
			}
		} 
	}
	
	
}
