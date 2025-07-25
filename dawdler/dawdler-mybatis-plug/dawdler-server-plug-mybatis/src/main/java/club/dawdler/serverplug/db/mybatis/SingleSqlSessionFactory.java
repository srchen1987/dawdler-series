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
package club.dawdler.serverplug.db.mybatis;

import java.util.Set;

import club.dawdler.core.db.mybatis.AbstractSqlSessionFactory;
import club.dawdler.server.context.DawdlerContext;

/**
 * @author jackson.song
 * @version V1.0
 * dawdler实现session单例工厂
 */
public class SingleSqlSessionFactory extends AbstractSqlSessionFactory {

	public static class SingletonHoler {
		private static SingleSqlSessionFactory instance = new SingleSqlSessionFactory();
	}

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
