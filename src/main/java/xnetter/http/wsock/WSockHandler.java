package xnetter.http.wsock;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

/**
 * 负责接收WebSocket消息，并转发给Action处理
 * 每个Handler需要有一个Action来与之对应
 * @author majikang
 * @create 2019-11-05
 */
public class WSockHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

	private static Logger logger = LoggerFactory.getLogger(WSockHandler.class);
	
	private final WSockAction action;
	private final WebSocketServerHandshaker handshaker;
	

	private ChannelHandlerContext ctx;
	
	public WSockHandler(WSockAction action, WebSocketServerHandshaker handshaker) {
		this.action = action;
		this.handshaker = handshaker;
	}
	
	private void setContext(ChannelHandlerContext ctx, boolean closeBefore) {
		if (closeBefore && this.ctx != null && this.ctx.channel().isActive()) {
			this.ctx.close();
		}
		
		this.ctx = ctx;
	}

	public ChannelHandlerContext getContext() {
		return ctx;
	}
	
	public Channel getChannel() {
		return ctx != null ? ctx.channel() : null;
	}
	
	public void sendMessage(String content) {
		if (ctx != null && ctx.channel().isActive()) {
			logger.info("read content from {}: {}", getRemoteAddr(ctx), content);
			ctx.channel().write(new TextWebSocketFrame(content));
		}
	}
	
	public void sendMessage(byte[] datas) {
		if (ctx != null && ctx.channel().isActive()) {
        	ByteBuf buf = Unpooled.copiedBuffer(datas);
        	logger.info("read content from {}: {}", getRemoteAddr(ctx),
        			buf.toString(StandardCharsets.UTF_8));
        	
			ctx.channel().write(new BinaryWebSocketFrame(buf));
		}
	}
	
	@Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.debug("active from {}", getRemoteAddr(ctx));
		setContext(ctx, true);
		action.onConnect(this);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.debug("inactive from {}", getRemoteAddr(ctx));
		action.onClose(this); 
	}
	
	@Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		logger.debug("read complete from {}", getRemoteAddr(ctx));
        ctx.flush();
    }
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame)
			throws Exception {
		// TODO Auto-generated method stub
		setContext(ctx, false);
		logger.info("read content from {}: {}", getRemoteAddr(ctx),
				frame.content().toString(StandardCharsets.UTF_8));
		
		if (frame instanceof CloseWebSocketFrame) { 
			// 收到关闭指令
			action.onRecv(this, (CloseWebSocketFrame)frame);
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
		} else if (frame instanceof PingWebSocketFrame) {
			// 收到ping消息
			action.onRecv(this, (PingWebSocketFrame)frame);
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
		} else if (frame instanceof TextWebSocketFrame) {
			action.onRecv(this, (TextWebSocketFrame)frame);
        } else if (frame instanceof BinaryWebSocketFrame) {
			action.onRecv(this, (BinaryWebSocketFrame)frame);
		}
	}
	
	private String getRemoteAddr(ChannelHandlerContext ctx) {
		return ctx.channel().remoteAddress().toString();
	}

}
