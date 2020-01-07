package xnetter.http.core;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * Action代理, 中间对象，具体业务由action完成
 * @author majikang
 * @create 2019-11-05
 */
public class ActionProxy {

	public final HttpRouter.Path path;
	public final String requestName;
	
	private FullHttpRequest request;
	private FullHttpResponse response;
	
	
	public ActionProxy(HttpRouter.Path path, String requestName) {
		this.path = path;
		this.requestName = requestName;
	}
	
	public Object execute(FullHttpRequest request, Object[] params) throws Exception {
		return path.method.invoke(path.action, params);
	}
	

	public void setRequest(FullHttpRequest request) {
		this.request = request;
	}
	
	public FullHttpRequest getRequest() {
		return this.request;
	}
	
	public void setResponse(FullHttpResponse response) {
		this.response = response;
	}
	
	public FullHttpResponse getResponse() {
		return this.response;
	}
}
