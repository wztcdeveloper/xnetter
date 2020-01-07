package xnetter.http.data.decode;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.codec.CharEncoding;

/**
 * 解析URL中的数据
 * @author majikang
 * @create 2019-11-05
 */
public class UrlDecoder extends Decoder {
	public UrlDecoder() {
		this(null);
	}
	
	public UrlDecoder(Decoder next) {
		super(next);
	}
	
	@Override
	protected void doDecode(FullHttpRequest request) {
		// TODO Auto-generated method stub
		
		QueryStringDecoder decoder = new QueryStringDecoder(request.uri(), Charset.forName(CharEncoding.UTF_8));
		for (Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
			params.put(entry.getKey(), entry.getValue().get(0));
		}
	}

}
