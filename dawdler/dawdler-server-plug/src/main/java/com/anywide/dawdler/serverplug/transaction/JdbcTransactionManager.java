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

import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.serverplug.annotation.DBTransaction;
import com.anywide.dawdler.serverplug.annotation.Isolation;
import com.anywide.dawdler.serverplug.annotation.Propagation;

/**
 * 
 * @Title: JdbcTransactionManager.java
 * @Description: 事务管理器 参考spring的实现
 * @author: jackson.song
 * @date: 2015年09月28日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class JdbcTransactionManager implements TransactionManager {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	private DataSource dataSource = null;

	protected JdbcTransactionManager(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

	@Override
	public final TransactionStatus getTransaction(DBTransaction dBTransaction) throws SQLException {
		JdbcTransactionStatus defStatus = new JdbcTransactionStatus(dBTransaction);
		defStatus.setTranConn(this.doGetConnection(defStatus));
		Propagation behavior = defStatus.getPropagationBehavior();
		if (this.isExistingTransaction(defStatus)) {
			switch (behavior) {
			case REQUIRES_NEW: {
				suspend(defStatus);
				doBegin(defStatus);
				break;
			}
			case NESTED: {
				defStatus.markHeldSavepoint();
				break;
			}
			case NOT_SUPPORTED: {
				suspend(defStatus);
				break;
			}
			case NEVER: {
				cleanupAfterCompletion(defStatus);
				throw new SQLException("Existing transaction found for transaction marked with propagation 'never'.");
			}
			default:
				break;
			}
		} else {
			if (behavior == Propagation.REQUIRED || behavior == Propagation.REQUIRES_NEW
					|| behavior == Propagation.NESTED) {
				this.doBegin(defStatus);
			}
			if (behavior == Propagation.MANDATORY) {
				this.cleanupAfterCompletion(defStatus);
				throw new SQLException(
						"No existing transaction found for transaction marked with propagation 'mandatory'.");
			}
		}
		return defStatus;
	}

	private boolean isExistingTransaction(final JdbcTransactionStatus defStatus) throws SQLException {
		return defStatus.getTranConn().hasTransaction();
	}

	protected void doBegin(final JdbcTransactionStatus defStatus) throws SQLException {
		TransactionObject tranConn = defStatus.getTranConn();
		tranConn.beginTransaction();
	}

	@Override
	public final void commit(final TransactionStatus status) throws SQLException {
		JdbcTransactionStatus defStatus = (JdbcTransactionStatus) status;
		if (defStatus.isCompleted()) {
			throw new SQLException(
					"Transaction is already completed - do not call commit or rollback more than once per transaction.");
		}
		if (defStatus.isReadOnly() || defStatus.isRollbackOnly()) {
			this.rollBack(defStatus);
			return;
		}
		try {
			if (defStatus.hasSavepoint()) {
				defStatus.releaseHeldSavepoint();
			} else if (defStatus.isNewConnection()) {
				this.doCommit(defStatus);
			}
		} catch (SQLException ex) {
			this.doRollback(defStatus);
			throw ex;
		} finally {
			this.cleanupAfterCompletion(defStatus);
		}
	}

	protected void doCommit(final JdbcTransactionStatus defStatus) throws SQLException {
		TransactionObject tranObject = defStatus.getTranConn();
		tranObject.commit();
	}

	@Override
	public final void rollBack(final TransactionStatus status) throws SQLException {
		JdbcTransactionStatus defStatus = (JdbcTransactionStatus) status;
		if (defStatus.isCompleted()) {
			throw new SQLException(
					"Transaction is already completed - do not call commit or rollback more than once per transaction");
		}
		try {
			if (defStatus.hasSavepoint()) {
				defStatus.rollbackToHeldSavepoint();
			} else if (defStatus.isNewConnection()) {
				this.doRollback(defStatus);
			}
			//
		} catch (SQLException ex) {
			this.doRollback(defStatus);
			throw ex;
		} finally {
			this.cleanupAfterCompletion(defStatus);
		}
	}

	protected void doRollback(final JdbcTransactionStatus defStatus) throws SQLException {
		TransactionObject tranObject = defStatus.getTranConn();
		tranObject.rollback();
	}

	protected final void suspend(final JdbcTransactionStatus defStatus) throws SQLException {
		if (defStatus.isSuspend()) {
			throw new SQLException("the Transaction has Suspend.");
		}
		//
		TransactionObject tranConn = defStatus.getTranConn();
		defStatus.setSuspendConn(tranConn);
		LocalConnectionFacotry.removeCurrentConnectionHolder(dataSource);
		defStatus.setTranConn(this.doGetConnection(defStatus));
	}

	protected final void resume(final JdbcTransactionStatus defStatus) throws SQLException {
		if (!defStatus.isCompleted()) {
			throw new SQLException("the Transaction has not completed.");
		}
		if (defStatus.isSuspend()) {
			TransactionObject tranConn = defStatus.getSuspendConn();
			LocalConnectionFacotry.changeCurrentConnectionHolder(getDataSource(), tranConn.getHolder());
			LocalConnectionFacotry.setWriteConnection(tranConn.getHolder().getConnection());
			defStatus.setTranConn(tranConn);
			defStatus.setSuspendConn(null);
			tranConn.getHolder().released();
		}
	}

	private void cleanupAfterCompletion(final JdbcTransactionStatus defStatus) throws SQLException {
		defStatus.setCompleted();
		TransactionObject tranObj = defStatus.getTranConn();
		Isolation transactionIsolation = tranObj.getOriIsolationLevel();
		if (transactionIsolation != null)
			tranObj.getHolder().getConnection().setTransactionIsolation(transactionIsolation.ordinal());
		tranObj.getHolder().released();
		tranObj.stopTransaction();
		if (defStatus.isSuspend()) {
			this.resume(defStatus);
		}
		defStatus.setTranConn(null);
		defStatus.setSuspendConn(null);
	}

	protected TransactionObject doGetConnection(final JdbcTransactionStatus defStatus) throws SQLException {
		WriteConnectionHolder holder = LocalConnectionFacotry.currentConnectionHolder(dataSource);
		if (!holder.isOpen() || !holder.hasTransaction())
			defStatus.markNewConnection();
		holder.requested();
		int isolationLevel = holder.getConnection().getTransactionIsolation();
		Isolation originalIsolationLevel = null;
		if (defStatus.getIsolationLevel() != Isolation.DEFAULT) {
			holder.getConnection().setTransactionIsolation(defStatus.getIsolationLevel().ordinal());
			originalIsolationLevel = Isolation.valueOf(isolationLevel);
		}
		return new TransactionObject(holder, originalIsolationLevel, this.getDataSource());
	}
}