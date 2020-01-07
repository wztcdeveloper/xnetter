package xnetter.sock.protocol;


import xnetter.sock.core.Dispatcher;

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
