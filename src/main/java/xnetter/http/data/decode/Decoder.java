package xnetter.http.data.decode;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * 解析request的请求参数和请求内容
 * @author majikang
 * @create 2019-11-05
 */

public abstract class Decoder {

	/**
	 * HTTP请求中，有时只上传value，不上传key
	 * 这时需要解析为默认key，方便Action接收。
	 * 但是不能同时出现多个这样的情况，否则无法处理。
	 */
	public static final String DEFAULT_KEY = "key";
	
	private Decoder next;
	protected Map<String, Object> params;
	
	protected Decoder() {
		this(null);
	}
	
	protected Decoder(Decoder next) {
		this.next = next;
		this.params = new HashMap<>();
	}

	public void setNext(Decoder next) {
		this.next = next;
	}

	public Decoder getNext() {
		return this.next;
	}

	protected abstract void doDecode(FullHttpRequest request);

	/**
	 * 通过链式解析，能将所有请求参数和请求内存解析到Map
	 * @param request
	 * @return Map<String, Object>
	 */
	protected Map<String, Object> decodeRequest(FullHttpRequest request) {
		this.doDecode(request);
		
		if (this.next != null) {
			params.putAll(this.next.decodeRequest(request));
		}
		
		return params;
	}
	
	protected Map<String, Object> putAll(Map<String, Object> map) {
		this.params.putAll(map);
		return this.params;
	}

	/**
	 * 对request进行解析，返回Map, key是变量，value是变量值。
	 * value可能是基础类型，可能是容器，也可能是对象
	 * @param request
	 * @return 返回Map
	 */
	public static Map<String, Object> decode(FullHttpRequest request) {
		Decoder decoder = getDecoder(request);
		return decoder != null ? decoder.decodeRequest(request) : new HashMap<>();
	}

	/**
	 * 根据request，返回相应的Decoder
	 * @param request
	 * @return
	 */
	public static Decoder getDecoder(FullHttpRequest request) {
		Decoder decoder = null;
		if (request.method().equals(HttpMethod.GET)) {
			decoder = new UrlDecoder();
		} else if (request.method().equals(HttpMethod.POST)) {
			decoder = new UrlDecoder();

			String contentType = getContentType(request);
			if (contentType.contains("application/json")){
				decoder.setNext(new JsonDecoder());
			} else if (contentType.contains("application/x-www-form-urlencoded")) {
				decoder.setNext(new FormDecoder());
			} else if (contentType.contains("multipart/form-data")) {
				decoder.setNext(new FormDecoder());
			}
		}
		
		return decoder;
	}
	
	private static String getContentType(FullHttpRequest request) {
		String contentType = "";
		if (request.headers().contains("Content-Type")) {
			contentType = request.headers().get("Content-Type");
		} else if (request.headers().contains("Content-type")) {
			contentType = request.headers().get("Content-type");
		} else if (request.headers().contains("content-Type")) {
			contentType = request.headers().get("content-Type");
		} else if (request.headers().contains("content-type")) {
			contentType = request.headers().get("content-type");
		} else {
			contentType = "";
		}
		
		return contentType.trim().toLowerCase();
	}
}
