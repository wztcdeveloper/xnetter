
package xnetter.sock.security;

import xnetter.sock.marshal.Octets;

/**
 * ARCFour加解密算法
 * @author majikang
 * @create 2019-01-10
 */
public final class ARCFourSecurity extends Security {
	private byte[] perm = new byte[256];
	private byte index1;
	private byte index2;

	protected ARCFourSecurity() {
	}

	@Override
	public Object clone() {
		try {
			ARCFourSecurity o = (ARCFourSecurity) super.clone();
			o.perm = new byte[256];
			System.arraycopy(perm, 0, o.perm, 0, 256);
			return o;
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public void setParameter(Octets o) {
		int keylen = o.size();
		byte j = 0;
		for (int i = 0; i < 256; i++) {
			perm[i] = (byte) i;
		}
		for (int i = 0; i < 256; i++) {
			j += perm[i] + o.getByte(i % keylen);
			byte k;
			k = perm[i];
			perm[i] = perm[j & 0xff];
			perm[j & 0xff] = k;
		}
		index1 = index2 = 0;
	}

	@Override
	public Octets doUpdate(Octets o) {
		int len = o.size();
		for (int i = 0; i < len; i++) {
			index2 += perm[(++index1) & 0xff];
			byte k = perm[index1 & 0xff];
			perm[index1 & 0xff] = perm[index2 & 0xff];
			perm[index2 & 0xff] = k;
			byte j = (byte) (perm[index1 & 0xff] + perm[index2 & 0xff]);
			o.setByte(i, (byte) (o.getByte(i) ^ perm[j & 0xff]));
		}
		return o;
	}

}
