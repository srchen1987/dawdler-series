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
package com.anywide.dawdler.clientplug.web.bind.resolver.impl;

import com.anywide.dawdler.clientplug.annotation.CookieValue;
import com.anywide.dawdler.clientplug.annotation.PathVariable;
import com.anywide.dawdler.clientplug.annotation.RequestAttribute;
import com.anywide.dawdler.clientplug.annotation.RequestBody;
import com.anywide.dawdler.clientplug.annotation.RequestHeader;
import com.anywide.dawdler.clientplug.annotation.SessionAttribute;
import com.anywide.dawdler.clientplug.web.bind.param.RequestParamFieldData;
import com.anywide.dawdler.clientplug.web.handler.ViewForward;
import com.anywide.dawdler.clientplug.web.handler.WebValidateExecutor;
import com.anywide.dawdler.clientplug.web.util.CookieUtil;
import com.anywide.dawdler.clientplug.web.validator.ValidateParser;
import com.anywide.dawdler.clientplug.web.validator.entity.ControlField;
import com.anywide.dawdler.clientplug.web.validator.entity.ControlValidator;
import com.anywide.dawdler.clientplug.web.validator.exception.ValidationException;
import com.anywide.dawdler.clientplug.web.wrapper.BodyReaderHttpServletRequestWrapper;
import com.anywide.dawdler.util.ClassUtil;
import com.anywide.dawdler.util.JsonProcessUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;







/**
 * @author jackson.song
 * @version V1.0
 * @Title AnnotationMethodArgumentResolver.java
 * @Description 获取mvc中常用方法参数注解相关参数值的决策者
 * @date 2021年4月3日
 * @email suxuan696@gmail.com
 */
public class AnnotationMethodArgumentResolver extends AbstractMethodArgumentResolver {

	@Override
	public boolean isSupport(RequestParamFieldData requestParamFieldData) {
		return (requestParamFieldData.hasAnnotation(PathVariable.class)
				|| requestParamFieldData.hasAnnotation(RequestBody.class)
				|| requestParamFieldData.hasAnnotation(RequestAttribute.class)
				|| requestParamFieldData.hasAnnotation(SessionAttribute.class)
				|| requestParamFieldData.hasAnnotation(CookieValue.class)
				|| requestParamFieldData.hasAnnotation(RequestHeader.class));
	}

	@Override
	public Object resolveArgument(RequestParamFieldData requestParamFieldData, ViewForward viewForward)
			throws Exception {
		Annotation[] annotations = requestParamFieldData.getAnnotations();
		Class<?> type = requestParamFieldData.getType();
		if (annotations != null) {
			String paramName = null;
			for (Annotation annotation : annotations) {
				Class<? extends Annotation> annotationClass = annotation.annotationType();
				if (annotationClass == PathVariable.class) {
					PathVariable pathVariable = (PathVariable) annotation;
					paramName = getParameterName(pathVariable.value(), requestParamFieldData);
					return ClassUtil.convert(viewForward.getParamsVariable(paramName), type);
				} else if (annotationClass == RequestBody.class) {
					String uri = null;
					String antPath = viewForward.getAntPath();
					if (antPath != null) {
						uri = antPath;
					} else {
						uri = viewForward.getUriShort();
					}
					ControlValidator controlValidator = WebValidateExecutor
							.getControlValidator(viewForward.getTransactionController().getClass());
					HttpServletRequest request = viewForward.getRequest();
					Object target = null;
					if (request.getClass() == BodyReaderHttpServletRequestWrapper.class) {
						BodyReaderHttpServletRequestWrapper requestWrapper = (BodyReaderHttpServletRequestWrapper) request;
						target = JsonProcessUtil.jsonToBean(requestWrapper.getBody(), type);
					} else {
						target = JsonProcessUtil.jsonToBean(request.getInputStream(), type);
					}
					if (controlValidator != null) {
						if (controlValidator.getMappings().containsKey(uri)) {
							Field[] fields = type.getDeclaredFields();
							for (Field field : fields) {
								validateField(field, controlValidator, target);
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
					if (type == String.class) {
						CookieValue cookieValue = (CookieValue) annotation;
						paramName = getParameterName(cookieValue.value(), requestParamFieldData);
						return CookieUtil.getCookieValue(viewForward.getRequest().getCookies(), paramName);
					}
				} else if (annotationClass == RequestHeader.class) {
					RequestHeader requestHeader = (RequestHeader) annotation;
					paramName = getParameterName(requestHeader.value(), requestParamFieldData);
					if (type == String.class) {
						return viewForward.getRequest().getHeader(paramName);
					} else if (type == String[].class) {
						Enumeration<String> enums = viewForward.getRequest().getHeaders(paramName);
						List<String> headers = new ArrayList<>();
						if (enums != null) {
							while (enums.hasMoreElements()) {
								headers.add(enums.nextElement());
							}
						}
						return headers.toArray(new String[0]);
					}
				}
			}
		}
		return null;
	}

	private void validateField(Field field, ControlValidator controlValidator, Object target)
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
				Field[] innerFileds = type.getDeclaredFields();
				for (Field innerFiled : innerFileds) {
					validateField(innerFiled, controlValidator, value);
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
							Field[] innerFileds = obj.getClass().getDeclaredFields();
							for (Field innerFiled : innerFileds) {
								validateField(innerFiled, controlValidator, obj);
							}
						}
						index++;
					}
				}
			}
		}
		ControlField controlField = controlValidator.getControlFields().get(field.getName());
		if (controlField != null) {
			String error = ValidateParser.validate(controlField.getFieldExplain(), value, controlField.getRules());
			if (error != null) {
				throw new ValidationException(field.getName(), error);
			}
		}

	}

	public boolean matchType(Class<?> type) {
		return !(type.getPackage().getName().startsWith("java.") || type.isInterface() || type.isEnum()
				|| type.isAnonymousClass() || Modifier.isAbstract(type.getModifiers()));
	}

}
