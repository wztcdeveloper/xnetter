package xnetter.http.type;

public final class TLong extends TType {
	@Override
	public Class<?>[] getClasses() {
		return new Class<?>[] {Long.TYPE, Long.class};
	}
	
	@Override
	public Object valueOf(Object value) {
		if (value == null) {
			return null;
		}
		
		Class<?> clazz = value.getClass();
		if (clazz == Long.class) { 
			return (Long) value;
		}
		if (clazz.getSuperclass() == Number.class) {
			return ((Number) value).longValue();
		}
		if (clazz == Boolean.class) {
			return ((Boolean) value).booleanValue() ? 1 : 0;
		}
		if (clazz == Character.class) {
			return ((Character) value).charValue();
		}
		
		String s = ((String)value).trim();
		return s.isEmpty() ? null : Long.valueOf(s);
	}
}
