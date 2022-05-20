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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author jackson.song
 * @version V1.0
 * @Title JsonProcessUtil.java
 * @Description TODO
 * @date 2010年3月28日
 * @email suxuan696@gmail.com
 */
public class JsonProcessUtil {
	private static final ObjectMapper mapper = new ObjectMapper();

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

	public static byte[] beanToJsonByte(Object obj) {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		try {
			return mapper.writeValueAsBytes(obj);
		} catch (Exception e) {
			return null;
		}
	}

	public static void beanToJson(Writer writer, Object obj)
			throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		mapper.writeValue(writer, obj);
	}

	public static void beanToJson(OutputStream out, Object obj)
			throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		mapper.writeValue(out, obj);
	}

	public static <T> T jsonToBean(String json, Class<T> valueType) {
		T obj = null;
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//		mapper.disable(DeserializationConfig.Feature.AUTO_DETECT_SETTERS);
		try {
			obj = mapper.readValue(json, valueType);
		} catch (JsonMappingException e) {
		} catch (JsonParseException e) {
		} catch (IOException e) {
		}
		return obj;
	}

	public static <T> T jsonToBean(byte[] json, Class<T> valueType) {
		T obj = null;
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//		mapper.disable(DeserializationConfig.Feature.AUTO_DETECT_SETTERS);
		try {
			obj = mapper.readValue(json, valueType);
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
			obj = mapper.readValue(jsonStream, valueType);
		} catch (JsonMappingException e) {
		} catch (JsonParseException e) {
		} catch (IOException e) {
		}
		return obj;
	}
}
