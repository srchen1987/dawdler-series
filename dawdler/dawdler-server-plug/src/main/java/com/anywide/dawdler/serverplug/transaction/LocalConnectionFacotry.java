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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import com.anywide.dawdler.serverplug.db.DBAction;

/**
 * 
 * @Title: LocalConnectionFacotry.java
 * @Description: 线程内存储Connection的工厂
 * @author: jackson.song
 * @date: 2015年09月28日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class LocalConnectionFacotry {
	private static ThreadLocal<Map<DBAction, Connection>> localconnection = new ThreadLocal<Map<DBAction, Connection>>() {
		protected java.util.Map<DBAction, Connection> initialValue() {
			return new HashMap<DBAction, Connection>(2);
		};
	};
	private final static ThreadLocal<ConcurrentMap<DataSource, TransactionManager>> localManager;
	private final static ThreadLocal<ConcurrentMap<DataSource, WriteConnectionHolder>> localWriteConnectionHolder;
	private final static ThreadLocal<SynReadConnectionObject> synTransaction = new ThreadLocal<>();

	private LocalConnectionFacotry() {
	}

	public static Connection getReadConnection() throws SQLException {
		Connection con = localconnection.get().get(DBAction.READ);
		if (con != null)
			return con;
		SynReadConnectionObject sb = LocalConnectionFacotry.getSynReadConnectionObject();
		if (sb == null)
			return null;
		if (sb.getReadConnectionHolder().isUseWriteConnection()) {
			con = getWriteConnection();
			setReadConnection(con);
			return con;
		}
		con = sb.getReadConnectionHolder().getConnection();
		setReadConnection(con);
		return con;
//		return localconnection.get().get(DBAction.READ);
	}

	public static Connection getWriteConnection() {
//		if(localconnection.get()==null)return null;
		return localconnection.get().get(DBAction.WRITE);
	}

	public static void setReadConnection(Connection con) {
		localconnection.get().put(DBAction.READ, con);
	}

	public static void setWriteConnection(Connection con) {
		localconnection.get().put(DBAction.WRITE, con);
	}

	public static void clear() {
		localconnection.remove();
	}
//	public static void closeAndclear(){
//		if(localconnection.get()!=null){
//			Collection<Connection> c = localconnection.get().values();
//			for(Iterator<Connection> it = c.iterator();it.hasNext();){
//				try {
//					it.next().close();
//				} catch (SQLException e) {
//				}
//			}
//		}
//		localconnection.remove();
//	}

	private static Context ctx = null;

	static {
		localManager = new ThreadLocal<ConcurrentMap<DataSource, TransactionManager>>() {
			protected ConcurrentMap<DataSource, TransactionManager> initialValue() {
				return new ConcurrentHashMap<DataSource, TransactionManager>();
			}
		};
		localWriteConnectionHolder = new ThreadLocal<ConcurrentMap<DataSource, WriteConnectionHolder>>() {
			protected ConcurrentMap<DataSource, WriteConnectionHolder> initialValue() {
				return new ConcurrentHashMap<DataSource, WriteConnectionHolder>();
			}
		};
		try {
			ctx = new InitialContext();
		} catch (NamingException e) {
		}
	}

	static WriteConnectionHolder currentConnectionHolder(DataSource dataSource) {
		ConcurrentMap<DataSource, WriteConnectionHolder> localMap = localWriteConnectionHolder.get();
		WriteConnectionHolder holder = localMap.get(dataSource);
		if (holder == null) {
			holder = createConnectionHolder(dataSource);
			WriteConnectionHolder preHolder = localMap.putIfAbsent(dataSource, holder);
			if (preHolder != null)
				holder = preHolder;
		}
		return holder;
	}

	static void changeCurrentConnectionHolder(DataSource dataSource, WriteConnectionHolder holder) {
		ConcurrentMap<DataSource, WriteConnectionHolder> localMap = localWriteConnectionHolder.get();
		localMap.put(dataSource, holder);
	}

	static void removeCurrentConnectionHolder(DataSource dataSource) {
		ConcurrentMap<DataSource, WriteConnectionHolder> localMap = localWriteConnectionHolder.get();
		localMap.remove(dataSource);
	}

	private static TransactionManager getTransactionManager(final DataSource dataSource) {
		ConcurrentMap<DataSource, TransactionManager> localMap = localManager.get();
		TransactionManager manager = localMap.get(dataSource);
		if (manager == null) {
			manager = new JdbcTransactionManager(dataSource);
			TransactionManager preManager = localMap.putIfAbsent(dataSource, manager);
			if (preManager != null)
				manager = preManager;
		}
		return manager;
	}

	static TransactionManager getManager(DataSource dataSource) {
		return getTransactionManager(dataSource);
	}

	static WriteConnectionHolder createConnectionHolder(DataSource dataSource) {
		return new WriteConnectionHolder(dataSource);
	}

	public static DataSource getDataSourceInDawdler(String id) throws NamingException {

		return (DataSource) ctx.lookup("java:" + id);
	}

	public static void setSynReadConnectionObject(SynReadConnectionObject synTransactionBean) {
		synTransaction.set(synTransactionBean);
	}

	public static SynReadConnectionObject getSynReadConnectionObject() {
		return synTransaction.get();
	}

	public static void clearSynReadConnectionObject() {
		synTransaction.remove();
	}
}
