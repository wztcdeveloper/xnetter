package xnetter.http.type;

public final class TString extends TType {
	@Override
	public Class<?>[] getClasses() {
		return new Class<?>[] {String.class};
	}
	
	@Override
	public Object valueOf(Object value) {
		if (value == null) {
			return null;
		}
		
		return value.toString().trim();
	}
}
