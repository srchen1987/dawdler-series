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

import java.sql.SQLException;

/**
 * 
 * @Title: TransactionStatus.java
 * @Description: 事务状态定义的接口
 * @author: jackson.song
 * @date: 2015年09月28日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public interface TransactionStatus extends TransactionDefinition {
	public boolean isCompleted();

	public boolean isRollbackOnly();

	public boolean isReadOnly();

	public boolean isNewConnection();

	public boolean isSuspend();

	public boolean hasSavepoint();

	public void setRollbackOnly() throws SQLException;

	public void setReadOnly() throws SQLException;
}