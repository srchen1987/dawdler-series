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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.anywide.dawdler.client.conf.ClientConfigParser;
import com.anywide.dawdler.clientplug.web.classloader.ClientPlugClassLoader;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.XmlObject;
import com.anywide.dawdler.util.XmlTool;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * @author jackson.song
 * @version V1.0
 * @Title LoadListener.java
 * @Description 加载模版类的监听器
 * @date 2007年5月8日
 * @email suxuan696@gmail.com
 */
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
	}

	public void contextInitialized(ServletContextEvent arg0) {
		XmlObject xmlo = ClientConfigParser.getXmlObject();
		classLoader = ClientPlugClassLoader.newInstance(DawdlerTool.getCurrentPath());
		try {
			for (Node loadItemNode : xmlo.selectNodes("/ns:config/ns:loads-on/ns:item")) {
				String host = loadItemNode.getTextContent();
				if (logger.isDebugEnabled()) {
					logger.debug("starting load.....\t" + host + "\tmodule!");
				}
				NamedNodeMap attributes = loadItemNode.getAttributes();
				sleep = XmlTool.getElementAttribute2Long(attributes, "sleep", sleep);

				String channelGroupId = XmlTool.getElementAttribute(attributes, "channel-group-id");
				LoadCore loadCore = new LoadCore(host, sleep, channelGroupId, classLoader);
				loadCore.toCheck();
				String mode = XmlTool.getElementAttribute(attributes, "mode");
				boolean run = mode != null && (mode.trim().equals("run"));
				if (!run) {
					Thread thread = new Thread(loadCore, host + "LoadThread");
					thread.start();
					threads.put(loadCore, thread);
				}
			}

		} catch (Throwable e) {
			logger.error("", e);
			throw new RuntimeException("Web application load module failed to start !", e);
		}

	}

}
