package xnetter.http.type;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class TBigDec extends TType {
	@Override
	public Class<?>[] getClasses() {
		return new Class<?>[] {BigDecimal.class};
	}
	
	@Override
	public Object valueOf(Object value) {
		if (value == null) {
			return null;
		}
		
		Class<?> clazz = value.getClass();
		if (clazz == BigDecimal.class) {
			return (BigDecimal) value;
		}
		if (clazz == BigInteger.class) {
			return new BigDecimal((BigInteger) value);
		}
		if (clazz.getSuperclass() == Number.class) {
			return new BigDecimal(((Number) value).doubleValue());
		}
		if (clazz == Boolean.class) {
			return BigDecimal.valueOf(((Boolean) value).booleanValue() ? 1 : 0);
		}
		if (clazz == Character.class) {
			return BigDecimal.valueOf(((Character) value).charValue());
		}
	
		String s = ((String)value).trim();
		return s.isEmpty() ? null : new BigDecimal(s);
	}
}
