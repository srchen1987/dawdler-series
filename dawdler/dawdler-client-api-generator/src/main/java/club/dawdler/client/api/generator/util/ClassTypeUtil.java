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
package club.dawdler.client.api.generator.util;

import java.util.List;

import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;

/**
 * @author jackson.song
 * @version V1.0
 * 类型操作工具类
 */
public class ClassTypeUtil {
	private ClassTypeUtil() {
	}

	private static final String LIST_NAME = "java.util.List";
	private static final String SET_NAME = "java.util.Set";
	private static final String COLLECTION_NAME = "java.util.Collection";
	private static final String VECTOR_NAME = "java.util.Vector";

	/**
	 * 
	 * @author jackson.song
	 * 是否是List,Set,Collection,Vector
	 * @param binaryName
	 * @return boolean
	 *
	 * 
	 */
	public static boolean isArray(String binaryName) {
		return LIST_NAME.equals(binaryName) || SET_NAME.equals(binaryName) || COLLECTION_NAME.equals(binaryName)
				|| VECTOR_NAME.equals(binaryName);
	}

	public static String getType0(JavaType javaType) {
		DefaultJavaParameterizedType dt = (DefaultJavaParameterizedType) javaType;
		List<JavaType> dtList = dt.getActualTypeArguments();
		if (!dtList.isEmpty()) {
			JavaType actualTypeArgument = dtList.get(0);
			return actualTypeArgument.getBinaryName();
		}
		return null;
	}

}
