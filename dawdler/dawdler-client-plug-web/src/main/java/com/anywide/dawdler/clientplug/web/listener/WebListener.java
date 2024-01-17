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
package com.anywide.dawdler.clientplug.web.listener;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.anywide.dawdler.client.ConnectionPool;
import com.anywide.dawdler.client.conf.ClientConfigParser;
import com.anywide.dawdler.clientplug.web.classloader.ClientPlugClassLoader;
import com.anywide.dawdler.core.component.injector.CustomComponentInjectionProvider;
import com.anywide.dawdler.core.component.injector.CustomComponentInjector;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycleProvider;
import com.anywide.dawdler.core.order.OrderData;
import com.anywide.dawdler.core.scan.DawdlerComponentScanner;
import com.anywide.dawdler.core.scan.component.reader.ClassStructureParser;
import com.anywide.dawdler.core.scan.component.reader.ClassStructureParser.ClassStructure;
import com.anywide.dawdler.core.serializer.SerializeDecider;
import com.anywide.dawdler.core.shutdown.ContainerGracefulShutdown;
import com.anywide.dawdler.core.shutdown.ContainerShutdownProvider;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.XmlObject;
import com.anywide.dawdler.util.XmlTool;
import com.anywide.dawdler.util.spring.antpath.Resource;

/**
 * @author jackson.song
 * @version V1.0
 * @Title WebListener.java
 * @Description 用于替换原有的LoadListener
 * @date 2007年5月8日
 * @email suxuan696@gmail.com
 */
public class WebListener implements ServletContextListener {
	private static final Logger logger = LoggerFactory.getLogger(WebListener.class);
	private ClientPlugClassLoader classLoader;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		XmlObject xmlo = ClientConfigParser.getXmlObject();
		String contextPath = sce.getServletContext().getContextPath();
		classLoader = ClientPlugClassLoader.newInstance(DawdlerTool.getCurrentPath());
		List<OrderData<ComponentLifeCycle>> lifeCycleList = ComponentLifeCycleProvider.getInstance(contextPath)
				.getComponentLifeCycles();
		List<OrderData<CustomComponentInjector>> customComponentInjectorList = CustomComponentInjectionProvider
				.getInstance(contextPath).getCustomComponentInjectors();

		try {
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
							classLoader.inject(resource, classStructure, customComponentInjector);
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
								classLoader.inject(resource, classStructure, customComponentInjector);
							} finally {
								if (input != null) {
									input.close();
								}
							}
						}
					}
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
			WebContextListenerProvider.listenerRun(true, sce.getServletContext());
			for (int i = 0; i < lifeCycleList.size(); i++) {
				OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
				lifeCycle.getData().afterInit();
			}
		} catch (Throwable e) {
			logger.error("", e);
			throw new RuntimeException("Web application failed to start !", e);
		}

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		List<OrderData<ComponentLifeCycle>> lifeCycleList = ComponentLifeCycleProvider
				.getInstance(sce.getServletContext().getContextPath()).getComponentLifeCycles();
		for (int i = lifeCycleList.size() - 1; i >= 0; i--) {
			try {
				OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
				lifeCycle.getData().prepareDestroy();
			} catch (Throwable e) {
				logger.error("", e);
			}
		}
		WebContextListenerProvider.listenerRun(false, sce.getServletContext());
		List<OrderData<ContainerGracefulShutdown>> containerShutdownList = ContainerShutdownProvider.getInstance()
				.getContainerShutdownList();
		Boolean gracefullShutdown = (Boolean) sce.getServletContext()
				.getAttribute(ContainerGracefulShutdown.class.getName());
		if (gracefullShutdown != null && gracefullShutdown) {
			CountDownLatch countDownLatch = new CountDownLatch(containerShutdownList.size());
			for (OrderData<ContainerGracefulShutdown> data : containerShutdownList) {
				try {
					data.getData().shutdown(() -> {
						countDownLatch.countDown();
					});
				} catch (Exception e) {
					logger.error("", e);
				}
			}
			try {
				countDownLatch.await(120, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		} else {
			for (OrderData<ContainerGracefulShutdown> data : containerShutdownList) {
				try {
					data.getData().shutdown(() -> {
					});
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
		for (int i = lifeCycleList.size() - 1; i >= 0; i--) {
			try {
				OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
				lifeCycle.getData().destroy();
			} catch (Throwable e) {
				logger.error("", e);
			}
		}
		for (int i = lifeCycleList.size() - 1; i >= 0; i--) {
			try {
				OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
				lifeCycle.getData().afterDestroy();
			} catch (Throwable e) {
				logger.error("", e);
			}
		}
		SerializeDecider.destroyed();
		try {
			ConnectionPool.shutdown();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

}
