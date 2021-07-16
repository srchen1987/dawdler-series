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
 * @Title RequestMapping.java
 * @Description http接口请求注解
 * @author jackson.song
 * @date 2007年04月17日
 * @version V1.0
 * @email suxuan696@gmail.com
 */
public @interface RequestMapping {
	String[] value() default {};

	RequestMethod[] method() default {};

	// ParamType paramType() default ParamType.httpType;
	ViewType viewType() default ViewType.json;

	boolean generateValidator() default false;

	String input() default "";

	long uploadSizeMax() default 0l;

	long uploadPerSizeMax() default 0l;

	String exceptionHandler() default "";

	// public enum ParamType{
//		jsonType,
//		httpType;
//	}
	enum ViewType {
		json, jsp, velocity
	}
}
