package xnetter.sock.core;

/**
 * 业务逻辑, 收到的每个数据对象对应一个Action
 * @author majikang
 * @create 2019-12-05
 */
public interface Action<T> {

    void process(T msg);
}
