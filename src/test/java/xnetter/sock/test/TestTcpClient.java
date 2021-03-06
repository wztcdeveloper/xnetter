package xnetter.sock.test;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ResourceLeakDetector;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xnetter.Utils;
import xnetter.http.core.HttpServer;
import xnetter.sock.core.Client;
import xnetter.sock.core.Dispatcher;
import xnetter.sock.core.Handler;
import xnetter.sock.protocol.Protocol;
import xnetter.sock.security.Security;
import xnetter.utils.TimeUtil;

public class TestTcpClient extends Client {

	private static final Logger logger = LoggerFactory.getLogger(TestTcpClient.class);
	
	protected TestTcpClient(Conf conf) {
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
		String logFile = TestTcpClient.class.getResource("/log4j.properties").getPath();
		PropertyConfigurator.configure(logFile);
		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
		 
		for (int i=0; i<10; i++) {
			new Thread(() -> {
				startInThread();
			}).start();
		}
	}
	
	private static void startInThread() {
    	Conf conf = new Conf("127.0.0.1", 1001,
				"xnetter.sock.test",
				"xnetter.sock.test");
		Utils.supportSsl(conf);
    	TestTcpClient client = new TestTcpClient(conf);
    	Dispatcher<Protocol> d = ((Dispatcher<Protocol>)client.dispatcher);
		d.regist(SChallenge.class, (msg) -> {
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			client.send(new CChallenge(msg.index + 1));
		});
		
		client.start();
	}
}
