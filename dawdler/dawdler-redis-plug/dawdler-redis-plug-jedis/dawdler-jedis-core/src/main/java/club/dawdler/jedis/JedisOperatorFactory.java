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
package club.dawdler.jedis;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import club.dawdler.jedis.annotation.JedisInjector;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * @author jackson.song
 * @version V1.0
 * jedis操作类工厂
 */
public class JedisOperatorFactory {
	private static final Map<String, JedisOperator> JEDIS_OPERATORS = new ConcurrentHashMap<>();

	public static class JedisHandler implements InvocationHandler {
		private UnifiedJedis unifiedJedis;
		private int failoverTryCount;
		private int failoverIntervalMillis;

		public JedisHandler(UnifiedJedisWarpper unifiedJedisWarpper) {
			this.unifiedJedis = unifiedJedisWarpper.getUnifiedJedis();
			this.failoverTryCount = unifiedJedisWarpper.getFailoverTryCount();
			this.failoverIntervalMillis = unifiedJedisWarpper.getFailoverInterval();
		}

		// 不加入任何判断是否是基础方法 必定基础方法调用的比较少,调用getJedis 一定要手动调用close关闭.
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			int tryCount = 0;
			JedisConnectionException jedisConnectionException = null;
			try {
				return method.invoke(unifiedJedis, args);
			} catch (JedisConnectionException e) {
				jedisConnectionException = e;
				while (tryCount < failoverTryCount) {
					try {
						Thread.sleep(failoverIntervalMillis);
					} catch (InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
					try {
						return method.invoke(unifiedJedis, args);
					} catch (JedisConnectionException e1) {
						tryCount++;
					}
				}
				throw jedisConnectionException;
			} catch (Throwable e) {
				throw e;
			}
		}
	}

	private static Class<?>[] jedisOperatorClass = new Class[] { JedisOperator.class };

	public static JedisOperator getJedisOperator(String fileName) throws Exception {
		JedisOperator operator = JEDIS_OPERATORS.get(fileName);
		if (operator != null) {
			return operator;
		}
		synchronized (JEDIS_OPERATORS) {
			operator = JEDIS_OPERATORS.get(fileName);
			if (operator == null) {
				JedisHandler handler = new JedisHandler(
						UnifiedJedisFactory.getUnifiedJedis(fileName));
				operator = (JedisOperator) Proxy.newProxyInstance(JedisOperator.class.getClassLoader(),
						jedisOperatorClass, handler);
				JEDIS_OPERATORS.put(fileName, operator);
			}
		}
		return operator;
	}

	public static void initField(Object target, Class<?> serviceType) throws Throwable {
		Field[] fields = serviceType.getDeclaredFields();
		for (Field field : fields) {
			JedisInjector jedisInjector = field.getAnnotation(JedisInjector.class);
			if (!field.getType().isPrimitive()) {
				Class<?> serviceClass = field.getType();
				if (jedisInjector != null && JedisOperator.class.isAssignableFrom(serviceClass)) {
					field.setAccessible(true);
					field.set(target, getJedisOperator(jedisInjector.value()));
				}
			}
		}
	}
}
