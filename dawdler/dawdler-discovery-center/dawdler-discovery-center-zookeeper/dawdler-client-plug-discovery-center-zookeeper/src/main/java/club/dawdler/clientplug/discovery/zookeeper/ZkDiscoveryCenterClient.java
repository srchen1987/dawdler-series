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
package club.dawdler.clientplug.discovery.zookeeper;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.client.ConnectionPool;
import club.dawdler.client.ConnectionPool.Action;
import club.dawdler.core.discovery.zookeeper.ZkDiscoveryCenter;

/**
 * @author jackson.song
 * @version V1.0
 * 注册中心zk监听器的客户端
 */
public class ZkDiscoveryCenterClient {
	private static final Logger logger = LoggerFactory.getLogger(ZkDiscoveryCenterClient.class);
	private ZkDiscoveryCenter zkDiscoveryCenter = null;
	private CuratorCache curatorCache = null;

	public ZkDiscoveryCenterClient() throws Exception {
		zkDiscoveryCenter = ZkDiscoveryCenter.getInstance();
		initListener();
	}

	private void initListener() {
		curatorCache = CuratorCache.builder(zkDiscoveryCenter.getClient(), zkDiscoveryCenter.getRootPath()).build();
		curatorCache.listenable().addListener((type, oldData, data) -> {
			String gid = null;
			String provider = null;
			Action action = null;
			ChildData handleData = null;
			switch (type) {
			case NODE_CREATED: {
				action = Action.ACTION_ADD;
				handleData = data;
				break;
			}
			case NODE_DELETED: {
				action = Action.ACTION_DEL;
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

	public void destroy() {
		if (curatorCache != null) {
			curatorCache.close();
		}
		if (zkDiscoveryCenter != null) {
			zkDiscoveryCenter.destroy();
		}
	}

}
