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
 * 转发器, 收到数据经过解码后，会由Dispatcher转发给对应的Processor
 * @author majikang
 * @create 2019-11-05
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
	
	protected final Map<Class<? extends T>, Processor<? extends T>> processors = new HashMap<>();
	
	public Dispatcher(String packageName) {
		if (StringUtils.isNotEmpty(packageName)) {
			regist(packageName);
		}
	}
	
	/**
	 * @param packageName Processor对应的包，会递归扫描所有的Processor注册
	 */
	@SuppressWarnings("unchecked")
	public void regist(String packageName) {
		try {
			Class<?> clazzT = ReflectUtil.getFirstGenericClass(this.getClass());
			for (Class<?> clazz : ClassScaner.scan(packageName, Processor.class, true)) {
				if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
					continue;
				}
				
				Class<?> generic = ReflectUtil.getFirstGenericClass(clazz);
				if (generic != null && clazzT.isAssignableFrom(generic) && clazzT != generic.getClass()) {
					regist((Class<T>)generic, (Processor<T>)clazz.newInstance());
				}
			}
		} catch (Exception ex) {
			logger.error("regist error.", ex);
		}
	}
	
	public <U extends T> void regist(Class<U> clazz, Processor<U> processor) {
        if (processors.put(clazz, processor) != null) {
            throw new RuntimeException("processor duplicate for class: " + clazz.getName());
        }
    }
	
	public <U extends T> void unregist(Class<U> clazz) {
		processors.remove(clazz);
	}
	
	@SuppressWarnings("unchecked")
	public void dispatch(T msg) {
		Processor<?> p = processors.get(msg.getClass());
		if (p != null) {
			this.beforeDispatch(msg);
        	((Processor<T>)p).process(msg);
        	this.afterDispatch(msg);
        } else {
        	logger.error("processor not exist for class: {}", msg.getClass().getName());
        	this.notFoundProcessor(msg);
        }
	}
	
	protected abstract void beforeDispatch(T msg);
	
	protected abstract void afterDispatch(T msg);
	
	protected abstract void notFoundProcessor(T msg);
	
}
