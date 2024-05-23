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
package com.anywide.dawdler.core.component.injector;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author jackson.song
 * @version V1.0
 * CustomComponentInjector注入接口
 */
public interface CustomComponentInjector {

	/**
	 * 注入方法
	 */
	default public void inject(Class<?> type, Object target) throws Throwable {
	};

	/**
	 * 是否注入
	 *
	 */
	default public boolean isInject() {
		return true;
	}

	/**
	 * 匹配的类或接口
	 */
	default Class<?>[] getMatchTypes() {
		return null;
	}

	/**
	 * 匹配的注解
	 */
	default Set<? extends Class<? extends Annotation>> getMatchAnnotations() {
		return null;
	}

	/**
	 * 扫描包路径
	 */
	default String[] scanLocations() {
		return null;
	}
	
	
	/**
	 * 是否对符合的组件进行aop编织
	 */
	default boolean useAop() {
		return true;
	}
	
	/**
	 * 是否将变量名到ParameterNameReader类中(一般不需要设置,WebComponentInjector应用此特性来实现mvc的api中方法参数映射, ServiceInjector设置此参数为了支持Cache模块)
	 */
	default boolean storeVariableNameByASM() {
		return false;
	}
}