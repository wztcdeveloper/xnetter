package xnetter.sock.protocol;

import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xnetter.sock.core.Dispatcher;
import xnetter.sock.core.Handler;
import xnetter.sock.core.Manager;

/**
 * 协议处理器
 * @author majikang
 * @create 2019-12-05
 */
public class ProtocolHandler extends Handler {

	private static final Logger logger = LoggerFactory.getLogger(ProtocolHandler.class);
	
	public ProtocolHandler(long sid, Manager manager) {
		super(sid, manager);
	}

	@Override
	public void sendKeepAlive() {
		this.send(KeepAlive.InsData);
	}

	/**
	 * 接收到数据后，调用分发器来分发
	 * @param ctx
	 * @param msg 这里已经解码并序列化为协议对象了
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onRecv(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof Protocol) {
			((Protocol)msg).setHandler(this);
			((Dispatcher<Protocol>)manager.dispatcher).dispatch((Protocol)msg);
		} else {
			logger.error("only supports class derived from {}, but recv {}", 
					Protocol.class.getName(), msg.getClass().getName());
		}
	}
}
