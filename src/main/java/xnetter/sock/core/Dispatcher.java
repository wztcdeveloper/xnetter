package xnetter.sock.core;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xnetter.sock.protocol.Protocol;
import xnetter.sock.protocol.ProtocolDispatcher;
import xnetter.utils.ClassScaner;
import xnetter.utils.ReflectUtil;

/**
 * 转发器, 收到数据经过解码后，会由Dispatcher转发给对应的Action
 * @author majikang
 * @create 2019-12-05
 */
public abstract class Dispatcher<T> {

	public interface Factory {
		Dispatcher<?> create(String packageName);

		public static final Dispatcher.Factory DEFAULT = new Dispatcher.Factory() {
			@Override
			public Dispatcher<Protocol> create(String packageName) {
				return new ProtocolDispatcher(packageName);
			}
		};
	}

	private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);
	
	protected final Map<Class<? extends T>, Action<? extends T>> actions = new HashMap<>();
	
	public Dispatcher(String packageName) {
		if (StringUtils.isNotEmpty(packageName)) {
			regist(packageName);
		}
	}
	
	/**
	 * 递归扫描所有的Action注册
	 * @param packageName Action对应的包
	 */
	@SuppressWarnings("unchecked")
	public void regist(String packageName) {
		try {
			Class<?> clazzT = ReflectUtil.getFirstGenericClass(this.getClass());
			for (Class<?> clazz : ClassScaner.scan(packageName, Action.class, true)) {
				if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
					continue;
				}
				
				Class<?> generic = ReflectUtil.getFirstGenericClass(clazz);
				if (generic != null && clazzT.isAssignableFrom(generic) && clazzT != generic.getClass()) {
					regist((Class<T>)generic, (Action<T>)clazz.newInstance());
				}
			}
		} catch (Exception ex) {
			logger.error("regist error.", ex);
		}
	}
	
	public <U extends T> void regist(Class<U> clazz, Action<U> action) {
        if (actions.put(clazz, action) != null) {
            throw new RuntimeException("action duplicate for class: " + clazz.getName());
        }
    }
	
	public <U extends T> void unregist(Class<U> clazz) {
		actions.remove(clazz);
	}
	
	@SuppressWarnings("unchecked")
	public void dispatch(T msg) {
		Action<?> p = actions.get(msg.getClass());
		if (p != null) {
			this.beforeDispatch(msg);
        	((Action<T>)p).process(msg);
        	this.afterDispatch(msg);
        } else {
        	logger.error("action not exist for class: {}", msg.getClass().getName());
        	this.notFoundAction(msg);
        }
	}

	/**
	 * 分发到Action处理前调用
	 * @param msg
	 */
	protected abstract void beforeDispatch(T msg);

	/**
	 * 分发到Action处理后调用
	 * @param msg
	 */
	protected abstract void afterDispatch(T msg);

	/**
	 * 没有找到相应的Action时调用
	 * @param msg
	 */
	protected abstract void notFoundAction(T msg);
	
}
