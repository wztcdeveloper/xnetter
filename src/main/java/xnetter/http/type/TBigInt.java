package xnetter.http.type;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class TBigInt extends TType {
	@Override
	public Class<?>[] getClasses() {
		return new Class<?>[] {BigInteger.class};
	}
	
	@Override
	public Object valueOf(Object value) {
		if (value == null) {
			return null;
		}
		
		Class<?> clazz = value.getClass();
		if (clazz == BigInteger.class) {
			return (BigInteger) value;
		}
		if (clazz == BigDecimal.class) {
			return ((BigDecimal) value).toBigInteger();
		}
		if (clazz.getSuperclass() == Number.class) {
			return BigInteger.valueOf(((Number) value).longValue());
		}
		if (clazz == Boolean.class) {
			return BigInteger.valueOf(((Boolean) value).booleanValue() ? 1 : 0);
		}
		if (clazz == Character.class) {
			return BigInteger.valueOf(((Character) value).charValue());
		}
	
		String s = ((String)value).trim();
		return s.isEmpty() ? null : new BigInteger(s);
	}
}
