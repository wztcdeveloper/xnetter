package xnetter.sock.core;

import java.util.List;

import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xnetter.sock.protocol.ProtocolCoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import xnetter.sock.security.Security;
import xnetter.sock.udp.UdpClient;
import xnetter.sock.udp.UdpHandler;
import xnetter.sock.udp.UdpServer;
import xnetter.utils.DumpUtil;

/**
 * 编解码器, 接收到数据的解码和发送数据前的编码
 * 需要实现Factory来构建，默认提供ProtocolCoder
 * @author majikang
 * @create 2019-12-05
 */

public abstract class Coder extends ByteToMessageCodec<Object> {

	public interface Factory {
		default Coder create(Manager manager, Handler handler, String packageName) {
			Coder coder = doCreate(manager, handler, packageName);
			if (manager.conf.inSecurity != null) {
				coder.setInSecurity(manager.conf.inSecurity.copied());
			}
			if (manager.conf.outSecurity != null) {
				coder.setOutSecurity(manager.conf.outSecurity.copied());
			}
			return coder;
		}
		Coder doCreate(Manager manager, Handler handler, String packageName);

		public static final Coder.Factory DEFAULT = new Coder.Factory() {
			@Override
			public Coder doCreate(Manager manager, Handler handler, String packageName) {
				return new ProtocolCoder(manager, handler, packageName);
			}
		};
	}

	protected static final Logger logger = LoggerFactory.getLogger(Coder.class);
	private static final ThreadLocal<ByteBuf> byteBuf
			= ThreadLocal.withInitial(() -> Unpooled.buffer(65535));

	protected final Manager manager;
	protected final Handler handler;
	private final boolean reusable;

	private Security inSecurity;
	private Security outSecurity;

	public Coder(Manager manager, Handler handler) {
		this.manager = manager;
		this.handler = handler;
		this.inSecurity = null;
		this.outSecurity = null;

		this.reusable = manager instanceof UdpClient || manager instanceof UdpServer;
	}

	public void setInSecurity(Security inSecurity) {
		this.inSecurity = inSecurity;
	}

	public void setOutSecurity(Security outSecurity) {
		this.outSecurity = outSecurity;
	}

	/**
	 * 对于无连接的，为了提供效率，会重用Coder。
	 * 但是加解密算法不能重用，所以需要拷贝inSecurity和outSecurity
	 */
	private Security getInSecurity() {
		if (inSecurity != null && reusable) {
			return inSecurity.copied();
		}
		return inSecurity;
	}

	private Security getOutSecurity() {
		if (outSecurity != null && reusable) {
			return outSecurity.copied();
		}
		return outSecurity;
	}

	/**
	 * 如果是发送数据，需要先编码到流对象，
	 * 然后再加密，最后发送到out
	 */
	public void doEncode(Object msg, ByteBuf out) throws Exception {
		manager.onBeforeEncode(msg, out);

		if (outSecurity != null) {
			ByteBuf tmp = byteBuf.get();
			tmp.clear();
			this.toEncode(msg, tmp);

			getOutSecurity().doUpdate(tmp);
			out.writeBytes(tmp);
		} else {
			this.toEncode(msg, out);
		}

		manager.onAfterEncode(msg, out);
	}

	/**
	 * 如果是收到数据，需要先解密到明文，再解码到对象
	 */
	public void doDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		manager.onBeforeDecode(ctx, in, out);

		if (inSecurity != null) {
			getInSecurity().doUpdate(in);
		}

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
