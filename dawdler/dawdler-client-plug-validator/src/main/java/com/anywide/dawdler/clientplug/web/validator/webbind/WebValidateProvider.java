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
package com.anywide.dawdler.clientplug.web.validator.webbind;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.anywide.dawdler.clientplug.web.validator.entity.ControlValidator;

/**
 * 
 * @Title: WebValidateProvider.java
 * @Description: 验证提供者 缓存controller配置的校验规则
 * @author: jackson.song
 * @date: 2007年07月22日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class WebValidateProvider {
	private static Map<Class, ControlValidator> validators = new HashMap<Class, ControlValidator>();

	public static ControlValidator getValidator(Class controlClass, ResourceBundle cproperty) {
		if (controlClass == null)
			return null;
		ControlValidator controlValidator = validators.get(controlClass);
		if (controlValidator != null)
			return controlValidator;
		ControlValidator cv = ValidateResourceLoader.getControlValidator(controlClass);
		validators.put(controlClass, cv);
		return cv;
	}
}
