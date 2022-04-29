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

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ReadConnectionHolder.java
 * @Description 读连接持有者
 * @date 2015年9月28日
 * @email suxuan696@gmail.com
 */
public class ReadConnectionHolder {
	private int referenceCount;
	private final DataSource dataSource;
	private Connection connection;
	private boolean useWriteConnection;

	ReadConnectionHolder(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public boolean isUseWriteConnection() {
		return useWriteConnection;
	}

	void setUseWriteConnection(boolean useWriteConnection) {
		this.useWriteConnection = useWriteConnection;
	}

	void requested() {
		this.referenceCount++;
	}

	void released() throws SQLException {
		this.referenceCount--;
		if (!this.isOpen()) {
			if (this.connection != null) {
				try {
					this.connection.close();
				} catch (SQLException e) {
					throw e;
				} finally {
					this.connection = null;
				}
			}
		}
	}

	public Connection getConnection() throws SQLException {
		if (!this.isOpen()) {
			return null;
		}
		if (this.connection == null) {
			this.connection = this.dataSource.getConnection();
			this.connection.setReadOnly(true);
		}
		return this.connection;
	}

	public boolean isOpen() {
		return this.referenceCount != 0;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

}
