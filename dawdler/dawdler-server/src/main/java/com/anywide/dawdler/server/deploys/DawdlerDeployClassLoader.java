/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anywide.dawdler.server.deploys;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.PrivilegedExceptionAction;
import java.util.WeakHashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.loader.DawdlerClassLoader;
import sun.misc.Resource;
import sun.misc.URLClassPath;

/**
 * 
 * @Title: DawdlerDeployClassLoader.java
 * @Description: Dawdler部署在deploys下的类加载器
 * @author: jackson.song
 * @date: 2015年03月09日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class DawdlerDeployClassLoader extends DawdlerClassLoader {
	private DawdlerContext dawdlerContext;
	private WeakHashMap<String, URL> urlcache = new WeakHashMap<>();
	private final URLClassPath ucp;
	private static Logger logger = LoggerFactory.getLogger(DawdlerDeployClassLoader.class);

	private final AccessControlContext acc;

	public DawdlerDeployClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
		this.acc = AccessController.getContext();
		ucp = new URLClassPath(urls, acc);
	}

	void setDawdlerContext(DawdlerContext dawdlerContext) {
		this.dawdlerContext = dawdlerContext;
	}

	public DawdlerContext getDawdlerContext() {
		return dawdlerContext;
	}

	public static DawdlerDeployClassLoader createLoader(ClassLoader parent, URL... urls) {
		return new DawdlerDeployClassLoader(urls, parent);
	}

	@Override
	public URL getResource(String name) {
		if (name == null || name.trim().equals(""))
			name = "/";
		URL url = urlcache.get(name);
		if (url != null)
			return url;
		if (name.equals("/")) {
			try {
				url = new File(dawdlerContext.getDeployClassPath()).toURI().toURL();
				urlcache.put(name, url);
				return url;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
		}
		File file = new File(dawdlerContext.getDeployClassPath() + name);
		if (file.exists())
			try {
				url = file.toURI().toURL();
				urlcache.put(name, url);
				return url;
			} catch (MalformedURLException e) {
				return null;
			}
		return super.getResource(name);
	}

	private Class<?> defineClassForDawdler(String name, Resource res) throws IOException, ClassNotFoundException {
		long t0 = System.nanoTime();
		int i = name.lastIndexOf('.');
		URL url = res.getCodeSourceURL();
		if (i != -1) {
			String pkgname = name.substring(0, i);
			Manifest man = res.getManifest();
			definePackageInternal(pkgname, man, url);
		}
		
		DawdlerDeployClassLoader classLoader = (DawdlerDeployClassLoader) Thread.currentThread().getContextClassLoader();
		java.nio.ByteBuffer bb = res.getByteBuffer();
		Object obj = dawdlerContext.getAttribute(ServiceBase.ASPECTSUPPORTOBJ);
		CodeSigner[] signers = res.getCodeSigners();
		CodeSource cs = new CodeSource(url, signers);
		sun.misc.PerfCounter.getReadClassBytesTime().addElapsedTimeFrom(t0);
		if (bb != null) {
			if(obj != null) {
				bb.flip();
				byte[] classData = bb.array();
				return loadClassFromBytes(name, classData, obj, classLoader);
			}else {
				return defineClass(name, bb, cs);
			}
		} else {
			byte[] b = res.getBytes();
			if(obj != null) { 
				return loadClassFromBytes(name, b, obj, classLoader);
			}
			return defineClass(name, b, 0, b.length, cs);
		}
	}

	private void definePackageInternal(String pkgname, Manifest man, URL url) {
		if (getAndVerifyPackage(pkgname, man, url) == null) {
			try {
				if (man != null) {
					definePackage(pkgname, man, url);
				} else {
					definePackage(pkgname, null, null, null, null, null, null, null);
				}
			} catch (IllegalArgumentException iae) {
				if (getAndVerifyPackage(pkgname, man, url) == null) {
					throw new AssertionError("Cannot find package " + pkgname);
				}
			}
		}
	}

	private Package getAndVerifyPackage(String pkgname, Manifest man, URL url) {
		Package pkg = getPackage(pkgname);
		if (pkg != null) {
			if (pkg.isSealed()) {
				if (!pkg.isSealed(url)) {
					throw new SecurityException("sealing violation: package " + pkgname + " is sealed");
				}
			} else {
				if ((man != null) && isSealed(pkgname, man)) {
					throw new SecurityException(
							"sealing violation: can't seal package " + pkgname + ": already loaded");
				}
			}
		}
		return pkg;
	}

	private boolean isSealed(String name, Manifest man) {
		String path = name.replace('.', '/').concat("/");
		Attributes attr = man.getAttributes(path);
		String sealed = null;
		if (attr != null) {
			sealed = attr.getValue(Name.SEALED);
		}
		if (sealed == null) {
			if ((attr = man.getMainAttributes()) != null) {
				sealed = attr.getValue(Name.SEALED);
			}
		}
		return "true".equalsIgnoreCase(sealed);
	}

	public Class<?> findClassForDawdler(final String name) throws ClassNotFoundException {
		final Class<?> result;
		try {
			result = AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
				public Class<?> run() throws ClassNotFoundException {
					String path = name.replace('.', '/').concat(".class");
					Resource res = ucp.getResource(path, false);
					if (res != null) {
						try {
							Class clazz = findLoadedClass(name);
							if (clazz != null)
								return clazz;
							return defineClassForDawdler(name, res);
						} catch (IOException e) {
							throw new ClassNotFoundException(name, e);
						}
					} else {
						return null;
					}
				}
			}, acc);
		} catch (java.security.PrivilegedActionException pae) {
			throw (ClassNotFoundException) pae.getException();
		}
		if (result == null) {
			throw new ClassNotFoundException(name);
		}
		return result;
	}

	public Class<?> loadClassFromBytes(String name, byte[] classData,Object obj,ClassLoader classLoader) throws ClassNotFoundException {
		Method method = (Method)dawdlerContext.getAttribute(ServiceBase.ASPECTSUPPORTMETHOD);
		try {
			classData = (byte[]) method.invoke(obj, name, classData, classLoader, null);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("", e);
		}
		Class clazz = defineClass(name, classData, 0, classData.length);
		resolveClass(clazz);
		return clazz;
	}

	@Override
	public String toString() {
		return getClass().getName() + "\tfor service : " + dawdlerContext.getDeployName();
	}
}
