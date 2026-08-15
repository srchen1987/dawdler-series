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
package club.dawdler.gateway.discovery.zookeeper.resource;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;

import club.dawdler.client.plug.discoverycenter.AbstractServerDiscoveryCenterLifeCycle;
import club.dawdler.core.annotation.Order;
import club.dawdler.core.discovery.zookeeper.ZkDiscoveryCenter;
import club.dawdler.core.discoverycenter.DiscoveryCenter;
import club.dawdler.web.gateway.route.RouteResolver;
import club.dawdler.web.gateway.route.RouteScheme;
import club.dawdler.web.gateway.route.ServiceInstancePool;

/**
 * @author jackson.song
 * @version V1.0
 * zookeeper注册中心初始化与销毁
 */
@Order(club.dawdler.core.order.Order.LOWEST_PRECEDENCE - 1)
public class ZookeeperLifeCycle extends AbstractServerDiscoveryCenterLifeCycle {
	private ZkDiscoveryCenter zkDiscoveryCenter = null;
	private CuratorCache curatorCache = null;

	@Override
	public void prepareInit() throws Throwable {
		zkDiscoveryCenter = ZkDiscoveryCenter.getInstance();
		List<RouteResolver.CachedRoute> cachedRoutes = RouteResolver.getCachedRoutes();
		Set<String> serviceNameSet = new LinkedHashSet<>();
		if (cachedRoutes != null) {
			for (RouteResolver.CachedRoute cachedRoute : cachedRoutes) {
				RouteScheme scheme = RouteScheme.fromUri(cachedRoute.route.getUri());
				if (scheme != null && scheme.isLb()) {
					String serviceName = RouteScheme.extractServiceName(cachedRoute.route.getUri());
					if (serviceName != null && !serviceName.isEmpty()) {
						serviceNameSet.add(serviceName);
					}
				}
			}
		}
		if (serviceNameSet.isEmpty()) {
			return;
		}
		for (String serviceName : serviceNameSet) {
			ServiceInstancePool.addGroup(serviceName);
			ServiceInstancePool pool = ServiceInstancePool.getGroup(serviceName);
			if (pool == null) {
				continue;
			}
			try {
				List<String> addresses = zkDiscoveryCenter.getServiceList(serviceName);
				for (String address : addresses) {
					pool.doChange(ServiceInstancePool.Action.ACTION_ADD, address);
				}
			} catch (Exception e) {
			}
		}
		curatorCache = CuratorCache.builder(zkDiscoveryCenter.getClient(), zkDiscoveryCenter.getRootPath()).build();
		curatorCache.listenable().addListener((type, oldData, data) -> {
			ChildData handleData = null;
			ServiceInstancePool.Action action = null;
			switch (type) {
			case NODE_CREATED: {
				action = ServiceInstancePool.Action.ACTION_ADD;
				handleData = data;
				break;
			}
			case NODE_DELETED: {
				action = ServiceInstancePool.Action.ACTION_DEL;
				handleData = oldData;
				break;
			}
			default:
				break;
			}
			if (handleData == null) {
				return;
			}
			String[] pathParts = handleData.getPath().split("/");
			if (pathParts.length != 4) {
				return;
			}
			String serviceName = pathParts[2];
			String address = pathParts[3];
			if (!serviceNameSet.contains(serviceName)) {
				return;
			}
			if (action == ServiceInstancePool.Action.ACTION_ADD) {
				zkDiscoveryCenter.addToServiceListCache(serviceName, address);
			} else {
				zkDiscoveryCenter.removeFromServiceListCache(serviceName, address);
			}
			ServiceInstancePool pool = ServiceInstancePool.getGroup(serviceName);
			if (pool != null) {
				pool.doChange(action, address);
			}
		});
		curatorCache.start();
	}

	@Override
	public void destroy() throws Throwable {
		if (curatorCache != null) {
			curatorCache.close();
		}
		if (zkDiscoveryCenter != null) {
			zkDiscoveryCenter.destroy();
		}
	}

	@Override
	public void afterInit() throws Throwable {
		addProvider(null);
	}

	@Override
	public DiscoveryCenter getDiscoveryCenter() throws Exception {
		return ZkDiscoveryCenter.getInstance();
	}

}
