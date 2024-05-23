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
package com.anywide.dawdler.clientplug.local.service.injector;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import com.anywide.dawdler.clientplug.local.service.context.DawdlerClientContextManager;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.component.injector.CustomComponentInjector;
import com.anywide.dawdler.core.service.annotation.Service;

/**
 * @author jackson.song
 * @version V1.0
 * 注入Service组件
 */
@Order(1)
public class ServiceInjector implements CustomComponentInjector {

	@Override
	public void inject(Class<?> type, Object target) throws Throwable {
		DawdlerClientContextManager.getDawdlerClientContext().getServicesManager().smartRegister(type, target);
	}
	@Override
	public Set<? extends Class<? extends Annotation>> getMatchAnnotations() {
		Set<Class<? extends Annotation>> annotationSet = new HashSet<>();
		annotationSet.add(Service.class);
		return annotationSet;
	}
	
	@Override
	public boolean storeVariableNameByASM() {
		return true;
	}
	
}
