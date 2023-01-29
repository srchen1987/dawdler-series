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
package com.anywide.dawdler.clientplug.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
/**
 *
 * @author jackson.song
 * @version V1.0
 * @Title RequestMapping.java
 * @Description http接口请求注解
 * @date 2007年4月17日
 * @email suxuan696@gmail.com
 */
public @interface RequestMapping {
	
	/**
	 *path 支持antPath 只有value可以用到类上，以下其他只在方法上生效
	 */
	String[] value() default {};
	
	/**
	 * 请求方法 POST GET以及其他
	 */
	RequestMethod[] method() default {};

	/**
	 * 响应的视图类型 支持json,jsp,velocity
	 */
	ViewType viewType() default ViewType.json;

	/**
	 * 生成验证规则，根据后台的验证框架生成前端的表达式
	 */
	boolean generateValidator() default false;

	/**
	 * 配置验证框架之后验证未通过的跳转路径，默认为空，返回json类型的错误提醒，如果配置会在request域下设置属性validate_error并forward到
	 */
	String input() default "";
	
	/**
	 * 上传文件最大的限制,单位byte
	 */
	long uploadSizeMax() default 0L;

	/**
	 * 上传单个文件最大的限制,单位byte
	 */
	long uploadPerSizeMax() default 0L;

	/**
	 * 异常处理者，系统内提供三种处理者json、jsp、
	 */
	String exceptionHandler() default "";// velocity，会根据ViewType自动选择，如果有需要可以扩展，参考HttpExceptionHolder的register方法，可以在监听器启动时扩展，一般不会考虑扩展所以没采用SPI方式配置

	enum ViewType {
		json, jsp, velocity
	}

	enum RequestMethod {
		GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE
	}
}
