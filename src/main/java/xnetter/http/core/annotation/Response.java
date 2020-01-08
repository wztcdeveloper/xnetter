package xnetter.http.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Response 对HTTP/HTTPS请求的响应
 * @author majikang
 * @create 2019-11-05
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Response {
	
	public enum Type {
		JSON, TEXT, XML, HTML
	}
	
	Type value() default Type.JSON;
	
}
