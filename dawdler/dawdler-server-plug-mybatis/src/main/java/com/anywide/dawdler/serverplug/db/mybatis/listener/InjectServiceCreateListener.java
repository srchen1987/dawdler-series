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
package com.anywide.dawdler.serverplug.db.mybatis.listener;

import java.lang.reflect.Field;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.service.listener.DawdlerServiceCreateListener;
import com.anywide.dawdler.serverplug.db.annotation.Repository;
import com.anywide.dawdler.serverplug.db.mybatis.SingleSqlSessionFactory;

/**
 * @author jackson.song
 * @version V1.0
 * @Title InjectServiceCreateListener.java
 * @Description 监听器实现mapper的注入
 * @date 2021年5月8日
 * @email suxuan696@gmail.com
 */
public class InjectServiceCreateListener implements DawdlerServiceCreateListener {
	private SqlSession sqlSession = SingleSqlSessionFactory.getInstance().getSqlSession();

	@Override
	public void create(Object service, boolean single, DawdlerContext dawdlerContext) throws IllegalArgumentException, IllegalAccessException {
		inject(service, dawdlerContext);
	}

	private void inject(Object service, DawdlerContext dawdlerContext) throws IllegalArgumentException, IllegalAccessException {
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
