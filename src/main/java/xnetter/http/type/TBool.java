package xnetter.http.type;

public final class TBool extends TType {
	@Override
	public Class<?>[] getClasses() {
		return new Class<?>[] {Boolean.TYPE, Boolean.class};
	}
	
	@Override
	public Object valueOf(Object value) {
		if (value == null) {
			return false;
		}
		
		Class<?> clazz = value.getClass();
		if (clazz == Boolean.class) {
			return (Boolean) value;
		}
		if (clazz == Character.class) {
			return ((Character) value).charValue() != 0;
		}		
		if (clazz.getSuperclass() == Number.class) {
			return ((Number) value).doubleValue() != 0;
		}
		
		if (clazz == String.class) {
			String sValue = ((String) value).trim();
			return !(sValue.length() == 0
					|| sValue.equals("0")
					|| sValue.equalsIgnoreCase("false")
					|| sValue.equalsIgnoreCase("no")
					|| sValue.equalsIgnoreCase("f") 
					|| sValue.equalsIgnoreCase("n"));
		}
		
		return null;
	}
}
