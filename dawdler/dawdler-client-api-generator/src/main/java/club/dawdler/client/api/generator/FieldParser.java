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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.impl.DefaultJavaField;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;

import club.dawdler.client.api.generator.TypesConverter.TypeData;
import club.dawdler.client.api.generator.data.ItemsData;
import club.dawdler.client.api.generator.data.MethodParameterData;
import club.dawdler.client.api.generator.data.SchemaData;
import club.dawdler.client.api.generator.util.ClassTypeUtil;
import club.dawdler.client.api.generator.util.CommentUtils;

/**
 * @author jackson.song
 * @version V1.0
 * 类属性解析器
 */
public class FieldParser {

	public static void parserFields(JavaClass javaClass, Map<String, ClassStruct> classStructs,
			Map<String, MethodParameterData> params, boolean isArray, Map<String, Object> definitionsMap) {
		List<JavaField> fields = javaClass.getFields();
		JavaClass superJavaClass = javaClass.getSuperJavaClass();
		while (!superJavaClass.getBinaryName().equals("java.lang.Object")) {
			fields.addAll(superJavaClass.getFields());
			superJavaClass = superJavaClass.getSuperJavaClass();
		}
		for (JavaField field : fields) {
			if (!field.isFinal() && !field.isStatic()) {
				MethodParameterData parameterData = params.get(field.getName());
				if (parameterData == null) {
					parameterData = new MethodParameterData();
					parameterData.setName(field.getName());
					parameterData.setIn("query");
					if (!field.getType().isEnum()) {
						parameterData.setRequired(false);
						params.put(field.getName(), parameterData);
						String comment = field.getComment();
						if (comment != null && !comment.trim().isEmpty()) {
							comment = field.getName() + " " + comment;
							CommentUtils.setRule(comment, parameterData);
						} else {
							parameterData.setSchema(new SchemaData());
						}
						TypeDataParser.convertion(field.getType(), parameterData, classStructs, params, isArray,
								definitionsMap);
					} else {
						SchemaData schema = new SchemaData();
						String typeName = field.getType().getFullyQualifiedName();
						String genericFullyQualifiedName = field.getType().getGenericFullyQualifiedName();
						String type = null;
						if (genericFullyQualifiedName.contains(">")) {
							type = genericFullyQualifiedName;
						} else {
							type = typeName;
						}
						boolean array = false;
						if (ClassTypeUtil.isArray(field.getType().getBinaryName())) {
							type = ClassTypeUtil.getType0(field.getType());
							array = true;
						} else if (type.endsWith("[]")) {
							type = type.substring(0, type.lastIndexOf("[]"));
							array = true;
						}
						FieldParser.parserFields(field.getType(), classStructs, definitionsMap, null,
								parameterData.getDescription());
						if (definitionsMap.containsKey(type)) {
							parameterData.setRequired(true);
							params.put(field.getName(), parameterData);
							if (array) {
								schema.setType("array");
								ItemsData items = new ItemsData();
								items.set$ref("#/components/schemas/" + type);
								schema.setItems(items);
							} else {
								schema.set$ref("#/components/schemas/" + type);
							}
							parameterData.setSchema(schema);
						}
					}

				}
			}
		}
	}

	public static void parserMultipartParameterFields(String paramName, JavaClass javaClass,
			Map<String, ClassStruct> classStructs,
			Map<String, SchemaData> params, boolean isArray) {
		List<JavaField> fields = javaClass.getFields();
		JavaClass superJavaClass = javaClass.getSuperJavaClass();
		while (!superJavaClass.getBinaryName().equals("java.lang.Object")) {
			fields.addAll(superJavaClass.getFields());
			superJavaClass = superJavaClass.getSuperJavaClass();
		}
		for (JavaField field : fields) {
			if (!field.isFinal() && !field.isStatic()) {
				SchemaData schemaData = params.get(field.getName());
				if (schemaData == null) {
					schemaData = new SchemaData();
					params.put(field.getName(), schemaData);
					String comment = field.getComment();
					if (comment != null && !comment.trim().isEmpty()) {
						comment = field.getName() + " " + comment;
						CommentUtils.setRule(comment, schemaData);
					}
					if (!field.getType().isEnum()) {
						TypeDataParser.convertionMultipartParameter(paramName, field.getType(), schemaData,
								classStructs, params, isArray);
					}

				}
			}
		}
	}

	public static void parserFields(JavaType fieldJavaType, Map<String, ClassStruct> classStructs,
			Map<String, Object> definitionsMap, Map<String, JavaType> javaTypes, String paramComment) {
		parserFields(fieldJavaType, classStructs, definitionsMap, javaTypes, null, paramComment);
	}

	public static void parserFields(JavaType fieldJavaType, Map<String, ClassStruct> classStructs,
			Map<String, Object> definitionsMap, Map<String, JavaType> javaTypes, Map<String, AtomicInteger> counter,
			String paramComment) {
		String typeName = fieldJavaType.getBinaryName();
		String genericFullyQualifiedName = fieldJavaType.getGenericFullyQualifiedName();
		String originalFullyQualifiedName = fieldJavaType.getFullyQualifiedName();
		boolean isEnum = false;
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
					isEnum = classStruct.getJavaClass().isEnum();
					Map<String, Object> objMap = new LinkedHashMap<>();
					Map<String, Object> propertiesMap = new LinkedHashMap<>();
					if (!isEnum) {
						objMap.put("type", "object");
						if (genericFullyQualifiedName.contains(">")) {
							objMap.put("title",
									genericFullyQualifiedName/* .replaceAll("<", "«").replaceAll(">","»") */);
						} else {
							objMap.put("title", typeName);
						}
						objMap.put("properties", propertiesMap);
					}
					Set<String> required = new HashSet<>();
					List<JavaField> fields;
					SchemaData enumSchemaData = null;
					if (isEnum) {
						enumSchemaData = new SchemaData();
						enumSchemaData.setType("string");
						fields = classStruct.getJavaClass().getEnumConstants();
					} else {
						fields = getAllFields(classStruct.getJavaClass());
					}
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
								if (javaType instanceof DefaultJavaParameterizedType dt) {
									array = ClassTypeUtil.isArray(dt.getBinaryName());
									List<JavaType> typeArguments = dt.getActualTypeArguments();
									genericFullyQualifiedFieldName = dt.getGenericFullyQualifiedName();
									MethodParameterData fieldParameterData = new MethodParameterData();
									if (genericFullyQualifiedFieldName.equals("java.lang.Void")) {
										fieldParameterData.setType("object");
										fieldParameterData.setNullable(true);

									} else {
										fieldParameterData.setType(genericFullyQualifiedFieldName);
									}
									propertiesMap.put(javaField.getName(), fieldParameterData);
									if (dt.getBinaryName().equals("java.util.Map")) {
										continue;
									} else {
										if (!typeArguments.isEmpty()) {
											JavaType typeArgument = typeArguments.get(0);
											if (typeArgument != null) {
												if (typeArgument.getBinaryName().equals("java.util.Map")) {
													continue;
												}
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
									schema.setItems(items);
									if (comment != null) {
										if (CommentUtils.setRuleForArray(comment, schema)) {
											required.add(javaField.getName());
										}
									}
									propertiesMap.put(javaField.getName(), schema);
								} else {
									SchemaData schema = new SchemaData();
									schema.setType(typeData.getType());
									schema.setFormat(typeData.getFormat());
									if (comment != null) {
										if (CommentUtils.setRule(comment, schema)) {
											required.add(javaField.getName());
										}
									}
									propertiesMap.put(javaField.getName(), schema);
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
								if (!isEnum) {
									AtomicInteger count = null;
									if (counter != null && (count = counter.get(originalFieldTypeName)) != null) {
										if (count.getAndIncrement() > 2)
											continue;
									} else if (originalFieldTypeName.equals(originalFullyQualifiedName)) {
										counter = new HashMap<>();
										counter.put(originalFullyQualifiedName, new AtomicInteger(1));
									}
								}
								if (classStructs.get(originalFieldTypeName) != null) {
									SchemaData schema = new SchemaData();
									if (!isEnum) {
										parserFields(javaField.getType(), classStructs, definitionsMap, null, counter,
												paramComment);
										if (array) {
											schema.setType("array");
											ItemsData items = new ItemsData();
											if (genericFullyQualifiedFieldName.contains(">")) {
												items.set$ref("#/components/schemas/"
														+ genericFullyQualifiedFieldName/*
																						 * .replaceAll("<",
																						 * "«").replaceAll(">","»")
																						 */);
											} else {
												items.set$ref("#/components/schemas/" + originalFieldTypeName);
											}
											schema.setItems(items);
										} else {
											if (genericFullyQualifiedFieldName.contains(">")) {
												schema.set$ref("#/components/schemas/"
														+ genericFullyQualifiedFieldName/*
																						 * .replaceAll("<",
																						 * "«").replaceAll(">","»")
																						 */);
											} else {
												schema.set$ref(
														"#/components/schemas/" + genericFullyQualifiedFieldName);
											}
										}
										propertiesMap.put(javaField.getName(), schema);
									} else {
										if (enumSchemaData.getEnumList() == null) {
											enumSchemaData.setEnumList(new ArrayList<>());
										}
										enumSchemaData.getEnumList().add(javaField.getName());
										if (javaField.getComment() != null) {
											if (enumSchemaData.getDescription() == null) {
												enumSchemaData.setDescription(paramComment == null ? ""
														: paramComment + " : " +
																javaField.getName() + "->" + javaField.getComment()
																+ " | ");
											} else {
												enumSchemaData.setDescription(enumSchemaData.getDescription()
														+ javaField.getName() + "->" + javaField.getComment() + " | ");
											}
										}
									}

								}
							}
						}
					}
					if (isEnum) {
						definitionsMap.putIfAbsent(typeName, enumSchemaData);
						if (!required.isEmpty()) {
							objMap.put("required", required);
						}
					} else {
						if (genericFullyQualifiedName.contains(">")) {
							definitionsMap.putIfAbsent(
									genericFullyQualifiedName.replaceAll("<", "_").replaceAll(">", "_"),
									objMap);
						} else {
							definitionsMap.putIfAbsent(typeName, objMap);
						}
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
