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
package com.anywide.dawdler.serverplug.service.listener;

import java.util.concurrent.TimeUnit;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.discoverycenter.DiscoveryCenter;
import com.anywide.dawdler.core.discoverycenter.ZkDiscoveryCenter;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.listener.DawdlerServiceListener;
import com.anywide.dawdler.util.HashedWheelTimer;
import com.anywide.dawdler.util.Timeout;
import com.anywide.dawdler.util.TimerTask;

/**
 * @author jackson.song
 * @version V1.0
 * @Title StartupProviderListener.java
 * @Description 监听器 启动后向注册中心注册服务
 * @date 2016年2月8日
 * @email suxuan696@gmail.com
 */
@Order(Integer.MAX_VALUE)
public class StartupProviderListener implements DawdlerServiceListener {
	private static final Logger logger = LoggerFactory.getLogger(StartupProviderListener.class);
	private DiscoveryCenter discoveryCenter;
	private Timeout timeout;
	private long checkTime = 5000;
	private HashedWheelTimer hashedWheelTimer;

	@Override
	public void contextInitialized(DawdlerContext dawdlerContext) throws Exception {
		Element root = dawdlerContext.getServicesConfig().getRoot();
		Element zkHost = (Element) root.selectSingleNode("zk-host");
		if (zkHost != null) {
			String username = zkHost.attributeValue("username");
			String password = zkHost.attributeValue("password");
			String url = zkHost.getTextTrim();
			String channelGroup = dawdlerContext.getDeployName();
			if ("".equals(url)) {
				throw new NullPointerException("zk-host can't be null!");
			}
			discoveryCenter = new ZkDiscoveryCenter(url, username, password);
			discoveryCenter.init();
			String path = channelGroup;
			String value = dawdlerContext.getHost() + ":" + dawdlerContext.getPort();
			if (discoveryCenter.addProvider(path, value)) {
				logger.info("add service {} on {}", channelGroup, value);
			}

			dawdlerContext.setAttribute(DiscoveryCenter.class, discoveryCenter);
			hashedWheelTimer = new HashedWheelTimer();
			timeout = hashedWheelTimer.newTimeout(new ProviderTimeoutTask(path, value), checkTime,
					TimeUnit.MILLISECONDS);

		} else {
			logger.error("not find discoveryServer config!");
		}

	}

	@Override
	public void contextDestroyed(DawdlerContext dawdlerContext) throws Exception {
		if (timeout != null) {
			timeout.cancel();
		}
		if (discoveryCenter != null) {
			discoveryCenter.destroy();
		}
		if (hashedWheelTimer != null) {
			hashedWheelTimer.stop();
		}
	}

	public class ProviderTimeoutTask implements TimerTask {
		private String path;
		private String value;

		public ProviderTimeoutTask(String path, String value) {
			this.path = path;
			this.value = value;
		}

		@Override
		public void run(Timeout timeout) throws Exception {
			try {
				if (!discoveryCenter.isExist(path)) {
					discoveryCenter.addProvider(path, value);
				}
			} catch (Exception e) {
				logger.error("", e);
			}
			StartupProviderListener.this.timeout = timeout.timer().newTimeout(this, checkTime, TimeUnit.MILLISECONDS);
		}

	}
}
