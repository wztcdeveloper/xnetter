package xnetter.sock.udp;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import xnetter.sock.core.Coder;
import xnetter.sock.core.Handler;
import xnetter.sock.core.Manager;

/**
 * UDP处理器, 每个Handler对应一个建立的连接
 * @author majikang
 * @create 2019-11-05
 */
public abstract class UdpHandler extends Handler {

	public static final Handler.Factory DEFAULT = new Handler.Factory() {
		@Override
		public Handler create(long sid, Manager manager) {
			return new ProtocolHandler(sid, manager);
		}
	};
	
	protected static final Logger logger = LoggerFactory.getLogger(UdpHandler.class);
	
	private Coder coder;
	private InetSocketAddress remoteAddress;
	private List<Object> outs = new ArrayList<Object>();
	private ByteBuf byteBuf = Unpooled.buffer(65535);
	
	public UdpHandler(long sid, Manager manager) {
		super(sid, manager);

		this.coder = manager.coderFactory.create(manager, this, manager.conf.msgPackageName);
	}

	@Override
	public void sendKeepAlive() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onRecv(ChannelHandlerContext ctx, Object msg) {
		// TODO Auto-generated method stub
		context = ctx;
		
		if (context != null && msg instanceof DatagramPacket) {
			try {
				DatagramPacket pack = (DatagramPacket)msg;
				remoteAddress = pack.sender();
				
				outs.clear();
				coder.doDecode(context, pack.content(), outs);
				pack.release(); // 需要主动释放，否则会内存泄漏
				
				outs.forEach(a -> onRecvMsg(a));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("recv message", e);
			}
		}
	}

	// 收到其他协议，会调用onRecv
	protected abstract void onRecvMsg(Object msg);
	
	@Override
	public <T> boolean send(T msg) {
		try {
			if (context != null) {
				byteBuf.clear();
				coder.doEncode(msg, byteBuf);
				return send(byteBuf);
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public <T> boolean send(Collection<T> msgs) {
		try {
			if (context != null) {
				byteBuf.clear();
				for (T msg : msgs) {
					coder.doEncode(msg, byteBuf);
				}
				return send(byteBuf);
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean send(ByteBuf byteBuf) throws InterruptedException {
		if (context != null) {
			context.channel().writeAndFlush(new DatagramPacket(
					Unpooled.copiedBuffer(byteBuf), getRemoteSocketAddr()));
			return true;
		}
		return false;
	}
	
	
	protected InetSocketAddress getRemoteSocketAddr() {
		if (remoteAddress == null) {
			remoteAddress = new InetSocketAddress(manager.conf.ip, manager.conf.port);
		}
		
		return remoteAddress;
	}
}
