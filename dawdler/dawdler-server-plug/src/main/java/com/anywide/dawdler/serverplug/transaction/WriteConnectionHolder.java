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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import javax.sql.DataSource;
/**
 * 
 * @Title:  WriteConnectionHolder.java   
 * @Description:    写连接持有者   
 * @author: jackson.song    
 * @date:   2015年09月28日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class WriteConnectionHolder implements SavepointManager {
	private int referenceCount;
	private DataSource dataSource;
	private Connection connection;
	WriteConnectionHolder(final DataSource dataSource) {
		this.dataSource = dataSource;
	}
	public void requested() {
		this.referenceCount++;
	}
	public void released() throws SQLException {
		this.referenceCount--;
		if (!this.isOpen() && this.connection != null)
			try {
				this.savepointCounter = 0;
				this.savepointsSupported = null;
				this.connection.close();
				LocalConnectionFacotry.clear();
			} catch (SQLException e) {
				throw e;
			} finally {
				this.connection = null;
			}
	}
	public Connection getConnection() throws SQLException {
		if (!this.isOpen()) {
			return null;
		}
		if (this.connection == null) {
			this.connection = this.dataSource.getConnection();
		}
		LocalConnectionFacotry.setWriteConnection(connection);
		return this.connection;
	}

	public boolean isOpen() {
		if (this.referenceCount == 0)
			return false;
		return true;
	}

 	public DataSource getDataSource() {
		return dataSource;
	}

	public boolean hasTransaction() throws SQLException {
		Connection conn = this.getConnection();
		if (conn == null)
			return false;
		return !conn.getAutoCommit();
	}

	public void setTransaction() throws SQLException {
		Connection conn = getConnection();
		if (conn != null && conn.getAutoCommit() == true)
			conn.setAutoCommit(false);
	}

	public void cancelTransaction() throws SQLException {
		Connection conn = this.getConnection();
		if (conn != null && conn.getAutoCommit() == false)
			conn.setAutoCommit(true);
	}

	private void checkConn(final Connection conn) throws SQLException {
		if (conn == null)
			throw new SQLException("Connection is null.");
	}

	public static final String SAVEPOINT_NAME_PREFIX = "SAVEPOINT_";
	private int savepointCounter = 0;
	private Boolean savepointsSupported;

	public boolean supportsSavepoints() throws SQLException {
		Connection conn = this.getConnection();
		this.checkConn(conn);
		if (this.savepointsSupported == null)
			this.savepointsSupported = conn.getMetaData().supportsSavepoints();
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
