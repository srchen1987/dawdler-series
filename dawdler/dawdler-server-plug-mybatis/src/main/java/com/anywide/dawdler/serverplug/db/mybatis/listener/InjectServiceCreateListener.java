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

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.service.listener.DawdlerServiceCreateListener;
import com.anywide.dawdler.serverplug.db.mybatis.SingleSqlSessionFactory;

/**
 * @author jackson.song
 * @version V1.0
 * @Title InjectServiceCreateListener.java
 * @Description 监听器实现service和dao的注入
 * @date 2015年07月08日
 * @email suxuan696@gmail.com
 */
public class InjectServiceCreateListener implements DawdlerServiceCreateListener {
	private static final Logger logger = LoggerFactory.getLogger(InjectServiceCreateListener.class);
	private SqlSession sqlSession = SingleSqlSessionFactory.getInstance().getSqlSession();

	@Override
	public void create(Object service, boolean single, DawdlerContext dawdlerContext) {
		inject(service, dawdlerContext);
	}

	private void inject(Object service, DawdlerContext dawdlerContext) {
		Field[] fields = service.getClass().getDeclaredFields();
		for (Field field : fields) {
			Resource resource = field.getAnnotation(Resource.class);
			if (!field.getType().isPrimitive()) {
				Class serviceClass = field.getType();
				field.setAccessible(true);
				try {
					if (resource != null && serviceClass.isInterface()) {
						field.set(service, sqlSession.getMapper(serviceClass));
					}
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
	}

}