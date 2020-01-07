package xnetter.http.data.decode;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;

/**
 * 解析from表单数据（Content-Type = x-www-form-urlencoded）
 * @author majikang
 * @create 2019-11-05
 */
public class FormDecoder extends Decoder {
	
	public FormDecoder() {
		this(null);
	}
	
	public FormDecoder(Decoder next) {
		super(next);
	}
	
	@Override
	protected void doDecode(FullHttpRequest request) {
		// TODO Auto-generated method stub

		HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
        for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                params.put(attribute.getName(), attribute.getValue());
            }
        }
	}
}
