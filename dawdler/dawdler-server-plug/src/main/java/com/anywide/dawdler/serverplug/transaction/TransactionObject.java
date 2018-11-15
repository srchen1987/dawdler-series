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

import com.anywide.dawdler.serverplug.annotation.Isolation;
/**
 * 
 * @Title:  TransactionObject.java   
 * @Description:    事务类相关信息的存储类   
 * @author: jackson.song    
 * @date:   2015年09月28日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class TransactionObject {
	private WriteConnectionHolder holder = null;
	private DataSource dataSource = null;
	private Isolation originalIsolationLevel; 
	public TransactionObject(final WriteConnectionHolder holder, final Isolation originalIsolationLevel,
			final DataSource dataSource) throws SQLException {
		this.holder = holder;
		this.dataSource = dataSource;
		this.originalIsolationLevel = originalIsolationLevel;
	}

	public Isolation getOriIsolationLevel() {
		return this.originalIsolationLevel;
	}

	public WriteConnectionHolder getHolder() {
		return this.holder;
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

	public SavepointManager getSavepointManager() {
		return this.getHolder();
	}

	public void rollback() throws SQLException {
		if (this.holder.hasTransaction()) {
			this.holder.getConnection().rollback();
		}
	}

	public void commit() throws SQLException {
		if (this.holder.hasTransaction()) {
			this.holder.getConnection().commit();
		}
	}

	public boolean hasTransaction() throws SQLException {
		return this.holder.hasTransaction();
	}

	private boolean recoverMark = false;

	public void beginTransaction() throws SQLException {
		if (!this.holder.hasTransaction()) {
			this.recoverMark = true;
		}
		this.holder.setTransaction();
	}

	public void stopTransaction() throws SQLException {
		if (!this.recoverMark) {
			return;
		}
		this.recoverMark = false;
		this.holder.cancelTransaction();
	}
}