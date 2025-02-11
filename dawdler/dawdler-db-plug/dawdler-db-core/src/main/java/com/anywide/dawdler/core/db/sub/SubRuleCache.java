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
package com.anywide.dawdler.core.db.sub;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.anywide.dawdler.core.db.sub.rule.SubRule;
import com.anywide.dawdler.util.YAMLUtil;

/**
 * @author jackson.song
 * @version V1.0
 * 分库规则配置缓存类
 */
public class SubRuleCache {

	private static final Map<String, SubRule> RULE_CACHE = new ConcurrentHashMap<>();

	public static void put(String key, SubRule value) {
		RULE_CACHE.put(key, value);
	}

	public static SubRule getSubRule(String configPath,Class<? extends SubRule> subRuleType) throws Exception {
		SubRule subRule = RULE_CACHE.get(configPath);
		if (subRule == null) {
			synchronized (SubRuleCache.class) {
				subRule = RULE_CACHE.get(configPath);
				if (subRule == null) {
					subRule = YAMLUtil.loadYAMLIfNotExistLoadConfigCenter(configPath, subRuleType);
					RULE_CACHE.put(configPath, subRule);
				}
			}
		}
		return subRule;
	}

}
