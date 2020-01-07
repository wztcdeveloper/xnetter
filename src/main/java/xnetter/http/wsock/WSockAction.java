package xnetter.http.wsock;

import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public interface WSockAction {

	void onConnect(WSockHandler handler);
	
	void onClose(WSockHandler handler);
	
	void onRecv(WSockHandler handler, CloseWebSocketFrame frame);
	
	void onRecv(WSockHandler handler, PingWebSocketFrame frame);
	
	void onRecv(WSockHandler handler, TextWebSocketFrame frame);
	
	void onRecv(WSockHandler handler, BinaryWebSocketFrame frame);
}
