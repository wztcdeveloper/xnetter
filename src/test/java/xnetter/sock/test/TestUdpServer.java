package xnetter.sock.test;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ResourceLeakDetector;

import org.apache.log4j.PropertyConfigurator;

import xnetter.Utils;
import xnetter.http.core.HttpServer;
import xnetter.sock.core.Handler;
import xnetter.sock.security.Security;
import xnetter.sock.udp.UdpServer;

public class TestUdpServer extends UdpServer {

	protected TestUdpServer(Conf conf) {
		super(conf);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onAddHandler(Handler handler) {
		// TODO Auto-generated method stub
		logger.info("onAddHandler: " + handler);
	}

	@Override
	protected void onDelHandler(Handler handler) {
		// TODO Auto-generated method stub
		logger.info("onDelHandler: " + handler);
	}

	@Override
	protected void onConnect(Handler handler) {
		// TODO Auto-generated method stub
		logger.info("onConnect: " + handler);
	}

	@Override
	protected void onClose(Handler handler) {
		// TODO Auto-generated method stub
		logger.info("onClose: " + handler);
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

    	Conf conf = new Conf("127.0.0.1", 1001, "xnetter.sock.test", "xnetter.sock.test");
		conf.outSecurity = Security.create("RC4Security", "h3ss0ylltrmbwgmt6blk5pwbfm7my5");
		conf.inSecurity = Security.create("RC4Security", "n9i5wpxar2t5g79bza99uu3a8kpnv3");

		try {
    		 ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
    		new TestUdpServer(conf).start();
    	} catch (InterruptedException ex) {
    		ex.printStackTrace();
    	}
	}
}
