package xnetter.utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类扫描工具类
 * @author majikang
 * @create 2019-11-05
 */
public final class ClassScaner {
	
	private ClassScaner(){
	}

	/**
	 * 扫描包下面的所有类
	 * @param packageName 包路径
	 * @param recursive 是否递归扫描
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Set<Class<?>> scan(String packageName, boolean recursive) 
			throws IOException, ClassNotFoundException {
		return scan(packageName, null, recursive);
	}

	/**
	 * 扫描包下面的所有类
	 * @param packageName 包路径
	 * @param superClazz 可以指定某类的所有子类
	 * @param recursive 是否递归扫描
	 * @param recursive
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Set<Class<?>> scan(String packageName, Class<?> superClazz, boolean recursive) 
			throws IOException, ClassNotFoundException {
		String packageDirName = packageName.replace('.', '/');

		Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
		while(dirs.hasMoreElements()){
			URL url = dirs.nextElement();
			String protocol = url.getProtocol();
			//如果是以文件形式保存在服务器上
			if("file".equals(protocol)){
				String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
				
				return findClassesInPackageByFile(packageName, filePath, superClazz, recursive);
			}
			else if("jar".equals(protocol)){
				return findClassesInPackageByJar(packageName, url, superClazz, recursive);
			}
		}
	
		return Collections.emptySet();
	}
	
	private static Set<Class<?>> findClassesInPackageByFile(String packageName, 
			String packagePath, Class<?> superClazz, boolean recursive) 
			throws ClassNotFoundException{
		Set<Class<?>> classSet = new LinkedHashSet<>();
		File dir = new File(packagePath);
		if(!dir.exists() || !dir.isDirectory()){
			return classSet;
		}
		
		File[] dirfiles = dir.listFiles(file -> (recursive && file.isDirectory()) || file.getName().endsWith(".class"));
		for(File file : dirfiles) {
			if(file.isDirectory()) {
				Set<Class<?>> subClassSet = findClassesInPackageByFile(packageName + "." + file.getName(), 
						file.getAbsolutePath(), superClazz, recursive);
				subClassSet.forEach(a -> {
					if (isDeriveFrom(a, superClazz)) {
						classSet.add(a);
					}
				});
				
			} else {
				String className = file.getName().substring(0, file.getName().length() - ".class".length());
				Class<?> clazz = loadClass(packageName + "." + className);
				if(clazz != null && isDeriveFrom(clazz, superClazz)) {
					classSet.add(clazz);
				}
			}
		}
		
		return classSet;
	}
	
	private static Set<Class<?>> findClassesInPackageByJar(String packageName, 
			URL url, Class<?> superClazz, boolean recursive)
			throws IOException, ClassNotFoundException{
		Set<Class<?>> classSet = new LinkedHashSet<>();
		String packageDirName = packageName.replace('.', '/');
		
		JarFile jar = ((JarURLConnection)url.openConnection()).getJarFile();
		
		Enumeration<JarEntry> entries = jar.entries();
		while(entries.hasMoreElements()){
			JarEntry entry = entries.nextElement();
			String name = entry.getName();
			if(name.charAt(0) == '/'){
				name = name.substring(1);
			}
			if(name.startsWith(packageDirName)){
				int idx = name.lastIndexOf('/');
				//如果是以“/”结尾，是一个包
				if(idx != -1){
					packageName = name.substring(0, idx).replace('/', '.');
				}
				
				if(idx != -1 || recursive){
					if(name.endsWith(".class") && !entry.isDirectory()){
						String className = name.substring(packageName.length() + 1, name.length() - ".class".length());
						Class<?> clazz = loadClass(packageName + "." + className);
						if(clazz != null && isDeriveFrom(clazz, superClazz)){
							classSet.add(clazz);
						}
					}
				}
			}
		}
		 
		return classSet;
	}
	
	private static boolean isDeriveFrom(Class<?> clazz, Class<?> superClazz) {
		return superClazz == null || (clazz != superClazz && superClazz.isAssignableFrom(clazz));
	}
	
	private static Class<?> loadClass(String fullClassName) throws ClassNotFoundException{
		return Thread.currentThread().getContextClassLoader().loadClass(fullClassName);
	}
}
