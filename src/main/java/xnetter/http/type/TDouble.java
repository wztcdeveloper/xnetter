package xnetter.http.type;

public final class TDouble extends TType {
	@Override
	public Class<?>[] getClasses() {
		return new Class<?>[] {Double.TYPE, Double.class};
	}
	
	@Override
	public Object valueOf(Object value) {
		if (value == null) {
			return null;
		}
		
		Class<?> clazz = value.getClass();
		if (clazz == Double.class) { 
			return (Double) value;
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
		return s.isEmpty() ? null : Double.valueOf(s);
	}
}
