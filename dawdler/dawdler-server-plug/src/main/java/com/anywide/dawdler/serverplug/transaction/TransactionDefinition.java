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

import com.anywide.dawdler.serverplug.annotation.Isolation;
import com.anywide.dawdler.serverplug.annotation.Propagation;
/**
 * 
 * @Title:  TransactionDefinition.java   
 * @Description:    传播性，隔离级别定义接口   
 * @author: jackson.song    
 * @date:   2015年09月28日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public interface TransactionDefinition {
	int PROPAGATION_REQUIRED = 0;
	int PROPAGATION_SUPPORTS = 1;
	int PROPAGATION_MANDATORY = 2;
	int PROPAGATION_REQUIRES_NEW = 3;
	int PROPAGATION_NOT_SUPPORTED = 4;
	int PROPAGATION_NEVER = 5;
	int PROPAGATION_NESTED = 6;

	int TRANSACTION_DEFAULT = -1;
	int TRANSACTION_READ_UNCOMMITTED = Connection.TRANSACTION_READ_UNCOMMITTED;
	int TRANSACTION_READ_COMMITTED = Connection.TRANSACTION_READ_COMMITTED;
	int TRANSACTION_REPEATABLE_READ = Connection.TRANSACTION_REPEATABLE_READ;
	int TRANSACTION_SERIALIZABLE = Connection.TRANSACTION_SERIALIZABLE;
	int TIMEOUT_DEFAULT = -1;
	
	public Propagation getPropagationBehavior();
	public Isolation getIsolationLevel();
	int getTimeout();
	boolean isReadOnly();

}
