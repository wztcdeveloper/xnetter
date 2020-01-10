package xnetter.sock.security;

import java.util.HashMap;

import xnetter.sock.marshal.Octets;

/**
 * 加解密算法
 * @author majikang
 * @create 2019-01-10
 */
public abstract class Security implements Cloneable {
	private static final HashMap<String, Security> map = new HashMap<String, Security>() {{
		put("NullSecurity".toUpperCase(), new NullSecurity());
		put("MD5Hash".toUpperCase(), new MD5Hash());
		put("HMAC_MD5Hash".toUpperCase(), new HMAC_MD5Hash());
		put("ARCFourSecurity".toUpperCase(), new ARCFourSecurity());
	}};

	public void setParameter(Octets o) {
	}

	public void getParameter(Octets o) {
	}

	public Octets doUpdate(Octets o) {
		return o;
	}

	public Octets doFinal(Octets o) {
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

	public static Security create(String name) {
		Security stub = map.get(name.toUpperCase());
		return stub == null ? new NullSecurity() : (Security) stub.clone();
	}
}
