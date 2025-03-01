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
package com.anywide.dawdler.core.db.aspect;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.db.annotation.DBTransaction;
import com.anywide.dawdler.core.db.annotation.DBTransaction.MODE;
import com.anywide.dawdler.core.db.annotation.DBTransaction.READ_CONFIG;
import com.anywide.dawdler.core.db.annotation.SubDatabase;
import com.anywide.dawdler.core.db.datasource.RWSplittingDataSourceManager;
import com.anywide.dawdler.core.db.datasource.RWSplittingDataSourceManager.MappingDecision;
import com.anywide.dawdler.core.db.exception.TransactionRequiredException;
import com.anywide.dawdler.core.db.sub.SubRuleCache;
import com.anywide.dawdler.core.db.sub.rule.SubRule;
import com.anywide.dawdler.core.db.transaction.JdbcReadConnectionStatus;
import com.anywide.dawdler.core.db.transaction.LocalConnectionFactory;
import com.anywide.dawdler.core.db.transaction.ReadConnectionHolder;
import com.anywide.dawdler.core.db.transaction.SynReadConnectionObject;
import com.anywide.dawdler.core.db.transaction.TransactionManager;
import com.anywide.dawdler.core.db.transaction.TransactionStatus;
import com.anywide.dawdler.util.JexlEngineFactory;

/**
 * @author jackson.song
 * @version V1.0
 * 事务传播器(aop方式实现代替TransactionServiceExecutor)
 */
@Aspect
public class TransactionAspect {
	private static final Logger logger = LoggerFactory.getLogger(TransactionAspect.class);
	private RWSplittingDataSourceManager dataSourceManager = RWSplittingDataSourceManager.getInstance();
	private static final AtomicInteger INDEX = new AtomicInteger(0);
	private static final JexlEngine JEXL_ENGINE = JexlEngineFactory.getJexlEngine();
	private SubRule subRule;

	@Around("@annotation(com.anywide.dawdler.core.db.annotation.DBTransaction)")
	public Object execute(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
		Method method = methodSignature.getMethod();
		Object target = pjp.getTarget();
		String packageName = target.getClass().getPackage().getName();
		DBTransaction dbt = method.getAnnotation(DBTransaction.class);
		SubDatabase subDatabase = method.getAnnotation(SubDatabase.class);
		TransactionStatus tranStatus = null;
		TransactionManager manager = null;
		SynReadConnectionObject synReadObj = null;
		JdbcReadConnectionStatus readStatus = null;
		int index = 0;
		try {
			if (dbt != null) {
				MODE mode = dbt.mode();
				synReadObj = LocalConnectionFactory.getSynReadConnectionObject();
				if (dataSourceManager != null) {
					MappingDecision mappingDecision = dataSourceManager.getMappingDecision(packageName);
					if (mappingDecision == null) {
						StringBuilder builder = new StringBuilder();
						builder.append(packageName);
						builder.append(" transaction needs to be set.");
						// FIXME aspectjweaver bug 以下字符串 packageName + " transaction needs to be set."
						// 会导致织入失败.
						// 尝试 Mar 13, 2024 最新版 1.9.21.2 问题依旧 另外还引发了转换字节位数组为空的问题. 结论1.9.21 是小版本不稳定
						// throw new TransactionRequiredException(
						// packageName + " transaction needs to be set." );
						throw new TransactionRequiredException(builder.toString());
					}
					String subfix = null;
					if (subDatabase != null) {
						String expression = subDatabase.expression();
						int expressionIndex = expression.indexOf(".");
						String parameterName;
						if (expressionIndex != -1) {
							parameterName = expression.substring(0, expressionIndex);
						} else {
							parameterName = expression;
						}
						Object parameterValue = null;
						String[] parameterNames = methodSignature.getParameterNames();
						for (int i = 0; i < parameterNames.length; i++) {
							if (parameterNames[i].equals(parameterName)) {
								parameterValue = pjp.getArgs()[i];
								break;
							}
						}
						System.out.println(parameterName+"="+parameterValue);
						if (expressionIndex != -1) {
							JexlExpression jexlExpression = JEXL_ENGINE.createExpression(expression);
							JexlContext jexlContext = new MapContext();
							jexlContext.set(parameterName, parameterValue);
							parameterValue = jexlExpression.evaluate(jexlContext);
						}
						if (parameterValue == null) {
							throw new TransactionRequiredException(
									"subDatabase expression parameter not found.");
						}

						subRule = SubRuleCache.getSubRule(subDatabase.configPath(), subDatabase.subRuleType());
						subfix = subRule.delimiter().concat(subRule.getRuleSubfix(parameterValue));
					}
					readStatus = new JdbcReadConnectionStatus(dbt);
					if (dbt.readConfig() == READ_CONFIG.idem) {
						if (synReadObj == null) {
							synReadObj = new SynReadConnectionObject(mappingDecision, dbt);
							LocalConnectionFactory.setSynReadConnectionObject(synReadObj);
							if (mode == MODE.forceReadOnWrite) {
								ReadConnectionHolder readConnectionHolder = new ReadConnectionHolder(null);
								readConnectionHolder.setUseWriteConnection(true);
								synReadObj.setReadConnectionHolder(readConnectionHolder);
								readStatus.setCurrentConn(readConnectionHolder);
							} else {
								if (mappingDecision.needBalance()) {
									index = Math.abs(INDEX.getAndIncrement());
								}
								DataSource dataSource = mappingDecision.getReadDataSource(subfix, index);
								ReadConnectionHolder readConnectionHolder = new ReadConnectionHolder(dataSource);
								readConnectionHolder.requested();
								readStatus.setCurrentConn(readConnectionHolder);
								synReadObj.setReadConnectionHolder(readConnectionHolder);
							}
						} else {
							if (!synReadObj.getReadConnectionHolder().isUseWriteConnection()) {
								synReadObj.getReadConnectionHolder().requested();
							}
							readStatus.setCurrentConn(synReadObj.getReadConnectionHolder());
						}
					} else {
						if (synReadObj == null) {
							synReadObj = new SynReadConnectionObject(mappingDecision, dbt);
							LocalConnectionFactory.setSynReadConnectionObject(synReadObj);
							if (mode == MODE.forceReadOnWrite) {
								ReadConnectionHolder readConnectionHolder = new ReadConnectionHolder(null);
								readConnectionHolder.setUseWriteConnection(true);
								synReadObj.setReadConnectionHolder(readConnectionHolder);
								readStatus.setCurrentConn(readConnectionHolder);
							} else {
								if (mappingDecision.needBalance()) {
									index = Math.abs(INDEX.getAndIncrement());
								}
								DataSource dataSource = mappingDecision.getReadDataSource(subfix, index);
								ReadConnectionHolder readConnectionHolder = new ReadConnectionHolder(dataSource);
								readConnectionHolder.requested();
								readStatus.setCurrentConn(readConnectionHolder);
								synReadObj.setReadConnectionHolder(readConnectionHolder);
							}
						} else {
							if (mode == MODE.forceReadOnWrite) {
								readStatus.setOldConn(synReadObj.getReadConnectionHolder());
								ReadConnectionHolder readConnectionHolder = new ReadConnectionHolder(null);
								readConnectionHolder.setUseWriteConnection(true);
								readStatus.setCurrentConn(readConnectionHolder);
								synReadObj.setReadConnectionHolder(readConnectionHolder);
							} else {
								if (synReadObj.getMappingDecision().equals(mappingDecision)
										&& !synReadObj.getReadConnectionHolder().isUseWriteConnection()) {
									synReadObj.getReadConnectionHolder().requested();
									readStatus.setCurrentConn(synReadObj.getReadConnectionHolder());
								} else {
									if (mappingDecision.needBalance()) {
										index = Math.abs(INDEX.getAndIncrement());
									}
									DataSource dataSource = mappingDecision.getReadDataSource(subfix, index);
									readStatus.setOldConn(synReadObj.getReadConnectionHolder());
									ReadConnectionHolder readConnectionHolder = new ReadConnectionHolder(dataSource);
									readConnectionHolder.requested();
									readStatus.setCurrentConn(readConnectionHolder);
									synReadObj.setReadConnectionHolder(readConnectionHolder);
								}
							}
							synReadObj.setMappingDecision(mappingDecision);
							synReadObj.setDBTransaction(dbt);
						}
					}
					synReadObj.requested();
					if (mode != MODE.readOnly && mappingDecision != null) {
						if (mappingDecision.needBalance()) {
							index = Math.abs(INDEX.getAndIncrement());
						}
						DataSource dataSource = mappingDecision.getWriteDataSource(subfix, index);
						manager = LocalConnectionFactory.getManager(dataSource);
						tranStatus = manager.getTransaction(dbt);
					}
				}
			}
			return pjp.proceed();
		} catch (Throwable e) {
			logger.error("", e);
			if (tranStatus != null) {
				if (!this.isNoRollBackFor(dbt.noRollbackFor(), e)) {
					try {
						tranStatus.setRollbackOnly();
					} catch (SQLException e1) {
						logger.error("", e1);
					}
				}
			}
			throw e;
		} finally {
			doCommit(tranStatus, manager);
			if (synReadObj != null) {
				if (synReadObj.getReadConnectionHolder() != null
						&& !synReadObj.getReadConnectionHolder().isUseWriteConnection()) {
					try {
						readStatus.getCurrentConn().released();
					} catch (SQLException e) {
						logger.error("", e);
					}
				}
				ReadConnectionHolder readConnectionHolder = readStatus.getOldConn();
				synReadObj.released();
				LocalConnectionFactory.removeReadConnection();
				if (readConnectionHolder != null) {
					synReadObj.setReadConnectionHolder(readConnectionHolder);
				}
			}
		}
	}

	public void doCommit(TransactionStatus tranStatus, TransactionManager manager) {
		if (tranStatus != null) {
			if (!tranStatus.isCompleted()) {
				try {
					manager.commit(tranStatus);
				} catch (SQLException e) {
					logger.error("", e);
				}
			}
		}
	}

	private boolean isNoRollBackFor(Class<? extends Throwable>[] noRollBackType, Throwable e) {
		for (Class<? extends Throwable> cls : noRollBackType) {
			if (cls.isInstance(e)) {
				return true;
			}
		}
		return false;
	}

}
