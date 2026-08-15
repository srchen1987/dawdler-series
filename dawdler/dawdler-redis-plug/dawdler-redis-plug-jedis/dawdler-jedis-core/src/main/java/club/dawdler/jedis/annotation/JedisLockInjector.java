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
package club.dawdler.jedis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import club.dawdler.jedis.lock.JedisDistributedLockHolder;

/**
 * @author jackson.song
 * @version V1.0
 * 标注一个成员变量 注入JedisDistributedLock
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JedisLockInjector {
	/**
	 * 配置文件名
	 */
	String fileName();

	/**
	 * 下一次重试等待，单位毫秒
	 */
	int intervalInMillis() default JedisDistributedLockHolder.DEFAULT_INTERVAL_IN_MILLIS;

	/**
	 * 锁的过期时长，单位毫秒
	 */
	long lockExpiryInMillis() default JedisDistributedLockHolder.DEFAULT_LOCK_EXPIRY_IN_MILLIS;

	/**
	 * 是否启用看门狗,用于延时未处理完的操作,默认开启
	 */
	boolean useWatchDog() default true;

}
