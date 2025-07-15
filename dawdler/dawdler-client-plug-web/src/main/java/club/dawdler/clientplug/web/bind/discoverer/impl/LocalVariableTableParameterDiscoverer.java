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
package club.dawdler.clientplug.web.bind.discoverer.impl;

import java.lang.reflect.Method;

import club.dawdler.clientplug.web.bind.discoverer.ParameterDiscoverer;
import club.dawdler.util.reflectasm.ParameterNameReader;

/**
 * @author jackson.song
 * @version V1.0
 * 基于asm获取方法参数名称的实现类
 */
public class LocalVariableTableParameterDiscoverer implements ParameterDiscoverer {

	@Override
	public String[] getParameterNames(Method method) {
		Class<?> clazz = method.getDeclaringClass();
		try {
			return ParameterNameReader.getParameterNames(clazz).get(method);
		} catch (Exception e) {
		}
		return null;
	}

}
