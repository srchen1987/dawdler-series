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
package com.anywide.dawdler.breaker.filter;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.breaker.CircuitBreaker;
import com.anywide.dawdler.breaker.LocalCircuitBreaker;
import com.anywide.dawdler.breaker.metric.Metric;
import com.anywide.dawdler.breaker.state.CircuitBreakerState;
import com.anywide.dawdler.client.filter.DawdlerClientFilter;
import com.anywide.dawdler.client.filter.FilterChain;
import com.anywide.dawdler.client.filter.RequestWrapper;
import com.anywide.dawdler.core.bean.RequestBean;
/**
 * 
 * @Title:  CircuitBreakerFilter.java
 * @Description:    过滤器用来做熔断拦截
 * @author: jackson.song    
 * @date:   2018年3月16日  
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class CircuitBreakerFilter implements DawdlerClientFilter {
	private static Logger logger = LoggerFactory.getLogger(DawdlerClientFilter.class);
	private ConcurrentHashMap<String, CircuitBreaker> breakers = new ConcurrentHashMap<String, CircuitBreaker>();

	@Override
	public Object doFilter(RequestBean request, FilterChain chain) throws Exception {
		RequestWrapper rw = (RequestWrapper) request;
		com.anywide.dawdler.core.annotation.CircuitBreaker cb = rw.getCircuitBreaker();
		if (cb == null)
			return chain.doFilter(request);
		String unique = "".equals(cb.breakerKey())
				? (request.getPath() + request.getServiceName() + request.getMethodName())
				: cb.breakerKey();
		CircuitBreaker circuitBreaker = breakers.get(unique);
		if (circuitBreaker == null) {
			circuitBreaker = new LocalCircuitBreaker(cb);
			CircuitBreaker pre = breakers.putIfAbsent(unique, circuitBreaker);
			if (pre != null)
				circuitBreaker = pre;
		}
		CircuitBreakerState state = circuitBreaker.getState();
		Metric metric = state.getStw().currentMetrics();
		metric.totalIncrt();
		if (circuitBreaker.check()) {
			try {
				Object result = chain.doFilter(request);
				circuitBreaker.pass();
				return result;
			} catch (Exception e) {
				metric.failIncrt();
				circuitBreaker.fail();
				throw e;
			}
		} else {
			Class<?> c = rw.getProxyInterface();
			String fallbackMethod = cb.fallbackMethod();
			if (c != null && !"".equals(fallbackMethod)) {
				try {
					Method method = c.getMethod(fallbackMethod,request.getTypes());
					return method.invoke(null, request.getArgs());
				} catch (Exception e) {
					logger.error("", e);
					throw e;
				}
//				ReflectionUtil.getMethodAccess(c).getIndex(methodName, paramTypes)
//				methodAccess.getIndex(methodName, paramTypes);
			}

			throw new IllegalAccessException(
					"CircuitBreaker State : " + state.getState().get().name() + ", unique : " + unique);
		}
	}
	

}
