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
package club.dawdler.server.deploys.loader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.core.context.DawdlerRuntimeContext;
import club.dawdler.core.loader.DeployClassLoader;
import club.dawdler.server.context.DawdlerContext;
import club.dawdler.server.loader.DawdlerClassLoader;

import jdk.internal.loader.Resource;
import jdk.internal.loader.URLClassPath;

/**
 * @author jackson.song
 * @version V1.0
 * Dawdler部署在deploys下的类加载器
 */
public class DawdlerDeployClassLoader extends DawdlerClassLoader implements DeployClassLoader {
	private static final Logger logger = LoggerFactory.getLogger(DawdlerDeployClassLoader.class);
	private final URLClassPath ucp;
	private final DawdlerContext dawdlerContext;
	private final File classesDir;

	public DawdlerDeployClassLoader(DawdlerContext dawdlerContext, URL binPath, ClassLoader parent, URL[] urls,
			File classesDir) {
		super(urls, parent, binPath);
		ucp = new URLClassPath(urls, null, null);
		this.classesDir = classesDir;
		this.dawdlerContext = dawdlerContext;
	}

	@Override
	public DawdlerRuntimeContext getDawdlerRuntimeContext() {
		return dawdlerContext;
	}

	@Override
	public URL getResource(String name) {
		if (name == null || name.trim().equals("")) {
			name = "/";
		}
		URL url = urlCache.get(name);
		if (url != null) {
			return url;
		}
		File file = classesDir;
		if (name.equals("/")) {
			try {
				url = file.toURI().toURL();
				urlCache.put(name, url);
				return url;
			} catch (MalformedURLException e) {
				logger.error("", e);
				return null;
			}
		}
		file = new File(file, name);
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

	@Override
	public Class<?> findClassForDawdler(final String name, boolean useAop, boolean storeVariableNameByASM) throws ClassNotFoundException {
		return findClassForDawdler(name, null, useAop, storeVariableNameByASM);
	}

	@Override
	public Class<?> findClassForDawdler(final String name, Resource res, boolean useAop, boolean storeVariableNameByASM) throws ClassNotFoundException {
		Class<?> clazz = findLoadedClass(name);
		if (clazz != null) {
			return clazz;
		}
		if (res == null) {
			String path = name.replace('.', '/').concat(".class");
			res = ucp.getResource(path, false);
		}
		if (res != null) {
			try {
				return defineClassForDawdler(name, res, useAop, storeVariableNameByASM);
			} catch (IOException e) {
				throw new ClassNotFoundException(name, e);
			}
		} else {
			throw new ClassNotFoundException(name);
		}
	}

	@Override
	public String toString() {
		return getClass().getName() + "\tfor service : " + dawdlerContext.getDeployName();
	}

	@Override
	public ClassLoader classLoader() {
		return this;
	}

	@Override
	public Package deployDefinePackage(String name, String specTitle, String specVersion, String specVendor,
			String implTitle, String implVersion, String implVendor, URL sealBase) {
		return definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase);
	}

	@Override
	public Package deployDefinePackage(String pkgname, Manifest man, URL url) {
		return definePackage(pkgname, man, url);
	}

	@Override
	public void deployResolveClass(Class<?> clazz) {
		resolveClass(clazz);
	}

	@Override
	public Class<?> deployDefineClass(String name, byte[] codeBytes, int i, int length, CodeSource cs) {
		return defineClass(name, codeBytes, i, length, cs);
	}

	@Override
	public Enumeration<URL> getDeployResources(String name) throws IOException {
		return this.getResources(name);
	}

	@Override
	public void close() throws IOException {
		ucp.closeLoaders();
		super.close();
	}

	@Override
	public Package getDeployDefinedPackage(String pkgname) {
		return getDefinedPackage(pkgname);
	}

	@Override
	public Class<?> deployFindClass(String name) throws ClassNotFoundException {
		return findClass(name);
	}

}
