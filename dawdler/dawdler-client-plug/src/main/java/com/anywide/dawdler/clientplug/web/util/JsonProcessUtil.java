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
package com.anywide.dawdler.clientplug.web.util;

import java.io.IOException;
import java.io.InputStream;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @Title: JsonProcessUtil.java
 * @Description: TODO
 * @author: jackson.song
 * @date: 2010年03月28日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class JsonProcessUtil {
	private static ObjectMapper mapper = new ObjectMapper();

	public static ObjectMapper getMapperInstance() {
		return mapper;
	}

	public static String beanToJson(Object obj) {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			return null;
		}
	}

	public static <T> T jsonToBean(String json, Class<T> valueType) {
		T obj = null;
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//		mapper.disable(DeserializationConfig.Feature.AUTO_DETECT_SETTERS);
		try {
			obj = (T) mapper.readValue(json, valueType);
		} catch (JsonMappingException e) {
		} catch (JsonParseException e) {
		} catch (IOException e) {
		}
		return obj;
	}

	public static <T> T jsonToBean(InputStream jsonStream, Class<T> valueType) {
		T obj = null;
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//		mapper.disable(DeserializationConfig.Feature.AUTO_DETECT_SETTERS);
		try {
			obj = (T) mapper.readValue(jsonStream, valueType);
		} catch (JsonMappingException e) {
		} catch (JsonParseException e) {
		} catch (IOException e) {
		}
		return obj;
	}

}
