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
package com.anywide.dawdler.core.db.mybatis;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

<<<<<<< HEAD
=======
import org.apache.ibatis.plugin.Interceptor;
>>>>>>> 0.0.6-jdk1.8-RELEASES
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

<<<<<<< HEAD
=======
import com.anywide.dawdler.core.db.mybatis.interceptor.SubTableInterceptor;
>>>>>>> 0.0.6-jdk1.8-RELEASES
import com.anywide.dawdler.util.spring.antpath.PathMatchingResourcePatternResolver;
import com.anywide.dawdler.util.spring.antpath.Resource;
import com.anywide.dawdler.util.spring.antpath.ResourcePatternResolver;

/**
 * @author jackson.song
 * @version V1.0
 * SqlSession抽象工厂
 */
public abstract class AbstractSqlSessionFactory {
	private static final Logger logger = LoggerFactory.getLogger(AbstractSqlSessionFactory.class);
	protected SqlSession sqlSession;
	protected ResourcePatternResolver resolver = PathMatchingResourcePatternResolver.getInstance();

	public List<Resource> getMapperLocations() throws IOException {
		Set<String> mappers = getMappers();
		if (mappers != null) {
			List<Resource> resourceList = new ArrayList<>();
			for (String mapper : mappers) {
				Resource[] resources = resolver.getResources(mapper);
				for (Resource resource : resources) {
					resourceList.add(resource);
				}
			}
			return resourceList;
		}
		return null;
	}

	public abstract Set<String> getMappers();

	protected void init() {
		String resource = "mybatis-config.xml";
		URL resourceURL = AbstractSqlSessionFactory.class.getResource(resource);
		DawdlerSqlSessionFactoryBuilder sessionBuilder = new DawdlerSqlSessionFactoryBuilder();
		if (resourceURL != null) {
			sessionBuilder.setConfigLocation(resourceURL.getFile());
		}
		sessionBuilder.setTransactionFactory(new DawdlerTransactionFactory());
		try {
			List<Resource> mapperLocations = getMapperLocations();
			sessionBuilder.setMapperLocations(mapperLocations);
		} catch (IOException e) {
			logger.error("", e);
			throw new RuntimeException(e);
		}
		SqlSessionFactory sqlSessionFactory;
		try {
<<<<<<< HEAD
=======
			sessionBuilder.setPlugins(new Interceptor[]	{new SubTableInterceptor()});
>>>>>>> 0.0.6-jdk1.8-RELEASES
			sqlSessionFactory = sessionBuilder.buildSqlSessionFactory();
			this.sqlSession = sqlSessionFactory.openSession();
		} catch (Exception e) {
			logger.error("", e);
			throw new RuntimeException(e);
		}
	}

	public SqlSession getSqlSession() {
		return sqlSession;
	}

	public void setSqlSession(SqlSession sqlSession) {
		this.sqlSession = sqlSession;
	}

}
