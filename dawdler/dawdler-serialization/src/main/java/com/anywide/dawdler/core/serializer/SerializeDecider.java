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
package com.anywide.dawdler.core.serializer;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jackson.song
 * @version V1.0
 * @Title SerializeDecider.java
 * @Description 序列化决策者，可通过SPI方式扩展
 * @date 2014年12月22日
 * @email suxuan696@gmail.com
 */
public class SerializeDecider {
	private static final Logger logger = LoggerFactory.getLogger(SerializeDecider.class);
	private static final Map<Byte, Serializer> SERIALIZERS = new ConcurrentHashMap<>();
	static {
		ServiceLoader<Serializer> loader = ServiceLoader.load(Serializer.class);
		loader.forEach(SerializeDecider::addSerializer);
	}

	public static void register(byte key, Serializer serializer) {
		Serializer preSerializer = SERIALIZERS.putIfAbsent(key, serializer);
		if (preSerializer != null) {
			logger.error(preSerializer.key() + " already exists in " + preSerializer.getClass().getName());
		}
	}

	public static Serializer decide(byte key) {
		return SERIALIZERS.get(key);
	}

	public static void addSerializer(Serializer serializer) {
		register(serializer.key(), serializer);
	}

	public static void destroyed() {
		SERIALIZERS.forEach((k, v) -> {
			v.destroyed();
		});
	}

	public static enum SerializeType {
		JDK((byte) 1), KRYO((byte) 2);

		private byte type;

		SerializeType(byte type) {
			this.type = type;
		}

		public byte getType() {
			return type;
		}

	}
}
