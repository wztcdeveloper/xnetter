package xnetter.http.test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import io.netty.handler.codec.http.FullHttpRequest;
import org.hibernate.validator.HibernateValidator;
import xnetter.http.core.HttpFilter;

import java.lang.reflect.Method;
import java.util.Set;

public final class HttpValidFilter extends HttpFilter {

    /**
     * 使用hibernate的注解来进行验证
     */
    private Validator validator = Validation.byProvider(HibernateValidator.class)
            .configure().failFast(true).buildValidatorFactory().getValidator();

    /**
     * 功能描述: <br>
     * 〈注解验证参数〉
     *
     * @param obj
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public <T> void validate(T obj) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(obj);
        // 抛出检验异常
        if (constraintViolations.size() > 0) {
            throw new RuntimeException(String.format("参数校验失败:%s", constraintViolations.iterator().next().getMessage()));
        }
    }

    @Override
    public Result onDownload(FullHttpRequest request) {
        return null;
    }

    @Override
    public Result onRequest(FullHttpRequest request, Method method, Object[] params) {
        return null;
    }
}
