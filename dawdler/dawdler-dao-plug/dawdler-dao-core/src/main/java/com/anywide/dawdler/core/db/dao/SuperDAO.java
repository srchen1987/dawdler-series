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
package com.anywide.dawdler.core.db.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.anywide.dawdler.core.db.transaction.LocalConnectionFactory;
import com.anywide.dawdler.serverplug.load.bean.Page;

/**
 * @author jackson.song
 * @version V1.0
 * dao基类
 */
public class SuperDAO implements BaseData {
	protected Connection con;
	private final BaseData basedata;

	public SuperDAO(Connection con) {
		this.con = con;
		basedata = new BaseDataImpl(this.con);
	}

	public SuperDAO() {
		basedata = new BaseDataImpl();
	}

	public Connection getReadConnection() throws SQLException {
		if (con != null) {
			return con;
		}
		return LocalConnectionFactory.getReadConnection();
	}

	public Connection getWriteConnection() {
		if (con != null) {
			return con;
		}
		return LocalConnectionFactory.getWriteConnection();
	}

	public <T> List<T> queryList(String sql, Class<T> c) throws SQLException {
		return basedata.queryList(sql, c);
	}

	public <T> List<T> queryListPrepare(String sql, Class<T> c, Object... values) throws SQLException {
		return basedata.queryListPrepare(sql, c, values);
	}

	public int update(String sql) throws SQLException {
		return basedata.update(sql);
	}

	public int updatePrepare(String sql, Object... values) throws SQLException {
		return basedata.updatePrepare(sql, values);
	}

	public int insert(String sql) throws SQLException {
		return basedata.insert(sql);
	}

	public int insertPrepare(String sql, Object... values) throws SQLException {
		return basedata.insertPrepare(sql, values);
	}

	public int delete(String sql) throws SQLException {
		return basedata.delete(sql);
	}

	public int deletePrepare(String sql, Object... values) throws SQLException {
		return basedata.deletePrepare(sql, values);
	}

	public int queryCount(String sql) throws SQLException {
		return basedata.queryCount(sql);
	}

	public int queryCountPrepare(String sql, Object... values) throws SQLException {
		return basedata.queryCountPrepare(sql, values);
	}

	public <T> List<T> queryListPage(String countsql, String sql, int pageOn, int row, Class<T> c) throws SQLException {
		int rowCount = queryCount(countsql);
		if (pageOn <= 0) {
			pageOn = 1;
		}
		if (row <= 0) {
			row = 1;
		}
		int pageCount = rowCount % row == 0 ? rowCount / row : rowCount / row + 1;
		pageOn = pageOn < pageCount ? pageOn : pageCount;
		if (rowCount == 0) {
			return new ArrayList<T>();
		}
		return basedata.queryList(sql + " limit " + ((pageOn - 1) * row) + "," + row, c);
	}

	public <T> List<T> queryListPagePrepare(String countsql, String sql, int pageOn, int row, Class<T> c,
			Object... values) throws SQLException {
		int rowCount = queryCountPrepare(countsql, values);
		if (pageOn <= 0) {
			pageOn = 1;
		}
		if (row <= 0) {
			row = 1;
		}
		int pageCount = rowCount % row == 0 ? rowCount / row : rowCount / row + 1;
		pageOn = pageOn < pageCount ? pageOn : pageCount;
		if (pageOn == 0) {
			pageOn = 1;
		}
		if (rowCount == 0) {
			return new ArrayList<T>();
		}
		return basedata.queryListPrepare(sql + " limit ?,? ", c, setPage(((pageOn - 1) * row), row, values));
	}

	public <T> List<T> queryListPage(String countsql, String sql, int pageOn, int row, Page page, Class<T> c)
			throws SQLException {
		page.setRow(row);
		page.setPageOn(pageOn);
		page.setRowCountAndCompute(queryCount(countsql));
		if (page.getRowCount() == 0) {
			return new ArrayList<T>();
		}
		return basedata.queryList(sql + " limit " + ((page.getPageOn() - 1) * page.getRow()) + "," + page.getRow(), c);
	}

	public <T> List<T> queryListPagePrepare(String countsql, String sql, int pageOn, int row, Page page, Class<T> c,
			Object... values) throws SQLException {
		page.setRow(row);
		page.setPageOn(pageOn);
		page.setRowCountAndCompute(queryCountPrepare(countsql, values));
		if (page.getRowCount() == 0) {
			return new ArrayList<T>();
		}
		return basedata.queryListPrepare(sql + " limit ?,? ", c,
				setPage(((page.getPageOn() - 1) * page.getRow()), page.getRow(), values));
	}

	private Object[] setPage(int pageOn, int row, Object... values) {
		if (values == null) {
			Object[] tem = { pageOn, row };
			return tem;
		}
		Object[] tem = new Object[values.length + 2];
		for (int i = 0; i < values.length; i++) {
			tem[i] = values[i];
		}
		tem[values.length] = pageOn;
		tem[values.length + 1] = row;
		return tem;
	}

	public long insertPrepareGetKey(String sql, Object... values) throws SQLException {
		return basedata.insertPrepareGetKey(sql, values);
	}

	public List<Map<String, Object>> queryListMaps(String sql) throws SQLException {
		return basedata.queryListMaps(sql);
	}

	public List<Map<String, Object>> queryListMapsPrepare(String sql, Object... values) throws SQLException {
		return basedata.queryListMapsPrepare(sql, values);
	}

	public List<Map<String, Object>> queryListMapsPage(String countsql, String sql, int pageOn, int row, Page page,
			Object... values) throws SQLException {
		page.setRow(row);
		page.setPageOn(pageOn);
		page.setRowCountAndCompute(queryCountPrepare(countsql, values));
		return basedata.queryListMaps(sql + " limit " + ((page.getPageOn() - 1) * row) + "," + row);
	}

	public List<Map<String, Object>> queryListMapsPagePrepare(String countsql, String sql, int pageOn, int row,
			Page page, Object... values) throws SQLException {
		page.setRow(row);
		page.setPageOn(pageOn);
		page.setRowCountAndCompute(queryCountPrepare(countsql, values));
		return basedata.queryListMapsPrepare(sql + " limit ?,? ",
				setPage(((page.getPageOn() - 1) * page.getRow()), page.getRow(), values));
	}

	public Page newPage() {
		return new Page();
	}

	@Override
	public int insertMap(String tableName, Map<String, Object> data) throws SQLException {
		return basedata.insertMap(tableName, data);
	}

	@Override
	public long insertMapGetKey(String tableName, Map<String, Object> data) throws SQLException {
		return basedata.insertMapGetKey(tableName, data);
	}

	@Override
	public <T> T queryObject(String sql, Class<T> c) throws SQLException {
		return basedata.queryObject(sql, c);
	}

	@Override
	public <T> T queryObjectPrepare(String sql, Class<T> c, Object... values) throws SQLException {
		return basedata.queryObjectPrepare(sql, c, values);
	}

	public Map<String, Object> queryMapPrepare(String sql, Object... values) throws SQLException {
		List<Map<String, Object>> list = queryListMapsPrepare(sql, values);
		return list.isEmpty() ? null : list.get(0);
	}
}