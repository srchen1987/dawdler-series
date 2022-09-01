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
package com.anywide.dawdler.client.discoverycenter;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.client.ConnectionPool;
import com.anywide.dawdler.core.discoverycenter.ZkDiscoveryCenter;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ZkDiscoveryCenterClient.java
 * @Description 配置中心zk的实现
 * @date 2018年8月13日
 * @email suxuan696@gmail.com
 */
public class ZkDiscoveryCenterClient extends ZkDiscoveryCenter {
	private static final Logger logger = LoggerFactory.getLogger(ZkDiscoveryCenterClient.class);

	public ZkDiscoveryCenterClient(String connectString, String user, String password) {
		super(connectString, user, password);
		try {
			initListener();
		} catch (Exception e) {
			logger.error("init listener failed", e);
		}
	}

	private void initListener() {
		curatorCache = CuratorCache.builder(client, ROOT_PATH).build();

		curatorCache.listenable().addListener((type, oldData, data) -> {
			String gid = null;
			String provider = null;
			String action = null;
			ChildData handleData = null;
			switch (type) {
			case NODE_CREATED: {
				action = "add";
				handleData = data;
				break;
			}
			case NODE_DELETED: {
				action = "del";
				handleData = oldData;
				break;
			}
			default:
				break;
			}
			if (handleData != null) {
				String[] gidAndProvider = handleData.getPath().split("/");
				if (gidAndProvider.length == 4) {
					gid = gidAndProvider[2];
					provider = gidAndProvider[3];
				}
				if (gid == null) {
					return;
				}
				logger.info(gid + " " + action + " " + provider);
				ConnectionPool cp = ConnectionPool.getConnectionPool(gid);
				if (cp != null) {
					cp.doChange(gid, action, provider);
				}
			}
		});
		curatorCache.start();
	}

}
