package xnetter.sock.core;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import com.alibaba.fastjson.JSON;

import xnetter.sock.marshal.Octets;

/**
 * 所有网络连接的基类
 * @author majikang
 * @create 2019-11-05
 */
public abstract class Manager {
	
	public final Conf conf;
	public final Dispatcher<?> dispatcher;
	public final Coder.Factory coderFactory;
	public final Handler.Factory handlerFactory;
	
	protected Manager(Conf conf, Dispatcher<?> dispatcher, 
			Coder.Factory coderFactory, Handler.Factory handlerFactory) {
		this.conf = conf;
		this.dispatcher = dispatcher;
		this.coderFactory = coderFactory;
		this.handlerFactory = handlerFactory;
	}
	
	protected abstract void onConnect(Handler handler);

	protected abstract void onClose(Handler handler);

	protected abstract void onExcept(Handler handler, Throwable cause);
	
	protected abstract void onAddHandler(Handler handler);
	
	protected abstract void onDelHandler(Handler handler);
	
	
	protected void onBeforeEncode(Object msg, ByteBuf out) {
		
	}
	
	protected void onAfterEncode(Object msg, ByteBuf out) {
		
	}
	
	protected void onBeforeDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		
	}
	
	protected void onAfterDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		
	}

	public boolean onUnknownMessage(Handler handler, int type, Octets os) {
		return false;
	}
	
    public static class Conf {
		 public final String ip;
	     public final int port;
	     public final String msgPackageName;
	     public final String procPackageName;
	     
	     public boolean keepAlive = true;
	     public boolean noDelay = false;
	     public int backlog = 100;

	     public int socketSendBuff = 16 * 1024;
	     public int socketRecvBuff = 16 * 1024;
	     public int maxMsgSize = 64 * 1024;

	     public int expireTime = 60; 		// second
	     public boolean reconnrect = true;	// for client
	     public int reconnectInterval = 1; 	// second (for client)
	     
	     public Conf(int port) {
	    	this("0.0.0.0", port); 
	     }
	     
	     public Conf(String ip, int port) {
	    	 this(ip, port, "", "");
	     }
	     
	     public Conf(int port, String msgPackageName, String procPackageName) {
	    	 this("0.0.0.0", port, msgPackageName, procPackageName);
	     }
	     
	     public Conf(String ip, int port, String msgPackageName, String procPackageName) {
	    	 this.ip = ip;
	    	 this.port = port;
	    	 this.msgPackageName = msgPackageName;
	    	 this.procPackageName = procPackageName;
	     }
	     
	     public String toJsonString() {
	        return JSON.toJSONString(this);
	    }
	}
}
