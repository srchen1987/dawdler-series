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
package club.dawdler.conf.init;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.conf.client.ConfigClient;
import club.dawdler.conf.client.factory.ConfigClientFactory;
import club.dawdler.util.DawdlerTool;
import club.dawdler.util.YAMLMapperFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/**
 * @author jackson.song
 * @version V1.0
 * 配置中心初始化,SPI方式加载不同配置中心的client实现
 */
public class ConfigInit {
	private static Logger logger = LoggerFactory.getLogger(ConfigInit.class);
	
	List<ConfigClient> configClients = null;
	private static ConfigInit configInit = new ConfigInit();

	private ConfigInit() {
	}

	public List<ConfigClient> getConfigClients() {
		return configClients;
	}

	public static ConfigInit getInstance() {
		return configInit;
	}

	@SuppressWarnings("unchecked")
	public void init() {
		initConfigClients();
		String prefix = "dawdler-config";
		String suffix = ".yml";
		InputStream input = DawdlerTool.getResourceFromClassPath(prefix, suffix);
		YAMLMapper YAMLMapper = YAMLMapperFactory.getYAMLMapper();
		Map<String, Map<String, Object>> data = null;
		if (input == null) {
			throw new RuntimeException("not found dawdler-config.yml in classPath!");
		}
		try {
			data = YAMLMapper.readValue(input, HashMap.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
		if (data != null) {
			this.configClients = new ArrayList<>();
			data.forEach((k, v) -> {
				String type = k;
				if (type == null) {
					logger.error("can't find server-type in dawdler-config.yml");
					return;
				}
				ConfigClient configClient = ConfigClientFactory.getClient(type);
				if (configClient == null) {
					logger.error("can't find {} in META-INF/service/club.dawdler.conf.client.ConfigClient!",
							type);
				}
				configClient.init(v);
				configClient.start();
				this.configClients.add(configClient);
			});
		}
	}

	public void destroy() {
		if (configClients != null) {
			for (ConfigClient configClient : configClients) {
				configClient.stop();
			}
		}
	}

	private void initConfigClients() {
		ServiceLoader<ConfigClient> configClients = ServiceLoader.load(ConfigClient.class);
		configClients.forEach(configClient -> {
			boolean success = ConfigClientFactory.addClient(configClient);
			if (!success) {
				logger.error(configClient.type() + " already exists!");
			}
		});
	}

}
