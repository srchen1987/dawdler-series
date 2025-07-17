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
package club.dawdler.core.db.transaction;

import java.sql.SQLException;

import javax.sql.DataSource;

import club.dawdler.core.db.annotation.Isolation;

/**
 * @author jackson.song
 * @version V1.0
 * 事务类相关信息的存储类
 */
public class TransactionObject {
	private WriteConnectionHolder holder = null;
	private DataSource dataSource = null;
	private final Isolation originalIsolationLevel;
	private boolean recoverMark = false;

	public TransactionObject(final WriteConnectionHolder holder, final Isolation originalIsolationLevel,
			final DataSource dataSource) throws SQLException {
		this.holder = holder;
		this.dataSource = dataSource;
		this.originalIsolationLevel = originalIsolationLevel;
	}

	public Isolation getOriIsolationLevel() {
		return this.originalIsolationLevel;
	}

	WriteConnectionHolder getHolder() {
		return this.holder;
	}

	DataSource getDataSource() {
		return this.dataSource;
	}

	SavepointManager getSavepointManager() {
		return this.getHolder();
	}

	void rollback() throws SQLException {
		if (this.holder.hasTransaction()) {
			this.holder.getConnection().rollback();
		}
	}

	void commit() throws SQLException {
		if (this.holder.hasTransaction()) {
			this.holder.getConnection().commit();
		}
	}

	boolean hasTransaction() throws SQLException {
		return this.holder.hasTransaction();
	}

	void beginTransaction() throws SQLException {
		if (!this.holder.hasTransaction()) {
			this.recoverMark = true;
		}
		this.holder.setTransaction();
	}

	void stopTransaction() throws SQLException {
		if (!this.recoverMark) {
			return;
		}
		this.recoverMark = false;
		this.holder.cancelTransaction();
	}
}
