package xnetter.sock.test;


import xnetter.sock.core.Processor;

public class PChallenge implements Processor<CChallenge> {
	
	@Override
	public void process(CChallenge msg) {
		// TODO Auto-generated method stub
		msg.getHandler().send(new SChallenge(0, msg.index));
	}
}
