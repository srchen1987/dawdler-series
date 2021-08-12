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
package com.anywide.dawdler.distributed.transaction.repository;

import java.util.List;
import java.util.Map;

import com.anywide.dawdler.core.serializer.Serializer;
import com.anywide.dawdler.distributed.transaction.context.DistributedTransactionContext;

/**
 *
 * @Title TransactionRepository.java
 * @Description 存储方式抽象类
 * @author jackson.song
 * @date 2021年4月10日
 * @version V1.0
 * @email suxuan696@gmail.com
 */
public abstract class TransactionRepository {

	protected Serializer serializer;

	public abstract int create(DistributedTransactionContext transaction) throws Exception;

	public abstract int update(DistributedTransactionContext transaction) throws Exception;

	public abstract int updateDataByGlobalTxId(String globalTxId, Map<String, Object> data) throws Exception;

	public abstract int deleteByBranchTxId(String globalTxId, String branchTxId) throws Exception;

	public abstract int deleteByGlobalTxId(String globalTxId) throws Exception;

	public abstract List<DistributedTransactionContext> findAllByGlobalTxId(String globalTxId) throws Exception;

	public abstract List<DistributedTransactionContext> findALLBySecondsLater(int seconds) throws Exception;

}
