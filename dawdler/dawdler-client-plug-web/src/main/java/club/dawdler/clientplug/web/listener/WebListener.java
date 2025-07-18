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
package club.dawdler.clientplug.web.listener;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.clientplug.web.classloader.DawdlerWebDeployClassLoader;
import club.dawdler.clientplug.web.conf.WebConfig;
import club.dawdler.clientplug.web.conf.WebConfigParser;
import club.dawdler.core.component.injector.CustomComponentInjectionProvider;
import club.dawdler.core.component.injector.CustomComponentInjector;
import club.dawdler.core.component.injector.CustomComponentOperator;
import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.core.component.resource.ComponentLifeCycleProvider;
import club.dawdler.core.loader.DeployClassLoader;
import club.dawdler.core.order.OrderData;
import club.dawdler.core.serializer.SerializeDecider;
import club.dawdler.core.shutdown.ContainerGracefulShutdown;
import club.dawdler.core.shutdown.ContainerShutdownProvider;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * @author jackson.song
 * @version V1.0
 * 用于替换原有的LoadListener
 */
public class WebListener implements ServletContextListener {
	private static final Logger logger = LoggerFactory.getLogger(WebListener.class);
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		DeployClassLoader classLoader = null;
		WebConfig webConfig = WebConfigParser.getWebConfig();
		String contextPath = sce.getServletContext().getContextPath();
		ClassLoader tcl = Thread.currentThread().getContextClassLoader();
		if (!(tcl instanceof DeployClassLoader)) {
			try {
				classLoader = new DawdlerWebDeployClassLoader(tcl);
			} catch (Exception e) {
				logger.error("", e);
				throw new RuntimeException("Web application failed to start !", e);
			}
			finally{
				try {
					if(classLoader != null){
						classLoader.close();
					}
				} catch (IOException e) {
				}
			}
		} else {
			classLoader = (DeployClassLoader) tcl;
		}
		List<OrderData<ComponentLifeCycle>> lifeCycleList = ComponentLifeCycleProvider.getInstance(contextPath)
				.getComponentLifeCycles();
		List<OrderData<CustomComponentInjector>> customComponentInjectorList = CustomComponentInjectionProvider
				.getDefaultInstance().getCustomComponentInjectors();
		try {
			for (int i = 0; i < lifeCycleList.size(); i++) {
				OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
				lifeCycle.getData().prepareInit();
			}
			if (webConfig != null) {
				CustomComponentOperator.scanAndInject(classLoader, customComponentInjectorList,
					webConfig.getPackagePaths());
			}
			
			for (int i = 0; i < lifeCycleList.size(); i++) {
				OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
				lifeCycle.getData().init();
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
		Boolean gracefulShutdown = (Boolean) sce.getServletContext()
				.getAttribute(ContainerGracefulShutdown.class.getName());
		if (gracefulShutdown != null && gracefulShutdown) {
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
	}

}
