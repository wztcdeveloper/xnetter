package xnetter.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public final class ObjectPool<T> {
	public static interface Factory<T> {
		T newObject();
	}
		
	public static class DefaultFactory<T> implements Factory<T>{ 
		
		private final Object[] args;
		private final Constructor<T> constructor;
		
		public DefaultFactory(Class<T> type) 
				throws NoSuchMethodException, SecurityException {
			this(type, new Object[0]);
		}
		
		public DefaultFactory(Class<T> type, Object[] args) 
				throws NoSuchMethodException, SecurityException {
			this.args = new Object[args.length];
			Class<?>[] params = new Class<?>[args.length];
			
			for (int i=0; i<args.length; i++) {
				this.args[i] = args[i];
				params[i] = args[i].getClass();
			}
			
			this.constructor = type.getConstructor(params);
		}
		
		@Override
		public T newObject() {
			try {
				return this.constructor.newInstance(this.args);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	private final int minCount;
	private final int maxCount;
	private final Factory<T> factory;

	private List<T> usings = new LinkedList<>();
	private List<T> objects = new LinkedList<>();
	
	public ObjectPool(int minCount, int maxCount, Factory<T> factory) 
			throws InstantiationException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		this.minCount = Math.min(1, minCount);
		this.maxCount = Math.max(this.minCount, maxCount);
		this.factory = factory;

		for (int i=0; i<this.minCount; i++) {
			objects.add(this.factory.newObject());
		}
	}
	
	public synchronized T get() 
			throws InstantiationException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		T t = null;
		if (!objects.isEmpty()) {
			t = objects.remove(0);
			usings.add(t);
		} else if (usings.size() < maxCount) {
			t = factory.newObject();
			usings.add(t);
		}
		return t;
	}
	
	public synchronized void release(T t) {
		if (usings.remove(t)) {
			objects.add(t);
		}
	}
}
