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
package com.anywide.dawdler.util;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import sun.reflect.ReflectionFactory;
/**
 * 
 * @Title:  SunReflectionFactoryInstantiator.java
 * @Description:    ReflectionFactory来构建对象 针对私有构造函数的类
 * @author: jackson.song    
 * @date:   2012年08月22日    
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class SunReflectionFactoryInstantiator {

	private static final ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();

	public static <T> T newInstance(Class<T> type) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor javaLangObjectConstructor;
		try {
			javaLangObjectConstructor = Object.class.getConstructor((Class[]) null);
		} catch (NoSuchMethodException e) {
			throw new Error("Cannot find constructor for java.lang.Object!");
		}
		Constructor  mungedConstructor = reflectionFactory.newConstructorForSerialization(
				type, javaLangObjectConstructor);
		mungedConstructor.setAccessible(true);
		return (T) mungedConstructor.newInstance(null);
	}
}