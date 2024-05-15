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
package com.anywide.dawdler.core.db.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import javax.sql.DataSource;

/**
 * @author jackson.song
 * @version V1.0
 * @Title WriteConnectionHolder.java
 * @Description 写连接持有者
 * @date 2015年9月28日
 * @email suxuan696@gmail.com
 */
public class WriteConnectionHolder implements SavepointManager {
	public static final String SAVEPOINT_NAME_PREFIX = "SAVEPOINT_";
	private int referenceCount;
	private final DataSource dataSource;
	private Connection connection;
	private int savepointCounter = 0;
	private Boolean savepointsSupported;

	WriteConnectionHolder(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	void requested() {
		this.referenceCount++;
	}

	void released() throws SQLException {
		this.referenceCount--;
		if (!this.isOpen() && this.connection != null) {
			try {
				LocalConnectionFactory.removeWriteConnection();
				this.savepointCounter = 0;
				this.savepointsSupported = null;
				this.connection.close();
			} catch (SQLException e) {
				throw e;
			} finally {
				this.connection = null;
			}
		}
	}

	public Connection getConnection() throws SQLException {
		if (!this.isOpen()) {
			return null;
		}
		if (this.connection == null) {
			this.connection = this.dataSource.getConnection();
		}
		return this.connection;
	}

	public boolean isOpen() {
		return this.referenceCount != 0;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public boolean hasTransaction() throws SQLException {
		Connection conn = this.getConnection();
		if (conn == null) {
			return false;
		}
		return !conn.getAutoCommit();
	}

	void setTransaction() throws SQLException {
		Connection conn = getConnection();
		if (conn != null && conn.getAutoCommit() == true) {
			conn.setAutoCommit(false);
		}
	}

	void cancelTransaction() throws SQLException {
		Connection conn = this.getConnection();
		if (conn != null && conn.getAutoCommit() == false) {
			conn.setAutoCommit(true);
		}
	}

	private void checkConn(final Connection conn) throws SQLException {
		if (conn == null) {
			throw new SQLException("Connection is null.");
		}
	}

	public boolean supportsSavepoints() throws SQLException {
		Connection conn = this.getConnection();
		this.checkConn(conn);
		if (this.savepointsSupported == null) {
			this.savepointsSupported = conn.getMetaData().supportsSavepoints();
		}
		return this.savepointsSupported;
	}

	public Savepoint createSavepoint() throws SQLException {
		Connection conn = this.getConnection();
		this.checkConn(conn);
		//
		this.savepointCounter++;
		return conn.setSavepoint(WriteConnectionHolder.SAVEPOINT_NAME_PREFIX + this.savepointCounter);
	}

	public void rollbackToSavepoint(final Savepoint savepoint) throws SQLException {
		Connection conn = this.getConnection();
		this.checkConn(conn);
		conn.rollback(savepoint);
	}

	public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
		Connection conn = this.getConnection();
		this.checkConn(conn);
		conn.releaseSavepoint(savepoint);
	}

	public boolean supportSavepoint() throws SQLException {
		Connection conn = this.getConnection();
		this.checkConn(conn);
		return conn.getMetaData().supportsSavepoints();
	}
}
