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
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import club.dawdler.core.component.injector.CustomComponentInjectionProvider;
import club.dawdler.core.component.injector.CustomComponentInjector;
import club.dawdler.core.order.OrderData;
import club.dawdler.core.scan.component.reader.ClassStructureParser;
import club.dawdler.core.scan.component.reader.ClassStructureParser.ClassStructure;
import club.dawdler.util.IOUtil;
import club.dawdler.util.SunReflectionFactoryInstantiator;
import club.dawdler.util.XmlObject;
import club.dawdler.util.XmlTool;
import club.dawdler.util.aspect.AspectHolder;
import club.dawdler.util.reflectasm.ParameterNameReader;
import club.dawdler.util.spring.antpath.Resource;

/**
 * @author jackson.song
 * @version V1.0
 * 提供加载方法来加载远端模版类到jvm中
 */
public class ClientPlugClassLoader {
	private static final Logger logger = LoggerFactory.getLogger(ClientPlugClassLoader.class);
	private static final Map<String, Class<?>> REMOTE_CLASSES = new ConcurrentHashMap<>();
	private static ClientPlugClassLoader classloader = null;
	private final List<OrderData<RemoteClassLoaderFire>> fireList = RemoteClassLoaderFireHolder.getInstance()
			.getRemoteClassLoaderFire();
	List<OrderData<CustomComponentInjector>> customComponentInjectorList = CustomComponentInjectionProvider
			.getDefaultInstance().getCustomComponentInjectors();
	private ClientClassLoader clientClassLoader = null;

	private ClientPlugClassLoader(String path) {
		updateLoad(path);
	}

	public static synchronized ClientPlugClassLoader newInstance(String path) {
		if (classloader == null) {
			classloader = new ClientPlugClassLoader(path);
		}
		return classloader;
	}

	public static Class<?> getRemoteClass(String key) {
		return REMOTE_CLASSES.get(key);
	}

	public void load(String host, String className, byte[] codeBytes) throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("loading %%%" + host + "%%%module  \t" + className + ".class");
		}
		ClassStructure classStructure = ClassStructureParser.parser(codeBytes);
		for (OrderData<CustomComponentInjector> data : customComponentInjectorList) {
			Class<?> clazz = inject(codeBytes, classStructure, data.getData());
			if (clazz == null) {
				continue;
			}
			Class<?> oldClazz = REMOTE_CLASSES.get(host.trim() + "-" + className);
			if (oldClazz == null || oldClazz != clazz) {
				REMOTE_CLASSES.put(host.trim() + "-" + className, clazz);
				try {
					ParameterNameReader.loadAllDeclaredMethodsParameterNames(clazz, codeBytes);
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
	}

	public Class<?> defineClass(String className, byte[] codeBytes, boolean useAop) {
		if (useAop && AspectHolder.aj != null) {
			try {
				codeBytes = (byte[]) AspectHolder.preProcessMethod.invoke(AspectHolder.aj, className, codeBytes,
						clientClassLoader, null);
			} catch (SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				logger.error("", e);
			}
		}

		return clientClassLoader.defineClass(className, codeBytes);
	}

	public Class<?> defineClass(String className, Resource res, boolean useAop) throws IOException {
		return clientClassLoader.defineClass(className, res, useAop);
	}

	public void remove(String name) {
		if (logger.isDebugEnabled()) {
			logger.debug("remove class " + name + ".class");
		}
		Class<?> clazz = REMOTE_CLASSES.remove(name);
		for (OrderData<RemoteClassLoaderFire> rf : fireList) {
			rf.getData().onRemoveFire(clazz);
		}
	}

	public void updateLoad(String path) {
		URLClassLoader oldUrlCL = clientClassLoader;
		try {
			URL url = new URI("file", path+"/", null).toURL();
			this.clientClassLoader = ClientClassLoader.newInstance(new URL[] { url }, getClass().getClassLoader());
			loadAspectj();
		} catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
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
							String className = XmlTool.getElementAttribute(aspectNode.getAttributes(), "name");
							if (className != null) {
								String fileName = className.replace(".", "/") + ".class";
								try (InputStream classInput = classLoader.getResourceAsStream(fileName)) {
									if (classInput == null) {
										logger.error(fileName + " not found !");
									} else {
										byte[] classData = IOUtil.toByteArray(classInput);
										classData = (byte[]) AspectHolder.preProcessMethod.invoke(AspectHolder.aj,
												className, classData, classLoader, null);
										Class<?> clazz = clientClassLoader.defineClass(className, classData);
										clientClassLoader.toResolveClass(clazz);
									}
								} catch (Exception e) {
									logger.error("", e);
								}
							}
						}
					} catch (Exception e) {
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
			logger.error("not found aspectjweaver in classpath !");
		}
	}

	public void close() {
		try {
			clientClassLoader.close();
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	public Class<?> inject(byte[] codeBytes, ClassStructure classStructure,
			CustomComponentInjector customComponentInjector) throws Throwable {
		return inject(null, codeBytes, classStructure, customComponentInjector);
	}

	private Class<?> inject(Resource resource, byte[] codeBytes, ClassStructure classStructure,
			CustomComponentInjector customComponentInjector) throws Throwable {
		boolean match = false;
		Class<?>[] matchTypes = customComponentInjector.getMatchTypes();
		if (matchTypes != null) {
			for (Class<?> matchType : matchTypes) {
				if (classStructure.getInterfaces().contains(matchType.getName())) {
					match = true;
					break;
				}
				if (classStructure.getClassName().equals(matchType.getName())
						|| classStructure.getSuperClasses().contains(matchType.getName())) {
					match = true;
					break;
				}
			}
		}
		if (!match) {
			Set<? extends Class<? extends Annotation>> annotationSet = customComponentInjector.getMatchAnnotations();
			if (annotationSet != null) {
				for (Class<? extends Annotation> annotationType : annotationSet) {
					if (classStructure.getAnnotationNames().contains(annotationType.getName())) {
						match = true;
						break;
					}
				}
			}
		}
		if (match) {
			Class<?> c;
			if (codeBytes != null) {
				c = defineClass(classStructure.getClassName(), codeBytes, customComponentInjector.useAop());
			} else {
				c = defineClass(classStructure.getClassName(), resource, customComponentInjector.useAop());
			}
			if (customComponentInjector.isInject() && !classStructure.isAbstract() && !classStructure.isAnnotation()
					&& !classStructure.isInterface()) {
				Object target = SunReflectionFactoryInstantiator.newInstance(c);
				customComponentInjector.inject(c, target);
			}
			return c;
		} else {
			Class<?> c;
			if (codeBytes != null) {
				c = defineClass(classStructure.getClassName(), codeBytes, false);
			} else {
				c = defineClass(classStructure.getClassName(), resource, false);
			}
			return c;
		}
	}

}
