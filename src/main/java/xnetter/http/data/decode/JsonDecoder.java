package xnetter.http.data.decode;

import io.netty.handler.codec.http.FullHttpRequest;

import java.nio.charset.Charset;

import org.apache.commons.codec.CharEncoding;

import com.alibaba.fastjson.JSONObject;

/**
 * 解析json数据（Content-Type = application/json）
 * @author majikang
 * @create 2019-11-05
 */
public class JsonDecoder extends Decoder {
	
	public JsonDecoder() {
		this(null);
	}
	
	public JsonDecoder(Decoder next) {
		super(next);
	}
	
	@Override
	protected void doDecode(FullHttpRequest request) {
		// TODO Auto-generated method stub
		String content = request.content().toString(Charset.forName(CharEncoding.UTF_8));
		Object json = JSONObject.parse(content);
		
		if (json instanceof JSONObject) {
			((JSONObject)json).forEach((k, v) -> params.put(k, v));
		} else {
			params.put(Decoder.DEFAULT_KEY, json);
		}
	}
}
