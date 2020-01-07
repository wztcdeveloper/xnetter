package xnetter.http.type;

public final class TInt extends TType {
	@Override
	public Class<?>[] getClasses() {
		return new Class<?>[] {Integer.TYPE, Integer.class};
	}
	
	@Override
	public Object valueOf(Object value) {
		if (value == null) {
			return null;
		}
		
		Class<?> clazz = value.getClass();
		if (clazz == Integer.class) { 
			return (Integer) value;
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
		return s.isEmpty() ? null : Integer.valueOf(s);
	}
}
