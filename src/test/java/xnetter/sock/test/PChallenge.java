package xnetter.sock.test;


import xnetter.sock.core.Action;

public class PChallenge implements Action<CChallenge> {
	
	@Override
	public void process(CChallenge msg) {
		// TODO Auto-generated method stub
		msg.getHandler().send(new SChallenge(0, msg.index));
	}
}
