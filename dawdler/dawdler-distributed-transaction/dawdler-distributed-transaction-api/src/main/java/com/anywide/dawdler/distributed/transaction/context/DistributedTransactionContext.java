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
package com.anywide.dawdler.distributed.transaction.context;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @Title DistributedTransactionContext.java
 * @Description 事务上下文
 * @author jackson.song
 * @date 2021年4月10日
 * @version V1.0
 * @email suxuan696@gmail.com
 */
public class DistributedTransactionContext implements Serializable, Cloneable {
	public static final String DISTRIBUTED_TRANSACTION_CONTEXT_KEY = "dtc_key";
	private static final long serialVersionUID = 3067909020545794630L;
	private String globalTxId;
	private String branchTxId;
	private boolean cancel = false;// 整个事务取消
	private String status;// 状态 trying cancel confirm
	private int addtime;// 添加时间
	private String action;// 模块功能的简称
	private int retryTime = 0;// 重试次数
	private Map<String, Object> data;
	private final transient static ThreadLocal<DistributedTransactionContext> THREAD_LOCAL = new ThreadLocal<>();

	/**
	 * 是否被干扰 如果被干扰 其他状态不生效 存为commting状态 在商城实际应用场景中是因为开发人员没有将订单业务绑定到分布式事务中 所以应用此字段作标记
	 * 一般用不上这个配置
	 */
	private boolean intervene;

	public boolean isIntervene() {
		return intervene;
	}

	public void setIntervene(boolean intervene) {
		this.intervene = intervene;
	}

	public void setRetryTime(int retryTime) {
		this.retryTime = retryTime;
	}

	public int getRetryTime() {
		return retryTime;
	}

	public void retryTimeIncre() {
		retryTime++;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public int getAddtime() {
		return addtime;
	}

	public void setAddtime(int addtime) {
		this.addtime = addtime;
	}

	public Map<String, Object> getDatas() {
		return data;
	}

	public void setDatas(Map<String, Object> data) {
		this.data = data;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public DistributedTransactionContext() {
	}

	public void init() {
		branchTxId = UUID.randomUUID().toString();
		addtime = (int) (System.currentTimeMillis() / 1000);
	}

	public DistributedTransactionContext(String globalTxId) {
		this.globalTxId = globalTxId;
	}

	public String getGlobalTxId() {
		return globalTxId;
	}

	public void setGlobalTxId(String globalTxId) {
		this.globalTxId = globalTxId;
	}

	public String getBranchTxId() {
		return branchTxId;
	}

	public void setBranchTxId(String branchTxId) {
		this.branchTxId = branchTxId;
	}

	public static DistributedTransactionContext getDistributedTransactionContext() {
		return THREAD_LOCAL.get();
	}

	public static void setDistributedTransactionContext(DistributedTransactionContext distributedTransactionContext) {
		THREAD_LOCAL.set(distributedTransactionContext);
	}

	public static void removeDistributedTransactionContext() {
		THREAD_LOCAL.remove();
	}

	public boolean isCancel() {
		return cancel;
	}

	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}

}
