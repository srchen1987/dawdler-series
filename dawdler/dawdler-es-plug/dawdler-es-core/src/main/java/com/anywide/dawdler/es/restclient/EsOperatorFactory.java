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
package com.anywide.dawdler.es.restclient;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.anywide.dawdler.es.annotation.EsInjector;
import com.anywide.dawdler.es.restclient.pool.factory.ElasticSearchClientFactory;
import com.anywide.dawdler.es.restclient.warpper.ElasticSearchClient;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

/**
 * @author jackson.song
 * @version V1.0
 * @Title EsOperatorFactory.java
 * @Description Es操作类工厂
 * @date 2022年4月16日
 * @email suxuan696@gmail.com
 */
public class EsOperatorFactory {
	private static Map<String, EsOperator> esRestHighLevelOperators = new ConcurrentHashMap<>();
	private static Map<String, Method> methodCache = new ConcurrentHashMap<>();
	static {
		Method[] methods = ElasticsearchClient.class.getMethods();
		for (Method method : methods) {
			methodCache.put(method.getName(), method);
		}
	}

	public static class EsHandler implements InvocationHandler {
		private ElasticSearchClientFactory factory;

		public EsHandler(ElasticSearchClientFactory factory) {
			this.factory = factory;
		}

		// 不加入任何判断是否是基础方法 必定基础方法调用的比较少
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Method clientMethod = methodCache.get(method.getName());
			if (clientMethod == null) {
				throw new java.lang.NoSuchMethodException(method.toString());
			}
			ElasticSearchClient client = factory.getElasticSearchClient();
			try {
				return clientMethod.invoke(client.getElasticsearchClient(), args);
			} finally {
				if (client != null) {
					client.close();
				}
			}
		}
	}

	private static Class<?>[] esRestHighLevelOperatorClass = new Class[] { EsOperator.class };

	public static EsOperator getEsRestHighLevelOperator(String fileName) throws Exception {
		EsOperator operator = esRestHighLevelOperators.get(fileName);
		if (operator != null) {
			return operator;
		}
		synchronized (esRestHighLevelOperators) {
			operator = esRestHighLevelOperators.get(fileName);
			if (operator == null) {
				EsHandler handler = new EsHandler(ElasticSearchClientFactory.getInstance(fileName));
				operator = (EsOperator) Proxy.newProxyInstance(EsOperator.class.getClassLoader(),
						esRestHighLevelOperatorClass, handler);
				esRestHighLevelOperators.put(fileName, operator);
			}
		}
		return operator;
	}

	public static void initField(Object target, Class<?> clazz)
			throws IllegalArgumentException, IllegalAccessException, Exception {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			EsInjector esRestHighLevelInjector = field.getAnnotation(EsInjector.class);
			if (!field.getType().isPrimitive()) {
				Class<?> serviceClass = field.getType();
				if (esRestHighLevelInjector != null && EsOperator.class.isAssignableFrom(serviceClass)) {
					field.setAccessible(true);
					field.set(target, EsOperatorFactory.getEsRestHighLevelOperator(esRestHighLevelInjector.value()));
				}
			}
		}
	}
}
