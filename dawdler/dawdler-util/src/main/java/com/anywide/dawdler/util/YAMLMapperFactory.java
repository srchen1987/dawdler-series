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
package com.anywide.dawdler.util;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/**
 * @author jackson.song
 * @version V1.0
 * @Title YAMLMapperFactory.java
 * @Description YAMLMapper工厂,提供单例
 * @date 2023年2月08日
 * @email suxuan696@gmail.com
 */
public class YAMLMapperFactory {
	private final static YAMLMapper YAML_MAPPER = YAMLMapper.builder().build();
	
	public static YAMLMapper getYAMLMapper() {
		return YAML_MAPPER;
	}
}
