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
package com.anywide.dawdler.client;

/**
 * @author jackson.song
 * @version V1.0
 * Transaction提供者，传入groupName获取Transaction
 */
public class TransactionProvider {

	public static Transaction getTransaction(String groupName) {
		ConnectionPool cp = ConnectionPool.getConnectionPool(groupName);
		if (cp == null) {
			throw new IllegalArgumentException("not find " + groupName + " provider!");
		}
		return new Transaction(cp.getConnections());
	}

}
