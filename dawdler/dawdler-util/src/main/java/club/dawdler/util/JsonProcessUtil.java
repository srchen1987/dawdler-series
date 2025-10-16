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
package club.dawdler.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

/**
 * @author jackson.song
 * @version V1.0
 * json工具类
 */
public class JsonProcessUtil {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final ObjectMapper NON_EMPTY_MAPPER = new ObjectMapper();

	static {
		MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		NON_EMPTY_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		NON_EMPTY_MAPPER.setSerializationInclusion(Include.NON_NULL);
		JavaTimeModule javaTimeModule = new JavaTimeModule();

		javaTimeModule.addSerializer(LocalDate.class,
				new LocalDateSerializer(DateTimeFormatter.ofPattern(DateUtil.DEFAULT_DATE_PATTERN)));
		javaTimeModule.addDeserializer(LocalDate.class,
				new LocalDateDeserializer(DateTimeFormatter.ofPattern(DateUtil.DEFAULT_DATE_PATTERN)));

		javaTimeModule.addSerializer(LocalDateTime.class,
				new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateUtil.DEFAULT_DATE_TIME_PATTERN)));
		javaTimeModule.addDeserializer(LocalDateTime.class,
				new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DateUtil.DEFAULT_DATE_TIME_PATTERN)));

		MAPPER.registerModule(javaTimeModule);
		NON_EMPTY_MAPPER.registerModule(javaTimeModule);
	}

	public static ObjectMapper getMapperInstance() {
		return MAPPER;
	}

	public static ObjectMapper getNonEmptyMapperInstance() {
		return NON_EMPTY_MAPPER;
	}

	public static String beanToJson(Object obj) {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] beanToJsonByte(Object obj) {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		try {
			return mapper.writeValueAsBytes(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
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
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		try {
			return mapper.readValue(json, valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Object jsonToBean(String json, TypeReferenceType typeReferenceType) {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		try {
			return mapper.readValue(json, typeReferenceType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T jsonToBean(String json, TypeReferenceGenerics<T> typeReferenceType) {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		try {
			return mapper.readValue(json, typeReferenceType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T jsonToBean(byte[] json, Class<T> valueType) {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		try {
			return mapper.readValue(json, valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Object jsonToBean(byte[] json, TypeReferenceType typeReferenceType) {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		try {
			return mapper.readValue(json, typeReferenceType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T jsonToBean(byte[] json, TypeReferenceGenerics<T> typeReferenceType) {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		try {
			return mapper.readValue(json, typeReferenceType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T jsonToBean(InputStream jsonStream, Class<T> valueType) {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		try {
			return mapper.readValue(jsonStream, valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Object jsonToBean(InputStream jsonStream, TypeReferenceType typeReferenceType) {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		try {
			return mapper.readValue(jsonStream, typeReferenceType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T jsonToBean(InputStream jsonStream, TypeReferenceGenerics<T> typeReferenceType) {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		try {
			return mapper.readValue(jsonStream, typeReferenceType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String ignoreNullBeanToJson(Object obj) {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] ignoreNullBeanToJsonByte(Object obj) {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		try {
			return mapper.writeValueAsBytes(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void ignoreNullBeanToJson(Writer writer, Object obj)
			throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = JsonProcessUtil.getNonEmptyMapperInstance();
		mapper.writeValue(writer, obj);
	}

	public static void ignoreNullBeanToJson(OutputStream out, Object obj)
			throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = JsonProcessUtil.getMapperInstance();
		mapper.writeValue(out, obj);
	}

	public static <T> T ignoreNullJsonToBean(String json, Class<T> valueType) {
		ObjectMapper mapper = JsonProcessUtil.getNonEmptyMapperInstance();
		try {
			return mapper.readValue(json, valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T ignoreNullJsonToBean(byte[] json, Class<T> valueType) {
		ObjectMapper mapper = JsonProcessUtil.getNonEmptyMapperInstance();
		try {
			return mapper.readValue(json, valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T ignoreNullJsonToBean(InputStream jsonStream, Class<T> valueType) {
		ObjectMapper mapper = JsonProcessUtil.getNonEmptyMapperInstance();
		try {
			return mapper.readValue(jsonStream, valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class TypeReferenceType extends TypeReference<Object> {
		private Type type;

		public TypeReferenceType(Type type) {
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

	}

	public static class TypeReferenceGenerics<T> extends TypeReference<T> {
		private Type type;

		public TypeReferenceGenerics(Type type) {
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

	}
}
