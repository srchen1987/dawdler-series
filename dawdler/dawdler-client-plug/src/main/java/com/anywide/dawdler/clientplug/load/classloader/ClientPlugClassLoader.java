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
package com.anywide.dawdler.clientplug.load.classloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.annotation.Controller;
import com.anywide.dawdler.clientplug.web.TransactionController;
import com.anywide.dawdler.clientplug.web.interceptor.HandlerInterceptor;
import com.anywide.dawdler.clientplug.web.listener.WebContextListener;
import com.anywide.dawdler.core.order.OrderData;
import com.anywide.dawdler.util.IOUtil;
import com.anywide.dawdler.util.XmlObject;
import com.anywide.dawdler.util.aspect.AspectHolder;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ClientPlugClassLoader.java
 * @Description 提供加载方法来加载远端模版类到jvm中
 * @date 2007年7月22日
 * @email suxuan696@gmail.com
 */
public class ClientPlugClassLoader {
	private static final Logger logger = LoggerFactory.getLogger(ClientPlugClassLoader.class);
	private static final Map<String, Class<?>> remoteClass = new ConcurrentHashMap<>();
	private static ClientPlugClassLoader classloader = null;
	private final List<OrderData<RemoteClassLoaderFire>> fireList = RemoteClassLoaderFireHolder.getInstance()
			.getRemoteClassLoaderFire();
	private ClientClassLoader urlCL = null;

	private ClientPlugClassLoader(String path) {
		updateLoad(path);
	}

	public synchronized static ClientPlugClassLoader newInstance(String path) {
		if (classloader == null) {
			classloader = new ClientPlugClassLoader(path);
		}
		return classloader;
	}

	public static Class<?> getRemoteClass(String key) {
		return remoteClass.get(key);
	}

	public void load(String host, String className, byte[] classBytes) throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("loading %%%" + host + "%%%module  \t" + className + ".class");
		}
		try {
			if (AspectHolder.aj != null) {
				try {
					classBytes = (byte[]) AspectHolder.preProcessMethod.invoke(AspectHolder.aj, className, classBytes,
							urlCL, null);
				} catch (SecurityException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					logger.error("", e);
				}
			}
			Class<?> clazz = defineClass(className, classBytes);
			remoteClass.put(host.trim() + "-" + className, clazz);
			if (clazz.getAnnotation(Controller.class) != null || TransactionController.class.isAssignableFrom(clazz)
					|| WebContextListener.class.isAssignableFrom(clazz)
					|| HandlerInterceptor.class.isAssignableFrom(clazz)) {
				Object target = clazz.getConstructor().newInstance();
				for (OrderData<RemoteClassLoaderFire> rf : fireList) {
					rf.getData().onLoadFire(clazz, target, classBytes);
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public Class<?> defineClass(String className, byte[] classBytes) {
		return urlCL.defineClass(className, classBytes);
	}

	public void remove(String name) {
		if (logger.isDebugEnabled()) {
			logger.debug("remove class " + name + ".class");
		}
		Class<?> clazz = remoteClass.remove(name);
		for (OrderData<RemoteClassLoaderFire> rf : fireList) {
			rf.getData().onRemoveFire(clazz);
		}
	}

	public void updateLoad(String path) {
		URLClassLoader oldUrlCL = urlCL;
		try {
			URL url = new URL("file:" + path + "/");
			this.urlCL = ClientClassLoader.newInstance(new URL[] { url }, getClass().getClassLoader());
			loadAspectj();
		} catch (MalformedURLException e) {
			logger.error("", e);
		} finally {
			try {
				if (oldUrlCL != null) {
					oldUrlCL.close();
				}
			} catch (IOException e) {
			}
		}
	}

	private void loadAspectj() {
		if (AspectHolder.aj != null) {
			ClassLoader classLoader = getClass().getClassLoader();
			try {
				Enumeration<URL> enums = classLoader.getResources("META-INF/aop.xml");
				while (enums.hasMoreElements()) {
					URL url = enums.nextElement();
					InputStream aopXmlInput = url.openStream();
					try {
						XmlObject xmlo = new XmlObject(aopXmlInput);
						for (Node aspectNode : xmlo.selectNodes("/aspectj/aspects/aspect")) {
							Element aspectElement = (Element) aspectNode;
							String className = aspectElement.attributeValue("name");
							if (className != null) {
								String fileName = className.replace(".", File.separator) + ".class";
								try (InputStream classInput = classLoader.getResourceAsStream(fileName)) {
									if (classInput == null) {
										logger.error(fileName + " not found !");
									} else {
										byte[] classData = IOUtil.toByteArray(classInput);
										classData = (byte[]) AspectHolder.preProcessMethod.invoke(AspectHolder.aj,
												className, classData, classLoader, null);
										Class<?> clazz = urlCL.defineClass(className, classData);
										urlCL.toResolveClass(clazz);
									}
								} catch (Exception e) {
									logger.error("", e);
								}
							}
						}
					} catch (DocumentException | IOException e) {
						logger.error("", e);
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
				logger.error("", e);
			}
		} else {
			logger.error("not found aspectjrt and aspectjweaver in classpath !");
		}
	}

}
