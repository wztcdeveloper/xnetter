package xnetter.http.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 对HTTP请求的处理工具
 * @author majikang
 * @create 2019-11-05
 */
public final class RequestUtil {
	private RequestUtil() {
		
	}
	
	private static final char PATH_SEPARATOR = '/';

	/**
	 * 请求的URL和响应的路径是否匹配
	 * @param names 响应的路径，已经做了分割处理
	 * @param urls 请求的URL，已经做了分割处理
	 * @return 匹配则返回true
	 */
	public static boolean match(String[] names, String[] urls) {
		if (names == null || urls == null
				|| names.length != urls.length) {
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
	 * 请求的URL和响应的路径是否一致
	 * 响应路径是"/list/{unikey}"，则请求URL格式要求是"/list/12WE87UE"
	 * 响应路径是"/login/{type:m|n}"，则请求URL只能是"/login/m"或"/login/n"
	 * @param name 响应的路径，分割后的一部分
	 * @param url 请求的URL，分割后的一部分
	 * @return 匹配则返回true
	 */
	public static boolean match(String name, String url) {
		if (name == null || url == null) {
			return false;
		}

		name = correctPath(name);
		url = correctPath(url);
		
		if (name.startsWith("{") && name.endsWith("}")) {
			name = name.substring(1, name.length() - 1);

			 // /list/{unikey}，需要将url直接替换到name进行访问
			int index = name.indexOf(":");
			if (index < 0) {
				return true;
			}
			
			name = name.substring(index + 1);
			// /login/{type:m|n}，url只要是m或n中的任意一个，都认为是匹配的
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
	 * 响应的路径与响应函数的参数变量是否匹配
	 * 响应路径是"/list/{unikey}"，则响应函数中需要声明变量unikey来接收参数
	 * 响应路径是"/login/{type:m|n}"，则响应函数中声明type来接受参数
	 * @param name 响应的路径，分割后的一部分
	 * @param var 响应函数的参数变量
	 * @return 匹配则返回true
	 */
	public static boolean matchVar(String name, String var) {
		name = correctPath(name);
		var = correctPath(var);
		
		if (name.startsWith("{") && name.endsWith("}")) {
			name = name.substring(1, name.length() - 1);

			// /list/{unikey}，需要将url直接替换到name进行访问
			int index = name.indexOf(":");
			if (index >= 0) {
				name = name.substring(0, index);
			}
			
			return name.trim().equals(var);
		} else {
			return name.equals(var);
		}
	}

	/**
	 * 响应的路径或请求URL里面，可能存在重复的分割符，需要去掉
	 * @param name 响应的路径或请求URL
	 * @return 处理后的路径
	 */
	public static String correctPath(String name) {
		if (StringUtils.isNotEmpty(name)) {
			int i = 0; 
			int j = name.length() - 1;		
			char[] values = name.toCharArray();
			
			while (i < j && (values[i] == PATH_SEPARATOR || values[j] == PATH_SEPARATOR)) {
				if (values[i] == PATH_SEPARATOR) {
					i++;
				}
				if (values[j] == PATH_SEPARATOR) {
					j--;
				}
			}
			
			StringBuilder sb = new StringBuilder();
			while (i <= j) {
				sb.append(values[i++]);
			}
			name = sb.toString();
		}
		
		return name;
	}

	/**
	 * 有些请求URL里面，可能包含多个分隔符，这里需要过滤掉
	 * @param name 请求URL
	 * @return 处理后的URL
	 */
	public static String currectSeparator(String name) {
		if (StringUtils.isNotEmpty(name)) {
			char prev = 0;
			StringBuilder sb = new StringBuilder();

			for (char value : name.toCharArray()) {
				if (value != PATH_SEPARATOR || prev != PATH_SEPARATOR) {
					sb.append(value);
				}
				prev = value;
			}

			name = sb.toString();
		}

		return name;
	}
}
