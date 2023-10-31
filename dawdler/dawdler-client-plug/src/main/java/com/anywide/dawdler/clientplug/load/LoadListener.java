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
package com.anywide.dawdler.clientplug.load;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.anywide.dawdler.client.ConnectionPool;
import com.anywide.dawdler.client.conf.ClientConfigParser;
import com.anywide.dawdler.clientplug.load.classloader.ClientPlugClassLoader;
import com.anywide.dawdler.clientplug.web.filter.ViewFilter;
import com.anywide.dawdler.clientplug.web.listener.WebContextListenerProvider;
import com.anywide.dawdler.core.component.injector.CustomComponentInjectionProvider;
import com.anywide.dawdler.core.component.injector.CustomComponentInjector;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycleProvider;
import com.anywide.dawdler.core.order.OrderData;
import com.anywide.dawdler.core.scan.DawdlerComponentScanner;
import com.anywide.dawdler.core.scan.component.reader.ClassStructureParser;
import com.anywide.dawdler.core.scan.component.reader.ClassStructureParser.ClassStructure;
import com.anywide.dawdler.core.serializer.SerializeDecider;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.JVMTimeProvider;
import com.anywide.dawdler.util.SunReflectionFactoryInstantiator;
import com.anywide.dawdler.util.XmlObject;
import com.anywide.dawdler.util.XmlTool;
import com.anywide.dawdler.util.spring.antpath.Resource;


/**
 * @author jackson.song
 * @version V1.0
 * @Title LoadListener.java
 * @Description 加载模版类的监听器
 * @date 2007年5月8日
 * @email suxuan696@gmail.com
 */
@WebListener
public class LoadListener implements ServletContextListener {
	private static final Logger logger = LoggerFactory.getLogger(LoadListener.class);
	private long sleep = 600000;
	private final Map<LoadCore, Thread> threads = new ConcurrentHashMap<>();
	private ClientPlugClassLoader classLoader;

	public void contextDestroyed(ServletContextEvent arg0) {
		for (Iterator<Entry<LoadCore, Thread>> it = threads.entrySet().iterator(); it.hasNext();) {
			Entry<LoadCore, Thread> entry = it.next();
			entry.getKey().stop();
			if (entry.getValue().isAlive()) {
				if (logger.isDebugEnabled()) {
					logger.debug("stop \t" + entry.getValue().getName() + "\tload");
				}
				entry.getValue().interrupt();
			}
		}
		WebContextListenerProvider.listenerRun(false, arg0.getServletContext());
		List<OrderData<ComponentLifeCycle>> lifeCycleList = ComponentLifeCycleProvider
				.getInstance(arg0.getServletContext().getContextPath()).getComponentLifeCycles();
		for (int i = lifeCycleList.size() - 1; i >= 0; i--) {
			try {
				OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
				lifeCycle.getData().destroy();
			} catch (Throwable e) {
				logger.error("", e);
			}
		}
		SerializeDecider.destroyed();
		JVMTimeProvider.stop();
		try {
			ConnectionPool.shutdown();
		} catch (Exception e) {
			logger.error("", e);
		}
		if (classLoader != null) {
			classLoader.close();
		}
	}

	public void contextInitialized(ServletContextEvent arg0) {
		XmlObject xmlo = ClientConfigParser.getXmlObject();
		String contextPath = arg0.getServletContext().getContextPath();
		classLoader = ClientPlugClassLoader.newInstance(DawdlerTool.getCurrentPath());
		List<OrderData<ComponentLifeCycle>> lifeCycleList = ComponentLifeCycleProvider.getInstance(contextPath)
				.getComponentLifeCycles();
		List<OrderData<CustomComponentInjector>> customComponentInjectorList = CustomComponentInjectionProvider
				.getInstance(contextPath).getCustomComponentInjectors();

		try {
			for (int i = 0; i < lifeCycleList.size(); i++) {
				OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
				lifeCycle.getData().prepareInit();
			}

			Map<String, Resource> removeDuplicates = new LinkedHashMap<>();

			Set<String> packagePaths = new HashSet<>();
			Node componentNode = xmlo.selectSingleNode("/ns:config/ns:component-scan");
			if (componentNode != null) {
				NamedNodeMap attributes = componentNode.getAttributes();
				String basePackage = XmlTool.getElementAttribute(attributes, "base-package");
				if (basePackage != null) {
					packagePaths = new HashSet<>();
					for (String base : basePackage.split(",")) {
						packagePaths.add(base);
					}
				}
			}

			if (packagePaths != null) {
				for (String packageInClasses : packagePaths) {
					Resource[] resources = DawdlerComponentScanner.getClasses(packageInClasses);
					for (Resource resource : resources) {
						removeDuplicates.putIfAbsent(resource.getURL().toString(), resource);
					}
				}
			}

			Collection<Resource> resources = removeDuplicates.values();
			for (Resource resource : resources) {
				InputStream input = null;
				try {
					input = resource.getInputStream();
					ClassStructure classStructure = ClassStructureParser.parser(input);
					if (classStructure != null) {
						for (OrderData<CustomComponentInjector> data : customComponentInjectorList) {
							CustomComponentInjector customComponentInjector = data.getData();
							inject(resource, classStructure, customComponentInjector, classLoader);
						}
					}
				} finally {
					if (input != null) {
						input.close();
					}
				}
			}

			for (OrderData<CustomComponentInjector> data : customComponentInjectorList) {
				CustomComponentInjector customComponentInjector = data.getData();
				String[] scanLocations = customComponentInjector.scanLocations();
				if (scanLocations != null) {
					for (String scanLocation : scanLocations) {
						Resource[] resourcesArray = DawdlerComponentScanner.getClasses(scanLocation);
						for (Resource resource : resourcesArray) {
							InputStream input = null;
							try {
								input = resource.getInputStream();
								ClassStructure classStructure = ClassStructureParser.parser(input);
								inject(resource, classStructure, customComponentInjector, classLoader);
							} finally {
								if (input != null) {
									input.close();
								}
							}
						}
					}
				}
			}

			for (Node loadItemNode : xmlo.selectNodes("/ns:config/ns:loads-on/ns:item")) {
				String host = loadItemNode.getTextContent();
				if (logger.isDebugEnabled()) {
					logger.debug("starting load.....\t" + host + "\tmodule!");
				}
				NamedNodeMap attributes = loadItemNode.getAttributes();
				sleep = XmlTool.getElementAttribute2Long(attributes, "sleep", sleep);

				String channelGroupId = XmlTool.getElementAttribute(attributes, "channel-group-id");
				LoadCore loadCore = new LoadCore(host, sleep, channelGroupId, classLoader);
				try {
					loadCore.toCheck();
				} catch (Throwable e) {
					logger.error("", e);
				}
				String mode = XmlTool.getElementAttribute(attributes, "mode");
				boolean run = mode != null && (mode.trim().equals("run"));
				if (!run) {
					Thread thread = new Thread(loadCore, host + "LoadThread");
					thread.start();
					threads.put(loadCore, thread);
				}
			}

			for (int i = 0; i < lifeCycleList.size(); i++) {
				try {
					OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
					lifeCycle.getData().init();
				} catch (Throwable e) {
					logger.error("", e);
				}
			}

			WebContextListenerProvider.listenerRun(true, arg0.getServletContext());
			EnumSet<DispatcherType> dispatcherType = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD,
					DispatcherType.ERROR, DispatcherType.INCLUDE);

			try {
				String filterName = "com.anywide.dawdler.clientplug.web.session.DawdlerSessionFilter";
				arg0.getServletContext().addFilter(filterName, Class.forName(filterName).asSubclass(Filter.class))
						.addMappingForUrlPatterns(dispatcherType, true, "/*");
			} catch (ClassNotFoundException e) {
			}
			arg0.getServletContext().addFilter("ViewController", ViewFilter.class)
					.addMappingForUrlPatterns(dispatcherType, true, "/*");

			for (int i = 0; i < lifeCycleList.size(); i++) {
				try {
					OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
					lifeCycle.getData().afterInit();
				} catch (Throwable e) {
					logger.error("", e);
				}
			}
		} catch (Throwable e) {
			logger.error("", e);
			throw new RuntimeException("Web application failed to start !", e);
		}

	}

	private void inject(Resource resource, ClassStructure classStructure,
			CustomComponentInjector customComponentInjector, ClientPlugClassLoader classLoader) throws Throwable {
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
					}
				}
			}
		}
		if (match) {
			Class<?> c = classLoader.defineClass(classStructure.getClassName(), resource);
			if (customComponentInjector.isInject() && !classStructure.isAbstract() && !classStructure.isAnnotation()
					&& !classStructure.isInterface()) {
				Object target = SunReflectionFactoryInstantiator.newInstance(c);
				customComponentInjector.inject(c, target);
			}
		}
	}

}
