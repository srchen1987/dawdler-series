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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.anywide.dawdler.client.api.generator.TypesConverter.TypeData;
import com.anywide.dawdler.client.api.generator.data.ClassStruct;
import com.anywide.dawdler.client.api.generator.data.ItemsData;
import com.anywide.dawdler.client.api.generator.data.MethodParameterData;
import com.anywide.dawdler.client.api.generator.data.ParserTypeData;
import com.anywide.dawdler.client.api.generator.data.SchemaData;
import com.anywide.dawdler.client.api.generator.util.ClassTypeUtil;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.impl.DefaultJavaField;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;

/**
 * @author jackson.song
 * @version V1.0
 * @Title FieldParser.java
 * @Description 类属性解析器
 * @date 2022年3月20日
 * @email suxuan696@gmail.com
 */
public class FieldParser {

	public static void parserFields(JavaClass javaClass, Map<String, ClassStruct> classStructs,
			Map<String, MethodParameterData> params, boolean isArray) {
		List<JavaField> fields = javaClass.getFields();
		JavaClass superJavaClass = javaClass.getSuperJavaClass();
		while (!superJavaClass.getBinaryName().equals("java.lang.Object")) {
			fields.addAll(superJavaClass.getFields());
			superJavaClass = superJavaClass.getSuperJavaClass();
		}
		for (JavaField field : fields) {
			if (!field.isFinal() && !field.isStatic()) {
				MethodParameterData parameterData = null;
				parameterData = params.get(field.getName());
				if (parameterData == null) {
					parameterData = new MethodParameterData();
					parameterData.setName(field.getName());
					parameterData.setIn("query");
					params.put(field.getName(), parameterData);
					parameterData.setDescription(field.getComment());
					ParserTypeData.convertion(field.getType(), parameterData, classStructs, params, isArray);
				}
			}
		}
	}

	public static void parserFields(JavaType fieldJavaType, Map<String, ClassStruct> classStructs,
			Map<String, Object> definitionsMap, Map<String, JavaType> javaTypes) {
		parserFields(fieldJavaType, classStructs, definitionsMap, javaTypes, null);
	}

	public static void parserFields(JavaType fieldJavaType, Map<String, ClassStruct> classStructs,
			Map<String, Object> definitionsMap, Map<String, JavaType> javaTypes, Map<String, AtomicInteger> counter) {
		String typeName = fieldJavaType.getBinaryName();
		String genericFullyQualifiedName = fieldJavaType.getGenericFullyQualifiedName();
		String originalFullyQualifiedName = fieldJavaType.getFullyQualifiedName();
		if (ClassTypeUtil.isArray(typeName)) {
			DefaultJavaParameterizedType dt = (DefaultJavaParameterizedType) fieldJavaType;
			List<JavaType> dtList = dt.getActualTypeArguments();
			if (!dtList.isEmpty()) {
				JavaType javaType = dtList.get(0);
				typeName = javaType.getBinaryName();
				originalFullyQualifiedName = javaType.getFullyQualifiedName();
				genericFullyQualifiedName = javaType.getGenericFullyQualifiedName();
			}
		}
		if (!definitionsMap.containsKey(genericFullyQualifiedName)) {
			TypeData typeData = TypesConverter.getType(typeName);
			if (typeData == null) {
				ClassStruct classStruct = classStructs.get(typeName);
				if (classStruct != null) {
					Map<String, Object> objMap = new HashMap<>();
					Map<String, Object> propertiesMap = new HashMap<>();
					objMap.put("type", "object");
					if (genericFullyQualifiedName.contains(">")) {
						objMap.put("title", genericFullyQualifiedName/* .replaceAll("<", "«").replaceAll(">","»") */);
					} else {
						objMap.put("title", typeName);
					}
					objMap.put("properties", propertiesMap);
					List<JavaField> fields = getAllFields(classStruct.getJavaClass());
					for (int i = 0; i < fields.size(); i++) {
						JavaField javaField = fields.get(i);
						if (!javaField.isStatic() && !javaField.isFinal()) {
							String fieldTypeName = javaField.getType().getFullyQualifiedName();
							String comment = javaField.getComment();
							String genericFullyQualifiedFieldName = javaField.getType().getGenericFullyQualifiedName();
							String originalFieldTypeName = fieldTypeName;
							String binaryName = javaField.getType().getBinaryName();
							boolean array = false;
							if (javaTypes != null) {
								JavaType javaType = javaTypes.get(binaryName);
								if (javaType != null) {
									DefaultJavaParameterizedType dt = (DefaultJavaParameterizedType) javaType;
									array = ClassTypeUtil.isArray(dt.getBinaryName());
									List<JavaType> typeArguments = dt.getActualTypeArguments();
									genericFullyQualifiedFieldName = dt.getGenericFullyQualifiedName();
									MethodParameterData fieldparameterData = new MethodParameterData();
									fieldparameterData.setType(genericFullyQualifiedFieldName);
									propertiesMap.put(javaField.getName(), fieldparameterData);
									if (dt.getBinaryName().equals("java.util.Map")) {
										continue;
									} else {
										if (!typeArguments.isEmpty()) {
											JavaType typeArgument = typeArguments.get(0);
											if (typeArgument != null) {
												if (typeArgument.getBinaryName().equals("java.util.Map")) {
													continue;
												}
												originalFieldTypeName = typeArgument.getFullyQualifiedName();
												binaryName = typeArgument.getBinaryName();
												genericFullyQualifiedFieldName = typeArgument
														.getGenericFullyQualifiedName();
											}
										}
									}
								}
							}
							if (fieldTypeName.endsWith("[]")) {
								originalFieldTypeName = fieldTypeName.substring(0, fieldTypeName.lastIndexOf("[]"));
								array = true;
							} else if (ClassTypeUtil.isArray(binaryName)) {
								DefaultJavaField df = (DefaultJavaField) javaField;
								DefaultJavaParameterizedType dt = (DefaultJavaParameterizedType) df.getType();
								List<JavaType> dtList = dt.getActualTypeArguments();
								if (!dtList.isEmpty()) {
									originalFieldTypeName = dtList.get(0).getGenericFullyQualifiedName();
									genericFullyQualifiedFieldName = originalFieldTypeName;
									array = true;
								}
							}
							if (javaTypes != null) {
								JavaType javaType = javaTypes.get(originalFieldTypeName);
								if (javaType != null) {
									originalFieldTypeName = javaType.getFullyQualifiedName();
									genericFullyQualifiedFieldName = javaType.getGenericFullyQualifiedName();
								}
							}
							typeData = TypesConverter.getType(originalFieldTypeName);
							if (typeData != null) {
								if (array) {
									SchemaData schema = new SchemaData();
									schema.setType("array");
									ItemsData items = new ItemsData();
									items.setType(typeData.getType());
									items.setFormat(typeData.getFormat());
									if (comment != null) {
										items.setDescription(comment);
									}
									schema.setItems(items);
									propertiesMap.put(javaField.getName(), schema);
								} else {
									MethodParameterData fieldparameterData = new MethodParameterData();
									fieldparameterData.setType(typeData.getType());
									fieldparameterData.setFormat(typeData.getFormat());
									if (comment != null) {
										fieldparameterData.setDescription(comment);
									}
									propertiesMap.put(javaField.getName(), fieldparameterData);
								}
							} else {
								if (ClassTypeUtil.isArray(originalFieldTypeName)) {
									JavaType javaType = javaTypes.get(binaryName);
									array = true;
									if (javaType != null) {
										DefaultJavaParameterizedType dt = (DefaultJavaParameterizedType) javaType;
										List<JavaType> typeArguments = dt.getActualTypeArguments();
										if (!typeArguments.isEmpty()) {
											JavaType typeArgument = typeArguments.get(0);
											originalFieldTypeName = typeArgument.getFullyQualifiedName();
											genericFullyQualifiedFieldName = typeArgument
													.getGenericFullyQualifiedName();
										}
									}
								}
								AtomicInteger count = null;
								if (counter != null && (count = counter.get(originalFieldTypeName)) != null) {
									if (count.getAndIncrement() > 2)
										return;
								} else if (originalFieldTypeName.equals(originalFullyQualifiedName)) {
									counter = new HashMap<String, AtomicInteger>();
									counter.put(originalFullyQualifiedName, new AtomicInteger(1));
								}
								if (classStructs.get(originalFieldTypeName) != null) {
									parserFields(javaField.getType(), classStructs, definitionsMap, null, counter);
									SchemaData schema = new SchemaData();
									if (array) {
										schema.setType("array");
										ItemsData items = new ItemsData();
										if (genericFullyQualifiedFieldName.contains(">")) {
											items.set$ref("#/definitions/"
													+ genericFullyQualifiedFieldName/*
																					 * .replaceAll("<",
																					 * "«").replaceAll(">","»")
																					 */);
										} else {
											items.set$ref("#/definitions/" + originalFieldTypeName);
										}
										schema.setItems(items);
									} else {
										if (genericFullyQualifiedFieldName.contains(">")) {
											schema.set$ref("#/definitions/"
													+ genericFullyQualifiedFieldName/*
																					 * .replaceAll("<",
																					 * "«").replaceAll(">","»")
																					 */);
										} else {
											schema.set$ref("#/definitions/" + genericFullyQualifiedFieldName);
										}
									}
									propertiesMap.put(javaField.getName(), schema);
								}
							}
						}
					}
					if (genericFullyQualifiedName.contains(">")) {
						definitionsMap.putIfAbsent(
								genericFullyQualifiedName/* .replaceAll("<", "«").replaceAll(">","»") */, objMap);
					} else {
						definitionsMap.putIfAbsent(typeName, objMap);
					}

				}
			}
		}
	}

	public static List<JavaField> getAllFields(JavaClass javaClass) {
		List<JavaField> javaFieldList = javaClass.getFields();
		while ((javaClass.getSuperClass() != null && !javaClass.getSuperClass().getBinaryName().equals("Object"))) {
			javaClass = javaClass.getSuperJavaClass();
			javaFieldList.addAll(javaClass.getFields());
		}
		return javaFieldList;
	}

}
