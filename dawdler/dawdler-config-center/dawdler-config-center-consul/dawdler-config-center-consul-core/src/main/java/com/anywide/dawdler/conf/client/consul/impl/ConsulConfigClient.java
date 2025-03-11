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
package com.anywide.dawdler.conf.client.consul.impl;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.conf.cache.ConfigDataCache;
import com.anywide.dawdler.conf.cache.ConfigDataCache.ConfigData;
import com.anywide.dawdler.conf.cache.ConfigMappingDataCache;
import com.anywide.dawdler.conf.cache.PathMappingTargetCache;
import com.anywide.dawdler.conf.client.ConfigClient;
import com.anywide.dawdler.core.thread.DefaultThreadFactory;
import com.anywide.dawdler.util.ConfigContentDecryptor;
import com.ecwid.consul.transport.TLSConfig;
import com.ecwid.consul.transport.TLSConfig.KeyStoreInstanceType;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.ConsulRawClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Self.Config;
import com.ecwid.consul.v1.kv.model.GetValue;

/**
 * @author jackson.song
 * @version V1.0
 * ConfigClient接口的consul实现，支持SPI扩展
 */
public class ConsulConfigClient implements ConfigClient {
	private ConsulClient client = null;
	private ConsulRawClient consulRawClient;
	private ExecutorService executor = null;
	private List<String> watchKeys;
	/**
	 * separator 分割符 只在keys的场景有意义，如以下请求 设 目前已有目录 /config/config-uat
	 * /config/config-dev /config/config
	 * http://localhost:8500/v1/kv/config?keys&separator=-&wait=5s&index=2 返回 [
	 * "config/config", "config/config-" ]
	 * 
	 */
	private String separator;
	private String token;
	private int waitTime;
	private AtomicBoolean destroyed = new AtomicBoolean();
	private static Logger logger = LoggerFactory.getLogger(ConsulConfigClient.class);
	private Map<String, Object> conf;
	private static final int DEFAULT_WAIT_TIME = 10000;

	@Override
	@SuppressWarnings("unchecked")
	public void init(Map<String, Object> conf) {
		this.conf = conf;
		watchKeys = (List<String>) conf.get("watch-keys");
		separator = (String) conf.get("separator");
		token = (String) conf.get("token");
		try {
			waitTime = Integer.parseInt(conf.get("wait-time").toString());
		} catch (Exception e) {
			waitTime = DEFAULT_WAIT_TIME;
		}

		initConsulClient();
		if (watchKeys != null) {
			executor = Executors.newFixedThreadPool(watchKeys.size(), new DefaultThreadFactory("consul-watcher", true));
		}
	}

	private void initConsulClient() {
		Object tlsConfig = conf.get("TLSConfig");
		TLSConfig config = null;
		if (tlsConfig != null && Map.class.isAssignableFrom(tlsConfig.getClass())) {
			@SuppressWarnings("unchecked")
			Map<String, Object> tlsConfigMap = (Map<String, Object>) tlsConfig;
			String keyStoreInstanceType = (String) tlsConfigMap.get("keyStoreInstanceType");
			String certificatePath = (String) tlsConfigMap.get("certificatePath");
			String certificatePassword = (String) tlsConfigMap.get("certificatePassword");
			String keyStorePath = (String) tlsConfigMap.get("keyStorePath");
			String keyStorePassword = (String) tlsConfigMap.get("keyStorePassword");
			config = new TLSConfig(KeyStoreInstanceType.valueOf(keyStoreInstanceType), certificatePath,
					certificatePassword, keyStorePath, keyStorePassword);
		}
		String host = (String) conf.get("host");
		Integer port = Integer.parseInt(conf.get("port").toString());
		if (config != null) {
			this.consulRawClient = new ConsulRawClient(host, port, config);
		} else {
			this.consulRawClient = new ConsulRawClient(host, port);
		}
		this.client = new ConsulClient(consulRawClient);
	}

	@Override
	public void start() {
		if (watchKeys != null) {
			for (String watchKey : watchKeys) {
				long updateIndex = flushCache(watchKey);
				executor.execute(() -> {
					long index = updateIndex;
					while (!destroyed.get()) {
						try {
							Response<List<String>> responseKeys = client.getKVKeysOnly(watchKey, separator, token,
									new QueryParams(waitTime, index));
							if (responseKeys == null) {
								Thread.sleep(waitTime * DEFAULT_WAIT_TIME);
								logger.error("not found watchKey {} !", watchKey);
								continue;
							}
							long currentIndex = responseKeys.getConsulIndex();
							if (currentIndex != index) {
								index = currentIndex;
								flushCache(watchKey);
							}
						} catch (Throwable e) {
							logger.error("", e);
							if (!destroyed.get()) {
								try {
									Thread.sleep(DEFAULT_WAIT_TIME);
								} catch (InterruptedException interruptedException) {
									Thread.currentThread().interrupt();
								}
							}
						}
					}
				});
			}
		}
	}

	@Override
	public void stop() {
		if (destroyed.compareAndSet(false, true)) {
			if (executor != null) {
				executor.shutdownNow();
			}
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
	public String type() {
		return "consul";
	}

	public long flushCache(String watchKey) {
		Response<List<GetValue>> responseValues = client.getKVValues(watchKey, token);
		List<GetValue> getValues = responseValues.getValue();
		if (getValues == null) {
			return -1;
		}
		for (GetValue getValue : getValues) {
			String key = getValue.getKey();
			String value = getValue.getDecodedValue();
			if (value == null) {
				continue;
			}
			ConfigData configData = ConfigDataCache.getConfigData(key);
			if (configData == null || configData.getVersion() != getValue.getModifyIndex()) {
				if (ConfigContentDecryptor.useDecrypt()) {
					try {
						value = ConfigContentDecryptor.decryptAndReplaceTag(value);
					} catch (Exception e) {
						logger.error("", e);
						continue;
					}
				}
				ConfigDataCache.addConfigData(key, value, getValue.getModifyIndex());
				ConfigMappingDataCache.removeMappingData(key);
				PathMappingTargetCache.rebindAllByPath(key);
			}
		}
		return responseValues.getConsulIndex();
	}

	@Override
	public String info() throws Exception {
		Config config = client.getAgentSelf(token).getValue().getConfig();
		return config.getNodeName() + "-" + config.getDatacenter() + "-" + config.getVersion();
	}

}
