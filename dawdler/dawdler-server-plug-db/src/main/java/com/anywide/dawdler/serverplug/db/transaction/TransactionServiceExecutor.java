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
package com.anywide.dawdler.serverplug.db.transaction;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.bean.RequestBean;
import com.anywide.dawdler.core.bean.ResponseBean;
import com.anywide.dawdler.core.exception.DawdlerOperateException;
import com.anywide.dawdler.server.bean.ServicesBean;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.thread.processor.ServiceExecutor;
import com.anywide.dawdler.serverplug.db.annotation.DBTransaction;
import com.anywide.dawdler.serverplug.db.annotation.DBTransaction.MODE;
import com.anywide.dawdler.serverplug.db.annotation.DBTransaction.READ_CONFIG;
import com.anywide.dawdler.serverplug.db.datasource.RWSplittingDataSourceManager;
import com.anywide.dawdler.serverplug.db.datasource.RWSplittingDataSourceManager.MappingDecision;
import com.anywide.dawdler.util.ReflectionUtil;
import com.anywide.dawdler.util.reflectasm.MethodAccess;

/**
 * @author jackson.song
 * @version V1.0
 * @Title TransactionServiceExecutor.java
 * @Description 实现dawdler的执行器，将事务绑定到Service的执行过程中
 * @date 2015年10月09日
 * @email suxuan696@gmail.com
 */
public class TransactionServiceExecutor implements ServiceExecutor {
	private static final Logger logger = LoggerFactory.getLogger(TransactionServiceExecutor.class);

	@Override
	public void execute(RequestBean requestBean, ResponseBean responseBean, ServicesBean servicesBean) {
		Object object = servicesBean.getService();
		String methodName = requestBean.getMethodName();
		long index = Math.abs(requestBean.getSeq());
		MethodAccess methodAccess;
		int methodIndex;
		try {
			methodAccess = ReflectionUtil.getMethodAccess(object);
		} catch (Throwable e) {
			logger.error("", e);
			responseBean.setCause(new DawdlerOperateException(e.getMessage()));
			return;
		}
		if (requestBean.isFuzzy()) {
			methodIndex = methodAccess.getIndex(methodName,
					requestBean.getArgs() == null ? 0 : requestBean.getArgs().length);
		} else {
			methodIndex = methodAccess.getIndex(methodName, requestBean.getTypes());
		}

		DBTransaction dbt = methodAccess.getAnnotation(methodIndex, DBTransaction.class);
		TransactionStatus tranStatus = null;
		TransactionManager manager = null;
		SynReadConnectionObject synReadObj = null;
		JdbcReadConnectionStatus readStatus = null;
		try {
			if (dbt != null) {
				DawdlerContext context = DawdlerContext.getDawdlerContext();
				RWSplittingDataSourceManager dm = (RWSplittingDataSourceManager) context
						.getAttribute(RWSplittingDataSourceManager.DATASOURCE_MANAGER_PREFIX);
				MODE mode = dbt.mode();
				synReadObj = LocalConnectionFactory.getSynReadConnectionObject();
				if (dm != null) {
					MappingDecision mappingDecision = dm.getMappingDecision(object.getClass().getPackage().getName());
					readStatus = new JdbcReadConnectionStatus(dbt);
					if (dbt.readConfig() == READ_CONFIG.idem) {
						if (synReadObj == null) {
							synReadObj = new SynReadConnectionObject(mappingDecision, dbt);
							LocalConnectionFactory.setSynReadConnectionObject(synReadObj);
							if(mode == MODE.forceReadOnWrite) {
								ReadConnectionHolder readConnection = new ReadConnectionHolder(null);
								readConnection.setUseWriteConnection(true);
								synReadObj.setReadConnectionHolder(readConnection);
								readStatus.setCurrentConn(readConnection);
							} else {
								DataSource dataSource = mappingDecision.getReadDataSource(index);
								ReadConnectionHolder readConnection = new ReadConnectionHolder(dataSource);
								readConnection.requested();
								readStatus.setCurrentConn(readConnection);
								synReadObj.setReadConnectionHolder(readConnection);
							}
						} else {
							if (!synReadObj.getReadConnectionHolder().isUseWriteConnection())
								synReadObj.getReadConnectionHolder().requested();
							readStatus.setCurrentConn(synReadObj.getReadConnectionHolder());
//							synReadObj.setMappingDecision(mappingDecision);
//							synReadObj.setDBTransaction(dbt);
						}
					} else {
						if (synReadObj == null) {
							synReadObj = new SynReadConnectionObject(mappingDecision, dbt);
							LocalConnectionFactory.setSynReadConnectionObject(synReadObj);
							if(mode == MODE.forceReadOnWrite) {
								ReadConnectionHolder readConnection = new ReadConnectionHolder(null);
								readConnection.setUseWriteConnection(true);
								synReadObj.setReadConnectionHolder(readConnection);
								readStatus.setCurrentConn(readConnection);
							} else {
								DataSource dataSource = mappingDecision.getReadDataSource(index);
								ReadConnectionHolder readConnection = new ReadConnectionHolder(dataSource);
								readConnection.requested();
								readStatus.setCurrentConn(readConnection);
								synReadObj.setReadConnectionHolder(readConnection);
							}
						} else {
							if (mode == MODE.forceReadOnWrite) {
								readStatus.setOldConn(synReadObj.getReadConnectionHolder());
								ReadConnectionHolder readConnection = new ReadConnectionHolder(null);
								readConnection.setUseWriteConnection(true);
								readStatus.setCurrentConn(readConnection);
								synReadObj.setReadConnectionHolder(readConnection);
							} else {
								if (synReadObj.getMappingDecision().equals(mappingDecision)
										&& !synReadObj.getReadConnectionHolder().isUseWriteConnection()) {// 来自同一个数据源配置
									synReadObj.getReadConnectionHolder().requested();
									readStatus.setCurrentConn(synReadObj.getReadConnectionHolder());
								} else {
									DataSource dataSource = mappingDecision.getReadDataSource(index);
									readStatus.setOldConn(synReadObj.getReadConnectionHolder());
									ReadConnectionHolder readConnection = new ReadConnectionHolder(dataSource);
									readConnection.requested();
									readStatus.setCurrentConn(readConnection);
									synReadObj.setReadConnectionHolder(readConnection);
								}
							}
							synReadObj.setMappingDecision(mappingDecision);
							synReadObj.setDBTransaction(dbt);
						}
					}
					synReadObj.requested();
					try {
						if (!(mode == MODE.readOnly) && mappingDecision != null) {
							DataSource dataSource = mappingDecision.getWriteDataSource(index);
							manager = LocalConnectionFactory.getManager(dataSource);
							tranStatus = manager.getTransaction(dbt);
						}
					} catch (SQLException e) {
						logger.error("", e);
						responseBean.setCause(new DawdlerOperateException(e.getMessage()));
						return;
					}
				}
			}
			object = ReflectionUtil.invoke(methodAccess, object, methodIndex, requestBean.getArgs());
			responseBean.setTarget(object);
		} catch (Throwable e) {
			logger.error("", e);
			responseBean.setCause(new DawdlerOperateException(e.getMessage()));
			if (tranStatus != null) {
				if (!this.isNoRollBackFor(dbt.noRollbackFor(), e)) {
					try {
						tranStatus.setRollbackOnly();
					} catch (SQLException e1) {
						logger.error("", e1);
					}
				}
			}
		} finally {
			doCommit(tranStatus, manager);
			if (synReadObj != null) {
				if (!synReadObj.getReadConnectionHolder().isUseWriteConnection()) {
					try {
						readStatus.getCurrentConn().released();
					} catch (SQLException e) {
						logger.error("", e);
					}
				}
				ReadConnectionHolder readConnectionHolder = readStatus.getOldConn();
				if (readConnectionHolder != null)
					synReadObj.setReadConnectionHolder(readConnectionHolder);
				synReadObj.released();
			}
		}
	}

	public void doCommit(TransactionStatus tranStatus, TransactionManager manager) {
		if (tranStatus != null)
			if (!tranStatus.isCompleted()) {
				try {
					manager.commit(tranStatus);
				} catch (SQLException e) {
					logger.error("", e);
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
