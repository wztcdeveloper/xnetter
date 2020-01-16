package xnetter.http.wsock;

import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * 处理Websocket消息的Action，需要继承WSockAction
 * @author majikang
 * @create 2019-11-05
 */
public interface WSockAction {

	/**
	 * 建立连接时调用
	 * @param handler
	 */
	void onConnect(WSockHandler handler);

	/**
	 * 关闭连接时调用
	 * @param handler
	 */
	void onClose(WSockHandler handler);

	/**
	 * 客户端关闭连接时调用
	 * @param handler
	 * @param frame
	 */
	void onRecv(WSockHandler handler, CloseWebSocketFrame frame);

	/**
	 * 接收到Ping消息时调用，回自动回复Pong的消息
	 * @param handler
	 * @param frame
	 */
	void onRecv(WSockHandler handler, PingWebSocketFrame frame);

	/**
	 * 接收到Text消息时调用
	 * @param handler
	 * @param frame
	 */
	void onRecv(WSockHandler handler, TextWebSocketFrame frame);

	/**
	 * 接收到二进制数据时调用
	 * @param handler
	 * @param frame
	 */
	void onRecv(WSockHandler handler, BinaryWebSocketFrame frame);
}
