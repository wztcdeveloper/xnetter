package xnetter.utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * 反射工具类
 * @author majikang
 * @create 2019-11-05
 */
public final class ReflectUtil {
	
	private ReflectUtil() {
		
	}
	
	public static final String PATH_SEPARATOR = "/";

	/** 根据thisClazz获取第一个泛型参数 */
	public static Class<?> getFirstGenericClass(Class<?> thisClazz) {
		java.lang.reflect.Type parent = thisClazz.getGenericSuperclass();
		if (parent != null && (Class<?>)parent.getClass() != Object.class) {
			if (parent instanceof ParameterizedType) {
				return getFirstGenericClass(parent);
			} else {
				Class<?> target = getFirstGenericClass((Class<?>)parent);
				if (target != null) {
					return target;
				}
			}
		}
		
		for (java.lang.reflect.Type t : thisClazz.getGenericInterfaces()) {
			Class<?> target = getFirstGenericClass(t);
			if (target != null) {
				return target;
			}
		}
		
		return null;
	}
	
	public static Class<?> getFirstGenericClass(java.lang.reflect.Type type) {
		if (type instanceof ParameterizedType) {
			java.lang.reflect.Type[] types = ((ParameterizedType)type).getActualTypeArguments();
			if (types.length > 0) {
				return (Class<?>)types[0];
			}
		}
		return null;
	}
	
	/** thisClazz 是否含有泛型参数 targetClazz */
	public static boolean containsGenericClass(Class<?> thisClazz, Class<?> targetClazz) {
		java.lang.reflect.Type parent = thisClazz.getGenericSuperclass();
		if (parent != null) {
			if (containsGenericClass(parent, targetClazz)) {
				return true;
			}
		}
		
		for (java.lang.reflect.Type t : thisClazz.getGenericInterfaces()) {
			if (containsGenericClass(t, targetClazz)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean containsGenericClass(java.lang.reflect.Type type, Class<?> targetClazz) {
		if (type instanceof ParameterizedType) {
			for (java.lang.reflect.Type t : ((ParameterizedType)type).getActualTypeArguments()) {
				if (t == targetClazz) {
					return true;
				}
			}
		} else {
			return containsGenericClass((Class<?>)type, targetClazz);
		}
		
		return false;
	}
	
	/** 使用字节码工具ASM来获取方法的参数名 */
    public static String[] getMethodParamNames(final Method method) throws IOException {

        final String methodName = method.getName();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final String[] rarametersNames = new String[parameterTypes.length];
        
        final String className = method.getDeclaringClass().getName();
        final boolean isStatic = Modifier.isStatic(method.getModifiers());
        

        ClassReader cr = new ClassReader(className);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        cr.accept(new ClassAdapter(cw) {
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

                final Type[] argTypes = Type.getArgumentTypes(desc);
    
                //参数类型不一致
                if (!methodName.equals(name) || !matchTypes(argTypes, parameterTypes)) {
                    return mv;
                }
                
                return new MethodAdapter(mv) {
                    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                        //如果是静态方法，第一个参数就是方法参数，非静态方法，则第一个参数是 this ,然后才是方法的参数
                        int parameterIndex = isStatic ? index : index - 1;
                        if (0 <= parameterIndex && parameterIndex < parameterTypes.length) {
                        	rarametersNames[parameterIndex] = name;
                        }
                        super.visitLocalVariable(name, desc, signature, start, end, index);
                    }
                };
            }
        }, 0);
        return rarametersNames;
    }

    /**
     * 比较参数是否一致
     */
    private static boolean matchTypes(Type[] types, Class<?>[] parameterTypes) {
        if (types.length != parameterTypes.length) {
            return false;
        }
        
        for (int i = 0; i < types.length; i++) {
            if (!Type.getType(parameterTypes[i]).equals(types[i])) {
                return false;
            }
        }
        return true;
    }
}
