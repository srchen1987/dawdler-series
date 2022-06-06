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
package com.anywide.dawdler.serverplug.db.mybatis;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerTransactionFactory.java
 * @Description dawdler实现session工厂
 * @date 2021年5月8日
 * @email suxuan696@gmail.com
 */
public class DawdlerTransactionFactory implements TransactionFactory {
	DawdlerMybatisTransaction dawdlerTransaction = new DawdlerMybatisTransaction();

	@Override
	public Transaction newTransaction(Connection conn) {
		return dawdlerTransaction;
	}

	@Override
	public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
		return dawdlerTransaction;
	}

}
