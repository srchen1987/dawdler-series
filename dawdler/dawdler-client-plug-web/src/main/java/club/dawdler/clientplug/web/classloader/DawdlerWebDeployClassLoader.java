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
package club.dawdler.clientplug.web.classloader;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceLoader;
import java.util.jar.Manifest;

import club.dawdler.core.loader.DeployClassLoader;
import club.dawdler.core.scan.DawdlerComponentScanner;
import jdk.internal.loader.Resource;
/**
 * @author jackson.song
 * @version V1.0
 * web模块类加载器
 */
public class DawdlerWebDeployClassLoader extends SecureClassLoader implements DeployClassLoader {
	private ClassLoader parent;

	private List<DawdlerClassLoaderMatcher> dawdlerClassLoaderMatchers = new ArrayList<>();

	public DawdlerWebDeployClassLoader(ClassLoader parent) throws Exception {
		super(parent);
		this.parent = parent;
		ServiceLoader.load(DawdlerClassLoaderMatcher.class).forEach(resolver -> {
			dawdlerClassLoaderMatchers.add(resolver);
		});
		loadAspectj();
	}

	@Override
	public ClassLoader classLoader() {
		return parent;
	}

	@Override
	public Package getDeployDefinedPackage(String pkgname) {
		return getDefinedPackage(pkgname);
	}

	@Override
	public Package deployDefinePackage(String name, String specTitle, String specVersion, String specVendor,
			String implTitle, String implVersion, String implVendor, URL sealBase) {
		return definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase);
	}

	@Override
	public Class<?> findClassForDawdler(String name, Resource res, boolean useAop, boolean storeVariableNameByASM)
			throws ClassNotFoundException {
		Class<?> clazz = findLoadedClass(name);
		if (clazz != null) {
			return clazz;
		}
		if (res == null) {
			res = DawdlerComponentScanner.getClass(name);
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
	public void deployResolveClass(Class<?> clazz) {
		resolveClass(clazz);
	}

	@Override
	public Class<?> deployDefineClass(String name, byte[] codeBytes, int i, int length, CodeSource cs) {
		return defineClass(name, codeBytes, i, length, cs);
	}

	@Override
	public Enumeration<URL> getDeployResources(String name) throws IOException {
		return parent.getResources(name);
	}

	@Override
	public Class<?> deployFindClass(String name) throws ClassNotFoundException {
		return findClass(name);
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public Package deployDefinePackage(String pkgname, Manifest man, URL url) {
		return null;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		for (DawdlerClassLoaderMatcher dawdlerClassLoaderMatcher : dawdlerClassLoaderMatchers) {
			for (String matchPackageName : dawdlerClassLoaderMatcher.matchPackageName()) {
				if (name.startsWith(matchPackageName)) {
					return findClassForDawdler(name, false, false);
				}
			}
		}
		return parent.loadClass(name);
	}

}
