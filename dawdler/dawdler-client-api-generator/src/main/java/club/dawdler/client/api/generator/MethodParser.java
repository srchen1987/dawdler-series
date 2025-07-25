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

import club.dawdler.client.api.generator.TypesConverter.TypeData;
import club.dawdler.client.api.generator.data.ClassStruct;
import club.dawdler.client.api.generator.data.ItemsData;
import club.dawdler.client.api.generator.data.MethodParameterData;
import club.dawdler.client.api.generator.data.ParserTypeData;
import club.dawdler.client.api.generator.data.ResponseData;
import club.dawdler.client.api.generator.data.SchemaData;
import club.dawdler.client.api.generator.util.AnnotationUtils;
import club.dawdler.client.api.generator.util.ClassTypeUtil;
import club.dawdler.clientplug.web.annotation.CookieValue;
import club.dawdler.clientplug.web.annotation.PathVariable;
import club.dawdler.clientplug.web.annotation.RequestAttribute;
import club.dawdler.clientplug.web.annotation.RequestBody;
import club.dawdler.clientplug.web.annotation.RequestHeader;
import club.dawdler.clientplug.web.annotation.RequestMapping;
import club.dawdler.clientplug.web.annotation.RequestMapping.RequestMethod;
import club.dawdler.clientplug.web.annotation.RequestParam;
import club.dawdler.clientplug.web.annotation.ResponseBody;
import club.dawdler.clientplug.web.annotation.SessionAttribute;
import club.dawdler.clientplug.web.plugs.AbstractDisplayPlug;
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

/**
 * @author jackson.song
 * @version V1.0
 * 方法解析器
 */
public class MethodParser {
	private MethodParser() {
	}

	private static final ResponseData RESPONSE_401 = new ResponseData("Unauthorized");
	private static final ResponseData RESPONSE_403 = new ResponseData("Forbidden");
	private static final ResponseData RESPONSE_404 = new ResponseData("Not Found");
	private static final String BRACKET = "[]";
	private static String[] allTypeArray = { "*/*" };
	private static String[] textArray = { AbstractDisplayPlug.MIME_TYPE_TEXT_HTML };
	private static String[] jsonArray = { AbstractDisplayPlug.MIME_TYPE_JSON };
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
			List<JavaAnnotation> methodAnnotations = method.getAnnotations();
			String[] requestClassMappingArray = AnnotationUtils.getAnnotationStringArrayValue(requestMappingAnnotation,
					"value");
			String[] requestMappingArray = null;
			List<String> httpMethods = new ArrayList<>(8);
			boolean responseBody = false;
			boolean requestBody = false;
			for (JavaAnnotation annotation : methodAnnotations) {
				if (ResponseBody.class.getName().equals(annotation.getType().getBinaryName())) {
					responseBody = true;
				}
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

			Map<String, MethodParameterData> params = new LinkedHashMap<>();
			Map<String, MethodParameterData> methodParameterMap = new LinkedHashMap<>();
			List<DocletTag> tags = method.getTags();
			for (DocletTag tag : tags) {
				if (tag.getName().equals("param")) {
					MethodParameterData parameterData = new MethodParameterData();
					List<String> parameterList = tag.getParameters();
					if (parameterList.size() == 1) {
						parameterData.setName(parameterList.get(0));
					} else if (parameterList.size() == 2) {
						parameterData.setName(parameterList.get(0));
						StringBuffer sb = new StringBuffer();
						for (int i = 1; i < parameterList.size(); i++) {
							sb.append(parameterList.get(i) + " ");
						}
						parameterData.setDescription(sb.toString());
					}
					params.put(parameterData.getName(), parameterData);
				}
			}
			JavaType returnType = method.getReturnType();

			List<JavaParameter> javaParameters = method.getParameters();
			for (JavaParameter javaParameter : javaParameters) {
				List<JavaAnnotation> paramAnnotationList = javaParameter.getAnnotations();
				String alias = null;
				String in = "query";
				SchemaData schema = null;
				boolean required = false;
				boolean add = true;
				if (!paramAnnotationList.isEmpty()) {
					for (JavaAnnotation paramAnnotation : paramAnnotationList) {
						String annotationName = paramAnnotation.getType().getFullyQualifiedName();
						if (annotationName.equals(SessionAttribute.class.getName()) || annotationName.equals(RequestAttribute.class.getName()) || annotationName.equals(CookieValue.class.getName())) {
							add = false;
							break;
						}
						if (annotationName.equals(RequestParam.class.getName())) {
							AnnotationValue annotationValue = paramAnnotation.getProperty("value");
							alias = getAnnotationValue(annotationValue, javaClass, classStructs);
						}
						if (annotationName.equals(PathVariable.class.getName())) {
							in = "path";
							AnnotationValue annotationValue = paramAnnotation.getProperty("value");
							alias = getAnnotationValue(annotationValue, javaClass, classStructs);
						} else if (annotationName.equals(RequestBody.class.getName())) {
							requestBody = true;
							in = "body";
						} else if (annotationName.equals(RequestHeader.class.getName())) {
							in = "header";
							AnnotationValue annotationValue = paramAnnotation.getProperty("value");
							alias = getAnnotationValue(annotationValue, javaClass, classStructs);
						}
					}
				}
				if(!add){
					continue;
				}
				if (alias == null || alias.trim().equals("")) {
					alias = javaParameter.getName();
				}
				MethodParameterData parameterData = params.get(alias);
				if (parameterData == null) {
					parameterData = new MethodParameterData();
					parameterData.setName(alias);
				}
				methodParameterMap.put(parameterData.getName(), parameterData);
				String typeName = javaParameter.getType().getFullyQualifiedName();
				String genericFullyQualifiedName = javaParameter.getType().getGenericFullyQualifiedName();
				if (in.equals("body")) {
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
					FieldParser.parserFields(javaParameter.getType(), classStructs, definitionsMap, null);
					if (definitionsMap.containsKey(type)) {
						schema = new SchemaData();
						required = true;
						if (array) {
							schema.setType("array");
							ItemsData items = new ItemsData();
							items.set$ref("#/definitions/" + type);
							schema.setItems(items);
						} else {
							schema.set$ref("#/definitions/" + type);
						}
					} else {
						schema = new SchemaData();
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
					}
				} else {
					ParserTypeData.convertion(javaParameter.getType(), parameterData, classStructs, methodParameterMap,
							false);
				}
				parameterData.setIn(in);
				parameterData.setRequired(required);
				parameterData.setSchema(schema);
			}
			MethodParameterData[] methodParameters = methodParameterMap.values().toArray(new MethodParameterData[0]);
			Map<String, Object> elements = new LinkedHashMap<>();
			elements.put("tags", new String[] { javaClass.getBinaryName() });
			DocletTag descriptionTag = method.getTagByName("Description");
			String summary = method.getComment();
			if (descriptionTag != null) {
				summary += descriptionTag.getValue();
			}
			elements.put("summary", summary);
			TypeData typeData = TypesConverter.getType(returnType.getBinaryName());
			if (responseBody) {
				if (typeData != null && !typeData.getType().equals("file")) {
					elements.put("produces", textArray);
				} else {
					elements.put("produces", jsonArray);
				}
			} else {
				elements.put("produces", allTypeArray);
			}
			if (requestBody) {
				elements.put("consumes", jsonArray);
			} else {
				elements.put("consumes", allTypeArray);
			}
			elements.put("parameters", methodParameters);
			if (returnType != null) {
				Map<String, JavaType> javaTypes = getActualTypesMap(method.getReturns());
				for (Entry<String, JavaType> entry : javaTypes.entrySet()) {
					parseType(method, entry.getValue(), classStructs, definitionsMap, javaTypes);
				}
				FieldParser.parserFields(returnType, classStructs, definitionsMap, javaTypes);
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
			FieldParser.parserFields(type, classStructs, definitionsMap, innerJavaTypes);
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
			}
		} else {
			String $ref;
			if (ClassTypeUtil.isArray(binaryName)) {
				List<JavaType> typeList = getActualTypeArguments(returnType);
				if (!typeList.isEmpty()) {
					schema.setType("array");
					$ref = typeList.get(0).getGenericFullyQualifiedName();
					if (definitionsMap.containsKey($ref)) {
						ItemsData items = new ItemsData();
						items.set$ref("#/definitions/" + $ref);
						schema.setItems(items);
					} else {
						ItemsData items = new ItemsData();
						items.setType("object");
						schema.setItems(items);
					}
				}
			} else {
				$ref = returnType.getGenericFullyQualifiedName();
				if (definitionsMap.containsKey($ref)) {
					schema.set$ref("#/definitions/" + $ref);
				} else {
					schema.setType("object");
				}
			}
		}
		ResponseData response200 = new ResponseData("ok", schema);
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
