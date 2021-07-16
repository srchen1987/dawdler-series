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
package com.anywide.dawdler.clientplug.web.bind.discoverer.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import com.anywide.dawdler.clientplug.web.bind.discoverer.ParameterDiscoverer;

/**
 * @author jackson.song
 * @version V1.0
 * @Title CompileParameterDiscoverer.java
 * @Description 基于java api 获取方法参数名称的实现类（1.8之后加入 parameter 才生效），由于开启的概率不高 所以此类无用
 *              没有加入到SPI
 * @date 2021年04月10日
 * @email suxuan696@gmail.com
 */
public class CompileParameterDiscoverer implements ParameterDiscoverer {

	@Override
	public String[] getParameterNames(Method method) {
		return getParameterNames(method.getParameters());
	}

	private String[] getParameterNames(Parameter[] parameters) {
		String[] parameterNames = null;
		if (parameters.length > 0)
			parameterNames = new String[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			Parameter param = parameters[i];
			if (!param.isNamePresent()) {
				return null;
			}
			parameterNames[i] = param.getName();
		}
		return parameterNames;
	}
}
