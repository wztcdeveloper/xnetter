package xnetter.http.type;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;

import xnetter.utils.ClassScaner;

/**
 * 抽象类型
 * @author majikang
 * @create 2019-11-05
 */

public abstract class TType {
	
	public abstract Class<?>[] getClasses();
	
	/**
	 * value的类型可能不是目标类型，需要进行转换
	 * @param value
	 * @return
	 */
	public abstract Object valueOf(Object value);

	private enum Holder {
		INSTANCE;
		
		private Map<Class<?>, TType> types = new HashMap<>();
		
		private Holder() {
			try {
				for (Class<?> clazz : ClassScaner.scan(TType.class.getPackage().getName(), TType.class, false)) {
					TType ttype = (TType)clazz.newInstance();
					for (Class<?> typeClazz : ttype.getClasses()) {
						types.put(typeClazz, ttype);
					}
				}
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 判断是否为基本类型
	 * @param clasz 需要进行判断的Class对象
	 * @return 是否为基本类型
	 */
	public static boolean isBaseType(Class<?> clasz) {
		return Holder.INSTANCE.types.containsKey(clasz);
	}

	/**
	 * 将value转换成目标类型
	 * @param clasz
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T valueOf(Class<T> clasz, Object value) {
		return (T)Holder.INSTANCE.types.get(clasz).valueOf(value);
	}
	
	
	// 将对象统一转为数组
	public static Object[] toArray(Object param) {
		Object[] objects = null;
		if (param.getClass().isArray()) {
			objects = (Object[])param;
		} else if (param instanceof JSONArray) {
			objects = ((JSONArray)param).toArray();
		} else if (param instanceof List) {
			objects = ((List<?>)param).toArray();
		} else if (param instanceof Set) {
			objects = ((Set<?>)param).toArray();
		} else if (param instanceof Map) {
			objects = ((Map<?,?>)param).values().toArray();
		} else {
			objects = (Object[]) Array.newInstance(param.getClass(), 1);
			Array.set(objects, 0, param);
		}
		return objects;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getBaseValue(Class<T> typeClazz, Object param) {
		Object[] objects = toArray(param);
		return (T)TType.valueOf(typeClazz, objects[0]);
	}
}
