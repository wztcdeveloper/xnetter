package xnetter.http.core;

import io.netty.util.internal.StringUtil;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xnetter.http.core.annotation.Action;
import xnetter.http.core.annotation.Request;
import xnetter.http.core.utils.RequestUtil;
import xnetter.utils.ClassScaner;
import xnetter.utils.DumpUtil;
import xnetter.utils.ReflectUtil;

/**
 * 路由上下文，包含所有的Action及其Request的响应处理
 * @author majikang
 * @create 2019-11-05
 */

public final class HttpRouter {
	private static Logger logger = LoggerFactory.getLogger(HttpRouter.class);
	
	public static final String PATH_SEPARATOR = "/";

	/**
	 * HTTP/HTTPS请求的响应路径
	 */
	public class Path {
		// 路径的完整URL
		public final String url;
		// 路径的URL进行分割的数组，为了跟请求的URL进行比对
		public final String[] names;
		
		public final Request request;
		public final Method method;
		public final Object action;
		public final Map<Class<?>, Annotation> methodAnns;
		
		// 参数的变量名
		public final String[] paramNames;
		// 参数数组
		public final Parameter[] parameters;
		// 参数的注解数组
		public final Annotation[][] paramAnns;
		
		public Path(String url, Request request, Method method, Object action) 
				throws IOException {
			this.url = url;
			this.names = StringUtils.split(url, PATH_SEPARATOR);
			
			this.request = request;
			this.method = method;
			this.action = action;
			
			this.methodAnns = new HashMap<>();
			for (Annotation ann : method.getAnnotations()) {
				this.methodAnns.put(ann.getClass(), ann);
			}
			
			this.parameters = method.getParameters();
			this.paramAnns = method.getParameterAnnotations();
			this.paramNames = ReflectUtil.getMethodParamNames(method);
		}
		
		@SuppressWarnings("unchecked")
		public <T> T getMethodAnn(Class<T> clazz) {
			if (methodAnns.containsKey(clazz)) {
				return (T) methodAnns.get(clazz);
			}
			return null;
		}

		public boolean matchType(Request.Type type) {
			for (Request.Type t : request.type()) {
				if (t == type) {
					return true;
				}
			}
			return false;
		}
	}
	
	public class Context {
		public final String name;
		public final Object action;
		public final Map<String, Path> paths;
		
		public Context(String name, Object action) {
			this.name = name;
			this.action = action;
			this.paths = new HashMap<>();
		}
	}
	
	private Map<String, Context> contexts = new HashMap<>();
	
	public HttpRouter(String... actionPackages) throws ClassNotFoundException, IOException, 
		InstantiationException, IllegalAccessException {
		
		logger.info("===== regist action begin =====");
		
		for (String packname : actionPackages) {
			logger.info("===== regist action scan package " + packname + " =====");
			
			for (Class<?> clazz : ClassScaner.scan(packname, true)) {
				regist(clazz);
			}
		}
		
		logger.info("===== regist action end =====");
	}

	public Context getContext(String url) {
		int index = url.indexOf("?");
		if (index > 0) {
			url = url.substring(0, index);
		}
		
		url = RequestUtil.correctPath(RequestUtil.currectSeparator(url));
		index = url.indexOf(PATH_SEPARATOR);
		if (index <= 0) {
			throw new RuntimeException(String.format("request url is wrong. url=%s", url));
		}
		
		String actionName = RequestUtil.correctPath(url.substring(0, index));
		if (!contexts.containsKey(actionName)) {
			throw new RuntimeException(String.format("action doesn't exist for name: %s", actionName));
		}
		
		return contexts.get(actionName);
	}
	
	public ActionProxy newAction(String url, Request.Type type) {
		return newAction(url, type, true);
	}
	
	/**
	 * @param url WEB请求的路径
	 * @param type WEB请求的方式
	 * @param forcePath 是否强制检查路径
	 * @return
	 */
	public ActionProxy newAction(String url, Request.Type type, boolean forcePath) {
		int index = url.indexOf("?");
		if (index > 0) {
			url = url.substring(0, index);
		}
		
		url = RequestUtil.correctPath(RequestUtil.currectSeparator(url));
		index = url.indexOf(PATH_SEPARATOR);
		if (index <= 0) {
			throw new RuntimeException(String.format("request url is wrong. url=%s, should like: user/login...", url));
		}
		
		String actionName = RequestUtil.correctPath(url.substring(0, index));
		String requestName = RequestUtil.correctPath(url.substring(index + 1));
		if (StringUtils.isEmpty(actionName) || StringUtils.isEmpty(requestName)) {
			throw new RuntimeException(String.format("request url is wrong. url=%s, should like: user/login...", url));
		}
		
		if (!contexts.containsKey(actionName)) {
			throw new RuntimeException(String.format("action doesn't exist for name: %s", actionName));
		}
		
		Context context = contexts.get(actionName);
		Path path = getMatchPath(context, requestName);
		if (forcePath && path == null) {
			throw new RuntimeException(String.format("request doesn't exist for name: %s/%s", actionName, requestName));
		} 
		
		boolean match = path != null && path.matchType(type);
		if (forcePath && !match) {
			throw new RuntimeException(String.format("request type doesn't match. need type: %s/%s/{%s}, but found: %s/%s/%s", 
					actionName, path.url, DumpUtil.toString(path.request.type()), actionName, requestName, type.toString()));
		}
		
		return new ActionProxy(path, requestName);
	}

	/**
	 * 从所有Path里面找出跟请求URL匹配的路径
	 * @param context
	 * @param requstName
	 * @return
	 */
	private Path getMatchPath(Context context, String requstName) {
		String[] needs = StringUtils.split(requstName, PATH_SEPARATOR);
		for (Entry<String, Path> entry : context.paths.entrySet()) {
			if (requstName.equals(entry.getKey())) {
				return entry.getValue();
			}
			
			if (RequestUtil.match(entry.getValue().names, needs)) {
				return entry.getValue();
			}
		}
		
		return null;
	}

	/**
	 * 注册所有的Action
	 * @param clazz
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws RuntimeException
	 * @throws IOException
	 */
	private void regist(Class<?> clazz) throws InstantiationException, IllegalAccessException,
		RuntimeException, IOException {
		
		Action action = clazz.getAnnotation(Action.class);
		if (action == null) {
			return;
		}
		
		String actionName = RequestUtil.correctPath(action.name());
		if (StringUtil.isNullOrEmpty(actionName)) {
			throw new RuntimeException(String.format("action name can't be empty."));
		}
		
		if (contexts.containsKey(actionName)) {
			throw new RuntimeException(String.format("duplicate action name: %s", actionName));
		}
		
		logger.info("regist action. name: {}, class: {}", actionName, clazz.toString());
		
		Context context = new Context(actionName, clazz.newInstance());
		contexts.put(actionName, context);

		for (Method method : clazz.getDeclaredMethods()) {
			regist(context, method);
		}
	}

	/**
	 * 注册Action上所有的响应函数
	 * @param context
	 * @param method
	 * @throws RuntimeException
	 * @throws IOException
	 */
	private void regist(Context context, Method method) throws RuntimeException, IOException {
		Request request = method.getAnnotation(Request.class);
		if (request == null) {
			return;
		}
		
		if (method.getModifiers() != Modifier.PUBLIC) {
			throw new RuntimeException(String.format("request method must be public. action: %s, method: %s",
					context.action.getClass().toString(), method.getName()));
		}
		
		String requestName = RequestUtil.correctPath(request.name());
		if (StringUtil.isNullOrEmpty(requestName)) {
			throw new RuntimeException(String.format("request name can't be empty. action: %s, method: %s",
					context.action.getClass().toString(), method.getName()));
		}
		
		
		if (context.paths.containsKey(requestName)) {
			throw new RuntimeException(String.format("duplicate request name: %s. action: %s, method: %s",
					requestName, context.action.getClass().toString(), method.getName()));
		}
		
		logger.info("regist request. name: {}, method: {}", requestName, method.getName());
		
		context.paths.put(requestName, new Path(requestName, request, method, context.action));
	}

}
