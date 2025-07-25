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
package club.dawdler.clientplug.discovery.zookeeper.resource;

import java.util.List;

import club.dawdler.client.ConnectionPool;
import club.dawdler.client.conf.ClientConfig;
import club.dawdler.client.conf.ClientConfig.ServerChannelGroup;
import club.dawdler.client.conf.ClientConfigParser;
import club.dawdler.clientplug.discovery.zookeeper.ZkDiscoveryCenterClient;
import club.dawdler.core.annotation.Order;
import club.dawdler.core.component.resource.ComponentLifeCycle;

/**
 * @author jackson.song
 * @version V1.0
 * Zookeeper注册中心初始化与销毁
 */
@Order(Integer.MAX_VALUE)
public class ZookeeperLifeCycle implements ComponentLifeCycle {
	private ZkDiscoveryCenterClient discoveryCenter = null;

	@Override
	public void prepareInit() throws Throwable {
		ClientConfig clientConfig = ClientConfigParser.getClientConfig();
		if (clientConfig == null) {
			return;
		}
		List<ServerChannelGroup> sgs = clientConfig.getServerChannelGroups();
		for (ServerChannelGroup sg : sgs) {
			String gid = sg.getGroupId();
			ConnectionPool.addServerChannelGroup(gid, sg);
		}
		discoveryCenter = new ZkDiscoveryCenterClient();
	}

	@Override
	public void destroy() throws Throwable {
		if (discoveryCenter != null) {
			discoveryCenter.destroy();
		}
	}

}
