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
 * @Title CachePut.java
 * @Description CachePut 注解
 * @date 2023年7月29日
 * @email suxuan696@gmail.com
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CachePut {

	/**
	 * 缓存的key 支持JEXL表达式 内置变量为方法中变量名
	 */
	String key();

	/**
	 * 缓存的名字
	 */
	String cacheName();

	/**
	 * 指定cacheManager 如:ceffeine或jedis
	 */
	String cacheManager() default "";

	/**
	 * 执行入参的条件表达式,支持JEXL表达式,表达式的结果必须是true或false 内置变量为方法中变量名 只有入参符合条件的才会放入缓存
	 */
	String condition() default "";

	/**
	 * 方法结果的条件表达式,支持JEXL表达式,表达式的结果必须是true或false 内置变量为result,result为方法执行结果的key
	 * 只有结果集不符合这个条件才会放入缓存
	 */
	String unless() default "";

}
