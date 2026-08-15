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
package club.dawdler.distributed.transaction.compensate.config;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.util.PropertiesUtil;

/**
 * @author jackson.song
 * @version V1.0
 * 补偿器配置,从classpath下的distributed-transaction-compensator.properties加载,
 * 支持统一配置中心覆盖.未配置时使用默认值.
 */
public class CompensationConfig {
	private static final Logger logger = LoggerFactory.getLogger(CompensationConfig.class);
	public static final String CONFIG_FILE = "distributed-transaction-compensator";

	private static final long DEFAULT_INITIAL_DELAY_SECONDS = 15;
	private static final long DEFAULT_DELAY_SECONDS = 15;
	private static final int DEFAULT_MAX_RETRY_TIMES = 50;

	private static final CompensationConfig INSTANCE = new CompensationConfig();

	private final long initialDelaySeconds;
	private final long delaySeconds;
	private final int maxRetryTimes;

	private CompensationConfig() {
		long initialDelay = DEFAULT_INITIAL_DELAY_SECONDS;
		long delay = DEFAULT_DELAY_SECONDS;
		int retry = DEFAULT_MAX_RETRY_TIMES;
		try {
			Properties ps = PropertiesUtil.loadPropertiesIfNotExistLoadConfigCenter(CONFIG_FILE);
			if (ps != null) {
				initialDelay = PropertiesUtil.getIfNullReturnDefaultValueLong("initialDelaySeconds", initialDelay, ps);
				delay = PropertiesUtil.getIfNullReturnDefaultValueLong("delaySeconds", delay, ps);
				retry = PropertiesUtil.getIfNullReturnDefaultValueInt("maxRetryTimes", retry, ps);
			}
		} catch (Exception e) {
			logger.warn(
					"load {} failed, use default config. initialDelaySeconds={} delaySeconds={} maxRetryTimes={}",
					CONFIG_FILE, initialDelay, delay, retry, e);
		}
		this.initialDelaySeconds = initialDelay;
		this.delaySeconds = delay;
		this.maxRetryTimes = retry;
	}

	public static CompensationConfig getInstance() {
		return INSTANCE;
	}

	public long getInitialDelaySeconds() {
		return initialDelaySeconds;
	}

	public long getDelaySeconds() {
		return delaySeconds;
	}

	public int getMaxRetryTimes() {
		return maxRetryTimes;
	}
}
