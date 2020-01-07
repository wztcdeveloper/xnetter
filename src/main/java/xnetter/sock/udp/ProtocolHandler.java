package xnetter.sock.udp;

import xnetter.sock.core.Dispatcher;
import xnetter.sock.core.Manager;
import xnetter.sock.protocol.Protocol;

public class ProtocolHandler extends UdpHandler {

	public ProtocolHandler(long sid, Manager manager) {
		super(sid, manager);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onRecvMsg(Object msg) {
		// TODO Auto-generated method stub
		if (msg instanceof Protocol) {
			((Protocol)msg).setHandler(this);
			((Dispatcher<Protocol>)manager.dispatcher).dispatch((Protocol)msg);
		} else {
			logger.error("only supports class derived from {}, but recv {}", 
					Protocol.class.getName(), msg.getClass().getName());
		}
		
		// UDP是一次性通信，客户端接收到数据后就关闭连接了
		if (context != null && manager instanceof UdpClient) {
			//context.close();
		}
	}

}
