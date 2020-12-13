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
package com.anywide.dawdler.clientplug.web.validator;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @Title: ValidatorFactory.java
 * @Description: 验证器工厂
 * @author: jackson.song
 * @date: 2007年07月21日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class ValidatorFactory {
	private static Logger logger = LoggerFactory.getLogger(ValidatorFactory.class);
	private static ConcurrentHashMap<String, AbstractValidator> validators = new ConcurrentHashMap<String, AbstractValidator>();

	public static AbstractValidator getValidator(String key) {
		try {
			AbstractValidator avalidator = validators.get(key);
			if (avalidator != null)
				return avalidator;
			avalidator = new AbstractValidator(key) {
			};
			AbstractValidator pre = validators.putIfAbsent(key, avalidator);
			if (pre != null)
				avalidator = pre;
			return avalidator;
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}
}
