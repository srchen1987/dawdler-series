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

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 
 * @Title: BaseData.java
 * @Description: TODO
 * @author: jackson.song
 * @date: 2007年04月15日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public interface BaseData {
	public <T extends Object> List<T> queryList(String sql, Class<T> c) throws SQLException;

	public <T extends Object> List<T> queryListPrepare(String sql, Class<T> c, Object... values) throws SQLException;

	public <T extends Object> T queryObject(String sql, Class<T> c) throws SQLException;

	public <T extends Object> T queryObjectPrepare(String sql, Class<T> c, Object... values) throws SQLException;

	public List<Map<String, Object>> queryListMaps(String sql) throws SQLException;

	public List<Map<String, Object>> queryListMapsPrepare(String sql, Object... values) throws SQLException;

	public int update(String sql) throws SQLException;

//	public int updateObject(String sql)throws SQLException ;
	public int updatePrepare(String sql, Object... values) throws SQLException;

	public int insert(String sql) throws SQLException;

//	public int insertObject(String sql)throws SQLException;
	public int insertPrepare(String sql, Object... values) throws SQLException;

//	public int insertObjectGetKey(String sql)throws SQLException;
	public int insertPrepareGetKey(String sql, Object... values) throws SQLException;

	public int insertMap(String tableName, Map<String, Object> datas) throws SQLException;

	public int insertMapGetKey(String tableName, Map<String, Object> datas) throws SQLException;

	public int delete(String sql) throws SQLException;

	public int deletePrepare(String sql, Object... values) throws SQLException;

	public int queryCount(String sql) throws SQLException;

	public int queryCountPrepare(String sql, Object... values) throws SQLException;
}
