package xnetter.sock.security;

import xnetter.sock.marshal.Octets;

/**
 * HMAC加解密算法
 * @author majikang
 * @create 2019-01-10
 */
public final class HMAC_MD5Hash extends Security {
	private Octets k_opad = new Octets(64);
	private MD5Hash md5hash = new MD5Hash();

	protected HMAC_MD5Hash() {
	}

	@Override
	public Object clone() {
		try {
			HMAC_MD5Hash o = (HMAC_MD5Hash) super.clone();
			(o.k_opad = (Octets) k_opad.clone()).reserve(64);
			o.md5hash = (MD5Hash) md5hash.clone();
			return o;
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public void setParameter(Octets param) {
		Octets k_ipad = new Octets(64);
		int keylen = param.size();
		if (keylen > 64) {
			Octets key = MD5Hash.digest(param);
			k_ipad.replace(key.array());
			k_opad.replace(key.array());
			keylen = key.size();
		} else {
			k_ipad.replace(param.array());
			k_opad.replace(param.array());
		}

		int i = 0;
		for (; i < keylen; i++) {
			k_ipad.setByte(i, (byte) (k_ipad.getByte(i) ^ 0x36));
			k_opad.setByte(i, (byte) (k_opad.getByte(i) ^ 0x5c));
		}
		for (; i < 64; i++) {
			k_ipad.setByte(i, (byte) 0x36);
			k_opad.setByte(i, (byte) 0x5c);
		}
		k_ipad.resize(64);
		k_opad.resize(64);
		md5hash.doUpdate(k_ipad);
	}

	@Override
	public Octets doUpdate(Octets o) {
		md5hash.doUpdate(o);
		return o;
	}

	@Override
	public Octets doFinal(Octets digest) {
		md5hash.doFinal(digest);
		MD5Hash ctx = new MD5Hash();
		ctx.doUpdate(k_opad);
		ctx.doUpdate(digest);
		return ctx.doFinal(digest);
	}
}
