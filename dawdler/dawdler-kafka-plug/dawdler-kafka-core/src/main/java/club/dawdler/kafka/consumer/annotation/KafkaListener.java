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
package club.dawdler.kafka.consumer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jackson.song
 * @version V1.0
 * 标注一个方法是否是kafka的消费者 此方法格式固定为void methodName(Message message)
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface KafkaListener {

	/**
	 * 指定kafka的配置文件名
	 */
	String fileName();

	/**
	 * 主题名
	 */
	String topic();

	/**
	 * 是否自动提交偏移量
	 */
	boolean autoCommit() default true;

	/**
	 * 每次拉取的最大消息数
	 */
	int maxPollRecords() default 500;

	/**
	 * 消费者线程数
	 */
	int consumerThreads() default 1;

	/**
	 * 会话超时时间(毫秒)
	 */
	int sessionTimeoutMs() default 10000;

	/**
	 * 心跳间隔时间(毫秒)
	 */
	int heartbeatIntervalMs() default 3000;

	/**
	 * 关闭超时时间(毫秒)，用于优雅停机时等待任务完成
	 */
	int shutdownTimeoutMs() default 5000;

	/**
	 * 消费者offset重置策略 (可选: earliest, latest, none)
	 * 不配置则使用Kafka官方默认值
	 */
	String autoOffsetReset() default "";
}