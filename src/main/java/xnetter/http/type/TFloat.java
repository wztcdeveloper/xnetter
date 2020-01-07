package xnetter.http.type;

public final class TFloat extends TType {
	@Override
	public Class<?>[] getClasses() {
		return new Class<?>[] {Float.TYPE, Float.class};
	}
	
	@Override
	public Object valueOf(Object value) {
		if (value == null) {
			return null;
		}
		
		Class<?> clazz = value.getClass();
		if (clazz == Float.class) { 
			return (Float) value;
		}
		if (clazz.getSuperclass() == Number.class) {
			return ((Number) value).doubleValue();
		}
		if (clazz == Boolean.class) {
			return ((Boolean) value).booleanValue() ? 1 : 0;
		}
		if (clazz == Character.class) {
			return ((Character) value).charValue();
		}
		
		String s = ((String)value).trim();
		return s.isEmpty() ? null : Float.valueOf(s);
	}
}
