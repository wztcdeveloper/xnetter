package xnetter.http.data.encode;

import io.netty.handler.codec.http.FullHttpRequest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import xnetter.http.core.HttpRouter;
import xnetter.http.core.annotation.ParamVariable;
import xnetter.http.core.annotation.PathVariable;
import xnetter.http.core.utils.RequestUtil;
import xnetter.http.data.decode.Decoder;
import xnetter.http.type.TType;

/**
 * 将解析结果映射为接口需要的参数
 * @author majikang
 * @create 2019-11-05
 */

public class Encoder {
	private final HttpRouter.Path path;
	private final FullHttpRequest request;
	
	public Encoder(FullHttpRequest request, HttpRouter.Path path) {
		this.request = request;
		this.path = path;
	}

	/**
	 * 将请求参数转换为接口参数数组
	 * @param url 请求URL
	 * @param params 请求参数
	 * @return 接口参数数组
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public Object[] encode(String url, Map<String, Object> params) 
			throws InstantiationException, IllegalAccessException {
		String[] urls = StringUtils.split(url, HttpRouter.PATH_SEPARATOR);
		Object[] results = new Object[path.parameters.length];
		
		// 构造调用所需要的参数数组
		for (int i = 0; i < path.parameters.length; i++) {
			String paramName = path.paramNames[i];
			Type paramType = path.parameters[i].getParameterizedType();
			PathVariable pathVar = getAnnotation(path.paramAnns[i], PathVariable.class);
			ParamVariable reqParam = getAnnotation(path.paramAnns[i], ParamVariable.class);
			
			if (reqParam != null) {
				paramName = reqParam.name();
			}

			if (getClass(paramType) == FullHttpRequest.class) {
				// 接口可以直接使用FullHttpRequest对象
				results[i] = request;
			} else if (pathVar != null) {
				// PathVariable注解的接口参数，从请求URL里面获得
				results[i] = getPathValue(urls, paramType, pathVar.name());
			} else if (params.containsKey(paramName)) {
				// 根据接口参数变量名，从params里面去获得相应的值
				results[i] = getParamValue(paramType, params.get(paramName));
			} else if (params.containsKey(Decoder.DEFAULT_KEY)) {
				// 有些请求只传递Value，所以提供默认的Key
				results[i] = getParamValue(paramType, params.get(Decoder.DEFAULT_KEY));
			} else {
				results[i] = getParamValue(paramType, params);
			}
		}
		
		return results;
	}

	/**
	 * 从所有注解里面，找到某类型的目标注解
	 * @param annotations
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T extends Annotation> T getAnnotation(Annotation[] annotations, Class<T> clazz) {
		if (annotations == null || annotations.length == 0) {
			return null;
		}
		
		for (Annotation ann : annotations) {
			if (ann.annotationType() == clazz) {
				return (T)ann;
			}
		}
		
		return null;
	}

	/**
	 * 路径中含有变量，则需要将URL中的值获得，并转换到接口变量中
	 * @param urls
	 * @param varType
	 * @param varName
	 * @return
	 */
	private Object getPathValue(String[] urls, Type varType, String varName) {
		if (path.names.length != urls.length) {
			return null;
		}
		
		Class<?> varClass = getClass(varType);
		for (int i=0; i<path.names.length; i++) {
			if (RequestUtil.matchVar(path.names[i], varName)) {
				return TType.getBaseValue(varClass, urls[i]);
			}
		}
		
		return null;
	}

	/**
	 * 根据特定的类型，将value转成需要的对象
	 * 目前支持基础类型、Bean对象、数组、List、Set、Map
	 * @param paramType 参数类型
	 * @param value 参数值
	 * @return 返回对象
	 */
	@SuppressWarnings("unchecked")
	private Object getParamValue(Type paramType, Object value) 
			throws InstantiationException, IllegalAccessException {
		if (value == null) {
			return null;
		}
	
		Class<?> paramClass = getClass(paramType);
		if (TType.isBaseType(paramClass)) {
			return TType.getBaseValue(paramClass, value);
		} else if (paramClass.isArray()) {
			Object[] objects = TType.toArray(value);
			
			// 获得数组的具体类型
			Class<?> componentType = paramClass.getComponentType();
			Object results = Array.newInstance(componentType, objects.length);
			for (int i=0; i<objects.length; i++) {
				Array.set(results, i, getParamValue(componentType, objects[i]));
			}
			return results;
		} else if (List.class.isAssignableFrom(paramClass)) {
			Object[] objects = TType.toArray(value);
			
			List<Object> results = null;
			if (paramClass == List.class) {
				results = new ArrayList<Object>();
			} else {
				results = (List<Object>)paramClass.newInstance();
			}
			
			// List的具体类型数组，第一个为Value的类型
			Type[] actualTypes = ((ParameterizedType)paramType).getActualTypeArguments();
			for (int i=0; i<objects.length; i++) {
				results.add(getParamValue(actualTypes[0], objects[i]));
			}
			return results;
		} else if (Set.class.isAssignableFrom(paramClass)) {
			Object[] objects = TType.toArray(value);
			
			Set<Object> results = null;
			if (paramClass == Set.class) {
				results = new HashSet<Object>();
			} else {
				results = (Set<Object>)paramClass.newInstance();
			}
			
			// List的具体类型数组，第一个为Value的类型
			Type[] actualTypes = ((ParameterizedType)paramType).getActualTypeArguments();
			for (int i=0; i<objects.length; i++) {
				results.add(getParamValue(actualTypes[0], objects[i]));
			}
			return results;
		} else if (Map.class.isAssignableFrom(paramClass) && Map.class.isAssignableFrom(value.getClass())) {
			Map<Object, Object> results = null;
			if (paramClass == Map.class) {
				results = new HashMap<Object, Object>();
			} else {
				results = (Map<Object, Object>)paramClass.newInstance();
			}
			
			// Map的具体类型数组，第一个为K的类型，第二个位V的类型
			Type[] actualTypes = ((ParameterizedType)paramType).getActualTypeArguments();
			for (Entry<Object, Object> entry : ((Map<Object, Object>)value).entrySet()) {
				results.put(getParamValue(actualTypes[0], entry.getKey()), 
						getParamValue(actualTypes[1], entry.getValue()));
			}
			return results;
		} else if (Map.class.isAssignableFrom(value.getClass())) {
			return toJavaObject((Map<Object, Object>)value, paramClass);
		} else if (JSON.class.isAssignableFrom(value.getClass())) {
			return JSONObject.toJavaObject((JSON)value, paramClass);
		} else {
			return value;
		}
	}

	/**
	 * 根据type获得相应的class
	 * 因为接口参数限定，有些类型暂时不支持
	 * @param type
	 * @return
	 * @throws EncodeException
	 */
	private Class<?> getClass(Type type) throws EncodeException {
		Class<?> clazz = null;
		if (type instanceof ParameterizedType) {
			clazz = (Class<?>) ((ParameterizedType)type).getRawType();
		} else if (type instanceof GenericArrayType) {
			throw new EncodeException("Parameter of interface does't suppert GenericArrayType");
		} else if (type instanceof WildcardType) {
			throw new EncodeException("Parameter of interface does't suppert WildcardType");
		} else if (type instanceof TypeVariable) {
			throw new EncodeException("Parameter of interface does't suppert TypeVariable");
		} else {
			clazz = (Class<?>)type;
		}
		return clazz;
	}

	/**
	 * 构造clazz对象，并根据map对其成员赋值
	 * @param map 参数Key与Value
	 * @param clazz 需要构建的类
	 * @return 构建好的对象
	 */
	private Object toJavaObject(Map<Object, Object> map, Class<?> clazz) {
		try {
			Object target = clazz.newInstance();
			for (Field field : clazz.getDeclaredFields()) {
				String varName = field.getName();
				ParamVariable ann = field.getAnnotation(ParamVariable.class);
				if (ann != null) {
					varName = ann.name();
				}
				
				Object value = map.get(varName);
				if (value == null) {
					continue;
				}
				
				field.setAccessible(true);
				field.set(target, getParamValue(field.getType(), value));
			}
			
			return target;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
}
