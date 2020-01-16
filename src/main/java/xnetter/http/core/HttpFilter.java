package xnetter.http.core;

import io.netty.handler.codec.http.FullHttpRequest;
import xnetter.http.annotation.Response;
import xnetter.http.core.HttpRouter.ActionContext;

import java.lang.reflect.Method;

/**
 * 所有HTTP访问的过滤器
 * @author majikang
 * @create 2020-01-15
 */

public abstract class HttpFilter {
    /**
     * 过滤器返回的结果
     */
    public final class Result {
        public final Response.Type respType;
        public final Object content;

        public Result(Object content) {
            this(Response.Type.JSON, content);
        }

        public Result(Response.Type respType, Object content) {
            this.respType = respType;
            this.content = content;
        }
    }

    /**
     * 返回为非空，才真正的下载文件
     * 否则直接将结果返回给客户端
     * @param request
     * @return
     */
    public abstract Result onDownload(FullHttpRequest request);

    /**
     * 返回为非空，才继续后面的请求
     * 否则直接将结果返回给客户端
     * @param request
     * @param action 响应的对象
     * @param method 响应的函数
     * @param params 函数传递的参数值
     * @return
     */
    public abstract Result onRequest(FullHttpRequest request,
                                     Object action, Method method, Object[] params);
}
