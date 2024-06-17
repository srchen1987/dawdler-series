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
package com.anywide.dawdler.server.plug.discoverycenter;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.discoverycenter.DiscoveryCenter;
import com.anywide.dawdler.core.health.Status;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.plug.discoverycenter.AbstractServerDiscoveryCenterLifeCycle.ProviderTimeoutTask;
import com.anywide.dawdler.util.HashedWheelTimer;
import com.anywide.dawdler.util.Timeout;
import com.anywide.dawdler.util.TimerTask;

/**
 * @author jackson.song
 * @version V1.0
 * 服务端注册中心抽象类
 */
public abstract class AbstractServerDiscoveryCenterLifeCycle implements ComponentLifeCycle {
	private static final Logger logger = LoggerFactory.getLogger(AbstractServerDiscoveryCenterLifeCycle.class);
	private Timeout timeout;
	private long checkTime = 5000;
	private HashedWheelTimer hashedWheelTimer;

	public void addProvider(Map<String, Object> attributes) throws Exception {
		DiscoveryCenter discoveryCenter = getDiscoveryCenter();
		DawdlerContext dawdlerContext = DawdlerContext.getDawdlerContext();
		String channelGroup = dawdlerContext.getDeployName();
		String path = channelGroup;
		String value = dawdlerContext.getHost() + ":" + dawdlerContext.getPort();
		new Thread(() -> {
			try {
				dawdlerContext.waitForStart();
			} catch (InterruptedException e) {
			}
			if (dawdlerContext.getServiceStatus().equals(Status.UP)) {
				try {
					if (discoveryCenter.addProvider(path, value, attributes)) {
						logger.info("add service {} on {}", channelGroup, value);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				hashedWheelTimer = new HashedWheelTimer();
				timeout = hashedWheelTimer.newTimeout(new ProviderTimeoutTask(path, value), checkTime,
						TimeUnit.MILLISECONDS);
			}
		}, "addProviderThread").start();
	}

	@Override
	public void prepareDestroy() throws Throwable {
		DawdlerContext dawdlerContext = DawdlerContext.getDawdlerContext();
		String channelGroup = dawdlerContext.getDeployName();
		String path = channelGroup;
		String value = dawdlerContext.getHost() + ":" + dawdlerContext.getPort();
		if (timeout != null) {
			timeout.cancel();
		}
		if (hashedWheelTimer != null) {
			hashedWheelTimer.stop();
		}
		DiscoveryCenter discoveryCenter = getDiscoveryCenter();
		if (discoveryCenter != null) {
			discoveryCenter.deleteProvider(path, value);
			discoveryCenter.destroy();
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
			if (timeout.isCancelled()) {
				return;
			}
			try {
				DiscoveryCenter discoveryCenter = getDiscoveryCenter();
				if (!discoveryCenter.isExist(path, value)) {
					discoveryCenter.addProvider(path, value, null);
				}
			} catch (Exception e) {
				logger.error("", e);
			}
			if(hashedWheelTimer.getWorkerState().get() == HashedWheelTimer.WORKER_STATE_STARTED){
				AbstractServerDiscoveryCenterLifeCycle.this.timeout = timeout.timer().newTimeout(this, checkTime,
					TimeUnit.MILLISECONDS);
			}
		}

	}

	public abstract DiscoveryCenter getDiscoveryCenter() throws Exception;
}
