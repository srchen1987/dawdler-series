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
package club.dawdler.core.db.dao;

import java.util.Map;

/**
 * @author jackson.song
 * @version V1.0
 * dao工厂
 */
public class DAOFactory {
	private static DAOFactory daofactory = new DAOFactory();
	private Map<Class<?>, SuperDAO> instances = null;

	private DAOFactory() {
		instances = new java.util.concurrent.ConcurrentHashMap<Class<?>, SuperDAO>();
	}

	public static DAOFactory getInstance() {
		return daofactory;
	}

	public SuperDAO getDAO(Class<?> clazz) {
		SuperDAO object = instances.get(clazz);
		if (object != null) {
			return object;
		}
		synchronized (instances) {
			object = instances.get(clazz);
			if (object == null) {
				try {
					object = (SuperDAO) clazz.getConstructor().newInstance();
				} catch (Exception e) {
					return null;
				}
				instances.put(clazz, object);
			}
		}
		return object;
	}
}
