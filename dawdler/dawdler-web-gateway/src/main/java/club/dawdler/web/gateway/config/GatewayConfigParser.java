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
package club.dawdler.web.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.util.YAMLUtil;

/**
 * @author jackson.song
 * @version V1.0
 * 
 * 网关配置解析器，加载并构建 GatewayConfig。
 */
public class GatewayConfigParser {

	private static final Logger logger = LoggerFactory.getLogger(GatewayConfigParser.class);
	private static GatewayConfig gatewayConfig;

	static {
		try {
			gatewayConfig = YAMLUtil.loadYAMLIfNotExistLoadConfigCenter("gateway", GatewayConfig.class);
		} catch (Exception e) {
			logger.error("Failed to parse gateway config", e);
		}
	}

	public static GatewayConfig getGatewayConfig() {
		return gatewayConfig;
	}

}
