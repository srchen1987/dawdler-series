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

import com.anywide.dawdler.serverplug.db.annotation.DBTransaction;

/**
 * @author jackson.song
 * @version V1.0
 * @Title JdbcReadConnectionStatus.java
 * @Description 数据库读连接的状态存储类
 * @date 2015年9月28日
 * @email suxuan696@gmail.com
 */
public class JdbcReadConnectionStatus {
	private final int timeOut;
	private ReadConnectionHolder currentConn;
	private ReadConnectionHolder oldConn;

	public JdbcReadConnectionStatus(DBTransaction dBTransaction) {
		timeOut = dBTransaction.timeOut();
	}

	public int getTimeOut() {
		return timeOut;
	}

	public ReadConnectionHolder getCurrentConn() {
		return currentConn;
	}

	public void setCurrentConn(ReadConnectionHolder currentConn) {
		this.currentConn = currentConn;
	}

	public ReadConnectionHolder getOldConn() {
		return oldConn;
	}

	public void setOldConn(ReadConnectionHolder oldConn) {
		this.oldConn = oldConn;
	}

}