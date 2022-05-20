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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.loader.DawdlerClassLoader;

import jdk.internal.loader.Resource;
import jdk.internal.loader.URLClassPath;
import jdk.internal.perf.PerfCounter;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerDeployClassLoader.java
 * @Description Dawdler部署在deploys下的类加载器
 * @date 2015年3月9日
 * @email suxuan696@gmail.com
 */
public class DawdlerDeployClassLoader extends DawdlerClassLoader {
	private static final Logger logger = LoggerFactory.getLogger(DawdlerDeployClassLoader.class);
	private final URLClassPath ucp;
	private DawdlerContext dawdlerContext;

	public DawdlerDeployClassLoader(URL binPath, URL[] urls, ClassLoader parent) {
		super(urls, parent, binPath);
		ucp = new URLClassPath(urls, null, null);
	}

	public static DawdlerDeployClassLoader createLoader(URL binPath, ClassLoader parent, URL... urls) {
		return new DawdlerDeployClassLoader(binPath, urls, parent);
	}

	public DawdlerContext getDawdlerContext() {
		return dawdlerContext;
	}

	void setDawdlerContext(DawdlerContext dawdlerContext) {
		this.dawdlerContext = dawdlerContext;
	}

	@Override
	public URL getResource(String name) {
		if (name == null || name.trim().equals(""))
			name = "/";
		URL url = urlCache.get(name);
		if (url != null)
			return url;
		if (name.equals("/")) {
			try {
				url = new File(dawdlerContext.getDeployClassPath()).toURI().toURL();
				urlCache.put(name, url);
				return url;
			} catch (MalformedURLException e) {
				logger.error("", e);
				return null;
			}
		}
		File file = new File(dawdlerContext.getDeployClassPath() + name);
		if (file.exists()) {
			try {
				url = file.toURI().toURL();
				urlCache.put(name, url);
				return url;
			} catch (MalformedURLException e) {
				logger.error("", e);
				return null;
			}
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

		DawdlerDeployClassLoader classLoader = (DawdlerDeployClassLoader) Thread.currentThread()
				.getContextClassLoader();
		java.nio.ByteBuffer codeBytes = res.getByteBuffer();
		Object obj = dawdlerContext.getAttribute(ServiceBase.ASPECT_SUPPORT_OBJ);
		CodeSigner[] signers = res.getCodeSigners();
		CodeSource cs = new CodeSource(url, signers);
		PerfCounter.getReadClassBytesTime().addElapsedTimeFrom(t0);
		if (codeBytes != null) {
			if (obj != null) {
				codeBytes.flip();
				byte[] classData = codeBytes.array();
				return loadClassFromBytes(name, classData, obj, classLoader);
			} else {
				return defineClass(name, codeBytes, cs);
			}
		} else {
			byte[] b = res.getBytes();
			if (obj != null) {
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
		Package pkg = getDefinedPackage(pkgname);
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
		String path = name.replace('.', '/').concat(".class");
		Resource res = ucp.getResource(path, false);
		if (res != null) {
			try {
				Class<?> clazz = findLoadedClass(name);
				if (clazz != null)
					return clazz;
				clazz = defineClassForDawdler(name, res);
				return clazz;
			} catch (IOException e) {
				throw new ClassNotFoundException(name, e);
			}
		} else {
			throw new ClassNotFoundException(name);
		}
	}

	public Class<?> findClassForDawdler(final String name, boolean resolve) throws ClassNotFoundException {
		Class<?> clazz = findClassForDawdler(name);
		if (resolve) {
			resolveClass(clazz);
		}
		return clazz;
	}

	public Class<?> loadClassFromBytes(String name, byte[] classData, Object obj, ClassLoader classLoader)
			throws ClassNotFoundException {
		Method method = (Method) dawdlerContext.getAttribute(ServiceBase.ASPECT_SUPPORT_METHOD);
		try {
			classData = (byte[]) method.invoke(obj, name, classData, classLoader, null);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("", e);
		}
		Class<?> clazz = defineClass(name, classData, 0, classData.length);
		return clazz;
	}

	@Override
	public String toString() {
		return getClass().getName() + "\tfor service : " + dawdlerContext.getDeployName();
	}
}
