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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import com.anywide.dawdler.serverplug.db.DBAction;

/**
 * @author jackson.song
 * @version V1.0
 * @Title LocalConnectionFacotry.java
 * @Description 线程内存储Connection的工厂
 * @date 2015年9月28日
 * @email suxuan696@gmail.com
 */
public class LocalConnectionFactory {
	private final static ConcurrentMap<DataSource, TransactionManager> LOCAL_MANAGER = new ConcurrentHashMap<>();
	private final static ThreadLocal<ConcurrentMap<DataSource, WriteConnectionHolder>> LOCAL_WRITE_CONNECTION_HOLDER;
	private final static ThreadLocal<SynReadConnectionObject> SYN_READ_CONNECTION = new ThreadLocal<>();
	private static final ThreadLocal<Map<DBAction, Connection>> LOCAL_CONNECTION = new ThreadLocal<Map<DBAction, Connection>>() {
		protected java.util.Map<DBAction, Connection> initialValue() {
			return new HashMap<DBAction, Connection>(2);
		}

	};

	static {
		LOCAL_WRITE_CONNECTION_HOLDER = new ThreadLocal<ConcurrentMap<DataSource, WriteConnectionHolder>>() {
			protected ConcurrentMap<DataSource, WriteConnectionHolder> initialValue() {
				return new ConcurrentHashMap<DataSource, WriteConnectionHolder>();
			}
		};
	}

	private LocalConnectionFactory() {
	}

	public static Connection getReadConnection() throws SQLException {
		Connection con = LOCAL_CONNECTION.get().get(DBAction.READ);
		if (con != null) {
			return con;
		}
		SynReadConnectionObject sb = LocalConnectionFactory.getSynReadConnectionObject();
		if (sb == null) {
			return null;
		}
		if (sb.getReadConnectionHolder().isUseWriteConnection()) {
			con = getWriteConnection();
			setReadConnection(con);
			return con;
		}
		con = sb.getReadConnectionHolder().getConnection();
		setReadConnection(con);
		return con;
	}

	public static void setReadConnection(Connection con) {
		LOCAL_CONNECTION.get().put(DBAction.READ, con);
	}

	public static void removeReadConnection() {
		LOCAL_CONNECTION.get().remove(DBAction.READ);
	}

	public static Connection getWriteConnection() {
		return LOCAL_CONNECTION.get().get(DBAction.WRITE);
	}

	public static void setWriteConnection(Connection con) {
		LOCAL_CONNECTION.get().put(DBAction.WRITE, con);
	}

	public static void removeWriteConnection() {
		LOCAL_CONNECTION.get().remove(DBAction.WRITE);
	}

	static WriteConnectionHolder currentConnectionHolder(DataSource dataSource) {
		ConcurrentMap<DataSource, WriteConnectionHolder> localMap = LOCAL_WRITE_CONNECTION_HOLDER.get();
		WriteConnectionHolder holder = localMap.get(dataSource);
		if (holder == null) {
			holder = createConnectionHolder(dataSource);
			WriteConnectionHolder preHolder = localMap.putIfAbsent(dataSource, holder);
			if (preHolder != null) {
				holder = preHolder;
			}
		}
		return holder;
	}

	static void changeCurrentConnectionHolder(DataSource dataSource, WriteConnectionHolder holder) {
		ConcurrentMap<DataSource, WriteConnectionHolder> localMap = LOCAL_WRITE_CONNECTION_HOLDER.get();
		localMap.put(dataSource, holder);
	}

	static void removeCurrentConnectionHolder(DataSource dataSource) {
		ConcurrentMap<DataSource, WriteConnectionHolder> localMap = LOCAL_WRITE_CONNECTION_HOLDER.get();
		localMap.remove(dataSource);
	}

	private static TransactionManager getTransactionManager(final DataSource dataSource) {
		TransactionManager manager = LOCAL_MANAGER.get(dataSource);
		if (manager == null) {
			manager = new JdbcTransactionManager(dataSource);
			TransactionManager preManager = LOCAL_MANAGER.putIfAbsent(dataSource, manager);
			if (preManager != null) {
				manager = preManager;
			}
		}
		return manager;
	}

	static TransactionManager getManager(DataSource dataSource) {
		return getTransactionManager(dataSource);
	}

	static WriteConnectionHolder createConnectionHolder(DataSource dataSource) {
		return new WriteConnectionHolder(dataSource);
	}

	public static SynReadConnectionObject getSynReadConnectionObject() {
		return SYN_READ_CONNECTION.get();
	}

	public static void setSynReadConnectionObject(SynReadConnectionObject synReadConnectionObject) {
		SYN_READ_CONNECTION.set(synReadConnectionObject);
	}

	public static void clearSynReadConnectionObject() {
		SYN_READ_CONNECTION.remove();
	}
}
