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

import com.anywide.dawdler.serverplug.annotation.DBTransaction;
import com.anywide.dawdler.serverplug.datasource.RWSplittingDataSourceManager.MappingDecision;

/**
 * 
 * @Title: SynReadConnectionObject.java
 * @Description: 存放读连接与事务配置的类
 * @author: jackson.song
 * @date: 2015年09月29日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class SynReadConnectionObject {
	private MappingDecision mappingDecision;
	private DBTransaction dBTransaction;
	private ReadConnectionHolder readConnectionHolder;

	public ReadConnectionHolder getReadConnectionHolder() {
		return readConnectionHolder;
	}

	public void setReadConnectionHolder(ReadConnectionHolder readConnectionHolder) {
		this.readConnectionHolder = readConnectionHolder;
	}

	private int referenceCount;

	public void requested() {
		referenceCount++;
	}

	public void released() {
		referenceCount--;
		if (referenceCount == 0) {
			LocalConnectionFacotry.clearSynReadConnectionObject();
			readConnectionHolder = null;
		}
	}

	public SynReadConnectionObject(MappingDecision mappingDecision, DBTransaction dBTransaction) {
		this.mappingDecision = mappingDecision;
		this.dBTransaction = dBTransaction;
	}

	public MappingDecision getMappingDecision() {
		return mappingDecision;
	}

	public void setMappingDecision(MappingDecision mappingDecision) {
		this.mappingDecision = mappingDecision;
	}

	public DBTransaction getdBTransaction() {
		return dBTransaction;
	}

	public void setdBTransaction(DBTransaction dBTransaction) {
		this.dBTransaction = dBTransaction;
	}
}
