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
package com.anywide.dawdler.client.api.generator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.anywide.dawdler.clientplug.web.upload.UploadFile;

/**
 * @author jackson.song
 * @version V1.0
 * @Title TypesConverter.java
 * @Description 类型转换者
 * @date 2022年3月20日
 * @email suxuan696@gmail.com
 */
public class TypesConverter {
	private static Map<String, TypeData> typesCache = new HashMap<String, TypeData>() {
		private static final long serialVersionUID = 2579422540146268592L;
		{
			put(Byte.class.getName(), new TypeData("integer", "int8"));
			put(byte.class.getName(), new TypeData("integer", "int8"));
			put(Short.class.getName(), new TypeData("integer", "int16"));
			put(short.class.getName(), new TypeData("integer", "int16"));
			put(Integer.class.getName(), new TypeData("integer", "int32"));
			put(int.class.getName(), new TypeData("integer", "int32"));
			put(Long.class.getName(), new TypeData("integer", "int64"));
			put(long.class.getName(), new TypeData("integer", "int64"));
			put(Character.class.getName(), new TypeData("string", null));
			put(char.class.getName(), new TypeData("string", null));
			put(String.class.getName(), new TypeData("string", null));
			put(boolean.class.getName(), new TypeData("boolean", null));
			put(Boolean.class.getName(), new TypeData("boolean", null));
			put(Double.class.getName(), new TypeData("number", "double"));
			put(double.class.getName(), new TypeData("number", "double"));
			put(Float.class.getName(), new TypeData("number", "float"));
			put(float.class.getName(), new TypeData("number", "float"));
			put(BigDecimal.class.getName(), new TypeData("number", null));
			put(UploadFile.class.getName(), new TypeData("file", null));

		}
	};

	public static TypeData getType(String javaType) {
		return typesCache.get(javaType);
	}

	public static class TypeData {
		private String type;
		private String format;

		public TypeData(String type, String format) {
			this.type = type;
			this.format = format;
		}

		public String getType() {
			return type;
		}

		public String getFormat() {
			return format;
		}

		@Override
		public String toString() {
			return type + ":" + format;
		}

	}

}
