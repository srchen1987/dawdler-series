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
package com.anywide.dawdler.cache;

import java.time.Duration;

import com.anywide.dawdler.core.serializer.SerializeDecider.SerializeType;

/**
 * @author jackson.song
 * @version V1.0
 * @Title CacheConfig.java
 * @Description CacheConfig接口,用于声明cache配置
 * @date 2023年7月29日
 * @email suxuan696@gmail.com
 */
public interface CacheConfig {

	/**
	 * 缓存名称
	 */
	String getName();

	/**
	 * 最大个数
	 */
	Long getMaxSize();

	/**
	 * 访问后过期时间
	 */
	Duration getExpireAfterAccessDuration();

	/**
	 * 缓存有效期
	 */
	Duration getExpireAfterWriteDuration();

	/**
	 * 序列化方式默认为 KRYO
	 */
	default SerializeType getSerializeType() {
		return SerializeType.KRYO;
	}

	/**
	 * 配置文件名（如用到dawdler-cache-jedis配置redis时需要）
	 */
	default String getFileName() {
		return null;
	}

}
