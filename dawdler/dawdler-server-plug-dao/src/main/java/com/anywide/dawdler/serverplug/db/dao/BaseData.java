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
 * @author jackson.song
 * @version V1.0
 * @Title BaseData.java
 * @Description TODO
 * @date 2007年4月15日
 * @email suxuan696@gmail.com
 */
public interface BaseData {
	<T extends Object> List<T> queryList(String sql, Class<T> c) throws SQLException;

	<T extends Object> List<T> queryListPrepare(String sql, Class<T> c, Object... values) throws SQLException;

	<T extends Object> T queryObject(String sql, Class<T> c) throws SQLException;

	<T extends Object> T queryObjectPrepare(String sql, Class<T> c, Object... values) throws SQLException;

	List<Map<String, Object>> queryListMaps(String sql) throws SQLException;

	List<Map<String, Object>> queryListMapsPrepare(String sql, Object... values) throws SQLException;

	int update(String sql) throws SQLException;

	// int updateObject(String sql)throws SQLException ;
	int updatePrepare(String sql, Object... values) throws SQLException;

	int insert(String sql) throws SQLException;

	// int insertObject(String sql)throws SQLException;
	int insertPrepare(String sql, Object... values) throws SQLException;

	// int insertObjectGetKey(String sql)throws SQLException;
	int insertPrepareGetKey(String sql, Object... values) throws SQLException;

	int insertMap(String tableName, Map<String, Object> datas) throws SQLException;

	int insertMapGetKey(String tableName, Map<String, Object> datas) throws SQLException;

	int delete(String sql) throws SQLException;

	int deletePrepare(String sql, Object... values) throws SQLException;

	int queryCount(String sql) throws SQLException;

	int queryCountPrepare(String sql, Object... values) throws SQLException;
}
