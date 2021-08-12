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
package com.anywide.dawdler.clientplug.web.bind.resolver.impl;

import com.anywide.dawdler.clientplug.annotation.RequestParam;
import com.anywide.dawdler.clientplug.web.bind.param.RequestParamFieldData;
import com.anywide.dawdler.clientplug.web.bind.resolver.MethodArgumentResolver;

/**
 * @author jackson.song
 * @version V1.0
 * @Title AbstractMethodArgumentResolver.java
 * @Description 获取参数值的决策者的抽象类，实现getParameterName 方便子类使用
 * @date 2021年4月03日
 * @email suxuan696@gmail.com
 */
public abstract class AbstractMethodArgumentResolver implements MethodArgumentResolver {

	protected String getParameterName(RequestParamFieldData requestParamFieldData) {
		RequestParam requestParam = requestParamFieldData.getAnnotation(RequestParam.class);
		String paramName = null;
		if (requestParam != null) {
			paramName = requestParam.value();
		}
		if (paramName == null || paramName.trim().equals(""))
			paramName = requestParamFieldData.getParamName();
		return paramName;
	}

	protected String getParameterName(String paramName, RequestParamFieldData requestParamFieldData) {
		if (paramName == null || paramName.trim().equals(""))
			paramName = requestParamFieldData.getParamName();
		return paramName;
	}

}
