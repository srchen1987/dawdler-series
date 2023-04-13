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
package com.anywide.dawdler.clientplug.discovery.consul.resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.anywide.dawdler.client.ConnectionPool;
import com.anywide.dawdler.client.ConnectionPool.Action;
import com.anywide.dawdler.client.conf.ClientConfig;
import com.anywide.dawdler.client.conf.ClientConfig.ServerChannelGroup;
import com.anywide.dawdler.client.conf.ClientConfigParser;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.discovery.consul.ConsulDiscoveryCenter;
import com.anywide.dawdler.core.thread.DefaultThreadFactory;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.HealthChecksForServiceRequest;
import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.health.model.Check.CheckStatus;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ConsulLifeCycle.java
 * @Description consul注册中心初始化与销毁
 * @date 2023年3月5日
 * @email suxuan696@gmail.com
 */
@Order(Integer.MAX_VALUE)
public class ConsulLifeCycle implements ComponentLifeCycle {
	private ConsulDiscoveryCenter consulDiscoveryCenter = null;
	private ExecutorService executor = null;

	@Override
	public void prepareInit() throws Throwable {
		ClientConfig clientConfig = ClientConfigParser.getClientConfig();
		if (clientConfig == null) {
			return;
		}
		List<ServerChannelGroup> sgs = clientConfig.getServerChannelGroups();
		consulDiscoveryCenter = ConsulDiscoveryCenter.getInstance();
		ConsulClient consulClient = consulDiscoveryCenter.getClient();

		executor = Executors.newFixedThreadPool(sgs.size(), new DefaultThreadFactory("consulPullThread#"));
		for (ServerChannelGroup sg : sgs) {
			String gid = sg.getGroupId();
			ConnectionPool.addServerChannelGroup(gid, sg);
			executor.execute(() -> {
				long lastIndex = -1;
				Set<String> oldSet = new HashSet<>();
				while (!consulDiscoveryCenter.getDestroyed().get()) {
					HealthChecksForServiceRequest hr = HealthChecksForServiceRequest.newBuilder()
							.setQueryParams(new QueryParams(30, lastIndex)).build();
					Response<List<Check>> listCheck = consulClient.getHealthChecksForService(gid, hr);
					Set<String> newSet = new HashSet<>();
					long currentLastIndex = listCheck.getConsulIndex();
					for (Check c : listCheck.getValue()) {
						String serviceId = c.getServiceId();
						if (c.getStatus() == CheckStatus.CRITICAL || c.getStatus() == CheckStatus.UNKNOWN) {
							try {
								consulClient.agentServiceDeregister(serviceId);
							} catch (Exception e) {
							}
						}
						if (c.getStatus() == CheckStatus.PASSING) {
							newSet.add(serviceId);
						}
					}

					ConnectionPool cp = ConnectionPool.getConnectionPool(gid);
					for (String k : newSet) {
						if (!oldSet.contains(k)) {
							if (cp != null) {
								cp.doChange(gid, Action.ACTION_ADD, k);
							}
						}
					}

					for (String k : oldSet) {
						if (!newSet.contains(k)) {
							if (cp != null) {
								cp.doChange(gid, Action.ACTION_DEL, k);
							}
						}
					}
					oldSet = newSet;
					lastIndex = currentLastIndex;
				}
			});
		}
	}

	@Override
	public void destroy() throws Throwable {
		if (executor != null) {
			executor.shutdownNow();
		}
		if (consulDiscoveryCenter != null) {
			consulDiscoveryCenter.destroy();
		}
	}

}
