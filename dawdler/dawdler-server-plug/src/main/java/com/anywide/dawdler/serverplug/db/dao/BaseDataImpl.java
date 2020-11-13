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

import com.anywide.dawdler.serverplug.transaction.LocalConnectionFacotry;
/**
 * 
 * @Title:  BaseDataImpl.java   
 * @Description:    TODO   
 * @author: jackson.song    
 * @date:   2007年04月15日       
 * @version V1.0 
 * @email: suxuan696@gmail.com
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
			st = getReadSatement();
			rs = st.executeQuery(sql);
			List<T> object = DataAutomaticNewV2.buildObject(c, rs);
			return object;
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
			ps = getReadPrepareSatement(sql);
			for (int i = 0; i < sum; i++) {
				ps.setObject(i + 1, values[i]);
			}
			rs = ps.executeQuery();
			List<T> list = DataAutomaticNewV2.buildObject(c, rs);
			return list;
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
			st = getWriteSatement();
			int i = st.executeUpdate(sql);
			return i;
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
			ps = getWritePrepareSatement(sql);
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
			st = getReadSatement();
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
			ps = getReadPrepareSatement(sql);
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

	public int insertPrepareGetKey(String sql, Object... values) throws SQLException {
		PreparedStatement ps = getWritePrepareSatement(sql, Statement.RETURN_GENERATED_KEYS);
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
		try {
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				i = rs.getInt(1);
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
		return i;
	}

	public List<Map<String, Object>> queryListMaps(String sql) throws SQLException {
		Statement st = getReadSatement();
		ResultSet rs = st.executeQuery(sql);
		try {
			List<Map<String, Object>> list = DataAutomaticNewV2.buildMaps(rs);
			return list;
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
			ps = getReadPrepareSatement(sql);
			for (int i = 0; i < sum; i++) {
				ps.setObject(i + 1, values[i]);
			}
			rs = ps.executeQuery();
			List<Map<String, Object>> list = DataAutomaticNewV2.buildMaps(rs);
			return list;
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

	private Statement getWriteSatement() throws SQLException {
		return getSatement(true);
	}

	private Statement getReadSatement() throws SQLException {
		return getSatement(false);
	}

	private Statement getSatement(boolean iswrite) throws SQLException {
		return con != null ? con.createStatement()
				: (iswrite ? LocalConnectionFacotry.getWriteConnection().createStatement()
						: LocalConnectionFacotry.getReadConnection().createStatement());
	}

	private PreparedStatement getWritePrepareSatement(String sql, int i) throws SQLException {
		return con != null ? con.prepareStatement(sql, i)
				: LocalConnectionFacotry.getWriteConnection().prepareStatement(sql, i);
	}

	private PreparedStatement getWritePrepareSatement(String sql) throws SQLException {
		return getPrepareSatement(true, sql);
	}

	private PreparedStatement getReadPrepareSatement(String sql) throws SQLException {
		return getPrepareSatement(false, sql);
	}

	private PreparedStatement getPrepareSatement(boolean iswrite, String sql) throws SQLException {
		return con != null ? con.prepareStatement(sql)
				: (iswrite ? LocalConnectionFacotry.getWriteConnection().prepareStatement(sql)
						: LocalConnectionFacotry.getReadConnection().prepareStatement(sql));
	}

	@Override
	public int insertMap(String tableName, Map<String, Object> datas) throws SQLException {
		if (!datas.isEmpty()) {
			Object[] values = new Object[datas.size()];
			String sql = builderInsertSql(tableName, datas, values);
			return insertPrepare(sql, values);
		}
		return 0;
	}

	@Override
	public int insertMapGetKey(String tableName, Map<String, Object> datas) throws SQLException {
		if (!datas.isEmpty()) {
			Object[] values = new Object[datas.size()];
			String sql = builderInsertSql(tableName, datas, values);
			return insertPrepareGetKey(sql, values);
		}
		return 0;
	}
	
	private String builderInsertSql(String tableName, Map<String, Object> datas,Object [] values) {
			int length = values.length;
			AtomicInteger index = new AtomicInteger(0);
			StringBuilder sb = new StringBuilder(32);
			StringBuilder vsql = new StringBuilder(32);
			vsql.append(" values(");
			sb.append("insert into ");
			sb.append(tableName);
			sb.append("(");
			datas.forEach((k, v) -> {
				sb.append(k);
				int tempIndex = index.getAndIncrement();
				values[tempIndex] = v;
				if(tempIndex==length-1) {
					sb.append(")");
					vsql.append("?);");
				}else {
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
		return list.isEmpty()?null:list.get(0);
	}

	@Override
	public <T> T queryObjectPrepare(String sql, Class<T> c, Object... values) throws SQLException {
		List<T> list = queryListPrepare(sql, c, values);
		return list.isEmpty()?null:list.get(0);
	}
}
