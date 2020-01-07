package xnetter.http.data.decode;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * 解析request的数据
 * @author majikang
 * @create 2019-11-05
 */

public abstract class Decoder {
	
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
	
	/**
	 * 对request进行解析
	 * @param value
	 * @return Map<String, Object>
	 */
	protected abstract void doDecode(FullHttpRequest request);
	
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
	
	
	// 解析所有的request，并返回Map
	public static Map<String, Object> decode(FullHttpRequest request) {
		Decoder decoder = get(request);
		return decoder != null ? decoder.decodeRequest(request) : new HashMap<String, Object>();
	}
	
	// 根据request，返回相应的Decoder
	public static Decoder get(FullHttpRequest request) {
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
