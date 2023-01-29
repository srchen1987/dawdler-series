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
package com.anywide.dawdler.core.discovery.zookeeper.health;

import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.ZooKeeper.States;

import com.anywide.dawdler.core.discovery.zookeeper.ZkDiscoveryCenter;
import com.anywide.dawdler.core.health.Health;
import com.anywide.dawdler.core.health.Health.Builder;
import com.anywide.dawdler.core.health.HealthIndicator;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ZkIndicator.java
 * @Description zookeeper 健康指示器
 * @date 2022年10月13日
 * @email suxuan696@gmail.com
 */
public class ZkIndicator implements HealthIndicator {
	private final static int TRYTIME = 3;

	@Override
	public String name() {
		return "zookeeper";
	}

	@Override
	public Health check(Builder builder) throws Exception {
		ZkDiscoveryCenter zkDiscoveryCenter= ZkDiscoveryCenter.getInstance();
		String state = zkDiscoveryCenter.state();
		int tryTime = 0;
		while (!States.CONNECTED.toString().equals(state)) {
			Thread.sleep(10);
			state = zkDiscoveryCenter.state();
			if (tryTime++ > TRYTIME) {
				throw new ConnectionLossException();
			}
		}
		builder.withDetail("zkState", zkDiscoveryCenter.state());
		return builder.build();
	}

}
