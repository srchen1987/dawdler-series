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
package com.anywide.dawdler.serverplug.transaction;

import com.anywide.dawdler.core.bean.RequestBean;
import com.anywide.dawdler.core.bean.ResponseBean;
import com.anywide.dawdler.core.exception.DawdlerOperateException;
import com.anywide.dawdler.server.bean.ServicesBean;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.thread.processor.ServiceExecutor;
import com.anywide.dawdler.serverplug.annotation.DBTransaction;
import com.anywide.dawdler.serverplug.annotation.DBTransaction.MODE;
import com.anywide.dawdler.serverplug.annotation.DBTransaction.READ_CONFIG;
import com.anywide.dawdler.serverplug.datasource.RWSplittingDataSourceManager;
import com.anywide.dawdler.serverplug.datasource.RWSplittingDataSourceManager.MappingDecision;
import com.anywide.dawdler.util.ReflectionUtil;
import com.anywide.dawdler.util.reflectasm.MethodAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

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
        MethodAccess methodAccess = ReflectionUtil.getMethodAccess(object);
        int methodIndex;
        if (requestBean.isFuzzy()) {
            methodIndex = methodAccess.getIndex(methodName,
                    requestBean.getArgs() == null ? 0 : requestBean.getArgs().length);
        } else {
            methodIndex = methodAccess.getIndex(methodName, requestBean.getTypes());
        }
        DBTransaction dbt = methodAccess.getAnnotation(methodIndex, DBTransaction.class);
        TransactionStatus tranStatus = null;
        TransactionManager manager = null;
        SynReadConnectionObject sb = null;
        JdbcReadConnectionStatus status = null;
        try {
            if (dbt != null && dbt.useConnection()) {
                DawdlerContext context = DawdlerContext.getDawdlerContext();
                RWSplittingDataSourceManager dm = (RWSplittingDataSourceManager) context
                        .getAttribute(RWSplittingDataSourceManager.DATASOURCE_MANAGER_PREFIX);
                MODE mode = dbt.mode();
                sb = LocalConnectionFacotry.getSynReadConnectionObject();
                if (dm != null) {
                    MappingDecision mappingDecision = dm.getMappingDecision(object.getClass().getPackage().getName());
                    status = new JdbcReadConnectionStatus(dbt);
                    if (dbt.readConfig() == READ_CONFIG.idem) {
                        if (sb == null) {
                            sb = new SynReadConnectionObject(mappingDecision, dbt);
                            LocalConnectionFacotry.setSynReadConnectionObject(sb);
                            if (mode == MODE.deferToConfig) {
                                DataSource dataSource = mappingDecision.getReadDataSource();
                                ReadConnectionHolder readConnection = new ReadConnectionHolder(dataSource);
                                readConnection.requested();
                                status.setCurrentConn(readConnection);
                                sb.setReadConnectionHolder(readConnection);
                            } else {
                                ReadConnectionHolder readConnection = new ReadConnectionHolder(null);
                                readConnection.setUseWriteConnection(true);
                                sb.setReadConnectionHolder(readConnection);
                                status.setCurrentConn(readConnection);
                            }
                        } else {
                            if (!sb.getReadConnectionHolder().isUseWriteConnection())
                                sb.getReadConnectionHolder().requested();
                            status.setCurrentConn(sb.getReadConnectionHolder());
                            sb.setMappingDecision(mappingDecision);
                            sb.setdBTransaction(dbt);
                        }
                    } else {
                        if (sb == null) {
                            sb = new SynReadConnectionObject(mappingDecision, dbt);
                            LocalConnectionFacotry.setSynReadConnectionObject(sb);
                            if (mode == MODE.deferToConfig) {
                                DataSource dataSource = mappingDecision.getReadDataSource();
                                ReadConnectionHolder readConnection = new ReadConnectionHolder(dataSource);
                                readConnection.requested();
                                status.setCurrentConn(readConnection);
                                sb.setReadConnectionHolder(readConnection);
                            } else {
                                ReadConnectionHolder readConnection = new ReadConnectionHolder(null);
                                readConnection.setUseWriteConnection(true);
                                status.setCurrentConn(readConnection);
                                sb.setReadConnectionHolder(readConnection);
                            }
                        } else {
                            if (mode == MODE.deferToConfig) {
                                if (sb.getMappingDecision().equals(mappingDecision)
                                        && !sb.getReadConnectionHolder().isUseWriteConnection()) {// 来自同一个数据源配置
                                    sb.getReadConnectionHolder().requested();
                                    status.setCurrentConn(sb.getReadConnectionHolder());
                                } else {
                                    DataSource dataSource = mappingDecision.getReadDataSource();
                                    status.setOldConn(sb.getReadConnectionHolder());
                                    ReadConnectionHolder readConnection = new ReadConnectionHolder(dataSource);
                                    readConnection.requested();
                                    status.setCurrentConn(readConnection);
                                    sb.setReadConnectionHolder(readConnection);
                                }
                            } else {
                                status.setOldConn(sb.getReadConnectionHolder());
                                ReadConnectionHolder readConnection = new ReadConnectionHolder(null);
                                readConnection.setUseWriteConnection(true);
                                status.setCurrentConn(readConnection);
                                sb.setReadConnectionHolder(readConnection);
                            }
                            sb.setMappingDecision(mappingDecision);
                            sb.setdBTransaction(dbt);
                        }
                    }
                    sb.requested();
                    try {
                        if (mappingDecision != null) {
                            DataSource dataSource = mappingDecision.getWriteDataSource();
                            manager = LocalConnectionFacotry.getManager(dataSource);
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
        } catch (Exception e) {
            logger.error("", e);
            responseBean.setCause(new DawdlerOperateException(e.getMessage()));
            if (tranStatus != null)
                if (!this.isNoRollBackFor(dbt.noRollbackFor(), e)) {
                    try {
                        tranStatus.setRollbackOnly();
                    } catch (SQLException e1) {
                        logger.error("", e1);
                    }
                }
        } finally {
            doCommit(tranStatus, manager);
            if (sb != null) {
                if (!sb.getReadConnectionHolder().isUseWriteConnection()) {
                    try {
                        status.getCurrentConn().released();
                    } catch (SQLException e) {
                        logger.error("", e);
                    }
                }
                ReadConnectionHolder readConnectionHolder = status.getOldConn();
                if (readConnectionHolder != null)
                    sb.setReadConnectionHolder(readConnectionHolder);
                sb.released();
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
