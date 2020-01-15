package xnetter.http.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import xnetter.http.annotation.Request;
import xnetter.http.annotation.Response;
import xnetter.http.core.HttpRouter.ActionContext;
import xnetter.http.core.HttpRouter.ActionHolder;
import xnetter.http.response.FileResponser;
import xnetter.http.response.Responser;
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
				String actionName = router.getActionName(request.uri());
				if (conf.isStaticDir(actionName)) {
					handleDownload(ctx, request, actionName);
				} else {
					handleHttp(ctx, request);
				}
			}
		} catch (Exception ex) {
			new Responser(request, ctx).writeError(ex);
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
			new Responser(request, ctx).writeNotFound();
			return;
		}

		Object[] params = new Encoder(request, context.path)
			.encode(context.requestName, Decoder.decode(request));
		logger.debug("params(count={})", params.length);
		logger.debug(DumpUtil.dump("\t", params));
		
		Object result = context.execute(params);
		if (File.class.isAssignableFrom(result.getClass())) {
			new FileResponser(request, ctx).write((File)result);
		} else {
			Response response = context.path.getMethodAnn(Response.class);
			new Responser(request, ctx).write(result, response, false);
		}
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

	/**
	 * 下载文件，不存在则抛出异常
	 * @param ctx
	 * @param request
	 * @throws Exception
	 */
	private void handleDownload(ChannelHandlerContext ctx, FullHttpRequest request, String rootPath)
			throws IOException {
		String filePath = new File("").getCanonicalPath();
		String fileName = String.format("%s\\%s", filePath, request.uri());

		File file = new File(fileName);
		if (file.exists() && file.isFile()) {
			logger.info("download file starting: {}", fileName);
			new FileResponser(request, ctx, conf, rootPath).write(file);
		} else {
			throw new FileNotFoundException(String.format("File not found: %s", fileName));
		}
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