package com.anywide.util.reflectasm;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.WeakHashMap;
/**
 * 
 * @Title:  AccessClassLoader.java
 * @Description:    asm的改进版   已升到asm支持jdk8   
 * @author: jackson.song    
 * @date:   2012年05月16日    
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
class AccessClassLoader extends ClassLoader {
	static private final WeakHashMap<ClassLoader, WeakReference<AccessClassLoader>> accessClassLoaders = new WeakHashMap();
	static private final ClassLoader selfContextParentClassLoader = getParentClassLoader(AccessClassLoader.class);
	static private volatile AccessClassLoader selfContextAccessClassLoader = new AccessClassLoader(selfContextParentClassLoader);
	static private volatile Method defineClassMethod;
	static AccessClassLoader get (Class type) {
		ClassLoader parent = getParentClassLoader(type);
		if (selfContextParentClassLoader.equals(parent)) {
			if (selfContextAccessClassLoader == null) {
				synchronized (accessClassLoaders) {
					if (selfContextAccessClassLoader == null)
						selfContextAccessClassLoader = new AccessClassLoader(selfContextParentClassLoader);
				}
			}
			return selfContextAccessClassLoader;
		}
		synchronized (accessClassLoaders) {
			WeakReference<AccessClassLoader> ref = accessClassLoaders.get(parent);
			if (ref != null) {
				AccessClassLoader accessClassLoader = ref.get();
				if (accessClassLoader != null)
					return accessClassLoader;
				else
					accessClassLoaders.remove(parent);  
			}
			AccessClassLoader accessClassLoader = new AccessClassLoader(parent);
			accessClassLoaders.put(parent, new WeakReference<AccessClassLoader>(accessClassLoader));
			return accessClassLoader;
		}
	}

	public static void remove (ClassLoader parent) {
		if (selfContextParentClassLoader.equals(parent)) {
			selfContextAccessClassLoader = null;
		} else {
			synchronized (accessClassLoaders) {
				accessClassLoaders.remove(parent);
			}
		}
	}

	public static int activeAccessClassLoaders () {
		int sz = accessClassLoaders.size();
		if (selfContextAccessClassLoader != null) sz++;
		return sz;
	}

	private AccessClassLoader (ClassLoader parent) {
		super(parent);
	}

	protected java.lang.Class<?> loadClass (String name, boolean resolve) throws ClassNotFoundException {
		if (name.equals(FieldAccess.class.getName())) return FieldAccess.class;
		if (name.equals(MethodAccess.class.getName())) return MethodAccess.class;
		if (name.equals(ConstructorAccess.class.getName())) return ConstructorAccess.class;
		if (name.equals(PublicConstructorAccess.class.getName())) return PublicConstructorAccess.class;
		return super.loadClass(name, resolve);
	}

	Class<?> defineClass (String name, byte[] bytes) throws ClassFormatError {
		try {
			return (Class<?>)getDefineClassMethod().invoke(getParent(), new Object[] {name, bytes, Integer.valueOf(0), Integer.valueOf(bytes.length),
				getClass().getProtectionDomain()});
		} catch (Exception ignored) {
		}
		return defineClass(name, bytes, 0, bytes.length, getClass().getProtectionDomain());
	}
	
	static boolean areInSameRuntimeClassLoader(Class type1, Class type2) {
		if (type1.getPackage()!=type2.getPackage()) {
			return false;
		}
		ClassLoader loader1 = type1.getClassLoader();
		ClassLoader loader2 = type2.getClassLoader();
		ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
		if (loader1==null) {
			return (loader2==null || loader2==systemClassLoader);
		}
		if (loader2==null) {
			return loader1==systemClassLoader;
		}
		return loader1==loader2;
	}

	private static ClassLoader getParentClassLoader (Class type) {
		ClassLoader parent = type.getClassLoader();
		if (parent == null) parent = ClassLoader.getSystemClassLoader();
		return parent;
	}
	
	private static Method getDefineClassMethod() throws Exception {
		if (defineClassMethod==null) {
			synchronized(accessClassLoaders) {
				defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] {String.class, byte[].class, int.class,
				int.class, ProtectionDomain.class});
				try {
					defineClassMethod.setAccessible(true);
				}
				catch (Exception ignored) {
				}
			}
		}
		return defineClassMethod;
	}
}
