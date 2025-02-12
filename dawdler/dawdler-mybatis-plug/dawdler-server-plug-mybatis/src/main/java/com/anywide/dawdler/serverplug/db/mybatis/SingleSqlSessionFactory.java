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

<<<<<<< HEAD
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
=======
>>>>>>> 0.0.6-jdk1.8-RELEASES
import java.util.Set;

import com.anywide.dawdler.core.db.mybatis.AbstractSqlSessionFactory;
import com.anywide.dawdler.server.context.DawdlerContext;
<<<<<<< HEAD
import com.anywide.dawdler.util.spring.antpath.Resource;
=======
>>>>>>> 0.0.6-jdk1.8-RELEASES

/**
 * @author jackson.song
 * @version V1.0
 * dawdler实现session单例工厂
 */
public class SingleSqlSessionFactory extends AbstractSqlSessionFactory {

	public static class SingletonHoler {
		private static SingleSqlSessionFactory instance = new SingleSqlSessionFactory();
	}

<<<<<<< HEAD
	public List<Resource> getMapperLocations() throws IOException {
		Set<String> mappers = DawdlerContext.getDawdlerContext().getServicesConfig().getMappers();
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

=======
>>>>>>> 0.0.6-jdk1.8-RELEASES
	private SingleSqlSessionFactory() {
		init();
	}

	public static SingleSqlSessionFactory getInstance() {
		return SingletonHoler.instance;
	}

	@Override
	public Set<String> getMappers() {
		return DawdlerContext.getDawdlerContext().getServicesConfig().getMappers();
	}
}
