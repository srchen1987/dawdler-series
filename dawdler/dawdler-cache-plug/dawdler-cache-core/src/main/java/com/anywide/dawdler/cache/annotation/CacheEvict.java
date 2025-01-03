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
package com.anywide.dawdler.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jackson.song
 * @version V1.0
 * CacheEvict 注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEvict {

	/**
	 * 缓存的key 支持JEXL表达式 内置变量为方法中变量名
	 */
	String key() default "";

	/**
	 * 缓存的名字的数组
	 */
	String[] cacheNames();

	/**
	 * 指定cacheManager 如:caffeine或jedis
	 */
	String cacheManager() default "";

	/**
	 * 执行入参的条件表达式,支持JEXL表达式,表达式的结果必须是true或false 内置变量为方法中变量名 只有入参符合条件的才会删除缓存
	 */
	String condition() default "";

	/**
	 * 是否清空全部缓存
	 */
	boolean allEntries() default false;

	/**
	 * 是否在方法执行前就清空
	 */
	boolean beforeInvocation() default false;

}
