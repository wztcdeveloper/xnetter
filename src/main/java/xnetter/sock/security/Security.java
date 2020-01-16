package xnetter.sock.security;

import java.util.HashMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.StringUtils;

/**
 * 加解密算法
 * @author majikang
 * @create 2020-01-10
 */
public abstract class Security implements Cloneable {
	private static final HashMap<String, Security> map = new HashMap<String, Security>() {{
		put("NullSecurity".toUpperCase(), new NullSecurity());
		put("RC4Security".toUpperCase(), new RC4Security());
	}};

	public void setParameter(ByteBuf o) {
	}

	public void getParameter(ByteBuf o) {
	}

	public ByteBuf doUpdate(ByteBuf o) {
		return o;
	}

	public ByteBuf doFinal(ByteBuf o) {
		return o;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Security copied() {
		return (Security)this.clone();
	}

	public static Security create(String name) {
		return create(name, "");
	}

	public static Security create(String name, String param) {
		Security cloned = null;

		Security stub = map.get(name.toUpperCase());
		if (stub != null) {
			cloned = (Security) stub.clone();
		} else {
			cloned = new NullSecurity();
		}

		if (StringUtils.isNotEmpty(param)) {
			cloned.setParameter(Unpooled.copiedBuffer(param.getBytes()));
		}
		return cloned;
	}
}
