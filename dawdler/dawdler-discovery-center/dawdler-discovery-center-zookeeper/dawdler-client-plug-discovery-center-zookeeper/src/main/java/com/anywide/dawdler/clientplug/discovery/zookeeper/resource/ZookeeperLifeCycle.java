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
package com.anywide.dawdler.clientplug.discovery.zookeeper.resource;

import java.util.List;

import com.anywide.dawdler.client.ConnectionPool;
import com.anywide.dawdler.client.conf.ClientConfig;
import com.anywide.dawdler.client.conf.ClientConfig.ServerChannelGroup;
import com.anywide.dawdler.client.conf.ClientConfigParser;
import com.anywide.dawdler.clientplug.discovery.zookeeper.ZkDiscoveryCenterClient;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ZookeeperLifeCycle.java
 * @Description Zookeeper注册中心初始化与销毁
 * @date 2022年9月30日
 * @email suxuan696@gmail.com
 */
@Order(Integer.MAX_VALUE)
public class ZookeeperLifeCycle implements ComponentLifeCycle {
	private ZkDiscoveryCenterClient discoveryCenter = null;

	@Override
	public void prepareInit() throws Throwable {
		ClientConfig clientConfig = ClientConfigParser.getClientConfig();
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
