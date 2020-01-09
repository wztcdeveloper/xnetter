package xnetter.http.test;

import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import xnetter.http.annotation.Action;
import xnetter.http.annotation.PathVariable;
import xnetter.http.annotation.Request;
import xnetter.http.annotation.Response;
import xnetter.http.wsock.WSockAction;
import xnetter.http.wsock.WSockHandler;

@Action(name="/live")
public class UserSockAction implements WSockAction {

	@Request(name="/init", type=Request.Type.GET)
	public void init(String token) {
		System.out.println(token);
	}
	
	@Override
	public void onConnect(WSockHandler handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClose(WSockHandler handler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRecv(WSockHandler handler, CloseWebSocketFrame frame) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRecv(WSockHandler handler, PingWebSocketFrame frame) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRecv(WSockHandler handler, TextWebSocketFrame frame) {
		// TODO Auto-generated method stub
		handler.sendMessage("{result:onReadText}");
	}

	@Override
	public void onRecv(WSockHandler handler, BinaryWebSocketFrame frame) {
		// TODO Auto-generated method stub
		handler.sendMessage("{result:onReadBinary}".getBytes());
	}
	

	@Request(name="/list/{unikey}", type=Request.Type.POST)
	public @Response(Response.Type.XML) Object list(@PathVariable(name="unikey") String unikey) {
		return "{result:success}";
	}
	
}
