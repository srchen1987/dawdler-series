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
package com.anywide.dawdler.core.bean;
import java.io.Serializable;
/**
 * 
 * @Title:  RequestBean.java
 * @Description:    远程请求信息类   
 * @author: jackson.song    
 * @date:   2007年11月15日  
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class RequestBean implements Serializable{
	private static final long serialVersionUID = 2432958149343667660L;
	private long seq;
	private String serviceName;
	private String methodName;
	private Class<?>[] types;
	private Object[] args;
	private boolean single;
	private String path;
	private boolean fuzzy;
	public boolean isFuzzy() {
		return fuzzy;
	}
	public void setFuzzy(boolean fuzzy) {
		this.fuzzy = fuzzy;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public boolean isSingle() {
		return single;
	}
	public void setSingle(boolean single) {
		this.single = single;
	}
	public long getSeq() {
		return seq;
	}
	public void setSeq(long seq) {
		this.seq = seq;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public Class<?>[] getTypes() {
		return types;
	}
	public void setTypes(Class<?>[] types) {
		this.types = types;
	}
	public Object[] getArgs() {
		return args;
	}
	public void setArgs(Object... args) {
		this.args = args;
	}
	@Override
	public String toString() {
		return serviceName+"\t"+methodName+"\t"+seq;
	}
}
