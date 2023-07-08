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

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.anywide.dawdler.client.ConnectionPool;
import com.anywide.dawdler.client.conf.ClientConfigParser;
import com.anywide.dawdler.clientplug.load.classloader.ClientPlugClassLoader;
import com.anywide.dawdler.clientplug.web.filter.ViewFilter;
import com.anywide.dawdler.clientplug.web.listener.WebContextListenerProvider;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycleProvider;
import com.anywide.dawdler.core.order.OrderData;
import com.anywide.dawdler.core.serializer.SerializeDecider;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.JVMTimeProvider;
import com.anywide.dawdler.util.XmlObject;
import com.anywide.dawdler.util.XmlTool;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

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
	private static long sleep = 600000;
	private final Map<LoadCore, Thread> threads = new ConcurrentHashMap<LoadCore, Thread>();

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
	}

	public void contextInitialized(ServletContextEvent arg0) {
		List<OrderData<ComponentLifeCycle>> lifeCycleList = ComponentLifeCycleProvider
				.getInstance(arg0.getServletContext().getContextPath()).getComponentLifeCycles();
		for (int i = 0; i < lifeCycleList.size(); i++) {
			try {
				OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
				lifeCycle.getData().prepareInit();
			} catch (Throwable e) {
				logger.error("", e);
			}
		}

		ClientPlugClassLoader classLoader = ClientPlugClassLoader.newInstance(DawdlerTool.getCurrentPath());
		XmlObject xmlo = ClientConfigParser.getXmlObject();
		try {
			for (Node node : xmlo.selectNodes("/config/loads-on/item")) {
				String host = node.getTextContent();
				if (logger.isDebugEnabled()) {
					logger.debug("starting load.....\t" + host + "\tmodule!");
				}
				NamedNodeMap attributes = node.getAttributes();
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
		} catch (Exception e) {
			logger.error("", e);
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
		arg0.getServletContext().addFilter("ViewController", ViewFilter.class).addMappingForUrlPatterns(dispatcherType,
				true, "/*");

		for (int i = 0; i < lifeCycleList.size(); i++) {
			try {
				OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
				lifeCycle.getData().afterInit();
			} catch (Throwable e) {
				logger.error("", e);
			}
		}

	}

//	private void loadConfModuleAndExecuteStaticMethod(String methodName) {
//		try {
//			Class<?> configInitClass = Class.forName("com.anywide.dawdler.conf.client.init.ClientConfigInit");
//			Method method = configInitClass.getMethod(methodName);
//			method.invoke(null);
//		} catch (Exception e) {
//			// ignore
//		}
//	}
}
