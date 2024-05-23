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
package com.anywide.dawdler.core.service.bean;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.order.OrderData;
import com.anywide.dawdler.core.service.listener.DawdlerServiceCreateListener;
import com.anywide.dawdler.core.service.listener.DawdlerServiceCreateProvider;
import com.anywide.dawdler.util.SunReflectionFactoryInstantiator;

/**
 * @author jackson.song
 * @version V1.0
 * 服务提供类(从dawdler-server独立出来)
 */
public class ServicesBean {
	private static final Logger logger = LoggerFactory.getLogger(ServicesBean.class);
	private final DawdlerServiceCreateProvider dawdlerServiceCreateProvider;
	private String name;
	private Object service;
	private boolean single;

	public ServicesBean(String name, Object service, DawdlerServiceCreateProvider dawdlerServiceCreateProvider,
			boolean single) {
		this.name = name;
		this.service = service;
		this.single = single;
		this.dawdlerServiceCreateProvider = dawdlerServiceCreateProvider;
	}

	public void fireCreate() throws Throwable {
		notify(service);
	}

	private void notify(Object service) throws Throwable {
		List<OrderData<DawdlerServiceCreateListener>> listeners = dawdlerServiceCreateProvider.getListeners();
		for (OrderData<DawdlerServiceCreateListener> listener : listeners) {
			listener.getData().create(service, single);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getService() throws Throwable {
		if (!isSingle()) {
			try {
				Object obj = SunReflectionFactoryInstantiator.newInstance(service.getClass());
				notify(obj);
				return obj;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException | NoSuchMethodException e) {
				logger.error("", e);
			}
		}
		return service;
	}

	public void setService(Object service) {
		this.service = service;
	}

	public boolean isSingle() {
		return single;
	}

	public void setSingle(boolean single) {
		this.single = single;
	}

}
