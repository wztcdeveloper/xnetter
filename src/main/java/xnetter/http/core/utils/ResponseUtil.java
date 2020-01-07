package xnetter.http.core.utils;

import xnetter.http.core.annotation.Response;
import xnetter.http.type.TType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * 响应处理工具类
 * @author majikang
 * @create 2019-11-05
 */
public final class ResponseUtil {

	private ResponseUtil() {
		
	}
	
	private static String CONTENT_TYPE = "Content-Type";
	private static String CONTENT_LENGTH = "Conteng-Length";
	
	/**
	 * 通过对方法的Response注解， 来决定返回的类型
	 */
	// 
	public static FullHttpResponse build(Object result, Response response) {
		String text = TType.valueOf(String.class, result);
		
		if (response != null) {
			if (response.value() == Response.Type.TEXT) {
				return buildText(text);
			} else if (response.value() == Response.Type.XML) {
				return buildXML(text);
			} else if (response.value() == Response.Type.HTML) {
				return buildHTML(text);
			} 
		}
		
		return buildJSON(text);
	}
	
	/**
	 * 输出纯Json字符串
	 */
	public static FullHttpResponse buildJSON(String json){
		return build(json, "text/x-json;charset=UTF-8");
	}
	
	/**
	 * 输出纯字符串
	 */
	public static FullHttpResponse buildText(String text) {
		return build(text, "text/plain;charset=UTF-8");
	}
	
	/**
	 * 输出纯XML
	 */
	public static FullHttpResponse buildXML(String xml) {
		return build(xml, "text/xml;charset=UTF-8");
	}
	
	/**
	 * 输出纯HTML
	 */
	public static FullHttpResponse buildHTML(String html) {
		return build(html, "text/html;charset=UTF-8");
	}
	
	public static FullHttpResponse getErroResponse(Exception ex) {
		return build("Server error: " + ex.getMessage(), "text/plain;charset=UTF-8");
	}
	
	public static FullHttpResponse getNotFoundResponse() {
		return build("Can not find specified action for name", "text/plain;charset=UTF-8");
	}
	
	/**
	 * response输出
	 */
	public static FullHttpResponse build(String message, String contentType) {
		if (message == null) {
			message = "";
		}
		
		ByteBuf byteBuf = Unpooled.wrappedBuffer(message.getBytes());
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, 
				HttpResponseStatus.OK, byteBuf);
		response.headers().add(CONTENT_TYPE, contentType);
		response.headers().add(CONTENT_LENGTH, String.valueOf(byteBuf.readableBytes()));
		return response;
	}
}
