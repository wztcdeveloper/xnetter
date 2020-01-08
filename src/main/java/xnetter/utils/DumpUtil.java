package xnetter.utils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import xnetter.http.type.TType;

import com.alibaba.fastjson.JSONArray;

/**
 * 格式化成字符串的工具类
 * @author majikang
 * @create 2019-11-05
 */
public final class DumpUtil {

	private DumpUtil() {
		
	}
	
	public static String toString(Object[] values) {
		StringBuilder sb = new StringBuilder();
		for (Object o : values) {
			sb.append(o).append(",");
		}
		return sb.toString();
	}
	
	public static String dump(String prefix, Object[] values) {
		return dump(0, prefix, values);
	}
	
	public static String dump(String prefix, List<?> values) {
		return dump(0, prefix, values);
	}
	
	public static String dump(String prefix, Set<?> values) {
		return dump(0, prefix, values);
	}
	
	
	public static String dump(String prefix, Map<?, ?> values) {
		return dump(0, prefix, values);
	}
	
	public static String dump(String prefix, Object value) {
		return dump(0, prefix, value);
	}
	
	public static String dump(int depth, String prefix, Object[] values) {
		StringBuilder sb = new StringBuilder();
		sb.append("array(count=").append(values.length).append("):\n");
		for (int i=0; i<values.length; i++) {
			sb.append(format(depth+1, prefix))
				.append(i).append("=")
				.append(dump(depth+1, prefix, values[i])).append("\n");
		}
		return sb.toString();
	}
	
	public static String dump(int depth, String prefix, List<?> values) {
		StringBuilder sb = new StringBuilder();
		sb.append("list(count=").append(values.size()).append("):\n");
		for (int i=0, count=values.size(); i<count; i++) {
			sb.append(format(depth+1, prefix))
				.append(i).append("=")
				.append(dump(depth+1, prefix, values.get(i))).append("\n");
		}
		return sb.toString();
	}
	
	public static String dump(int depth, String prefix, Set<?> values) {
		StringBuilder sb = new StringBuilder();
		sb.append("set(count=").append(values.size()).append("):\n");
		
		int i = 0;
		for (Object value : values) {
			sb.append(format(depth+1, prefix))
				.append(i++).append("=")
				.append(dump(depth+1, prefix, value)).append("\n");
		}
		return sb.toString();
	}
	
	
	public static String dump(int depth, String prefix, Map<?, ?> values) {
		StringBuilder sb = new StringBuilder();
		sb.append("map(count=").append(values.size()).append("):\n");
		for (Entry<?, ?> entry : values.entrySet()) {
			sb.append(format(depth+1, prefix))
				.append(dump(depth+1, prefix, entry.getKey())).append("=")
				.append(dump(depth+1, prefix, entry.getValue())).append("\n");
		}
		return sb.toString();
	}
	
	public static String dump(int depth, String prefix, Object value) {
		if (value == null) {
			return "null";
		} else if (value.getClass().isArray()) {
			return dump(depth, prefix, (Object[])value);
		} else if (value instanceof JSONArray) {
			return dump(depth, prefix, ((JSONArray)value).toArray());
		} else if (value instanceof List) {
			return dump(depth, prefix, (List<?>)value);
		} else if (value instanceof Set) {
			return dump(depth, prefix, (Set<?>)value);
		} else if (value instanceof Map) {
			return dump(depth, prefix, ((Map<?,?>)value));
		} else if (TType.isBaseType(value.getClass())) {
			return String.format("%s", value.toString());
		} else {
			return dumpBean(depth, prefix, value);
		}
	}
	
	private static String dumpBean(int depth, String prefix, Object value) {
		StringBuilder sb = new StringBuilder();
		sb.append(value.getClass().getName()).append("@").append(String.format("%08X\n", value.hashCode()));
		
		for (Field f : value.getClass().getDeclaredFields()) {
			try {
				f.setAccessible(true);
				sb.append(format(depth+1, prefix)).append(f.getName())
					.append("=").append(dump(depth+1, prefix, f.get(value))).append("\n");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		return sb.toString();
	}
	
	private static String format(int depth, String prefix) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<depth; i++) {
			sb.append(prefix);
		}
		return sb.toString();
	}
}
