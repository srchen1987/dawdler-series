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
package com.anywide.dawdler.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })

/**
 *
 * @Title CircuitBreaker.java
 * @Description 熔断器接口注解
 * @author jackson.song
 * @date 2018年3月10日
 * @version V1.0
 * @email suxuan696@gmail.com
 */
public @interface CircuitBreaker {

	String breakerKey() default "";

	/**
	 * @return int
	 * @Description 统计时长 intervalInMs/windowsCount 最好为整数
	 * @date 2018年03月10日
	 */
	int intervalInMs() default 3000;

	/**
	 * @return int
	 * @Description 窗口大小
	 * @date 2018年03月10日
	 */
	int windowsCount() default 2;

	/**
	 * @return int
	 * @Title sleepWindowInMilliseconds
	 * @Description 熔断器打开后，所有的请求都会直接失败，熔断器打开时会在经过一段时间后就放行一条请求成功则关闭熔断器，此配置就为指定的这段时间，默认值是
	 *              5000。
	 * @date 2018年03月10日
	 */
	int sleepWindowInMilliseconds() default 5000;

	/**
	 * @return int
	 * @Description 启用熔断器功能窗口时间内的最小请求数。
	 * @date 2018年03月10日
	 */

	int requestVolumeThreshold() default 5;

	/**
	 * @return double
	 * @Description 错误百分比，默认为40% 达到40%的错误率会触发熔断（大于requestVolumeThreshold）
	 * @date 2018年03月10日
	 */
	double errorThresholdPercentage() default 0.4;

	/**
	 * @return String
	 * @Description 熔断后执行的方法 参数与返回值与执行的方法相同
	 * @date 2018年03月10日
	 */
	String fallbackMethod() default "";

}
