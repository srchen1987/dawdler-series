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
package com.anywide.dawdler.conf.client.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.conf.cache.ConfigDataCache;
import com.anywide.dawdler.conf.cache.ConfigDataCache.ConfigData;
import com.anywide.dawdler.conf.cache.ConfigMappingDataCache;
import com.anywide.dawdler.conf.cache.PathMappingTargetCache;
import com.anywide.dawdler.conf.client.ConfigClient;
import com.ecwid.consul.transport.TLSConfig;
import com.ecwid.consul.transport.TLSConfig.KeyStoreInstanceType;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ConsulConfigClient.java
 * @Description ConfigClient接口的consul实现，支持SPI扩展
 * @date 2021年5月30日
 * @email suxuan696@gmail.com
 */
public class ConsulConfigClient implements ConfigClient {
	private ConsulClient client = null;
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
	private volatile boolean start = true;
	private static Logger logger = LoggerFactory.getLogger(ConsulConfigClient.class);

	@Override
	public void init(Map<String, Object> conf) {
		Object tlsConfig = conf.get("TLSConfig");
		TLSConfig config = null;
		if (tlsConfig != null && Map.class.isAssignableFrom(tlsConfig.getClass())) {
			Map tlsConfigMap = (Map) tlsConfig;
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
		watchKeys = (List<String>) conf.get("watch-keys");
		separator = (String) conf.get("separator");
		token = (String) conf.get("token");
		waitTime = Integer.parseInt(conf.get("wait-time").toString());
		try {
			if (config != null) {
				client = new ConsulClient(host, port, config);
			} else {
				client = new ConsulClient(host, port);
			}
		} catch (Throwable e) {
			logger.error("", e);
		}
		
		if (watchKeys != null)
			executor = Executors.newFixedThreadPool(watchKeys.size());
	}

	@Override
	public void start() {
		if (watchKeys != null) {
			for (String watchKey : watchKeys) {
				long updateIndex = flushCache(watchKey);
				executor.execute(() -> {
					try {
						long index = updateIndex;
						while (start) {
							Response<List<String>> responseKeys = client.getKVKeysOnly(watchKey, separator, token,
									new QueryParams(waitTime, index));
							if (responseKeys == null) {
								Thread.sleep(10000);
								logger.error("not found watchKey {} !", watchKey);
								continue;
							}
							long currentIndex = responseKeys.getConsulIndex();
							if (currentIndex != index) {
								index = currentIndex;
								flushCache(watchKey);
							}
						}
					} catch (Exception e) {
						logger.error("", e);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException interruptedException) {
							// ignore
						}
					}
				});
			}
		}
	}

	@Override
	public void stop() {
		this.start = false;
		if (executor != null)
			executor.shutdown();
	}

	@Override
	public String type() {
		return "consul";
	}

	public long flushCache(String watchKey) {
		Response<List<GetValue>> responseValues = client.getKVValues(watchKey);
		List<GetValue> getValues = responseValues.getValue();
		if (getValues == null)
			return -1;
		for (GetValue getValue : getValues) {
			String key = getValue.getKey();
			String value = getValue.getDecodedValue();
			ConfigData configData = ConfigDataCache.getConfigData(key);
			if (configData == null || configData.getVersion() != getValue.getModifyIndex()) {
				ConfigDataCache.addConfigData(key, value, getValue.getModifyIndex());
				ConfigMappingDataCache.removeMappingData(key);
				PathMappingTargetCache.rebindAllByPath(key);
			}
		}
		return responseValues.getConsulIndex();
	}

}
