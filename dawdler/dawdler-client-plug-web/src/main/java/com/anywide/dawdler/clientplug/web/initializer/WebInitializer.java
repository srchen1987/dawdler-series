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
package com.anywide.dawdler.clientplug.web.initializer;

import java.util.EnumSet;
import java.util.Set;

import com.anywide.dawdler.clientplug.web.filter.ViewFilter;
import com.anywide.dawdler.clientplug.web.listener.WebListener;
import com.anywide.dawdler.core.annotation.Order;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @author jackson.song
 * @version V1.0
 * @Title WebInitializer.java
 * @Description web初始化
 * @date 2015年3月11日
 * @email suxuan696@gmail.com
 */
@Order(1)
public class WebInitializer implements ServletContainerInitializer {
	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		ctx.addListener(WebListener.class);
		EnumSet<DispatcherType> dispatcherType = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD,
				DispatcherType.ERROR, DispatcherType.INCLUDE);
		ctx.addFilter(ViewFilter.class.getName(), ViewFilter.class).addMappingForUrlPatterns(dispatcherType, true,
				"/*");
	}
}
