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

import org.elasticsearch.client.RestHighLevelClient;

import com.anywide.dawdler.es.annotation.EsRestHighLevelInjector;
import com.anywide.dawdler.es.restclient.pool.factory.ElasticSearchClientFactory;
import com.anywide.dawdler.es.restclient.warpper.ElasticSearchClient;

/**
 * @author jackson.song
 * @version V1.0
 * @Title JedisOperatorFactory.java
 * @Description jedis操作类工厂
 * @date 2022年4月16日
 * @email suxuan696@gmail.com
 */
public class EsRestHighLevelOperatorFactory {
	private static Map<String, EsRestHighLevelOperator> esRestHighLevelOperators = new ConcurrentHashMap<>();
	private static Map<String, Method> methodCache = new ConcurrentHashMap<>();
	static {
		Method[] methods = RestHighLevelClient.class.getMethods();
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
				return clientMethod.invoke(client.getRestHighLevelClient(), args);
			} finally {
				if (client != null) {
					client.close();
				}
			}
		}
	}

	private static Class<?>[] esRestHighLevelOperatorClass = new Class[] { EsRestHighLevelOperator.class };

	public static EsRestHighLevelOperator getEsRestHighLevelOperator(String fileName) throws Exception {
		EsRestHighLevelOperator operator = esRestHighLevelOperators.get(fileName);
		if (operator != null) {
			return operator;
		}
		synchronized (esRestHighLevelOperators) {
			operator = esRestHighLevelOperators.get(fileName);
			if (operator == null) {
				EsHandler handler = new EsHandler(ElasticSearchClientFactory.getInstance(fileName));
				operator = (EsRestHighLevelOperator) Proxy.newProxyInstance(
						EsRestHighLevelOperator.class.getClassLoader(), esRestHighLevelOperatorClass, handler);
				esRestHighLevelOperators.put(fileName, operator);
			}
		}
		return operator;
	}

	public static void initField(Object target, Class<?> clazz)
			throws IllegalArgumentException, IllegalAccessException, Exception {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			EsRestHighLevelInjector esRestHighLevelInjector = field.getAnnotation(EsRestHighLevelInjector.class);
			if (!field.getType().isPrimitive()) {
				Class<?> serviceClass = field.getType();
				if (esRestHighLevelInjector != null && EsRestHighLevelOperator.class.isAssignableFrom(serviceClass)) {
					field.setAccessible(true);
					field.set(target,
							EsRestHighLevelOperatorFactory.getEsRestHighLevelOperator(esRestHighLevelInjector.value()));
				}
			}
		}
	}
}
