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
package com.anywide.dawdler.clientplug.db.mybatis.create.listener;

import java.lang.reflect.Field;

import org.apache.ibatis.session.SqlSession;

import com.anywide.dawdler.clientplug.db.mybatis.SingleSqlSessionFactory;
import com.anywide.dawdler.core.db.annotation.Repository;
import com.anywide.dawdler.core.service.listener.DawdlerServiceCreateListener;

/**
 * @author jackson.song
 * @version V1.0
 * 实现Service的组件注入
 */
public class InjectMapperCreateListener implements DawdlerServiceCreateListener {
	private SqlSession sqlSession = SingleSqlSessionFactory.getInstance().getSqlSession();

	@Override
	public void create(Object service, boolean single) throws Throwable {
		Field[] fields = service.getClass().getDeclaredFields();
		for (Field field : fields) {
			Repository resource = field.getAnnotation(Repository.class);
			Class<?> serviceClass = field.getType();
			if (resource != null && serviceClass.isInterface()) {
				field.setAccessible(true);
				field.set(service, sqlSession.getMapper(serviceClass));
			}
		}
	}

}
