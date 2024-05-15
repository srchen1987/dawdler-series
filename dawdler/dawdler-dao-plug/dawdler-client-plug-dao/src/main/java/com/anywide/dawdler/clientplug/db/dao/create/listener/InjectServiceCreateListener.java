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
package com.anywide.dawdler.clientplug.db.dao.create.listener;

import java.lang.reflect.Field;

import com.anywide.dawdler.core.db.annotation.Repository;
import com.anywide.dawdler.core.db.dao.DAOFactory;
import com.anywide.dawdler.core.db.dao.SuperDAO;
import com.anywide.dawdler.core.service.listener.DawdlerServiceCreateListener;

/**
 * @author jackson.song
 * @version V1.0
 * @Title InjectServiceCreateListener.java
 * @Description 实现Service的组件注入
 * @date 2022年4月16日
 * @email suxuan696@gmail.com
 */
public class InjectServiceCreateListener implements DawdlerServiceCreateListener {

	@Override
	public void create(Object service, boolean single) throws Throwable {
		inject(service);
	}
	
	private void inject(Object service) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = service.getClass().getDeclaredFields();
		for (Field field : fields) {
			Repository resource = field.getAnnotation(Repository.class);
			Class<?> serviceClass = field.getType();
			if (resource != null && SuperDAO.class.isAssignableFrom(serviceClass)) {
				field.setAccessible(true);
				field.set(service, DAOFactory.getInstance().getDAO(serviceClass));
			}
		}
	}

}
