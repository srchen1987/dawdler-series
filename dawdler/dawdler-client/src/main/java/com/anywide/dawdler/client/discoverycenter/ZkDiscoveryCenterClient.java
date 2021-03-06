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
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.client.ConnectionPool;
import com.anywide.dawdler.core.discoverycenter.ZkDiscoveryCenter;

/**
 * 
 * ZkDiscoveryCenterClient
 * 
 * @Description: 配置中心zk的实现 代替 PropertiesCenter.java
 * @author: jackson.song
 * @date: 2018年08月13日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class ZkDiscoveryCenterClient extends ZkDiscoveryCenter {
	private static Logger logger = LoggerFactory.getLogger(ZkDiscoveryCenterClient.class);

	public ZkDiscoveryCenterClient(String connectString, String user, String password) {
		super(connectString, user, password);
		try {
			initListener();
		} catch (Exception e) {
			logger.error("init listener faild", e);
		}
	}

	private void initListener() throws Exception {
		curatorCache = CuratorCache.builder(client, ROOTPATH).build();

		curatorCache.listenable().addListener(new CuratorCacheListener() {
			@Override
			public void event(Type type, ChildData oldData, ChildData data) {
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
					if (gid == null)
						return;

					logger.info(gid + " " + action + " " + provider);
					ConnectionPool cp = ConnectionPool.getConnectionPool(gid);
					if (cp != null)
						cp.doChange(gid, action, provider);
//			}
				}
			}
		});
		curatorCache.start();
//		treeCache.getListenable().addListener(new TreeCacheListener() {//老版本 被CuratorCacheListener 替代
//			@Override
//			public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
//				ChildData data = event.getData();
//				if (data != null) {
//					String[] gidAndProvider = data.getPath().split("/");
//					String gid = null;
//					String provider=null;
//					if(gidAndProvider.length==4) {
//						gid = gidAndProvider[2];
//						provider=gidAndProvider[3];
//					}
//					if(gid==null)return;
//					
//					switch (event.getType()) {
//					case NODE_ADDED: {
//						logger.info(gid+" add " + provider); 
//							ConnectionPool cp = ConnectionPool.getConnectionPool(gid);
//							if(cp != null)
//								cp.doChange(gid,"add",provider);
//						break;
//					}
//					case NODE_REMOVED:{
//						logger.info("remove " + provider);
//						ConnectionPool cp = ConnectionPool.getConnectionPool(gid);
//						if (cp != null) { 
//							cp.doChange(gid,"del",provider);
//						}
//						break;
//					}
//						//以下是之前上个版本的 之后不需要 通过update方式来更新节点了 只保留 add del
////					case NODE_UPDATED: {
////						String gid = data.getPath().replace(ROOTPATH + "/", "");
////						ConnectionPool cp = ConnectionPool.getConnectionPool(gid);
////						if (cp != null) {
////							cp.doChange("update", new String(data.getData()), gid);
////						}
////						break;
////					}
//					default:
//						break;
//					}
//				}
//			}
//		});
		// 开始监听
//		treeCache.start();
	}

}
