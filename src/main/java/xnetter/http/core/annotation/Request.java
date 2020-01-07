package xnetter.http.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Request 注解
 * @author majikang
 * @create 2019-11-05
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Request {

	String name() default "";
	
	/**
	 * http 请求类型
	 */
	public enum Type {
		GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
	}
	
	Type[] type() default {Type.POST};
}
