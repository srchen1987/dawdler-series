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
package club.dawdler.clientplug.web.bind.resolver.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import club.dawdler.clientplug.web.annotation.CookieValue;
import club.dawdler.clientplug.web.annotation.DateTimeFormat;
import club.dawdler.clientplug.web.annotation.PathVariable;
import club.dawdler.clientplug.web.annotation.QueryParam;
import club.dawdler.clientplug.web.annotation.RequestAttribute;
import club.dawdler.clientplug.web.annotation.RequestBody;
import club.dawdler.clientplug.web.annotation.RequestHeader;
import club.dawdler.clientplug.web.annotation.RequestParam;
import club.dawdler.clientplug.web.annotation.SessionAttribute;
import club.dawdler.clientplug.web.bind.param.RequestParamFieldData;
import club.dawdler.clientplug.web.exception.ConvertException;
import club.dawdler.clientplug.web.handler.ViewForward;
import club.dawdler.clientplug.web.handler.WebValidateExecutor;
import club.dawdler.clientplug.web.plugs.AbstractDisplayPlug;
import club.dawdler.clientplug.web.util.CookieUtil;
import club.dawdler.clientplug.web.validator.ValidateParser;
import club.dawdler.clientplug.web.validator.entity.ControlField;
import club.dawdler.clientplug.web.validator.entity.ControlValidator;
import club.dawdler.clientplug.web.wrapper.BodyReaderHttpServletRequestWrapper;
import club.dawdler.util.ClassUtil;
import club.dawdler.util.DateUtil;
import club.dawdler.util.JsonProcessUtil;
import club.dawdler.util.JsonProcessUtil.TypeReferenceType;
import club.dawdler.util.SunReflectionFactoryInstantiator;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author jackson.song
 * @version V1.0
 * 获取mvc中常用方法参数注解相关参数值的决策者
 */
public class AnnotationMethodArgumentResolver extends AbstractMethodArgumentResolver {

	@Override
	public boolean isSupport(RequestParamFieldData requestParamFieldData) {
		return (requestParamFieldData.hasAnnotation(PathVariable.class)
				|| requestParamFieldData.hasAnnotation(RequestBody.class)
				|| requestParamFieldData.hasAnnotation(RequestAttribute.class)
				|| requestParamFieldData.hasAnnotation(SessionAttribute.class)
				|| requestParamFieldData.hasAnnotation(CookieValue.class)
				|| requestParamFieldData.hasAnnotation(RequestHeader.class)
				|| requestParamFieldData.hasAnnotation(QueryParam.class));
	}

	@Override
	public Object resolveArgument(RequestParamFieldData requestParamFieldData, ViewForward viewForward, String uri)
			throws Exception {
		RequestParam requestParam = requestParamFieldData.getAnnotation(RequestParam.class);
		String paramName = requestParamFieldData.getParamName();
		if (requestParam != null && requestParam.value() != null && !requestParam.value().trim().equals("")) {
			paramName = requestParam.value();
			requestParamFieldData.setParamName(paramName);
		}
		Object date = null;
		ControlValidator controlValidator = WebValidateExecutor
				.getControlValidator(viewForward.getTransactionController().getClass());
		Annotation[] annotations = requestParamFieldData.getParameter().getAnnotations();
		Class<?> type = requestParamFieldData.getType();
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				Class<? extends Annotation> annotationClass = annotation.annotationType();
				if (annotationClass == PathVariable.class) {
					PathVariable pathVariable = (PathVariable) annotation;
					paramName = getParameterName(pathVariable.value(), requestParamFieldData);
					String value = viewForward.getParamsVariable(paramName);
					Map<String, ControlField> pathVariableFields = controlValidator.getPathVariableFields(uri);
					if (pathVariableFields != null) {
						ControlField controlField = pathVariableFields.get(paramName);
						if (controlField != null) {
							ValidateParser.validateIfFailedThrow(controlField.getFieldExplain(), value,
									controlField.getRules());
						}
					}
					try {
						if (value == null && type.isPrimitive()) {
							throw new ConvertException(
									uri + ":" + paramName + " value null can't convert " + type.getName() + "!");
						}
						if (type == String.class || ClassUtil.isSimpleValueType(type)) {
							return ClassUtil.convert(value, type);
						} else if ((date = DateUtil.convertToDate(value, paramName, type,
								requestParamFieldData.getFormatter())) != null) {
							return date;
						}
					} catch (Exception e) {
						throw new ConvertException(
								uri + ":" + paramName + " value " + value + " can't convert " + type.getName() + "!");
					}
				} else if (annotationClass == RequestBody.class) {
					HttpServletRequest request = viewForward.getRequest();
					Object target = null;
					if (request.getClass() == BodyReaderHttpServletRequestWrapper.class) {
						BodyReaderHttpServletRequestWrapper requestWrapper = (BodyReaderHttpServletRequestWrapper) request;
						if (type == String.class) {
							target = requestWrapper.getBody();
						} else if (request.getContentType() != null
								&& AbstractDisplayPlug.MIME_TYPE_JSON.contains(request.getContentType())) {
							target = JsonProcessUtil.jsonToBean(requestWrapper.getBody(),
									new TypeReferenceType(requestParamFieldData.getParameterType()));
						}
					} else {
						if (type == String.class) {
							target = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
						} else if (request.getContentType() != null
								&& AbstractDisplayPlug.MIME_TYPE_JSON.contains(request.getContentType())) {
							target = JsonProcessUtil.jsonToBean(request.getInputStream(),
									new TypeReferenceType(requestParamFieldData.getParameterType()));
						}
					}
					if (controlValidator != null) {
						Map<String, ControlField> bodyFields = controlValidator.getBodyFields(uri);
						if (bodyFields != null) {
							Field[] fields = type.getDeclaredFields();
							for (Field field : fields) {
								validateField(field, bodyFields, target);
							}
						}
					}
					return target;
				} else if (annotationClass == RequestAttribute.class) {
					RequestAttribute requestAttribute = (RequestAttribute) annotation;
					paramName = getParameterName(requestAttribute.value(), requestParamFieldData);
					return viewForward.getRequest().getAttribute(paramName);
				} else if (annotationClass == SessionAttribute.class) {
					SessionAttribute sessionAttribute = (SessionAttribute) annotation;
					paramName = getParameterName(sessionAttribute.value(), requestParamFieldData);
					return viewForward.getRequest().getSession().getAttribute(paramName);
				} else if (annotationClass == CookieValue.class) {
					CookieValue cookieValue = (CookieValue) annotation;
					paramName = getParameterName(cookieValue.value(), requestParamFieldData);
					String value = CookieUtil.getCookieValue(viewForward.getRequest().getCookies(), paramName);
					Map<String, ControlField> cookieFields = controlValidator.getCookieFields(uri);
					if (cookieFields != null) {
						ControlField controlField = cookieFields.get(paramName);
						if (controlField != null) {
							ValidateParser.validateIfFailedThrow(controlField.getFieldExplain(), value,
									controlField.getRules());
						}
					}
					if (value == null && type.isPrimitive()) {
						throw new ConvertException(
								uri + ":" + paramName + " value null can't convert " + type.getName() + "!");
					}
					try {
						if (type == String.class || ClassUtil.isSimpleValueType(type)) {
							return ClassUtil.convert(value, type);
						} else if ((date = DateUtil.convertToDate(value, paramName, type,
								requestParamFieldData.getFormatter())) != null) {
							return date;
						}
					} catch (Exception e) {
						throw new ConvertException(
								uri + ":" + paramName + " value " + value + " can't convert " + type.getName()
										+ "!");
					}
				} else if (annotationClass == RequestHeader.class) {
					RequestHeader requestHeader = (RequestHeader) annotation;
					paramName = getParameterName(requestHeader.value(), requestParamFieldData);
					Map<String, ControlField> headerFields = controlValidator.getHeaderFields(uri);
					ControlField controlField = null;
					if (headerFields != null) {
						controlField = headerFields.get(paramName);
					}
					String[] values = null;
					String value = null;
					if (type.isArray()) {
						values = getHeaders(viewForward.getRequest(), paramName);
						if (controlField != null) {
							ValidateParser.validateIfFailedThrow(controlField.getFieldExplain(), values,
									controlField.getRules());
						}
						if (values == null && type.getComponentType().isPrimitive()) {
							throw new ConvertException(
									uri + ":" + paramName + " value null can't convert " + type.getName() + "!");
						}
					} else {
						value = viewForward.getRequest().getHeader(paramName);
						if (controlField != null) {
							ValidateParser.validateIfFailedThrow(controlField.getFieldExplain(), value,
									controlField.getRules());
						}
						if (value == null && type.isPrimitive()) {
							throw new ConvertException(
									uri + ":" + paramName + " value null can't convert " + type.getName() + "!");
						}
					}
					try {
						if (type == String.class || ClassUtil.isSimpleValueType(type)) {
							return ClassUtil.convert(value, type);
						} else if ((date = DateUtil.convertToDate(value, requestParamFieldData.getPattern(),
								type, requestParamFieldData.getFormatter())) != null) {
							return date;
						} else if (ClassUtil.isSimpleArrayType(type)) {
							return ClassUtil.convertArray(values, type);
						} else if (DateUtil.isDateTypeArray(type)) {
							return DateUtil.convertToDateArray(viewForward.paramValues(paramName),
									requestParamFieldData.getPattern(),
									type.getComponentType(), requestParamFieldData.getFormatter());
						}
					} catch (Exception e) {
						throw new ConvertException(
								uri + ":" + paramName + " value " + (values != null ? Arrays.toString(values) : value)
										+ " can't convert " + type.getName() + "!");
					}

				} else if (annotationClass == QueryParam.class) {
					QueryParam queryParam = (QueryParam) annotation;
					paramName = getParameterName(queryParam.value(), requestParamFieldData);
					Map<String, ControlField> queryParamFields = controlValidator.getQueryParamFields(uri);
					ControlField controlField = null;
					if (queryParamFields != null) {
						controlField = queryParamFields.get(paramName);
					}
					String[] values = null;
					String value = null;
					if (type.isArray()) {
						values = viewForward.paramValues(paramName);
						if (controlField != null) {
							ValidateParser.validateIfFailedThrow(controlField.getFieldExplain(), values,
									controlField.getRules());
						}
						if (values == null && type.getComponentType().isPrimitive()) {
							throw new ConvertException(
									uri + ":" + paramName + " value null can't convert " + type.getName() + "!");
						}
					} else {
						value = viewForward.paramString(paramName);
						if (controlField != null) {
							ValidateParser.validateIfFailedThrow(controlField.getFieldExplain(), value,
									controlField.getRules());
						}
						if (value == null && type.isPrimitive()) {
							throw new ConvertException(
									uri + ":" + paramName + " value null can't convert " + type.getName() + "!");
						}
					}
					try {
						if (type == String.class || ClassUtil.isSimpleValueType(type)) {
							return ClassUtil.convert(value, type);
						} else if ((date = DateUtil.convertToDate(value, requestParamFieldData.getPattern(),
								type, requestParamFieldData.getFormatter())) != null) {
							return date;
						} else if (ClassUtil.isSimpleArrayType(type)) {
							return ClassUtil.convertArray(values, type);
						} else if (DateUtil.isDateTypeArray(type)) {
							return DateUtil.convertToDateArray(values,
									requestParamFieldData.getPattern(),
									type.getComponentType(), requestParamFieldData.getFormatter());
						} else if (type.isArray() && type.getComponentType().isEnum()) {
							if (values == null) {
								return null;
							}
							try {
								return ClassUtil.createEnumArray((Class<Enum>) type.getComponentType(), values);
							} catch (Exception e) {
								throw new ConvertException(
										uri + ":" + paramName + " " + e.getMessage());
							}
						} else if (type.isEnum()) {
							if (value == null) {
								return null;
							}
							try {
								return Enum.valueOf((Class<Enum>) type, value);
							} catch (Exception e) {
								throw new ConvertException(
										uri + ":" + paramName + " " + e.getMessage());
							}
						}
					} catch (Exception e) {
						throw new ConvertException(
								uri + ":" + paramName + " value " + (values != null ? Arrays.toString(values) : value)
										+ " can't convert " + type.getName() + "!");
					}
					return setField(type, viewForward, null, uri);
				}
			}
		}
		return null;
	}

	private void validateField(Field field, Map<String, ControlField> bodyFields, Object target)
			throws IllegalArgumentException, IllegalAccessException {
		Class<?> type = field.getType();
		field.setAccessible(true);
		Object value = field.get(target);
		if (value != null) {
			if (ClassUtil.isSimpleValueType(type)) {
				value = value.toString();
			} else if (ClassUtil.isSimpleArrayType(type)) {
				value = ClassUtil.convertSimpleArrayToStringArray(value);
			} else if (matchType(type)) {
				Field[] innerFields = type.getDeclaredFields();
				for (Field innerField : innerFields) {
					validateField(innerField, bodyFields, value);
				}
			} else if (Collection.class.isAssignableFrom(type)) {
				if (value != null) {
					Collection<?> collection = (Collection<?>) value;
					String[] array = new String[collection.size()];
					int index = 0;
					for (Object obj : collection) {
						if (obj instanceof String valueString) {
							array[index] = valueString;
						} else if (ClassUtil.isSimpleValueType(type)) {
							array[index] = obj.toString();
						} else if (matchType(obj.getClass())) {
							Field[] innerFields = obj.getClass().getDeclaredFields();
							for (Field innerField : innerFields) {
								validateField(innerField, bodyFields, obj);
							}
						}
						index++;
					}
				}
			}
		}
		ControlField controlField = bodyFields.get(field.getName());
		if (controlField != null) {
			ValidateParser.validateIfFailedThrow(controlField.getFieldExplain(), value, controlField.getRules());
		}

	}

	public boolean matchType(Class<?> type) {
		return !(type.getPackageName().startsWith("java.") || type.isInterface()
				|| type.isAnonymousClass() || Modifier.isAbstract(type.getModifiers()));
	}

	public String[] getHeaders(HttpServletRequest request, String paramName) {
		Enumeration<String> enums = request.getHeaders(paramName);
		List<String> headers = new ArrayList<>();
		if (enums != null) {
			while (enums.hasMoreElements()) {
				headers.add(enums.nextElement());
			}
		}
		return headers.toArray(new String[0]);
	}

	public Object setField(Class<?> type, ViewForward viewForward, Object instance, String uri)
			throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		if (!matchType(type) || type.isArray()) {
			return instance;
		}
		if (instance == null) {
			instance = SunReflectionFactoryInstantiator.newInstance(type);
		}

		do {
			Field[] fields = type.getDeclaredFields();
			setField(fields, type, viewForward, instance, uri);
			type = type.getSuperclass();
		} while (type != null && type != Object.class);

		return instance;
	}

	public void setField(Field[] fields, Class<?> type, ViewForward viewForward, Object instance, String uri)
			throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		for (Field field : fields) {
			if ((Modifier.isFinal(field.getModifiers())) || Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			field.setAccessible(true);
			String typeName = field.getName();
			Class<?> fieldType = field.getType();
			DateTimeFormat dateTimeFormat = fieldType.getAnnotation(DateTimeFormat.class);
			String pattern = null;
			DateTimeFormatter formatter = null;
			if (dateTimeFormat != null) {
				pattern = dateTimeFormat.pattern();
				if (dateTimeFormat.iso() == DateTimeFormat.ISO.BASED) {
					formatter = DateUtil.getISODateTimeFormatter(fieldType);
				}
			}
			Object fieldValue = null;
			if (String.class == fieldType) {
				fieldValue = viewForward.paramString(typeName);
			} else if (ClassUtil.isSimpleValueType(fieldType)) {
				String value = viewForward.paramString(typeName);
				if (value == null && type.isPrimitive()) {
					throw new ConvertException(
							uri + ":" + typeName + " value null can't convert " + fieldType.getName() + "!");
				}
				try {
					fieldValue = ClassUtil.convert(value, fieldType);
				} catch (Exception e) {
					throw new ConvertException(
							uri + ":" + typeName + " value " + value + " can't convert " + fieldType.getName() + "!");
				}

			} else if (String[].class == fieldType) {
				fieldValue = viewForward.paramString(typeName);
			} else if (ClassUtil.isSimpleArrayType(fieldType)) {
				String[] values = viewForward.paramValues(typeName);
				try {
					fieldValue = ClassUtil.convertArray(values, fieldType);
				} catch (Exception e) {
					throw new ConvertException(uri + ":" + typeName + " value " + Arrays.toString(values)
							+ " can't convert " + type.getName() + "!");
				}
				if (fieldValue == null && type.getComponentType().isPrimitive()) {
					throw new ConvertException(
							uri + ":" + typeName + " value null can't convert " + fieldType.getName() + "!");
				}
			} else if ((fieldValue = DateUtil.convertToDate(viewForward.paramString(typeName), pattern, type,
					formatter)) != null) {
			} else if (DateUtil.isDateTypeArray(fieldType)) {
				fieldValue = DateUtil.convertToDateArray(viewForward.paramValues(typeName), pattern,
						type.getComponentType(), formatter);
			} else if (type.isArray() && type.getComponentType().isEnum()) {
				String[] values = viewForward.paramValues(typeName);
				if (values != null) {
					fieldValue = ClassUtil.createEnumArray((Class<Enum>) type.getComponentType(), values);
					try {
					} catch (Exception e) {
						throw new ConvertException(
								uri + ":" + typeName + " value " + Arrays.toString(values) + " can't convert "
										+ fieldType.getName() + "!");
					}
				}
			} else if (type.isEnum()) {
				String value = viewForward.paramString(typeName);
				if (value != null) {
					try {
						fieldValue = Enum.valueOf((Class<Enum>) type, value);
					} catch (Exception e) {
						throw new ConvertException(
								uri + ":" + typeName + " value " + value + " can't convert " + fieldType.getName()
										+ "!");
					}
				}
			} else {
				field.set(instance, setField(fieldType, viewForward, null, uri));
			}
			if (fieldValue != null) {
				field.set(instance, fieldValue);
			}
		}
	}

}
