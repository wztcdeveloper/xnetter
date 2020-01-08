package xnetter.sock.protocol;


import xnetter.sock.core.Dispatcher;

/**
 * 对协议进行转发
 * @author majikang
 * @create 2019-12-05
 */
public class ProtocolDispatcher extends Dispatcher<Protocol> {
	public ProtocolDispatcher(String packageName) {
		super(packageName);
	}
	
	@Override
	protected void beforeDispatch(Protocol msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void afterDispatch(Protocol msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void notFoundProcessor(Protocol msg) {
		// TODO Auto-generated method stub
		 msg.getHandler().close();
	}

}
