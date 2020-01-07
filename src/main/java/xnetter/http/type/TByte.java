package xnetter.http.type;

public final class TByte extends TType {
	@Override
	public Class<?>[] getClasses() {
		return new Class<?>[] {Byte.TYPE, Byte.class};
	}
	
	@Override
	public Object valueOf(Object value) {
		if (value == null) {
			return null;
		}
		
		Class<?> clazz = value.getClass();
		if (clazz == Byte.class) { 
			return (Byte) value;
		}
		if (clazz.getSuperclass() == Number.class) {
			return ((Number) value).intValue();
		}
		if (clazz == Boolean.class) {
			return ((Boolean) value).booleanValue() ? 1 : 0;
		}
		if (clazz == Character.class) {
			return ((Character) value).charValue();
		}
		
		String s = ((String)value).trim();
		return s.isEmpty() ? null : Byte.valueOf(s);
	}
}
