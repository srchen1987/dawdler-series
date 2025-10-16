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
package club.dawdler.client.api.generator;

import java.util.Map;

import com.thoughtworks.qdox.model.JavaType;

import club.dawdler.client.api.generator.TypesConverter.TypeData;
import club.dawdler.client.api.generator.data.ItemsData;
import club.dawdler.client.api.generator.data.MethodParameterData;
import club.dawdler.client.api.generator.data.SchemaData;
import club.dawdler.client.api.generator.util.ClassTypeUtil;

/**
 * @author jackson.song
 * @version V1.0
 * TypeDataParser
 */
public class TypeDataParser {

	public static void convertion(JavaType javaType, MethodParameterData parameterData,
			Map<String, ClassStruct> classStructs, Map<String, MethodParameterData> params, boolean isArray, Map<String, Object> definitionsMap) {
		String typeName = javaType.getFullyQualifiedName();
		boolean typeArray = typeName.endsWith("[]");
		boolean collection = ClassTypeUtil.isArray(javaType.getBinaryName());
		String type = null;
		if (typeArray || isArray || collection) {
			if (typeArray) {
				type = typeName.substring(0, typeName.lastIndexOf("[]"));
			} else if (collection) {
				type = ClassTypeUtil.getType0(javaType);
			} else {
				type = typeName;
			}
			SchemaData schema = parameterData.getSchema();
			schema.setType("array");
			parameterData.setStyle("form");
			parameterData.setExplode(true);
			TypeData typeData = TypesConverter.getType(type);
			if (typeData != null) {
				ItemsData items = new ItemsData();
				items.setType(typeData.getType());
				items.setFormat(typeData.getFormat());
				items.setMaxLength(schema.getMaxLength());
				items.setMinLength(schema.getMinLength());
				items.setMaximum(schema.getMaximum());
				items.setMinimum(schema.getMinimum());
				schema.setItems(items);
				schema.setMaximum(null);
				schema.setMinimum(null);
				schema.setMaxLength(null);
				schema.setMinLength(null);
			} else {
				ClassStruct classStruct = classStructs.get(type);
				if (classStruct != null) {
					if (params != null) {
						params.remove(parameterData.getName());
					}
					FieldParser.parserFields(classStruct.getJavaClass(), classStructs, params, true, definitionsMap);
				}
			}
		} else {
			TypeData typeData = TypesConverter.getType(typeName);
			if (typeData != null) {
				SchemaData schemaData = parameterData.getSchema();
				schemaData.setType(typeData.getType());
				schemaData.setFormat(typeData.getFormat());
			} else {
				if (params != null) {
					params.remove(parameterData.getName());
				}
				ClassStruct classStruct = classStructs.get(typeName);
				if (classStruct != null) {
					FieldParser.parserFields(classStruct.getJavaClass(), classStructs, params, false,definitionsMap);
				}
			}
		}
	}

		public static void convertionMultipartParameter(String paramName,JavaType javaType, SchemaData schema,
			Map<String, ClassStruct> classStructs, Map<String, SchemaData> params, boolean isArray) {
		String typeName = javaType.getFullyQualifiedName();
		boolean typeArray = typeName.endsWith("[]");
		boolean collection = ClassTypeUtil.isArray(javaType.getBinaryName());
		String type = null;
		if (typeArray || isArray || collection) {
			if (typeArray) {
				type = typeName.substring(0, typeName.lastIndexOf("[]"));
			} else if (collection) {
				type = ClassTypeUtil.getType0(javaType);
			} else {
				type = typeName;
			}
			schema.setType("array");
			schema.setStyle("form");
			schema.setExplode(true);
			TypeData typeData = TypesConverter.getType(type);
			if (typeData != null) {
				ItemsData items = new ItemsData();
				items.setType(typeData.getType());
				items.setFormat(typeData.getFormat());
				items.setMaxLength(schema.getMaxLength());
				items.setMinLength(schema.getMinLength());
				items.setMaximum(schema.getMaximum());
				items.setMinimum(schema.getMinimum());
				schema.setItems(items);
				schema.setMaximum(null);
				schema.setMinimum(null);
				schema.setMaxLength(null);
				schema.setMinLength(null);
			} else {
				ClassStruct classStruct = classStructs.get(type);
				if (classStruct != null) {
					if (params != null) {
						params.remove(paramName);
					}
					FieldParser.parserMultipartParameterFields(paramName, classStruct.getJavaClass(), classStructs, params, true);
				}
			}
		} else {
			TypeData typeData = TypesConverter.getType(typeName);
			if (typeData != null) {
				schema.setType(typeData.getType());
				schema.setFormat(typeData.getFormat());
			} else {
				if (params != null) {
					params.remove(paramName);
				}
				ClassStruct classStruct = classStructs.get(typeName);
				if (classStruct != null) {
					FieldParser.parserMultipartParameterFields(paramName, classStruct.getJavaClass(), classStructs, params, false);
				}
			}
		}
	}

}
