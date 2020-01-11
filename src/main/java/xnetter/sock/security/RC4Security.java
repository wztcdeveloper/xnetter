
package xnetter.sock.security;

import io.netty.buffer.ByteBuf;

/**
 * RC4/ARC4/ARCFour加解密算法
 * @author majikang
 * @create 2019-01-10
 */
public final class RC4Security extends Security {
	private final static int PERM_LENGTH = 256;

	private byte[] perm = new byte[PERM_LENGTH];
	private byte index1;
	private byte index2;

	protected RC4Security() {
	}

	@Override
	public Object clone() {
		RC4Security o = (RC4Security) super.clone();
		o.perm = new byte[PERM_LENGTH];
		System.arraycopy(perm, 0, o.perm, 0, PERM_LENGTH);
		return o;
	}

	@Override
	public void setParameter(ByteBuf o) {
		int keylen = o.readableBytes();
		byte j = 0;
		for (int i = 0; i < PERM_LENGTH; i++) {
			perm[i] = (byte) i;
		}
		for (int i = 0; i < PERM_LENGTH; i++) {
			j += perm[i] + o.getByte(i % keylen);
			byte k;
			k = perm[i];
			perm[i] = perm[j & 0xff];
			perm[j & 0xff] = k;
		}
		index1 = index2 = 0;
	}

	@Override
	public ByteBuf doUpdate(ByteBuf o) {
		int len = o.readableBytes();
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
