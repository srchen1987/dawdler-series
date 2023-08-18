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
package com.anywide.dawdler.cache.aspect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.jexl3.internal.Engine;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import com.anywide.dawdler.cache.Cache;
import com.anywide.dawdler.cache.CacheManager;
import com.anywide.dawdler.cache.CacheManagerProvider;
import com.anywide.dawdler.cache.annotation.CacheEvict;
import com.anywide.dawdler.cache.annotation.CachePut;
import com.anywide.dawdler.cache.annotation.Cacheable;
import com.anywide.dawdler.cache.exception.ConditionTypeException;
import com.anywide.dawdler.cache.exception.KeyExpressionException;
import com.anywide.dawdler.util.reflectasm.ParameterNameReader;

/**
 *
 * @author jackson.song
 * @version V1.0
 * @Title CacheAspect.java
 * @Description cache切面
 * @date 2023年7月30日
 * @email suxuan696@gmail.com
 */
@Aspect
public class CacheAspect {
	private static final JexlEngine JEXL_ENGINE;
	static {
		JexlBuilder jexlBuilder = new JexlBuilder();
		jexlBuilder.options().setStrictArithmetic(false);
		JEXL_ENGINE = new Engine(jexlBuilder);
	}

	@Pointcut("@annotation(com.anywide.dawdler.cache.annotation.Cacheable)")
	public void cacheable() {
	}

	@Around("cacheable()")
	public Object interceptCacheableMethod(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
		Method method = methodSignature.getMethod();
		Object[] args = pjp.getArgs();
		String targetName = pjp.getTarget().getClass().getName();
		Cacheable cacheable = method.getAnnotation(Cacheable.class);
		String cacheManagerName = cacheable.cacheManager();
		String condition = cacheable.condition();
		String unless = cacheable.unless();
		CacheManager cacheManager = CacheManagerProvider.getCacheManager(cacheManagerName);
		if (cacheManager == null) {
			throw new IllegalArgumentException("not found cacheManagerName " + cacheManagerName + " !");
		}
		Cache cache = cacheManager.getCache(cacheable.cacheName());
		if (cache == null) {
			throw new IllegalArgumentException("not found cacheName " + cacheable.cacheName() + " !");
		}
		Class<?> clazz = pjp.getTarget().getClass();
		Boolean conditionResult = null;
		JexlContext paramsContext = null;
		if (!condition.equals("")) {
			paramsContext = initParamsContext(clazz, method, args);
			JexlExpression expression = JEXL_ENGINE.createExpression(condition);
			Object conditionObjResult = expression.evaluate(paramsContext);
			if (conditionObjResult == null || !(conditionObjResult instanceof Boolean)) {
				throw new ConditionTypeException(condition + " result not Boolean type! ");
			}
			conditionResult = (Boolean) conditionObjResult;
		}

		String key = cacheable.key();
		if (key.equals("")) {
			StringBuilder builder = new StringBuilder();
			builder.append(targetName);
			builder.append("/");
			builder.append(method.getName());
			builder.append("/");
			builder.append(Arrays.deepHashCode(args));
			key = builder.toString();
		} else {
			if (paramsContext == null) {
				paramsContext = initParamsContext(clazz, method, args);
			}
			JexlExpression expression = JEXL_ENGINE.createExpression(key);
			Object objKey = expression.evaluate(paramsContext);
			if (objKey == null) {
				throw new KeyExpressionException(key + " expression evaluate can't be null !");
			}
			key = objKey.toString();
		}
		Object result = cache.get(key);
		if (result != null) {
			return result;
		} else {
			result = pjp.proceed();
			if (conditionResult != null && !conditionResult) {
				return result;
			}
			if (!unless.equals("")) {
				JexlExpression expression = JEXL_ENGINE.createExpression(unless);
				JexlContext context = new MapContext();
				context.set("result", result);
				Object unlessObjResult = expression.evaluate(context);
				if (unlessObjResult == null || !(unlessObjResult instanceof Boolean)) {
					throw new ConditionTypeException(condition + " result not Boolean type! ");
				}
				if ((Boolean) unlessObjResult) {
					return result;
				}
			}
			cache.put(key, result);
			return result;
		}
	}

	public JexlContext initParamsContext(Class<?> clazz, Method method, Object[] args) {
		JexlContext paramsContext = new MapContext();
		String[] variableNames = ParameterNameReader.getParameterNames(clazz).get(method);
		if (variableNames != null) {
			for (int i = 0; i < variableNames.length; i++) {
				paramsContext.set(variableNames[i], args[i]);
			}
		}
		return paramsContext;
	}

	@Pointcut("@annotation(com.anywide.dawdler.cache.annotation.CacheEvict)")
	public void cacheEvict() {
	}

	@Around("cacheEvict()")
	public Object interceptCacheEvictMethod(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
		Method method = methodSignature.getMethod();
		Object[] args = pjp.getArgs();
		String targetName = pjp.getTarget().getClass().getName();
		CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);
		String cacheManagerName = cacheEvict.cacheManager();
		String condition = cacheEvict.condition();
		CacheManager cacheManager = CacheManagerProvider.getCacheManager(cacheManagerName);
		if (cacheManager == null) {
			throw new IllegalArgumentException("not found cacheManagerName " + cacheManagerName + " !");
		}
		List<Cache> cacheList = new ArrayList<>(cacheEvict.cacheNames().length);
		for (String cacheName : cacheEvict.cacheNames()) {
			Cache cache = cacheManager.getCache(cacheName);
			if (cache == null) {
				throw new IllegalArgumentException("not found cacheName " + cacheName + " !");
			}
			cacheList.add(cache);
		}

		Class<?> clazz = pjp.getTarget().getClass();
		Boolean conditionResult = null;
		JexlContext paramsContext = null;
		if (!condition.equals("")) {
			paramsContext = initParamsContext(clazz, method, args);
			JexlExpression expression = JEXL_ENGINE.createExpression(condition);
			Object conditionObjResult = expression.evaluate(paramsContext);
			if (conditionObjResult == null || !(conditionObjResult instanceof Boolean)) {
				throw new ConditionTypeException(condition + " result not Boolean type! ");
			}
			conditionResult = (Boolean) conditionObjResult;
		}

		String key = cacheEvict.key();
		if (key.equals("")) {
			StringBuilder builder = new StringBuilder();
			builder.append(targetName);
			builder.append("/");
			builder.append(method.getName());
			builder.append("/");
			builder.append(Arrays.deepHashCode(args));
			key = builder.toString();
		} else {
			if (paramsContext == null) {
				paramsContext = initParamsContext(clazz, method, args);
			}
			JexlExpression expression = JEXL_ENGINE.createExpression(key);
			Object objKey = expression.evaluate(paramsContext);
			if (objKey == null) {
				throw new KeyExpressionException(key + " expression evaluate can't be null !");
			}
			key = objKey.toString();
		}

		if (cacheEvict.beforeInvocation()) {
			if (conditionResult == null || conditionResult) {
				if (cacheEvict.allEntries()) {
					for (Cache cache : cacheList) {
						cache.clear();
					}
				} else {
					for (Cache cache : cacheList) {
						cache.remove(key);
					}
				}
			}
			return pjp.proceed();
		} else {
			Object result = pjp.proceed();
			if (conditionResult == null || conditionResult) {
				if (cacheEvict.allEntries()) {
					for (Cache cache : cacheList) {
						cache.clear();
					}
				} else {
					for (Cache cache : cacheList) {
						cache.remove(key);
					}
				}
			}
			return result;
		}
	}

	@Pointcut("@annotation(com.anywide.dawdler.cache.annotation.CachePut)")
	public void cachePut() {
	}

	@Around("cachePut()")
	public Object interceptCachePutMethod(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
		Method method = methodSignature.getMethod();
		Object[] args = pjp.getArgs();
		String targetName = pjp.getTarget().getClass().getName();
		CachePut cachePut = method.getAnnotation(CachePut.class);
		String cacheManagerName = cachePut.cacheManager();
		String condition = cachePut.condition();
		String unless = cachePut.unless();
		CacheManager cacheManager = CacheManagerProvider.getCacheManager(cacheManagerName);
		if (cacheManager == null) {
			throw new IllegalArgumentException("not found cacheManagerName " + cacheManagerName + " !");
		}
		Cache cache = cacheManager.getCache(cachePut.cacheName());
		if (cache == null) {
			throw new IllegalArgumentException("not found cacheName " + cachePut.cacheName() + " !");
		}
		Class<?> clazz = pjp.getTarget().getClass();
		Boolean conditionResult = null;
		JexlContext paramsContext = null;
		if (!condition.equals("")) {
			paramsContext = initParamsContext(clazz, method, args);
			JexlExpression expression = JEXL_ENGINE.createExpression(condition);
			Object conditionObjResult = expression.evaluate(paramsContext);
			if (conditionObjResult == null || !(conditionObjResult instanceof Boolean)) {
				throw new ConditionTypeException(condition + " result not Boolean type! ");
			}
			conditionResult = (Boolean) conditionObjResult;
		}

		String key = cachePut.key();
		if (key.equals("")) {
			StringBuilder builder = new StringBuilder();
			builder.append(targetName);
			builder.append("/");
			builder.append(method.getName());
			builder.append("/");
			builder.append(Arrays.deepHashCode(args));
			key = builder.toString();
		} else {
			if (paramsContext == null) {
				paramsContext = initParamsContext(clazz, method, args);
			}
			JexlExpression expression = JEXL_ENGINE.createExpression(key);
			Object objKey = expression.evaluate(paramsContext);
			if (objKey == null) {
				throw new KeyExpressionException(key + " expression evaluate can't be null !");
			}
			key = objKey.toString();
		}
		Object result = pjp.proceed();
		if (conditionResult != null && !conditionResult) {
			return result;
		}
		if (!unless.equals("")) {
			JexlExpression expression = JEXL_ENGINE.createExpression(unless);
			JexlContext context = new MapContext();
			context.set("result", result);
			Object unlessObjResult = expression.evaluate(context);
			if (unlessObjResult == null || !(unlessObjResult instanceof Boolean)) {
				throw new ConditionTypeException(condition + " result not Boolean type! ");
			}
			if ((Boolean) unlessObjResult) {
				return result;
			}
		}
		cache.put(key, result);
		return result;
	}

}
