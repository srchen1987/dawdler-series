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
package com.anywide.dawdler.core.loader;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.w3c.dom.Node;

import com.anywide.dawdler.core.context.DawdlerRuntimeContext;
import com.anywide.dawdler.util.XmlObject;
import com.anywide.dawdler.util.XmlTool;
import com.anywide.dawdler.util.aspect.AspectHolder;

import jdk.internal.loader.Resource;
import jdk.internal.perf.PerfCounter;

/**
 * @author jackson.song
 * @version V1.0
 * 通用ClassLoader接口,为了boot版本的classloader复用,将原有的类方法提到这里公用
 */
public interface DeployClassLoader extends Closeable {

	default DawdlerRuntimeContext getDawdlerRuntimeContext() {
		return null;
	}

	ClassLoader classLoader();

	default Class<?> defineClassForDawdler(String name, byte[] codeBytes, boolean useAop, boolean storeVariableNameByASM)
			throws ClassNotFoundException, IOException {
		return loadClassFromBytes(name, codeBytes, classLoader(), null, useAop, storeVariableNameByASM);
	}

	default Class<?> defineClassForDawdler(String name, Resource res, boolean useAop, boolean storeVariableNameByASM)
			throws IOException, ClassNotFoundException {
		long t0 = System.nanoTime();
		int i = name.lastIndexOf('.');
		URL url = res.getCodeSourceURL();
		if (i != -1) {
			String pkgname = name.substring(0, i);
			Manifest man = res.getManifest();
			definePackageInner(pkgname, man, url);
		}

		java.nio.ByteBuffer codeByteBuffer = res.getByteBuffer();
		CodeSigner[] signers = res.getCodeSigners();
		CodeSource cs = new CodeSource(url, signers);
		PerfCounter.getReadClassBytesTime().addElapsedTimeFrom(t0);
		byte[] codeBytes = null;
		if (codeByteBuffer != null) {
			codeByteBuffer.flip();
			codeBytes = codeByteBuffer.array();
		} else {
			codeBytes = res.getBytes();
		}
		return loadClassFromBytes(name, codeBytes, classLoader(), cs, useAop, storeVariableNameByASM);
	}

	default void definePackageInner(String pkgname, Manifest man, URL url) {
		if (getAndVerifyPackageInner(pkgname, man, url) == null) {
			try {
				if (man != null) {
					deployDefinePackage(pkgname, man, url);
				} else {
					deployDefinePackage(pkgname, null, null, null, null, null, null, null);
				}
			} catch (IllegalArgumentException iae) {
				if (getAndVerifyPackageInner(pkgname, man, url) == null) {
					throw new AssertionError("Cannot find package " + pkgname);
				}
			}
		}
	}

	default Package getAndVerifyPackageInner(String pkgname, Manifest man, URL url) {
		Package pkg = getDeployDefinedPackage(pkgname);
		if (pkg != null) {
			if (pkg.isSealed()) {
				if (!pkg.isSealed(url)) {
					throw new SecurityException("sealing violation: package " + pkgname + " is sealed");
				}
			} else {
				if ((man != null) && isSealedInner(pkgname, man)) {
					throw new SecurityException(
							"sealing violation: can't seal package " + pkgname + ": already loaded");
				}
			}
		}
		return pkg;
	}

	Package getDeployDefinedPackage(String pkgname);

	default boolean isSealedInner(String name, Manifest man) {
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

	default public Class<?> findClassForDawdler(final String name, boolean useAop, boolean storeVariableNameByASM) throws ClassNotFoundException {
		return findClassForDawdler(name, null, useAop, storeVariableNameByASM);
	}

	default public Class<?> findClassForDawdler(final String name, boolean resolve, Resource res, boolean useAop,  boolean storeVariableNameByASM)
			throws ClassNotFoundException {
		Class<?> clazz = findClassForDawdler(name, res, useAop, storeVariableNameByASM);
		if (resolve) {
			deployResolveClass(clazz);
		}
		return clazz;
	}

	default public Class<?> loadClassFromBytes(String name, byte[] codeBytes, ClassLoader classLoader, CodeSource cs,
			boolean useAop, boolean storeVariableNameByASM) throws ClassNotFoundException, IOException {
		if (useAop && AspectHolder.aj != null) {
			try {
				codeBytes = (byte[]) AspectHolder.preProcessMethod.invoke(AspectHolder.aj, name, codeBytes, classLoader,
						null);
			} catch (SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new IOException(e);
			}
		}
		Class<?> clazz = deployDefineClass(name, codeBytes, 0, codeBytes.length, cs);
		if(storeVariableNameByASM) {
			Class<?> parameterNameReaderClass = loadClass("com.anywide.dawdler.util.reflectasm.ParameterNameReader");
			try {
				Method method = parameterNameReaderClass.getDeclaredMethod("loadAllDeclaredMethodsParameterNames",
						Class.class, byte[].class);
				method.invoke(null, clazz, codeBytes);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
		return clazz;
	}

	default public void loadAspectj() throws Exception {
		if (AspectHolder.aj != null) {
			try {
				Enumeration<URL> enums = getDeployResources("META-INF/aop.xml");
				while (enums.hasMoreElements()) {
					URL url = enums.nextElement();
					InputStream aopXmlInput = url.openStream();
					try {
						XmlObject xmlo = new XmlObject(aopXmlInput);
						for (Node aspectNode : xmlo.selectNodes("/aspectj/aspects/aspect")) {
							String className = XmlTool.getElementAttribute(aspectNode.getAttributes(), "name");
							if (className != null) {
								findClassForDawdler(className, true, false);
							}
						}
					} catch (Exception e) {
						throw e;
					} finally {
						if (aopXmlInput != null) {
							try {
								aopXmlInput.close();
							} catch (IOException e) {
							}
						}
					}
				}
			} catch (IOException e) {
				throw e;
			}
		} else {
			throw new IOException("not found aspectjweaver in classpath !");
		}
	}

	Package deployDefinePackage(String name, String specTitle, String specVersion, String specVendor, String implTitle,
			String implVersion, String implVendor, URL sealBase);

	Package deployDefinePackage(String pkgname, Manifest man, URL url);

	Class<?> findClassForDawdler(final String name, Resource res, boolean useAop, boolean storeVariableNameByASM) throws ClassNotFoundException;

	default Class<?> findClassForDawdlerByte(String name, byte[] codeBytes, boolean useAop, boolean storeVariableNameByASM)
			throws ClassNotFoundException {
		try {
			return defineClassForDawdler(name, codeBytes, useAop, storeVariableNameByASM);
		} catch (IOException e) {
			throw new ClassNotFoundException(name, e);
		}
	}

	void deployResolveClass(Class<?> clazz);

	Class<?> deployDefineClass(String name, byte[] codeBytes, int i, int length, CodeSource cs);

	Enumeration<URL> getDeployResources(String name) throws IOException;

	Class<?> loadClass(String name) throws ClassNotFoundException;

	Class<?> deployFindClass(final String name) throws ClassNotFoundException;

}
