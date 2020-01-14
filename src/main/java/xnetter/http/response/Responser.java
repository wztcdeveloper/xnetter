package xnetter.http.response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xnetter.http.annotation.Response;
import xnetter.http.type.TType;

import java.nio.charset.StandardCharsets;

/**
 * 根据Action的注解，返回不同的内容给客户端
 * 默认是JSON格式
 * @author majikang
 * @create 2019-01-15
 */
public class Responser {
    protected static Logger logger = LoggerFactory.getLogger(Responser.class);

    protected static final String CONNECTION_KEEP_ALIVE = "keep-alive";
    protected static final String CONNECTION_CLOSE = "close";

    protected final FullHttpRequest request;
    protected final ChannelHandlerContext ctx;

    public Responser(FullHttpRequest request, ChannelHandlerContext ctx) {
        this.request = request;
        this.ctx = ctx;
    }

    /**
     * 根据返回的注解输出不同的内容
     * @param result
     * @param response
     * @param forceClose
     */
    public void write(Object result, Response response, boolean forceClose) {
        String text = TType.valueOf(String.class, result);
        if (response == null) {
            writeJSON(forceClose, text);
            return;
        }

        if (response.value() == Response.Type.TEXT) {
            writeText(forceClose, text);
        } else if (response.value() == Response.Type.XML) {
            writeXML(forceClose, text);
        } else if (response.value() == Response.Type.HTML) {
            writeHTML(forceClose, text);
        } else {
            writeJSON(forceClose, text);
        }
    }

    public void writeError(Exception ex) {
        FullHttpResponse response = make("text/plain;charset=UTF-8",
                "Server error: " + ex.getMessage());
        response.setStatus(HttpResponseStatus.SERVICE_UNAVAILABLE);
        write(true, response);
    }

    /**
     * 没有找到相应的处理
     */
    public void writeNotFound() {
        FullHttpResponse response = make("text/plain;charset=UTF-8",
                String.format("Can not find specified action for name: %s", request.uri()));
        response.setStatus(HttpResponseStatus.NOT_FOUND);
        write(true, response);
    }

    /**
     * 输出纯Json字符串
     */
    public void writeJSON(boolean forceClose, String content) {
        write(forceClose, make("application/json;charset=UTF-8", content));
    }

    /**
     * 输出纯字符串
     */
    public void writeText(boolean forceClose, String content) {
        write(forceClose, make("text/plain;charset=UTF-8", content));
    }

    /**
     * 输出纯XML
     */
    public void writeXML(boolean forceClose, String content) {
        write(forceClose, make("text/xml;charset=UTF-8", content));
    }

    /**
     * 输出纯HTML
     */
    public void writeHTML(boolean forceClose, String content) {
        write(forceClose, make("text/html;charset=UTF-8", content));
    }

    private FullHttpResponse make(String contentType, String message) {
        if (message == null) {
            message = "";
        }

        ByteBuf byteBuf = Unpooled.wrappedBuffer(message.getBytes());
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK, byteBuf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());
        return response;
    }

    /**
     * response输出
     */
    public void write(boolean forceClose, FullHttpResponse response) {
        boolean close = isClose(request);
        if(!close && !forceClose){
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }

        logger.debug("send to {}: {}", ctx.channel().remoteAddress().toString(),
                response.content().toString(StandardCharsets.UTF_8));

        ChannelFuture future = ctx.writeAndFlush(response);
        if(close || forceClose){
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    protected boolean isClose(HttpRequest request) {
        if (request.headers().contains(HttpHeaderNames.CONNECTION, CONNECTION_CLOSE, true) ||
                (request.protocolVersion().equals(HttpVersion.HTTP_1_0) &&
                        !request.headers().contains(HttpHeaderNames.CONNECTION, CONNECTION_KEEP_ALIVE, true))) {
            return true;
        }
        return false;
    }
}
