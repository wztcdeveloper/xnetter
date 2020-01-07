package xnetter.http.core.utils;

import org.apache.commons.lang3.StringUtils;

public final class RequestUtil {
	private RequestUtil() {
		
	}
	
	public static final char PATH_SEPARATOR = '/';
	
	/**
	 * 请求的URL和响应的路径是否一致
	 * @param names 响应的路径
	 * @param urls 请求的URL
	 * @return
	 */
	public static boolean match(String[] names, String[] urls) {
		if (names.length != urls.length) {
			return false;
		}
		
		boolean match = true;
		for (int i=0; i<names.length; i++) {
			String name = names[i].trim();
			String url = urls[i].trim();
			if (!match(name, url)) {
				match = false;
				break;
			}
		}
		return match;
	}
	
	/**
	 * For Example
	 * "/list/{unikey}"
	 * "/login/{type:finger|account}"
	 */
	public static boolean match(String name, String url) {
		name = correctPath(name);
		url = correctPath(url);
		
		if (name.startsWith("{") && name.endsWith("}")) {
			name = name.substring(1, name.length() - 1);
			
			// /list/{unikey}
			// 需要将url直接替换到name进行访问
			int index = name.indexOf(":");
			if (index < 0) {
				return true;
			}
			
			name = name.substring(index + 1);
			// /login/{type:finger|account}
			for (String branch : StringUtils.split(name, "\\|")) {
				if (url.equals(branch.trim())) {
					return true;
				}
			}
			
			return false;
		} else {
			return name.equals(url);
		}
	}
	
	/**
	 * For Example
	 * "/list/{unikey}"
	 * "/login/{type:finger|account}"
	 */
	public static boolean matchVar(String name, String var) {
		name = correctPath(name);
		var = correctPath(var);
		
		if (name.startsWith("{") && name.endsWith("}")) {
			name = name.substring(1, name.length() - 1);
			
			// /list/{unikey}
			// 需要将url直接替换到name进行访问
			int index = name.indexOf(":");
			if (index >= 0) {
				name = name.substring(0, index);
			}
			
			return name.trim().equals(var);
		} else {
			return name.equals(var);
		}
	}
	
	
	// 把路径中的分隔符去掉
	public static String correctPath(String name) {
		if (!StringUtils.isEmpty(name)) {
			int i = 0; 
			int j = name.length() - 1;		
			char[] values = name.toCharArray();
			
			while (i < j && (values[i] == PATH_SEPARATOR || values[j] == PATH_SEPARATOR)) {
				if (values[i] == PATH_SEPARATOR) i++;
				if (values[j] == PATH_SEPARATOR) j--;
			}
			
			StringBuilder sb = new StringBuilder();
			while (i <= j) {
				sb.append(values[i++]);
			}
			name = sb.toString();
		}
		
		return name;
	}
}
