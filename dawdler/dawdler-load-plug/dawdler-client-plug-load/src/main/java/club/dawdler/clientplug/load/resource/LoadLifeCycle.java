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
package club.dawdler.clientplug.load.resource;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import club.dawdler.client.ConnectionPool;
import club.dawdler.client.conf.ClientConfigParser;
import club.dawdler.clientplug.load.LoadCore;
import club.dawdler.clientplug.web.classloader.ClientPlugClassLoader;
import club.dawdler.core.annotation.Order;
import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.core.order.OrderData;
import club.dawdler.util.DawdlerTool;
import club.dawdler.util.XmlObject;

/**
 * @author jackson.song
 * @version V1.0
 * 远程加载初始化与销毁 代替LoadListener
 */
@Order(OrderData.LOWEST_PRECEDENCE)
public class LoadLifeCycle implements ComponentLifeCycle {
	private static final Logger logger = LoggerFactory.getLogger(LoadLifeCycle.class);
	private long sleep = 600000;
	private static final int TRY_TIME=16;
	private final Map<LoadCore, Thread> threads = new ConcurrentHashMap<>();
	private ClientPlugClassLoader classLoader;

	@Override
	public void afterDestroy() throws Throwable {
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

	@Override
	public void prepareInit() throws Throwable {
		XmlObject xmlo = ClientConfigParser.getXmlObject();
		classLoader = ClientPlugClassLoader.newInstance(DawdlerTool.getCurrentPath());
		try {
			List<Node> loadItemNodeList = xmlo.selectNodes("/ns:config/ns:loads-on/ns:item");
			if(loadItemNodeList == null || loadItemNodeList.isEmpty()) {
				logger.warn("no load module configured !");
				return;
			}
			for (Node loadItemNode : loadItemNodeList) {
				String host = loadItemNode.getTextContent();
				if (logger.isDebugEnabled()) {
					logger.debug("starting load.....\t" + host + "\tmodule!");
				}
				NamedNodeMap attributes = loadItemNode.getAttributes();
				sleep = XmlObject.getElementAttribute2Long(attributes, "sleep", sleep);

				String channelGroupId = XmlObject.getElementAttribute(attributes, "channel-group-id");
				LoadCore loadCore = new LoadCore(host, sleep, channelGroupId, classLoader);
				int tryCount = 0;
				while (!ConnectionPool.getConnectionPool(channelGroupId).hasConnection() && tryCount++ < TRY_TIME) {
					Thread.sleep(200);
				}
				loadCore.toCheck();
				String mode = XmlObject.getElementAttribute(attributes, "mode");
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
