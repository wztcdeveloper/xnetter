package xnetter.sock.security;

import java.security.MessageDigest;

import xnetter.sock.marshal.Octets;

/**
 * MD5加解密算法
 * @author majikang
 * @create 2019-01-10
 */
public final class MD5Hash extends Security {
	private MessageDigest md5 = null;

	protected MD5Hash() {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object clone() {
		try {
			MD5Hash o = (MD5Hash) super.clone();
			o.md5 = (MessageDigest) md5.clone();
			return o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Octets doUpdate(Octets o) {
		if (md5 != null)
			md5.update(o.array(), 0, o.size());
		return o;
	}

	@Override
	public Octets doFinal(Octets digest) {
		return md5 != null ? new Octets(md5.digest()) : digest;
	}

	public static Octets digest(Octets o) {
		try {
			return new Octets(MessageDigest.getInstance("MD5").digest(o.array()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Octets();
	}
}
