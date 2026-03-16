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
package club.dawdler.clientplug.web.bind.param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

import club.dawdler.clientplug.web.annotation.DateTimeFormat;
import club.dawdler.util.DateUtil;

/**
 * @author jackson.song
 * @version V1.0
 * 存储参数基本信息的类
 */
public class RequestParamFieldData {

	private String paramName;

	private Class<?> type;

	private Type parameterType;

	private int index;

	private Method method;

	private Parameter parameter;

	private String pattern;

	private DateTimeFormat.ISO iso;

	public RequestParamFieldData(String paramName, int index, Method method,
			Parameter parameter) {
		this.paramName = paramName;
		this.type = parameter.getType();
		this.parameterType = method.getGenericParameterTypes()[index];
		this.index = index;
		this.method = method;
		this.parameter = parameter;
		DateTimeFormat dateTimeFormat = parameter.getAnnotation(DateTimeFormat.class);
		if (dateTimeFormat != null) {
			if ((iso = dateTimeFormat.iso()) == DateTimeFormat.ISO.NONE) {
				pattern = dateTimeFormat.pattern();
			}
		}
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public Type getParameterType() {
		return parameterType;
	}

	public void setParameterType(Type parameterType) {
		this.parameterType = parameterType;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotation) {
		return parameter.getAnnotation(annotation);
	}

	public <T extends Annotation> boolean hasAnnotation(Class<T> annotation) {
		return getAnnotation(annotation) != null;
	}

	public Parameter getParameter() {
		return parameter;
	}

	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	public String getPattern() {
		return pattern;
	}

	public DateTimeFormat.ISO getIso() {
		return iso;
	}

	public DateTimeFormatter getFormatter() {
		if (iso == DateTimeFormat.ISO.BASED) {
			return DateUtil.getISODateTimeFormatter(type);
		}
		return null;
	}

}
