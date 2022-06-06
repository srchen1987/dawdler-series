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
package com.anywide.dawdler.serverplug.db.mybatis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.util.spring.antpath.PathMatchingResourcePatternResolver;
import com.anywide.dawdler.util.spring.antpath.Resource;
import com.anywide.dawdler.util.spring.antpath.ResourcePatternResolver;

/**
 * @author jackson.song
 * @version V1.0
 * @Title SingleSqlSessionFactory.java
 * @Description dawdler实现session单例工厂
 * @date 2021年5月8日
 * @email suxuan696@gmail.com
 */
public class SingleSqlSessionFactory {
	private static final Logger logger = LoggerFactory.getLogger(SingleSqlSessionFactory.class);
	private SqlSession sqlSession;
	private ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	public static class SingletonHoler {
		private static SingleSqlSessionFactory instance = new SingleSqlSessionFactory();
	}

	public List<Resource> getMapperLocations() throws IOException {
		List<Node> mappers = DawdlerContext.getDawdlerContext().getServicesConfig()
				.selectNodes("/config/mybatis/mappers/mapper");
		if (!mappers.isEmpty()) {
			List<Resource> resourceList = new ArrayList<>();
			for (Node Nodemapper : mappers) {
				Element mapper = (Element) Nodemapper;
				Resource[] resources = resolver.getResources(mapper.getTextTrim());
				for (Resource resource : resources) {
					resourceList.add(resource);
				}
			}
			return resourceList;
		}
		return null;
	}

	private SingleSqlSessionFactory() {
		String resource = "mybatis-config.xml";
		String path = Thread.currentThread().getContextClassLoader().getResource(resource).getFile();
		DawdlerSqlSessionFactoryBuilder sessionBuilder = new DawdlerSqlSessionFactoryBuilder();
		sessionBuilder.setConfigLocation(path);
		sessionBuilder.setTransactionFactory(new DawdlerTransactionFactory());
		try {
			List<Resource> mapperLocations = getMapperLocations();
			sessionBuilder.setMapperLocations(mapperLocations);
		} catch (IOException e) {
			logger.error("", e);
		}
		SqlSessionFactory sqlSessionFactory;
		try {
			sqlSessionFactory = sessionBuilder.buildSqlSessionFactory();
			this.sqlSession = sqlSessionFactory.openSession();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public SqlSession getSqlSession() {
		return sqlSession;
	}

	public void setSqlSession(SqlSession sqlSession) {
		this.sqlSession = sqlSession;
	}

	public static SingleSqlSessionFactory getInstance() {
		return SingletonHoler.instance;
	}
}
