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
package com.anywide.dawdler.core.db.annotation;

import com.anywide.dawdler.core.db.transaction.TransactionDefinition;

/**
 * @author jackson.song
 * @version V1.0
 * 隔离级别的定义
 */
public enum Isolation {
	DEFAULT(TransactionDefinition.TRANSACTION_DEFAULT),
	READ_UNCOMMITTED(TransactionDefinition.TRANSACTION_READ_UNCOMMITTED),
	READ_COMMITTED(TransactionDefinition.TRANSACTION_READ_COMMITTED),
	REPEATABLE_READ(TransactionDefinition.TRANSACTION_REPEATABLE_READ),
	SERIALIZABLE(TransactionDefinition.TRANSACTION_SERIALIZABLE);

	private final int value;

	Isolation(int value) {
		this.value = value;
	}

	public static Isolation valueOf(final int value) {
		switch (value) {
		case -1:
			return Isolation.DEFAULT;
		case TransactionDefinition.TRANSACTION_READ_UNCOMMITTED:
			return Isolation.READ_UNCOMMITTED;
		case TransactionDefinition.TRANSACTION_READ_COMMITTED:
			return Isolation.READ_COMMITTED;
		case TransactionDefinition.TRANSACTION_REPEATABLE_READ:
			return Isolation.REPEATABLE_READ;
		case TransactionDefinition.TRANSACTION_SERIALIZABLE:
			return Isolation.SERIALIZABLE;
		}
		throw new IllegalStateException(String.format("Connection ISOLATION error level %s.", value));
	}

	public int value() {
		return this.value;
	}
}
