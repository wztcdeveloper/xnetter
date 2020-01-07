package xnetter.sock.core;

/**
 * 处理器, 收到的每个数据对象对应一个处理器
 * @author majikang
 * @create 2019-11-05
 */
public interface Processor<T> {

    void process(T msg);
}
