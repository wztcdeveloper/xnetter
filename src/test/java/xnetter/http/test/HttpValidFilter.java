package xnetter.http.test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;

import io.netty.handler.codec.http.FullHttpRequest;
import org.hibernate.validator.HibernateValidator;
import xnetter.http.core.HttpFilter;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * 使用hibernate的注解来验证参数
 * @author majikang
 * @create 2020-01-15
 */
public final class HttpValidFilter extends HttpFilter {

    private Validator validator = Validation.byProvider(HibernateValidator.class)
            .configure().failFast(true).buildValidatorFactory().getValidator();

    @Override
    public Result onDownload(FullHttpRequest request) {
        return EMPTY_RESULT;
    }

    @Override
    public Result onRequest(FullHttpRequest request, Object action,
                            Method method, Object[] params) {
        ExecutableValidator validatorParam = validator.forExecutables();
        Set<ConstraintViolation<Object>> results = validatorParam.validateParameters(
                action, method, params);
        if (!results.isEmpty()) {
            return new Result(results.iterator().next().getMessage());
        }

        return EMPTY_RESULT;
    }
}
