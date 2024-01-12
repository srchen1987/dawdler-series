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

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.anywide.dawdler.core.discoverycenter.DiscoveryCenter;
import com.anywide.dawdler.util.PropertiesUtil;
import com.ecwid.consul.transport.TLSConfig;
import com.ecwid.consul.transport.TLSConfig.KeyStoreInstanceType;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.ConsulRawClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.agent.model.NewService.Check;
import com.ecwid.consul.v1.agent.model.Self.Config;
import com.ecwid.consul.v1.catalog.CatalogServiceRequest;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.ecwid.consul.v1.health.HealthChecksForServiceRequest;
import com.ecwid.consul.v1.health.model.Check.CheckStatus;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ConsulDiscoveryCenter.java
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
	private String checkTime = "90s";
	public static final String HEALTH_CHECK_PORT = "health_check_port";
	public static final String HEALTH_CHECK_SCHEME = "health_check_scheme";
	public static final String HEALTH_CHECK_USERNAME = "health_check_username";
	public static final String HEALTH_CHECK_PASSWORD = "health_check_password";
	private ConsulRawClient consulRawClient;
	private TLSConfig config;

	private String healthCheckType = HealthCheckTypes.TCP.name;

	public static enum HealthCheckTypes {
		TCP("tcp"), HTTP("http");

		private String name;

		private HealthCheckTypes(String name) {
			this.name = name;
		}
	}

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
		String healthCheckType = ps.getProperty("healthCheckType");
		if (healthCheckType != null && (healthCheckType.equals(HealthCheckTypes.TCP.name)
				|| healthCheckType.equals(HealthCheckTypes.HTTP.name))) {
			this.healthCheckType = healthCheckType;
		}
		this.port = PropertiesUtil.getIfNullReturnDefaultValueInt("port", 8500, ps);
		String checkTime = ps.getProperty("checkTime");
		if (checkTime != null) {
			this.checkTime = checkTime;
		}
		String keyStoreInstanceType = ps.getProperty("keyStoreInstanceType");
		String certificatePath = ps.getProperty("certificatePath");
		String certificatePassword = ps.getProperty("certificatePassword");
		String keyStorePath = ps.getProperty("keyStorePath");
		String keyStorePassword = ps.getProperty("keyStorePassword");
		if (keyStoreInstanceType != null) {
			config = new TLSConfig(KeyStoreInstanceType.valueOf(keyStoreInstanceType), certificatePath,
					certificatePassword, keyStorePath, keyStorePassword);
		}

		init();
	}

	@Override
	public void init() {
		if (config != null) {
			this.consulRawClient = new ConsulRawClient(host, port, config);
		} else {
			this.consulRawClient = new ConsulRawClient(host, port);
		}

		this.client = new ConsulClient(consulRawClient);
	}

	@Override
	public void destroy() {
		if (destroyed.compareAndSet(false, true)) {
			try {
				Field field = consulRawClient.getClass().getDeclaredField("httpTransport");
				field.setAccessible(true);
				Object httpTransport = field.get(consulRawClient);
				Method method = httpTransport.getClass().getDeclaredMethod("getHttpClient");
				method.setAccessible(true);
				Closeable closeable = (Closeable) method.invoke(httpTransport);
				closeable.close();
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
					| NoSuchMethodException | InvocationTargetException | IOException e) {
			}
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
		service.setId(getServiceId(path, value));
		Check check = new Check();
		if (healthCheckType.equals(HealthCheckTypes.HTTP.name)) {
			check.setHttp(attributes.get(HEALTH_CHECK_SCHEME) + "://" + ipAddress + ":"
					+ attributes.get(HEALTH_CHECK_PORT) + "/status");
			String username = (String) attributes.get(HEALTH_CHECK_USERNAME);
			String password = (String) attributes.get(HEALTH_CHECK_PASSWORD);
			if (username != null && password != null) {
				Map<String, List<String>> header = new HashMap<>();
				List<String> auth = new ArrayList<>();
				auth.add(getAuth(username, password));
				header.put("Authorization", auth);
				check.setHeader(header);
				check.setStatus("passing");
			}
		} else {
			check.setTcp(value);
			check.setStatus("passing");
		}
		check.setInterval(checkTime);
		service.setCheck(check);
		client.agentServiceRegister(service);
		return true;
	}

	@Override
	public boolean deleteProvider(String path, String value) throws Exception {
		if (isExist(path, value)) {
			client.agentServiceDeregister(getServiceId(path, value));
		}
		return true;
	}

	@Override
	public boolean isExist(String path, String value) throws Exception {
		Response<List<CatalogService>> response = client.getCatalogService(path, catalogServiceRequest);
		for (CatalogService service : response.getValue()) {
			if (service.getServiceId().equals(getServiceId(path, value))) {
				return true;
			}
		}
		return false;
	}

	public String info() throws Exception {
		Config config = client.getAgentSelf().getValue().getConfig();
		return config.getNodeName() + "-" + config.getDatacenter() + "-" + config.getVersion();
	}

	public AtomicBoolean getDestroyed() {
		return destroyed;
	}

	public String getRootPath() {
		return ROOT_PATH;
	}

	private String getServiceId(String path, String value) {
		return path + ":" + value;
	}

	public String getAuth(String username, String password) {
		return "Basic " + Base64.getUrlEncoder().encodeToString((username + ":" + password).getBytes());
	}

	public ConsulClient getClient() {
		return client;
	}

	public String getHealthCheckType() {
		return healthCheckType;
	}

}
