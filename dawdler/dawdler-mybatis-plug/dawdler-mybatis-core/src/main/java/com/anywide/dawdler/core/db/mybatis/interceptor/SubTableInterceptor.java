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
package com.anywide.dawdler.core.db.mybatis.interceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;

import com.anywide.dawdler.core.db.mybatis.annotation.SubParam;
import com.anywide.dawdler.core.db.mybatis.annotation.SubTable;
import com.anywide.dawdler.core.db.sub.SubRuleCache;
import com.anywide.dawdler.core.db.sub.rule.SubRule;
import com.anywide.dawdler.util.JexlEngineFactory;
import com.anywide.dawdler.util.WordReplaceUtil;

/**
 * @author jackson.song
 * @version V1.0
 * mybatis分表拦截器
 */
@Intercepts({
		@Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class })
})
public class SubTableInterceptor implements Interceptor {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(SubTableInterceptor.class);
	private static final ConcurrentHashMap<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Method, Parameter[]> METHOD_PARAMETER_CACHE = new ConcurrentHashMap<>();
	private static final JexlEngine JEXL_ENGINE = JexlEngineFactory.getJexlEngine();
	private static final String BOUND_SQL = "delegate.boundSql.sql";

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		StatementHandler handler = (StatementHandler) invocation.getTarget();
		MetaObject metaObject = SystemMetaObject.forObject(handler);
		MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
		Method method = findMethod(ms);
		if (method == null) {
			return invocation.proceed();
		}
		handleAnnotations(method, ms, handler.getBoundSql().getParameterObject(), metaObject);
		return invocation.proceed();
	}

	private Method findMethod(MappedStatement ms) {
		String msId = ms.getId();
		return METHOD_CACHE.computeIfAbsent(msId, key -> {
			try {
				String className = key.substring(0, key.lastIndexOf("."));
				String methodName = key.substring(key.lastIndexOf(".") + 1);
				Class<?> clazz = Class.forName(className);
				Method[] methods = clazz.getDeclaredMethods();
				for (Method method : methods) {
					if (methodName.equals(method.getName())) {
						return method;
					}
				}
			} catch (Exception e) {
				log.error("Failed to find method: " + key, e);
			}
			return null;
		});

	}

	private void handleAnnotations(Method method, MappedStatement ms, Object parameterObject, MetaObject metaObject)
			throws Exception {
		SubTable subTable = method.getAnnotation(SubTable.class);
		if (subTable != null) {
			if (parameterObject != null) {
				Parameter[] parameters = METHOD_PARAMETER_CACHE.computeIfAbsent(
						method, Method::getParameters);
				Parameter parameter = findParameter(parameters);
				if (parameter != null) {
					Param param = parameter.getAnnotation(Param.class);
					Object value = null;
					String paramName = null;
					if (param != null) {
						paramName = param.value();
					}
					if (parameterObject instanceof Map && paramName != null) {
						value = ((Map<?, ?>) parameterObject).get(paramName);
					} else {
						value = parameterObject;
					}
					if (value != null) {
						String expression = subTable.expression();
						if (!expression.isEmpty()) {
							int index = expression.indexOf(".");
							if (index != -1) {
								String objName = expression.substring(0, index);
								JexlExpression jexlExpression = JEXL_ENGINE.createExpression(expression);
								JexlContext jexlContext = new MapContext();
								jexlContext.set(objName, value);
								value = jexlExpression.evaluate(jexlContext);
							}
						}
						SubRule subRule = SubRuleCache.getSubRule(subTable.configPath(), subTable.subRuleType());
						String subfix = subRule.delimiter().concat(subRule.getRuleSubfix(value));
						metaObject.setValue(BOUND_SQL,
								replaceTable(metaObject.getValue(BOUND_SQL).toString(), subTable.tables(), subfix));
					}
				}
			}
		}

	}

	private String replaceTable(String sql, String[] tables, String subfix) {
		for (String table : tables) {
			sql = WordReplaceUtil.replaceSpecialWord(sql, table, table.concat(subfix));
		}
		return sql;
	}

	private Parameter findParameter(Parameter[] parameters) {
		for (Parameter parameter : parameters) {
			if (parameter.isAnnotationPresent(SubParam.class)) {
				return parameter;
			}
		}
		return null;
	}

}
