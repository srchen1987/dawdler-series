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
package club.dawdler.core.db.mybatis;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ibatis.transaction.Transaction;

import club.dawdler.core.db.transaction.LocalConnectionFactory;
import club.dawdler.core.db.transaction.SynReadConnectionObject;
import club.dawdler.util.TLS;

/**
 * @author jackson.song
 * @version V1.0
 * dawdler实现Mybatis事务支持
 */
public class DawdlerMybatisTransaction implements Transaction {
	public static final String CURRENT_CONNECTION = "dmt_CURRENT_CONNECTION";

	@Override
	public Connection getConnection() throws SQLException {
		return (Connection) TLS.get(CURRENT_CONNECTION);
	}

	@Override
	public void commit() throws SQLException {

	}

	@Override
	public void rollback() throws SQLException {

	}

	@Override
	public void close() throws SQLException {

	}

	@Override
	public Integer getTimeout() throws SQLException {
		SynReadConnectionObject synReadObj = LocalConnectionFactory.getSynReadConnectionObject();
		if (synReadObj != null) {
			return synReadObj.getDBTransaction().timeOut();
		}
		return null;
	}

}
