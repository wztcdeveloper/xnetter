package xnetter.sock.core;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import com.alibaba.fastjson.JSON;

import xnetter.sock.marshal.Octets;

/**
 * 所有网络连接的基类
 * @author majikang
 * @create 2019-12-05
 */
public abstract class Manager {

	/**
	 * 网络参数配置
	 */
	public final Conf conf;

	/**
	 * 网络数据分发器
	 */
	public final Dispatcher<?> dispatcher;

	/**
	 * 网络数据编解码的工厂
	 * 编解码器是建立连接之后才创建，所以需要工厂
	 */
	public final Coder.Factory coderFactory;

	/**
	 * 网络数据处理器的工厂
	 * 处理器是建立连接之后才创建，所以需要工厂
	 */
	public final Handler.Factory handlerFactory;
	
	protected Manager(Conf conf, Dispatcher<?> dispatcher, 
			Coder.Factory coderFactory, Handler.Factory handlerFactory) {
		this.conf = conf;
		this.dispatcher = dispatcher;
		this.coderFactory = coderFactory;
		this.handlerFactory = handlerFactory;
	}

	/**
	 * 建立连接之后调用
	 * @param handler
	 */
	protected abstract void onConnect(Handler handler);

	/**
	 * 关闭连接后调用
	 * @param handler
	 */
	protected abstract void onClose(Handler handler);

	/**
	 * 网络异常调用，比如对端异常断开
	 * @param handler
	 */
	protected abstract void onExcept(Handler handler, Throwable cause);

	/**
	 * 建立连接之后调用，有效的连接才调用
	 * @param handler
	 */
	protected abstract void onAddHandler(Handler handler);

	/**
	 * 关闭连接之后调用
	 * @param handler
	 */
	protected abstract void onDelHandler(Handler handler);

	/**
	 * 数据编码前调用
	 * @param msg 待发送的对象
	 * @param out 等待序列化的字节buf
	 */
	protected void onBeforeEncode(Object msg, ByteBuf out) {
		
	}

	/**
	 * 数据编码后调用
	 * @param msg 待发送的对象
	 * @param out 已经序列化的字节buf
	 */
	protected void onAfterEncode(Object msg, ByteBuf out) {
		
	}

	/**
	 * 收到协议数据，解码前调用
	 * @param ctx
	 * @param in 待解码的字节buf
	 * @param out 待输出的对象，还没有填充内容
	 */
	protected void onBeforeDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		
	}

	/**
	 * 收到协议数据，解码后调用
	 * @param ctx
	 * @param in 待解码的字节buf
	 * @param out 已经输出的对象列表，已经填充内容
	 */
	protected void onAfterDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

	}

	/**
	 * 接收到不认识的对象时，调用该函数处理
	 * @param handler 当前的处理器
	 * @param type 接收的对象标识
	 * @param os 接收到字节流数据
	 * @return
	 */
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
