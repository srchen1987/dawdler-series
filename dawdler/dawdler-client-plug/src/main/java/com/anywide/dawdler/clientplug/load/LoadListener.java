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

import javax.servlet.DispatcherType;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * @author jackson.song
 * @version V1.0
 * @Title LoadListener.java
 * @Description 加载模版类的监听器
 * @date 2007年5月08日
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
				if (logger.isDebugEnabled())
					logger.debug("stop \t" + entry.getValue().getName() + "\tload");
				entry.getValue().interrupt();
			}
		}
		WebContextListenerProvider.listenerRun(false, arg0.getServletContext());
		List<OrderData<ComponentLifeCycle>> lifeCycleList = ComponentLifeCycleProvider.getInstance(arg0.getServletContext().getServletContextName()).getComponentLifeCycles();
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
		List<OrderData<ComponentLifeCycle>> lifeCycleList = ComponentLifeCycleProvider.getInstance(arg0.getServletContext().getServletContextName()).getComponentLifeCycles();
		for (int i = 0; i < lifeCycleList.size(); i++) {
			try {
				OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
				lifeCycle.getData().prepareInit();
			} catch (Throwable e) {
				logger.error("", e);
			}
		}
		
		ClientPlugClassLoader classLoader = ClientPlugClassLoader.newInstance(DawdlerTool.getcurrentPath());
		XmlObject xml = ClientConfigParser.getXmlObject();
		for (Object o : xml.selectNodes("/config/loads-on/item")) {
			Element ele = (Element) o;
			String host = ele.getText();
			if (logger.isDebugEnabled())
				logger.debug("starting load.....\t" + host + "\tmodule!");
			if (ele.attribute("sleep") != null) {
				try {
					sleep = Long.parseLong(ele.attributeValue("sleep"));
				} catch (Exception e) {
				}
			}
			String channelGroupId = ele.attributeValue("channel-group-id");
			LoadCore loadCore = new LoadCore(host, sleep, channelGroupId, classLoader);
			try {
				loadCore.initWebComponent();
			} catch (Throwable e) {
				logger.error("", e);
			}
			try {
				loadCore.toCheck();
			} catch (Throwable e) {
				logger.error("", e);
			}
			String mode = ele.attributeValue("mode");
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
			Class sessionClass = Class.forName("com.anywide.dawdler.clientplug.web.session.DawdlerSessionFilter");
			arg0.getServletContext().addFilter(sessionClass.getSimpleName(), sessionClass)
					.addMappingForUrlPatterns(dispatcherType, true, "/*");
		} catch (ClassNotFoundException e) {
		}
		arg0.getServletContext().addFilter("ViewController", ViewFilter.class).addMappingForUrlPatterns(dispatcherType,
				true, "/*");
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
