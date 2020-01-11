package xnetter.sock.test;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ResourceLeakDetector;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xnetter.http.core.HttpServer;
import xnetter.sock.core.Dispatcher;
import xnetter.sock.core.Handler;
import xnetter.sock.protocol.Protocol;
import xnetter.sock.security.Security;
import xnetter.sock.udp.UdpClient;

public class TestUdpClient extends UdpClient {

	private static final Logger logger = LoggerFactory.getLogger(TestUdpClient.class);
	
	protected TestUdpClient(Conf conf) {
		super(conf);
	}

	@Override
	protected void onAddHandler(Handler handler) {
		// TODO Auto-generated method stub
		logger.info("onAddHandler: {}", handler);
		
		handler.send(new CChallenge(1));
	}

	@Override
	protected void onDelHandler(Handler handler) {
		// TODO Auto-generated method stub
		logger.info("onDelHandler: {}", handler);
	}
	
	@Override
	protected void onConnect(Handler handler) {
		// TODO Auto-generated method stub
		logger.info("onConnect: {}", handler);
	}

	@Override
	protected void onClose(Handler handler) {
		// TODO Auto-generated method stub
		logger.info("onClose: {}", handler);
	}
	
    @Override
    protected void onExcept(Handler handler, Throwable cause) {
    	logger.info("except from {}, reason: {}", handler.toString(), cause.getMessage());
    }
    
	@Override
	protected void onBeforeEncode(Object msg, ByteBuf out) {
		logger.debug("send: {}", msg);
	}
	
	@Override
	protected void onAfterEncode(Object msg, ByteBuf out) {
		
	}
	
	@Override
	protected void onBeforeDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		
	}
	
	@Override
	protected void onAfterDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		out.forEach(a -> logger.debug("recv: {}", a));
	}
	
	public static void main(String[] args) {
		String logFile = HttpServer.class.getResource("/log4j.properties").getPath();
		PropertyConfigurator.configure(logFile);
		
		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
		 
		for (int i=0; i<10; i++) {
			new Thread(() -> {
				startInThread();
			}).start();
		}
	}
	
	public static void startInThread() {
		Conf conf = new Conf("127.0.0.1", 1001, "xnetter.sock.test", "xnetter.sock.test");
		conf.inSecurity = Security.create("RC4Security", "h3ss0ylltrmbwgmt6blk5pwbfm7my5");
		conf.outSecurity = Security.create("RC4Security", "n9i5wpxar2t5g79bza99uu3a8kpnv3");

		TestUdpClient client = new TestUdpClient(conf);
    	
    	Dispatcher<Protocol> d = ((Dispatcher<Protocol>)client.dispatcher);
		d.regist(SChallenge.class, (msg) -> {
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			client.send(new CChallenge(msg.index + 1));
		});
		
		
		
		try {
			client.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
