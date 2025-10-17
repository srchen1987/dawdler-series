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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.thoughtworks.qdox.builder.impl.EvaluatingVisitor;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaGenericDeclaration;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaParameterizedType;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.JavaTypeVariable;
import com.thoughtworks.qdox.model.expression.AnnotationValue;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;
import com.thoughtworks.qdox.model.impl.DefaultJavaWildcardType;

import club.dawdler.client.api.generator.TypesConverter.TypeData;
import club.dawdler.client.api.generator.data.ContentTypeData;
import club.dawdler.client.api.generator.data.ItemsData;
import club.dawdler.client.api.generator.data.MethodParameterData;
import club.dawdler.client.api.generator.data.ResponseData;
import club.dawdler.client.api.generator.data.SchemaData;
import club.dawdler.client.api.generator.util.AnnotationUtils;
import club.dawdler.client.api.generator.util.ClassTypeUtil;
import club.dawdler.client.api.generator.util.CommentUtils;
import club.dawdler.clientplug.web.annotation.CookieValue;
import club.dawdler.clientplug.web.annotation.PathVariable;
import club.dawdler.clientplug.web.annotation.QueryParam;
import club.dawdler.clientplug.web.annotation.RequestAttribute;
import club.dawdler.clientplug.web.annotation.RequestBody;
import club.dawdler.clientplug.web.annotation.RequestHeader;
import club.dawdler.clientplug.web.annotation.RequestMapping;
import club.dawdler.clientplug.web.annotation.RequestMapping.RequestMethod;
import club.dawdler.clientplug.web.annotation.RequestParam;
import club.dawdler.clientplug.web.annotation.SessionAttribute;
import club.dawdler.clientplug.web.upload.UploadFile;

/**
 * @author jackson.song
 * @version V1.0
 * 方法解析器
 */
public class MethodParser {
	public static final String MIME_TYPE_TEXT_HTML = "text/html";
	public static final String MIME_TYPE_JSON = "application/json";

	private MethodParser() {
	}

	private static final ResponseData RESPONSE_401 = new ResponseData("Unauthorized");
	private static final ResponseData RESPONSE_403 = new ResponseData("Forbidden");
	private static final ResponseData RESPONSE_404 = new ResponseData("Not Found");
	private static final String BRACKET = "[]";
	private static EvaluatingVisitor evaluatingVisitor = new EvaluatingVisitor();

	private static Map<String, String> requestMethodCache = new HashMap<String, String>() {
		private static final long serialVersionUID = 8332918465511505167L;
		{
			put("GET", "get");
			put("POST", "post");
			put("HEAD", "head");
			put("PUT", "put");
			put("DELETE", "delete");
			put("OPTIONS", "options");
			put("TRACE", "trace");
		}
	};

	public static void generateMethodParamCode(Map<String, Object> pathMap,
			Map<String, ClassStruct> classStructs, Map<String, Object> definitionsMap, JavaClass javaClass,
			JavaAnnotation requestMappingAnnotation) {
		List<JavaMethod> methods = javaClass.getMethods();
		for (JavaMethod method : methods) {
			Map<String, Object> elements = new LinkedHashMap<>();
			List<JavaAnnotation> methodAnnotations = method.getAnnotations();
			String[] requestClassMappingArray = AnnotationUtils.getAnnotationStringArrayValue(requestMappingAnnotation,
					"value");
			String[] requestMappingArray = null;
			List<String> httpMethods = new ArrayList<>(8);
			for (JavaAnnotation annotation : methodAnnotations) {
				if (RequestMapping.class.getName().equals(annotation.getType().getBinaryName())) {
					requestMappingArray = AnnotationUtils.getAnnotationStringArrayValue(annotation, "value");
					Object annotationMethodObj = AnnotationUtils.getAnnotationObjectValue(annotation, "method");
					if (annotationMethodObj == null) {
						for (RequestMethod requestMethod : RequestMethod.values()) {
							httpMethods.add(requestMethod.name().toLowerCase());
						}
					} else if (annotationMethodObj.getClass() == String.class) {
						httpMethods.add(requestMethodCache.get(getHttpMethod(annotationMethodObj.toString())));
					} else if (annotationMethodObj.getClass() == LinkedList.class) {
						@SuppressWarnings("unchecked")
						List<String> httpMethodList = (List<String>) annotationMethodObj;
						for (String httpMethod : httpMethodList) {
							httpMethods.add(requestMethodCache.get(getHttpMethod(httpMethod)));
						}
					}
				}
			}
			if (httpMethods.isEmpty()) {
				continue;
			}

			Map<String, MethodParameterData> docParams = new LinkedHashMap<>();
			Map<String, MethodParameterData> methodParameterMap = new LinkedHashMap<>();
			Map<String, SchemaData> multipartParameterMap = new LinkedHashMap<>();
			boolean hasMultipart = false;
			SchemaData requestBodyData = null;
			List<String> requiredList = null;
			List<DocletTag> tags = method.getTags();
			for (DocletTag tag : tags) {
				if (tag.getName().equals("param")) {
					MethodParameterData parameterData = new MethodParameterData();
					if (tag.getValue() == null || tag.getValue().isEmpty()) {
						continue;
					}
					CommentUtils.setRule(tag.getValue(), parameterData);
					docParams.put(parameterData.getName(), parameterData);
				}
			}
			JavaType returnType = method.getReturnType();

			List<JavaParameter> javaParameters = method.getParameters();
			for (JavaParameter javaParameter : javaParameters) {
				if (javaParameter.getType().getFullyQualifiedName().equals(UploadFile.class.getName())) {
					hasMultipart = true;
					break;
				}
			}
			for (JavaParameter javaParameter : javaParameters) {
				List<JavaAnnotation> paramAnnotationList = javaParameter.getAnnotations();
				String alias = null;
				String in = "query";
				boolean requestBody = false;
				boolean required = false;
				boolean add = true;
				boolean isQueryParam = false;
				boolean isEnum = javaParameter.getJavaClass().isEnum();
				if (!paramAnnotationList.isEmpty()) {
					for (JavaAnnotation paramAnnotation : paramAnnotationList) {
						String annotationName = paramAnnotation.getType().getFullyQualifiedName();
						if (annotationName.equals(SessionAttribute.class.getName())
								|| annotationName.equals(RequestAttribute.class.getName())
								|| annotationName.equals(CookieValue.class.getName())) {
							add = false;
							break;
						}
						if (annotationName.equals(RequestParam.class.getName())) {
							AnnotationValue annotationValue = paramAnnotation.getProperty("value");
							alias = getAnnotationValue(annotationValue, javaClass, classStructs);
						} else if (annotationName.equals(PathVariable.class.getName())) {
							in = "path";
							required = true;
							AnnotationValue annotationValue = paramAnnotation.getProperty("value");
							alias = getAnnotationValue(annotationValue, javaClass, classStructs);
						} else if (annotationName.equals(RequestBody.class.getName())) {
							requestBody = true;
							in = null;
						} else if (annotationName.equals(RequestHeader.class.getName())) {
							in = "header";
							AnnotationValue annotationValue = paramAnnotation.getProperty("value");
							alias = getAnnotationValue(annotationValue, javaClass, classStructs);
						} else if (annotationName.equals(QueryParam.class.getName())) {
							isQueryParam = true;
							AnnotationValue annotationValue = paramAnnotation.getProperty("value");
							alias = getAnnotationValue(annotationValue, javaClass, classStructs);
						}
					}
				}

				if (!add) {
					continue;
				}
				if (alias == null || alias.trim().equals("")) {
					alias = javaParameter.getName();
				}
				SchemaData schema = null;
				MethodParameterData parameterData = docParams.get(alias);
				if (parameterData == null) {
					parameterData = new MethodParameterData();
					parameterData.setName(alias);
				} else {
					schema = parameterData.getSchema();
				}
				if (schema == null) {
					schema = new SchemaData();
					parameterData.setSchema(schema);
				}
				String typeName = javaParameter.getType().getFullyQualifiedName();
				String genericFullyQualifiedName = javaParameter.getType().getGenericFullyQualifiedName();
				if (requestBody) {
					String type = null;
					if (genericFullyQualifiedName.contains(">")) {
						type = genericFullyQualifiedName;
					} else {
						type = typeName;
					}
					boolean array = false;
					if (ClassTypeUtil.isArray(javaParameter.getType().getBinaryName())) {
						type = ClassTypeUtil.getType0(javaParameter.getType());
						array = true;
					} else if (type.endsWith("[]")) {
						type = type.substring(0, type.lastIndexOf("[]"));
						array = true;
					}
					FieldParser.parserFields(javaParameter.getType(), classStructs, definitionsMap, null,
							parameterData.getDescription());
					if (definitionsMap.containsKey(type)) {
						required = true;
						if (array) {
							schema.setType("array");
							ItemsData items = new ItemsData();
							items.set$ref("#/components/schemas/" + type);
							schema.setItems(items);
						} else {
							schema.set$ref("#/components/schemas/" + type);
						}
						requestBodyData = schema;
					} else {
						required = true;
						if (array) {
							schema.setType("array");
						}
						ItemsData items = new ItemsData();
						TypeData typeData = TypesConverter.getType(type);
						if (typeData != null) {
							items.setType(typeData.getType());
							items.setFormat(typeData.getFormat());
						}
						schema.setItems(items);
						requestBodyData = schema;
					}
					ContentTypeData requestBodyMap = new ContentTypeData();
					requestBodyMap.setRequired(true);
					Map<String, Map<String, SchemaData>> content = new HashMap<>();
					Map<String, SchemaData> schemaDataMap = new HashMap<>();
					schemaDataMap.put("schema", requestBodyData);
					content.put(MIME_TYPE_JSON, schemaDataMap);
					requestBodyMap.setContent(content);
					elements.put("requestBody", requestBodyMap);
				} else {
					if (hasMultipart) {
						if (isQueryParam) {
							if (isEnum) {
								String type = null;
								if (genericFullyQualifiedName.contains(">")) {
									type = genericFullyQualifiedName;
								} else {
									type = typeName;
								}
								boolean array = false;
								if (ClassTypeUtil.isArray(javaParameter.getType().getBinaryName())) {
									type = ClassTypeUtil.getType0(javaParameter.getType());
									array = true;
								} else if (type.endsWith("[]")) {
									type = type.substring(0, type.lastIndexOf("[]"));
									array = true;
								}
								FieldParser.parserFields(javaParameter.getType(), classStructs, definitionsMap, null,
										parameterData.getDescription());
								if (definitionsMap.containsKey(type)) {
									required = true;
									if (array) {
										schema.setType("array");
										ItemsData items = new ItemsData();
										items.set$ref("#/components/schemas/" + type);
										schema.setItems(items);
									} else {
										schema.set$ref("#/components/schemas/" + type);
									}
									requestBodyData = schema;
								}
								Map<String, SchemaData> schemaDataMap = new HashMap<>();
								schemaDataMap.put("schema", requestBodyData);
								parameterData.setSchema(schema);
								methodParameterMap.put(parameterData.getName(), parameterData);
							} else {
								methodParameterMap.put(parameterData.getName(), parameterData);
								TypeDataParser.convertion(javaParameter.getType(), parameterData, classStructs,
										methodParameterMap,
										false, definitionsMap);
							}
						} else {
							schema.setDescription(parameterData.getDescription());
							if (parameterData.getRequired() != null && parameterData.getRequired()) {
								if (requiredList == null) {
									requiredList = new ArrayList<>();
								}
								requiredList.add(parameterData.getName());
							}
							if (isEnum) {
								String type = null;
								if (genericFullyQualifiedName.contains(">")) {
									type = genericFullyQualifiedName;
								} else {
									type = typeName;
								}
								boolean array = false;
								if (ClassTypeUtil.isArray(javaParameter.getType().getBinaryName())) {
									type = ClassTypeUtil.getType0(javaParameter.getType());
									array = true;
								} else if (type.endsWith("[]")) {
									type = type.substring(0, type.lastIndexOf("[]"));
									array = true;
								}
								FieldParser.parserFields(javaParameter.getType(), classStructs, definitionsMap, null,
										parameterData.getDescription());
								if (definitionsMap.containsKey(type)) {
									required = true;
									if (array) {
										schema.setType("array");
										ItemsData items = new ItemsData();
										items.set$ref("#/components/schemas/" + type);
										schema.setItems(items);
									} else {
										schema.set$ref("#/components/schemas/" + type);
									}
									requestBodyData = schema;
								}
								multipartParameterMap.put(parameterData.getName(), schema);
							} else {
								multipartParameterMap.put(parameterData.getName(), schema);
								TypeDataParser.convertionMultipartParameter(parameterData.getName(),
										javaParameter.getType(), schema, classStructs,
										multipartParameterMap,
										false);
							}

						}
					} else {
						if (isEnum) {
							String type = null;
							if (genericFullyQualifiedName.contains(">")) {
								type = genericFullyQualifiedName;
							} else {
								type = typeName;
							}
							boolean array = false;
							if (ClassTypeUtil.isArray(javaParameter.getType().getBinaryName())) {
								type = ClassTypeUtil.getType0(javaParameter.getType());
								array = true;
							} else if (type.endsWith("[]")) {
								type = type.substring(0, type.lastIndexOf("[]"));
								array = true;
							}
							FieldParser.parserFields(javaParameter.getType(), classStructs, definitionsMap, null,
									parameterData.getDescription());
							if (definitionsMap.containsKey(type)) {
								required = true;
								if (array) {
									schema.setType("array");
									ItemsData items = new ItemsData();
									items.set$ref("#/components/schemas/" + type);
									schema.setItems(items);
								} else {
									schema.set$ref("#/components/schemas/" + type);
								}
								requestBodyData = schema;
							}
							Map<String, SchemaData> schemaDataMap = new HashMap<>();
							schemaDataMap.put("schema", requestBodyData);
							parameterData.setSchema(schema);
							methodParameterMap.put(parameterData.getName(), parameterData);
						} else {
							methodParameterMap.put(parameterData.getName(), parameterData);
							TypeDataParser.convertion(javaParameter.getType(), parameterData, classStructs,
									methodParameterMap,
									false, definitionsMap);
						}

					}

				}
				parameterData.setIn(in);
				if (parameterData.getRequired() == null) {
					parameterData.setRequired(required);
				}
			}
			MethodParameterData[] methodParameters = methodParameterMap.values().toArray(new MethodParameterData[0]);
			elements.put("tags", new String[] { javaClass.getBinaryName() });
			DocletTag descriptionTag = method.getTagByName("Description");
			String summary = method.getComment();
			if (descriptionTag != null) {
				summary += descriptionTag.getValue();
			}
			elements.put("summary", summary);
			if (methodParameters != null && methodParameters.length > 0) {
				elements.put("parameters", methodParameters);
			}
			if (!multipartParameterMap.isEmpty()) {
				Map<String, Object> contentMap = new HashMap<>();
				Map<String, Object> multipartFormData = new HashMap<>();
				Map<String, Object> schema = new HashMap<>();
				Map<String, Object> properties = new HashMap<>();
				properties.put("type", "object");
				contentMap.put("content", multipartFormData);
				multipartFormData.put("multipart/form-data", schema);
				schema.put("schema", properties);
				if (requiredList != null) {
					properties.put("required", requiredList);
				}
				properties.put("properties", multipartParameterMap);
				elements.put("requestBody", contentMap);
			}
			if (returnType != null) {
				Map<String, JavaType> javaTypes = getActualTypesMap(method.getReturns());
				for (Entry<String, JavaType> entry : javaTypes.entrySet()) {
					parseType(method, entry.getValue(), classStructs, definitionsMap, javaTypes);
				}
				FieldParser.parserFields(returnType, classStructs, definitionsMap, javaTypes, null);
				elements.put("responses", getResponse(returnType, definitionsMap));
			}
			if (requestMappingArray != null) {
				for (String mapping : requestMappingArray) {
					if (requestClassMappingArray != null) {
						int i = 0;
						for (String classMapping : requestClassMappingArray) {
							pathMap.put(classMapping + mapping, createHttpMethod(httpMethods, elements, method, i++));
						}
					} else {
						pathMap.put(mapping, createHttpMethod(httpMethods, elements, method, null));
					}

				}
			}

		}
	}

	private static Map<String, Object> createHttpMethod(List<String> httpMethods, Map<String, Object> elements,
			JavaMethod method, Integer index) {
		Map<String, Object> httpMethodMap = new LinkedHashMap<>();
		for (String httpMethod : httpMethods) {
			Map<String, Object> elementsCopy = new LinkedHashMap<>();
			elementsCopy.putAll(elements);
			elementsCopy.put("operationId",
					method.getName() + "Using" + httpMethod.toUpperCase() + (index == null ? "" : "_" + index));
			httpMethodMap.put(httpMethod, elementsCopy);
		}
		return httpMethodMap;
	}

	public static void parseType(JavaMethod method, JavaType type, Map<String, ClassStruct> classStructs,
			Map<String, Object> definitionsMap, Map<String, JavaType> javaTypes) {
		List<JavaType> actualTypeArguments = null;
		List<JavaTypeVariable<JavaGenericDeclaration>> typeList = null;
		Map<String, JavaType> innerJavaTypes = new HashMap<>();
		try {
			if (type instanceof DefaultJavaWildcardType) {
				System.out.println(method + ":" + " not support wildcard type <?> !\r\n");
				return;
			}
			if (type instanceof DefaultJavaParameterizedType) {
				DefaultJavaParameterizedType dt = (DefaultJavaParameterizedType) type;
				typeList = dt.getTypeParameters();
				actualTypeArguments = getActualTypeArguments(dt);
				if (!typeList.isEmpty()) {
					if (actualTypeArguments.size() != typeList.size()) {
						System.out.println(method + ":" + type + " not defined type " + typeList + " !\r\n");
					} else {
						for (int i = 0; i < typeList.size(); i++) {
							parseType(method, actualTypeArguments.get(i), classStructs, definitionsMap, innerJavaTypes);
							innerJavaTypes.put(typeList.get(i).getBinaryName(), actualTypeArguments.get(i));
						}
					}
				}
			}
			FieldParser.parserFields(type, classStructs, definitionsMap, innerJavaTypes, null);
		} catch (Exception e) {
			System.out.println(method + " failed");
			e.printStackTrace();
		}

	}

	public static Map<String, JavaType> getActualTypesMap(JavaClass javaClass) {
		Map<String, JavaType> typeMapping = new HashMap<>(8);
		List<JavaTypeVariable<JavaGenericDeclaration>> parameters = javaClass.getTypeParameters();
		if (parameters.isEmpty()) {
			return typeMapping;
		}
		List<JavaType> javaTypes = getActualTypeArguments(javaClass);
		if (javaTypes.isEmpty()) {
			return typeMapping;
		}
		for (int i = 0; i < parameters.size(); i++) {
			typeMapping.put(parameters.get(i).getName(), javaTypes.get(i));
		}
		return typeMapping;
	}

	public static List<JavaType> getActualTypeArguments(JavaType javaType) {
		if (javaType == null) {
			return Collections.emptyList();
		}
		return ((JavaParameterizedType) javaType).getActualTypeArguments();
	}

	public static Map<String, Object> getResponse(JavaType returnType, Map<String, Object> definitionsMap) {
		String responseType = MIME_TYPE_JSON;
		Map<String, Object> response = new LinkedHashMap<>();
		SchemaData schema = new SchemaData();
		String genericFullyQualifiedName = returnType.getGenericFullyQualifiedName();
		boolean array = false;
		if (genericFullyQualifiedName.endsWith(BRACKET)) {
			array = true;
		}
		String binaryName = returnType.getBinaryName();
		TypeData type = TypesConverter.getType(binaryName);
		if (type != null) {
			if (array) {
				schema.setType("array");
				ItemsData items = new ItemsData();
				items.setType(type.getType());
				items.setFormat(type.getFormat());
				schema.setItems(items);
			} else {
				schema.setType(type.getType());
				schema.setFormat(type.getFormat());
				responseType = MIME_TYPE_TEXT_HTML;
			}
		} else {
			String $ref;
			if (ClassTypeUtil.isArray(binaryName)) {
				List<JavaType> typeList = getActualTypeArguments(returnType);
				if (!typeList.isEmpty()) {
					schema.setType("array");
					$ref = typeList.get(0).getGenericFullyQualifiedName().replaceAll("<", "_").replaceAll(">", "_");
					if (definitionsMap.containsKey($ref)) {
						ItemsData items = new ItemsData();
						items.set$ref("#/components/schemas/" + $ref);
						schema.setItems(items);
					} else {
						ItemsData items = new ItemsData();
						items.setType("object");
						schema.setItems(items);
					}
				}
			} else {
				$ref = returnType.getGenericFullyQualifiedName().replaceAll("<", "_").replaceAll(">", "_");
				if (definitionsMap.containsKey($ref)) {
					schema.set$ref("#/components/schemas/" + $ref);
				} else {
					schema.setType("object");
				}
			}
		}
		Map<String, Map<String, SchemaData>> content = new HashMap<>();
		Map<String, SchemaData> schemaData = new HashMap<>();
		schemaData.put("schema", schema);
		content.put(responseType, schemaData);
		ResponseData response200 = new ResponseData("ok", content);
		response.put("200", response200);
		response.put("401", RESPONSE_401);
		response.put("403", RESPONSE_403);
		response.put("404", RESPONSE_404);
		return response;
	}

	public static String getHttpMethod(String method) {
		if (method == null) {
			return null;
		}
		int index = method.lastIndexOf(".");
		if (index == -1) {
			return method;
		}
		return method.substring(index + 1, method.length());
	}

	public static String getAnnotationValue(AnnotationValue annotationValue, JavaClass javaClass,
			Map<String, ClassStruct> classStructs) {
		String value = null;
		if (annotationValue != null) {
			String parameterValue = (String) annotationValue.getParameterValue();
			int index = parameterValue.lastIndexOf(".");
			if (index > 0) {
				String className = parameterValue.substring(0, index);
				String fieldName = parameterValue.substring(index + 1, parameterValue.length());
				String samePackageClassName = javaClass.getPackageName() + "." + className;
				ClassStruct classStruct = classStructs.get(samePackageClassName);
				if (classStruct == null) {
					List<String> packages = classStructs.get(javaClass.getFullyQualifiedName()).getImportPackages();
					for (String classPackage : packages) {
						if (classPackage.endsWith(className)) {
							classStruct = classStructs.get(classPackage);
							if (classStruct != null) {
								break;
							}
						}
					}
				}
				if (classStruct != null) {
					value = classStruct.getJavaClass().getFieldByName(fieldName).getInitializationExpression()
							.replaceAll("\"", "");
				}
			}
			if (value == null) {
				value = (String) annotationValue.accept(evaluatingVisitor);
			}
		}

		return value;
	}

}
