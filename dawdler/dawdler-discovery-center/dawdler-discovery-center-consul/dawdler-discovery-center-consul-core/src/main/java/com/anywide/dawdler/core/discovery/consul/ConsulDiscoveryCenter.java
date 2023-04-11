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
package com.anywide.dawdler.core.discovery.consul;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.anywide.dawdler.core.discoverycenter.DiscoveryCenter;
import com.anywide.dawdler.util.PropertiesUtil;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.agent.model.NewService.Check;
import com.ecwid.consul.v1.catalog.CatalogServiceRequest;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.ecwid.consul.v1.health.HealthChecksForServiceRequest;
import com.ecwid.consul.v1.health.model.Check.CheckStatus;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ConsulDiscoveryCenter
 * @Description consul注册中心的实现的实现
 * @date 2023年3月4日
 * @email suxuan696@gmail.com
 */
public class ConsulDiscoveryCenter implements DiscoveryCenter {
	private AtomicBoolean destroyed = new AtomicBoolean();
	private static ConsulDiscoveryCenter consulDiscoveryCenter = null;
	private ConsulClient client;
	private String host;
	private int port;
	private String checkTime = "3s";
	public static final String HEALTH_CHECK_PORT = "health_check_port";
	public static final String HEALTH_CHECK_SCHEME = "health_check_scheme";
	public static final String HEALTH_CHECK_USERNAME = "health_check_username";
	public static final String HEALTH_CHECK_PASSWORD = "health_check_password";
	private CatalogServiceRequest catalogServiceRequest = CatalogServiceRequest.newBuilder().build();
	private HealthChecksForServiceRequest healthChecksForServiceRequest = HealthChecksForServiceRequest.newBuilder()
			.build();

	public static synchronized ConsulDiscoveryCenter getInstance() throws Exception {
		if (consulDiscoveryCenter == null) {
			consulDiscoveryCenter = new ConsulDiscoveryCenter();
		}

		return consulDiscoveryCenter;
	}

	private ConsulDiscoveryCenter() throws Exception {
		Properties ps = PropertiesUtil.loadPropertiesIfNotExistLoadConfigCenter("consul");
		this.host = ps.getProperty("host");
		this.port = PropertiesUtil.getIfNullReturnDefaultValueInt("port", 8500, ps);
		String checkTime = ps.getProperty("checkTime");
		if (checkTime != null) {
			this.checkTime = checkTime;
		}
		init();
	}

	@Override
	public void init() {
		client = new ConsulClient(host, port);
	}

	@Override
	public void destroy() {
		if (destroyed.compareAndSet(false, true)) {
		}
	}

	@Override
	public List<String> getServiceList(String path) throws Exception {
		Response<List<com.ecwid.consul.v1.health.model.Check>> response = client.getHealthChecksForService(path,
				healthChecksForServiceRequest);
		List<String> serviceList = new ArrayList<>();
		response.getValue().forEach((c) -> {
			if (c.getStatus() == CheckStatus.PASSING) {
				serviceList.add(c.getServiceId());
			}
		});
		return serviceList;
	}

	@Override
	public boolean addProvider(String path, String value, Map<String, Object> attributes) throws Exception {
		String[] values = value.split(":");
		NewService service = new NewService();
		String ipAddress = values[0];
		int port = Integer.parseInt(values[1]);
		service.setAddress(ipAddress);
		service.setName(path);
		service.setPort(port);
		service.setId(value);
		Check check = new Check();
		check.setHttp(attributes.get(HEALTH_CHECK_SCHEME) + "://" + ipAddress + ":" + attributes.get(HEALTH_CHECK_PORT)
				+ "/status");
		String username = (String) attributes.get(HEALTH_CHECK_USERNAME);
		String password = (String) attributes.get(HEALTH_CHECK_PASSWORD);
		if (username != null && password != null) {
			Map<String, List<String>> header = new HashMap<>();
			List<String> auth = new ArrayList<>();
			auth.add(getAuth(username, password));
			header.put("Authorization", auth);
			check.setHeader(header);
		}
		check.setInterval(checkTime);
		check.setStatus("warning");
		service.setCheck(check);
		client.agentServiceRegister(service);
		return true;
	}

	@Override
	public boolean deleteProvider(String path, String value) throws Exception {
		client.agentServiceDeregister(value);
		return true;
	}

	@Override
	public boolean isExist(String path, String value) throws Exception {
		Response<List<CatalogService>> response = client.getCatalogService(path, catalogServiceRequest);
		for (CatalogService service : response.getValue()) {
			if (service.getServiceId().equals(value)) {
				return true;
			}
		}
		return false;
	}

	public String state() throws Exception {
		return "ok";
	}

	public AtomicBoolean getDestroyed() {
		return destroyed;
	}

	public String getRootPath() {
		return ROOT_PATH;
	}

	public String getAuth(String username, String password) {
		return "Basic " + Base64.getUrlEncoder().encodeToString((username + ":" + password).getBytes());
	}

	public ConsulClient getClient() {
		return client;
	}

}
