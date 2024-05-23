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
package com.anywide.dawdler.serverplug.discovery.zookeeper.resource;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.discovery.zookeeper.ZkDiscoveryCenter;
import com.anywide.dawdler.core.discoverycenter.DiscoveryCenter;
import com.anywide.dawdler.server.plug.discoverycenter.AbstractServerDiscoveryCenterLifeCycle;

/**
 * @author jackson.song
 * @version V1.0
 * Zookeeper注册中心初始化与销毁
 */
@Order(com.anywide.dawdler.core.order.Order.LOWEST_PRECEDENCE)
public class ZookeeperLifeCycle extends AbstractServerDiscoveryCenterLifeCycle {
	@Override
	public void afterInit() throws Throwable {
		addProvider(null);
	}

	@Override
	public DiscoveryCenter getDiscoveryCenter() throws Exception {
		return ZkDiscoveryCenter.getInstance();
	}

}
