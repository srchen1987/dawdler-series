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
package com.anywide.dawdler.client.api.generator.util;

import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.expression.AnnotationValue;

/**
 * @author jackson.song
 * @version V1.0
 * @Title AnnotationUtils.java
 * @Description 读取注解的工具类
 * @date 2022年3月19日
 * @email suxuan696@gmail.com
 */
public class AnnotationUtils {
	public static String getAnnotationStringValue(JavaAnnotation javaAnnotation, String name) {
		if (javaAnnotation == null) {
			return null;
		}
		AnnotationValue annotationValue = javaAnnotation.getProperty(name);
		if (annotationValue == null) {
			return null;
		}
		return getAnnotationObjectValue(javaAnnotation, name).toString().replaceAll("'", "").replaceAll("\"", "")
				.replaceAll(" ", "");
	}

	public static String[] getAnnotationStringArrayValue(JavaAnnotation javaAnnotation, String name) {
		String value = getAnnotationStringValue(javaAnnotation, name);
		if (value == null) {
			return null;
		}
		return value.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
	}

	public static Object getAnnotationObjectValue(JavaAnnotation javaAnnotation, String name) {
		if (javaAnnotation == null) {
			return null;
		}
		AnnotationValue annotationValue = javaAnnotation.getProperty(name);
		if (annotationValue == null) {
			return null;
		}
		return annotationValue.getParameterValue();
	}

}
