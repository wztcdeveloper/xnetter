package xnetter.http.core;

import java.nio.charset.StandardCharsets;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import xnetter.http.annotation.Request;
import xnetter.http.annotation.Response;
import xnetter.http.core.HttpRouter.ActionContext;
import xnetter.http.core.HttpRouter.ActionHolder;
import xnetter.http.utils.HttpHeader;
import xnetter.http.utils.ResponseUtil;
import xnetter.http.data.decode.Decoder;
import xnetter.http.data.encode.Encoder;
import xnetter.http.wsock.WSockAction;
import xnetter.http.wsock.WSockHandler;
import xnetter.utils.DumpUtil;
/**
 * 将所有HTTP请求分发给相应的Action
 * @author majikang
 * @create 2019-11-05
 */

public final class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private static Logger logger = LoggerFactory.getLogger(HttpHandler.class);

	private static final String CONNECTION_KEEP_ALIVE = "keep-alive";
    private static final String CONNECTION_CLOSE = "close";

    private final HttpConf conf;
	private final HttpRouter router;

	public HttpHandler(HttpConf conf, HttpRouter router) {
		this.conf = conf;
		this.router = router;
	}

	@Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception{
		logger.debug("recv from {}: {}", ctx.channel().remoteAddress().toString(), request.uri());
		logger.debug("recv content: \n{}", request.content().toString(StandardCharsets.UTF_8));

		try {
			if ("websocket".equalsIgnoreCase(request.headers().get("Upgrade"))) {
				handleWebsocket(ctx, request);
			} else {
				handleHttp(ctx, request);
			}
		} catch (Exception ex) {
			writeResponse(request, ResponseUtil.buildError(ex), ctx.channel(), true);
		}
	}
		
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

	/**
	 * 如果是HTTP请求，则开始分发给Action
	 * @param ctx
	 * @param request
	 * @throws Exception
	 */
	private void handleHttp(ChannelHandlerContext ctx, FullHttpRequest request)
			throws Exception ,OutOfMemoryError{
		Request.Type requestType = getType(request.method());
		ActionContext context = router.newAction(request.uri(), requestType);

		if (context == null) {
			writeResponse(request, ResponseUtil.buildNotFound(request.uri()), ctx.channel(), true);
			return;
		}

		Object[] params = new Encoder(request, context.path)
			.encode(context.requestName, Decoder.decode(request));
		logger.debug("params(count={})", params.length);
		logger.debug(DumpUtil.dump("\t", params));
		
		Object result = context.execute(params);
		Response response = context.path.getMethodAnn(Response.class);
		writeResponse(request, ResponseUtil.build(result, response), ctx.channel(), false);
	}

	/**
	 * 如果是Websocket请求，则启动握手流程
	 * @param ctx
	 * @param request
	 * @throws Exception
	 */
	private void handleWebsocket(ChannelHandlerContext ctx, FullHttpRequest request)
			throws Exception {
		ActionHolder holder = router.getAction(request.uri());
		if (holder == null || !(holder.action instanceof WSockAction)) {
			throw new RuntimeException(String.format("wsock action doesn't exist for name: %s", request.uri()));
		}
		
		String wsUrl = getWebSocketUrl(holder);
		WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(wsUrl, null, false);
		WebSocketServerHandshaker handshaker = factory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            return;
        } 
        
    	WSockAction action = (WSockAction)holder.action.getClass().newInstance();
    	Request.Type requestType = getType(request.method());
		ActionContext context = router.newAction(request.uri(), requestType, false);
		if (context != null && context.path != null) {
			Object[] params = new Encoder(request, context.path)
				.encode(context.requestName, Decoder.decode(request));
    		logger.debug("params(count={})", params.length);
    		logger.debug(DumpUtil.dump("\t", params));
			context.execute(params);
		}

		/**
		 * Websocket握手成功后，不需要HttpHandler处理
		 * 所以移除HttpHandler，加入WSockHandler处理
		 */
    	ctx.pipeline().remove(this.getClass());
    	ctx.pipeline().addLast(new WSockHandler(action, handshaker));
        handshaker.handshake(ctx.channel(), request);
	}

	private void writeResponse(HttpRequest request, FullHttpResponse response, Channel channel, boolean forceClose) {
		boolean close = isClose(request);
		if(!close && !forceClose){
			response.headers().set(HttpHeader.CONTENT_LENGTH, response.content().readableBytes());
		}
		
		logger.debug("send to {}: {}", channel.remoteAddress().toString(), 
				response.content().toString(StandardCharsets.UTF_8));
		
		ChannelFuture future = channel.writeAndFlush(response);
		if(close || forceClose){
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	private boolean isClose(HttpRequest request) {
		if (request.headers().contains(HttpHeader.CONNECTION, CONNECTION_CLOSE, true) ||
			(request.protocolVersion().equals(HttpVersion.HTTP_1_0) &&
			!request.headers().contains(HttpHeader.CONNECTION, CONNECTION_KEEP_ALIVE, true))) {
			return true;
		}
		return false;
	}
	
	private Request.Type getType(HttpMethod method) {
		if (method.name().equals(HttpMethod.GET.name())) {
			return Request.Type.GET;
		} else if (method.name().equals(HttpMethod.POST.name())) {
			return Request.Type.POST;
		} else if (method.name().equals(HttpMethod.PUT.name())) {
			return Request.Type.PUT;
		} else if (method.name().equals(HttpMethod.DELETE.name())) {
			return Request.Type.DELETE;
		} else if (method.name().equals(HttpMethod.HEAD.name())) {
			return Request.Type.HEAD;
		} else if (method.name().equals(HttpMethod.PATCH.name())) {
			return Request.Type.PATCH;
		} else if (method.name().equals(HttpMethod.OPTIONS.name())) {
			return Request.Type.OPTIONS;
		} else if (method.name().equals(HttpMethod.TRACE.name())) {
			return Request.Type.TRACE;
		} else {
			return Request.Type.POST;
		}
	}

	private String getWebSocketUrl(ActionHolder holder) {
		if (conf.sslEnabled) {
			return String.format("wss://0.0.0.0:%d/%s", conf.port, holder.name);
		} else {
			return String.format("ws://0.0.0.0:%d/%s", conf.port, holder.name);
		}
	}
}