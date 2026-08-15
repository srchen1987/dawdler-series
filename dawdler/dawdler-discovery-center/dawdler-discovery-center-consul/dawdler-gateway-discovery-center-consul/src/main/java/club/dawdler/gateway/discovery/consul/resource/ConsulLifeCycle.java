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
package club.dawdler.gateway.discovery.consul.resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.Check.CheckStatus;
import com.ecwid.consul.v1.health.model.HealthService;

import club.dawdler.client.plug.discoverycenter.AbstractServerDiscoveryCenterLifeCycle;
import club.dawdler.clientplug.web.conf.WebConfigParser;
import club.dawdler.clientplug.web.health.HealthCheck;
import club.dawdler.core.annotation.Order;
import club.dawdler.core.discovery.consul.ConsulDiscoveryCenter;
import club.dawdler.core.discovery.consul.ConsulDiscoveryCenter.HealthCheckTypes;
import club.dawdler.core.discoverycenter.DiscoveryCenter;
import club.dawdler.core.thread.DefaultThreadFactory;
import club.dawdler.web.gateway.route.RouteResolver;
import club.dawdler.web.gateway.route.RouteScheme;
import club.dawdler.web.gateway.route.ServiceInstancePool;

/**
 * @author jackson.song
 * @version V1.0
 * consul注册中心初始化与销毁
 */
@Order(club.dawdler.core.order.Order.LOWEST_PRECEDENCE - 1)
public class ConsulLifeCycle extends AbstractServerDiscoveryCenterLifeCycle {
	private ConsulDiscoveryCenter consulDiscoveryCenter = null;
	private ExecutorService executor = null;
	private static final int DEFAULT_WAIT_TIME = 10000;

	@Override
	public void prepareInit() throws Throwable {
		consulDiscoveryCenter = ConsulDiscoveryCenter.getInstance();
		ConsulClient consulClient = consulDiscoveryCenter.getClient();
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
		String[] serviceNames = serviceNameSet.toArray(new String[0]);
		executor = Executors.newFixedThreadPool(serviceNames.length, new DefaultThreadFactory("consulPullThread#"));
		Semaphore semaphore = new Semaphore(serviceNames.length);
		for (String serviceName : serviceNames) {
			ServiceInstancePool.addGroup(serviceName);
			executor.execute(() -> {
				long lastIndex = -1;
				Set<String> oldSet = new HashSet<>();
				while (!consulDiscoveryCenter.getDestroyed().get()) {
					HealthServicesRequest healthServicesRequest = HealthServicesRequest.newBuilder()
							.setQueryParams(new QueryParams(30, lastIndex))
							.setToken(consulDiscoveryCenter.getToken())
							.setPassing(true).build();
					Set<String> newSet = new HashSet<>();
					long currentLastIndex = 0;
					try {
						Response<List<HealthService>> response = consulClient
								.getHealthServices(
										serviceName,
										healthServicesRequest);
						currentLastIndex = response.getConsulIndex();
						response.getValue().forEach((c) -> {
							c.getChecks().forEach((check) -> {
								String serviceId = check.getServiceId();
								if (!serviceId.equals("")) {
									if (check.getStatus() == CheckStatus.CRITICAL
											|| check.getStatus() == CheckStatus.UNKNOWN) {
										try {
											consulClient.agentServiceDeregister(serviceId,
													consulDiscoveryCenter.getToken());
										} catch (Exception e) {
										}
									}
									if (check.getStatus() == CheckStatus.PASSING) {
										newSet.add(serviceId);
									}
								}
							});

						});

						ServiceInstancePool pool = ServiceInstancePool.getGroup(serviceName);
						if (pool != null) {
							for (String k : newSet) {
								if (!oldSet.contains(k)) {
									String serviceAddress = getServiceAddress(k);
									pool.doChange(ServiceInstancePool.Action.ACTION_ADD, serviceAddress);
								}
							}
							for (String k : oldSet) {
								if (!newSet.contains(k)) {
									String serviceAddress = getServiceAddress(k);
									pool.doChange(ServiceInstancePool.Action.ACTION_DEL, serviceAddress);
								}
							}
						}
						oldSet = newSet;
						semaphore.release();
					} catch (Exception e) {
						if (!consulDiscoveryCenter.getDestroyed().get()) {
							try {
								Thread.sleep(DEFAULT_WAIT_TIME);
							} catch (InterruptedException e1) {
								Thread.currentThread().interrupt();
							}
						} else {
							return;
						}
					}
					lastIndex = currentLastIndex;
				}
			});
		}
		semaphore.tryAcquire(serviceNames.length, 5000, TimeUnit.MILLISECONDS);
	}

	private String getServiceAddress(String serviceId) {
		return serviceId.substring(serviceId.indexOf(":") + 1);
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

	@Override
	public void afterInit() throws Throwable {
		if(webApplicationConfig == null) {
			return;
		}
		ConsulDiscoveryCenter discoveryCenter = ConsulDiscoveryCenter.getInstance();
		Map<String, Object> attributes = new HashMap<>();
		if (discoveryCenter.getHealthCheckType().equals(HealthCheckTypes.HTTP.getName())) {
			HealthCheck healthCheck = WebConfigParser.getWebConfig().getHealthCheck();
			if (!healthCheck.isCheck()) {
				throw new java.lang.IllegalArgumentException(
						"use consul to discovery-center must open health-check in web-conf.xml!");
			}
			attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_PORT, webApplicationConfig.getPort());
			attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_SCHEME, webApplicationConfig.getScheme());
			attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_USERNAME, healthCheck.getUsername());
			attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_PASSWORD, healthCheck.getPassword());
			attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_URI, healthCheck.getUri());
		}
		addProvider(attributes);
	}

	@Override
	public DiscoveryCenter getDiscoveryCenter() throws Exception {
		return ConsulDiscoveryCenter.getInstance();
	}

}
