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
package com.anywide.dawdler.serverplug.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.anywide.dawdler.serverplug.db.transaction.LocalConnectionFactory;
import com.anywide.dawdler.serverplug.db.transaction.SynReadConnectionObject;

/**
 * @author jackson.song
 * @version V1.0
 * @Title BaseDataImpl.java
 * @Description jdbc常用操作的具体实现
 * @date 2007年4月15日
 * @email suxuan696@gmail.com
 */
public class BaseDataImpl implements BaseData {
	private Connection con;

	public BaseDataImpl(Connection con) {
		this.con = con;
	}

	public BaseDataImpl() {
	}

	public <T extends Object> List<T> queryList(String sql, Class<T> c) throws SQLException {
		Statement st = null;
		ResultSet rs = null;
		try {
			st = getReadStatement();
			rs = st.executeQuery(sql);
			return DataAutomaticNewV2.buildObject(c, rs);
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (Exception e) {
			}

			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
		}
	}

	public <T> List<T> queryListPrepare(String sql, Class<T> c, Object... values) throws SQLException {
		PreparedStatement ps = null;
		/*
		 * int sum = matchChar(sql, '?'); if (sum > 0 && values == null) throw new
		 * NullPointerException("not set values!"); if (values != null && sum !=
		 * values.length) throw new NullPointerException(
		 * "values amount has error! Error caused by ? !=values ");
		 */
		int sum = values != null ? values.length : 0;
		ResultSet rs = null;
		try {
			ps = getReadPrepareStatement(sql);
			for (int i = 0; i < sum; i++) {
				ps.setObject(i + 1, values[i]);
			}
			rs = ps.executeQuery();
			return DataAutomaticNewV2.buildObject(c, rs);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
			}

			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
		}

	}

	public int update(String sql) throws SQLException {
		Statement st = null;
		try {
			st = getWriteStatement();
			return st.executeUpdate(sql);
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (Exception e) {
			}
		}
	}

	public int updatePrepare(String sql, Object... values) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = getWritePrepareStatement(sql);
			// int sum = matchChar(sql, '?');
			int sum = values != null ? values.length : 0;
			for (int i = 0; i < sum; i++) {
				ps.setObject(i + 1, values[i]);
			}
			int i = ps.executeUpdate();
			return i;
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
			}

		}

	}

	public int insert(String sql) throws SQLException {
		return update(sql);
	}

	public int insertPrepare(String sql, Object... values) throws SQLException {
		return updatePrepare(sql, values);
	}

	public int delete(String sql) throws SQLException {
		return insert(sql);
	}

	public int deletePrepare(String sql, Object... values) throws SQLException {
		return insertPrepare(sql, values);
	}

	public int queryCount(String sql) throws SQLException {
		Statement st = null;
		ResultSet rs = null;
		try {
			st = getReadStatement();
			rs = st.executeQuery(sql);
			int count = 0;
			if (rs.next())
				count = rs.getInt(1);
			return count;
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (Exception e) {
			}
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
		}

	}

	public int queryCountPrepare(String sql, Object... values) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getReadPrepareStatement(sql);
			// int sum = matchChar(sql, '?');
			int sum = values != null ? values.length : 0;
			/*
			 * if (sum > 0 && values == null) throw new
			 * NullPointerException("not set values!"); if (values != null && sum !=
			 * values.length) throw new NullPointerException(
			 * "values amount has error! Error caused by ? !=values ");
			 */
			for (int i = 0; i < sum; i++) {
				ps.setObject(i + 1, values[i]);
			}
			rs = ps.executeQuery();
			int count = 0;
			if (rs.next())
				count = rs.getInt(1);
			return count;
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
			}
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
		}

	}

	public long insertPrepareGetKey(String sql, Object... values) throws SQLException {
		PreparedStatement ps = getWritePrepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		int sum = values != null ? values.length : 0;
		for (int i = 0; i < sum; i++) {
			ps.setObject(i + 1, values[i]);
		}
		int i = ps.executeUpdate();
		if (i == 0) {
			ps.close();
			return 0;
		}
		ResultSet rs = null;
		long id = 0;
		try {
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getLong(1);
			}
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
			}
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}

		}
		return id;
	}

	public List<Map<String, Object>> queryListMaps(String sql) throws SQLException {
		Statement st = getReadStatement();
		ResultSet rs = st.executeQuery(sql);
		try {
			return DataAutomaticNewV2.buildMaps(rs);
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (Exception e) {
			}
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
		}
	}

	public List<Map<String, Object>> queryListMapsPrepare(String sql, Object... values) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		// int sum = matchChar(sql, '?');
		/*
		 * if (sum > 0 && values == null) throw new
		 * NullPointerException("not set values!"); if (values != null && sum !=
		 * values.length) throw new NullPointerException(
		 * "values amount has error! Error caused by ? !=values ");
		 */
		int sum = values != null ? values.length : 0;
		try {
			ps = getReadPrepareStatement(sql);
			for (int i = 0; i < sum; i++) {
				ps.setObject(i + 1, values[i]);
			}
			rs = ps.executeQuery();
			return DataAutomaticNewV2.buildMaps(rs);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
			}
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
		}

	}

	private Statement getWriteStatement() throws SQLException {
		return getStatement(true);
	}

	private Statement getReadStatement() throws SQLException {
		return getStatement(false);
	}

	private Statement getStatement(boolean isWrite) throws SQLException {
		Statement statement = con != null ? con.createStatement()
				: (isWrite ? LocalConnectionFactory.getWriteConnection().createStatement()
						: LocalConnectionFactory.getReadConnection().createStatement());
		applyTransactionTimeout(statement);
		return statement;
	}

	private PreparedStatement getWritePrepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		PreparedStatement preparedStatement = con != null ? con.prepareStatement(sql, autoGeneratedKeys)
				: LocalConnectionFactory.getWriteConnection().prepareStatement(sql, autoGeneratedKeys);
		applyTransactionTimeout(preparedStatement);
		return preparedStatement;
	}

	private PreparedStatement getWritePrepareStatement(String sql) throws SQLException {
		PreparedStatement preparedStatement = getPrepareStatement(true, sql);
		applyTransactionTimeout(preparedStatement);
		return preparedStatement;
	}

	private PreparedStatement getReadPrepareStatement(String sql) throws SQLException {
		PreparedStatement preparedStatement = getPrepareStatement(false, sql);
		applyTransactionTimeout(preparedStatement);
		return preparedStatement;
	}

	private PreparedStatement getPrepareStatement(boolean isWrite, String sql) throws SQLException {
		PreparedStatement preparedStatement = con != null ? con.prepareStatement(sql)
				: (isWrite ? LocalConnectionFactory.getWriteConnection().prepareStatement(sql)
						: LocalConnectionFactory.getReadConnection().prepareStatement(sql));
		applyTransactionTimeout(preparedStatement);
		return preparedStatement;
	}

	@Override
	public int insertMap(String tableName, Map<String, Object> data) throws SQLException {
		if (!data.isEmpty()) {
			Object[] values = new Object[data.size()];
			String sql = builderInsertSql(tableName, data, values);
			return insertPrepare(sql, values);
		}
		return 0;
	}

	@Override
	public long insertMapGetKey(String tableName, Map<String, Object> data) throws SQLException {
		if (!data.isEmpty()) {
			Object[] values = new Object[data.size()];
			String sql = builderInsertSql(tableName, data, values);
			return insertPrepareGetKey(sql, values);
		}
		return 0;
	}

	private String builderInsertSql(String tableName, Map<String, Object> data, Object[] values) {
		int length = values.length;
		AtomicInteger index = new AtomicInteger(0);
		StringBuilder sb = new StringBuilder(32);
		StringBuilder vsql = new StringBuilder(32);
		vsql.append(" values(");
		sb.append("insert into ");
		sb.append(tableName);
		sb.append("(");
		data.forEach((k, v) -> {
			sb.append(k);
			int tempIndex = index.getAndIncrement();
			values[tempIndex] = v;
			if (tempIndex == length - 1) {
				sb.append(")");
				vsql.append("?);");
			} else {
				sb.append(",");
				vsql.append("?,");
			}
		});
		sb.append(vsql);
		return sb.toString();
	}

	@Override
	public <T> T queryObject(String sql, Class<T> c) throws SQLException {
		List<T> list = queryList(sql, c);
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public <T> T queryObjectPrepare(String sql, Class<T> c, Object... values) throws SQLException {
		List<T> list = queryListPrepare(sql, c, values);
		return list.isEmpty() ? null : list.get(0);
	}

	public static void applyTransactionTimeout(Statement statement) throws SQLException {
		SynReadConnectionObject synReadObj = LocalConnectionFactory.getSynReadConnectionObject();
		if (synReadObj == null)
			return;
		Integer transactionTimeout = synReadObj.getDBTransaction().timeOut();
		Integer queryTimeout = statement.getQueryTimeout();
		if (queryTimeout == null || queryTimeout == 0 || transactionTimeout < queryTimeout) {
			statement.setQueryTimeout(transactionTimeout);
		}
	}
}
