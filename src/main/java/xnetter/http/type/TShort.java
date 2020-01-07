package xnetter.http.type;

public final class TShort extends TType {
	@Override
	public Class<?>[] getClasses() {
		return new Class<?>[] {Short.TYPE, Short.class};
	}
	
	@Override
	public Object valueOf(Object value) {
		if (value == null) {
			return null;
		}
		
		Class<?> clazz = value.getClass();
		if (clazz == Short.class) { 
			return (Short) value;
		}
		if (clazz.getSuperclass() == Number.class) {
			return ((Number) value).shortValue();
		}
		if (clazz == Boolean.class) {
			return ((Boolean) value).booleanValue() ? 1 : 0;
		}
		if (clazz == Character.class) {
			return ((Character) value).charValue();
		}
		
		String s = ((String)value).trim();
		return s.isEmpty() ? null : Short.valueOf(s);
	}
}
