package xnetter.sock.core;

import java.util.List;

import xnetter.sock.protocol.ProtocolCoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

/**
 * 编解码器, 接收到数据的解码和发送数据前的编码
 * 需要实现Factory来构建，默认提供ProtocolCoder
 * @author majikang
 * @create 2019-12-05
 */
public abstract class Coder extends ByteToMessageCodec<Object> {
	public interface Factory {
		Coder create(Manager manager, Handler handler, String packageName);

		public static final Coder.Factory DEFAULT = new Coder.Factory() {
			@Override
			public Coder create(Manager manager, Handler handler, String packageName) {
				return new ProtocolCoder(manager, handler, packageName);
			}
		};
	}

	protected final Manager manager;
	protected final Handler handler;
	
	public Coder(Manager manager, Handler handler) {
		this.manager = manager;
		this.handler = handler;
	}
	
	public void doEncode(Object msg, ByteBuf out) throws Exception {
		manager.onBeforeEncode(msg, out);
		
		this.toEncode(msg, out);
		
		manager.onAfterEncode(msg, out);
	}
	
	public void doDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		manager.onBeforeDecode(ctx, in, out);
		
		this.toDecode(ctx, in, out);
		
		manager.onAfterDecode(ctx, in, out);
	}

	/**
	 * 真正的编码实现
	 * @param msg
	 * @param out
	 * @throws Exception
	 */
	protected abstract void toEncode(Object msg, ByteBuf out) throws Exception ;

	/**
	 * 真正的解码实现
	 * @param ctx
	 * @param in
	 * @param out
	 * @throws Exception
	 */
	protected abstract void toDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;
	
	@Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		this.doEncode(msg, out);
	}
	
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		this.doDecode(ctx, in, out);
    }
}
