package xnetter.http.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ParamVariable 从请求参数和请求内容中获取变量
 * @author majikang
 * @create 2019-11-05
 */

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ParamVariable {

	String name();
}
