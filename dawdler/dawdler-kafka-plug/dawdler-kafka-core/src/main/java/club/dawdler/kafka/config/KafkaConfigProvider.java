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
package club.dawdler.kafka.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jackson.song
 * @version V1.0
 * Kafka配置多例工厂，提供基于配置文件名的KafkaConfig实例管理
 */
public class KafkaConfigProvider {
    private static final Map<String, KafkaConfig> INSTANCES = new ConcurrentHashMap<>();

    /**
     * 获取指定配置文件的KafkaConfig实例，如果不存在则创建新的实例
     * @param fileName 配置文件名
     * @return KafkaConfig实例
     */
    public static KafkaConfig getInstance(String fileName) {
        KafkaConfig config = INSTANCES.get(fileName);
        if (config != null) {
            return config;
        }
        synchronized (INSTANCES) {
            config = INSTANCES.get(fileName);
            if (config == null) {
                config = new KafkaConfig(fileName);
                INSTANCES.put(fileName, config);
            }
        }
        return config;
    }

    /**
     * 获取所有配置实例
     * @return 配置实例映射
     */
    public static Map<String, KafkaConfig> getInstances() {
        return INSTANCES;
    }

    /**
     * 关闭所有配置实例
     */
    public static void shutdownAll() {
        INSTANCES.clear();
    }
}