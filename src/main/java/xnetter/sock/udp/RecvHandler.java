package xnetter.sock.udp;

import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import xnetter.sock.core.Handler;
import xnetter.sock.core.Manager;
import xnetter.utils.ObjectPool;

/**
 * UDP是无连接的，服务器启动单线程监听端口并接收数据
 * 为了服务器处理性能，用RecvHandler来接收数据，并从对象池handlers里面获取一个Handler，
 * 扔到线程池里面去执行
 * @author majikang
 * @create 2019-12-25
 */
public class RecvHandler extends Handler {
	private static final int MIN_COUNT = 1;
	private static final int MAX_COUNT = 10000;

	private final AtomicLong sessionId;
	private final ScheduledThreadPoolExecutor executor;
	
	private ObjectPool<Handler> handlers;
	
	public RecvHandler(Manager manager) {
		super(0, manager);
		
		this.sessionId = new AtomicLong();
		this.executor = new ScheduledThreadPoolExecutor(MIN_COUNT);
		
		try {
			this.handlers = new ObjectPool<Handler>(MIN_COUNT, MAX_COUNT, () -> {
				return manager.handlerFactory.create(sessionId.incrementAndGet(), manager);
			});
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void sendKeepAlive() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 接收到数据，获取任意一个Handler，并扔到线程池里面去处理
	 * @param ctx
	 * @param msg
	 */
	@Override
	public void onRecv(ChannelHandlerContext ctx, Object msg) {
		// TODO Auto-generated method stub
		try {
			final ChannelHandlerContext context = ctx;
			Handler handler = this.handlers.get();
			if (handler != null) {
				executor.execute(() -> {
					handler.onRecv(context, msg);
					this.handlers.release(handler);
				});
			}
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
