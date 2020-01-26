package xnetter.sock.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xnetter.Utils;
import xnetter.http.core.HttpServer;
import xnetter.sock.core.Handler;
import xnetter.sock.core.Server;
import xnetter.sock.marshal.Octets;
import xnetter.sock.security.Security;
import xnetter.utils.DumpUtil;

public class TestTcpServer extends Server {

	private static final Logger logger = LoggerFactory.getLogger(TestTcpServer.class);
	
	protected TestTcpServer(Conf conf) {
		super(conf);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onAddHandler(Handler handler) {
		// TODO Auto-generated method stub
		logger.debug("onAddHandler: " + handler);
	}

	@Override
	protected void onDelHandler(Handler handler) {
		// TODO Auto-generated method stub
		logger.debug("onDelHandler: " + handler);
	}
	
	@Override
	protected void onBeforeEncode(Object msg, ByteBuf out) {
		logger.info("debug: {}", msg);
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
		String logFile = TestTcpServer.class.getResource("/log4j.properties").getPath();
		PropertyConfigurator.configure(logFile);
		
    	Conf conf = new Conf("0.0.0.0", 1001,
				"xnetter.sock.test",
				"xnetter.sock.test");
		//conf.outSecurity = Security.create("RC4Security", "h3ss0ylltrmbwgmt6blk5pwbfm7my5");
		//conf.inSecurity = Security.create("RC4Security", "n9i5wpxar2t5g79bza99uu3a8kpnv3");
		Utils.supportSsl(conf);

		try {
    		new TestTcpServer(conf).start();
    	} catch (InterruptedException ex) {
    		ex.printStackTrace();
    	}
	}
}
