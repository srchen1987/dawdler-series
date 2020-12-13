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
package com.anywide.dawdler.clientplug.web.handler;

import java.lang.reflect.Method;
import com.anywide.dawdler.clientplug.annotation.RequestMapping;
import com.anywide.dawdler.clientplug.web.TransactionController;

/**
 * 
 * @Title: RequestUrlData.java
 * @Description: 一个包装的实体类
 * @author: jackson.song
 * @date: 2007年04月17日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class RequestUrlData {
	private RequestMapping requestMapping;
	private TransactionController target;
	private Method method;

	public RequestMapping getRequestMapping() {
		return requestMapping;
	}

	public void setRequestMapping(RequestMapping requestMapping) {
		this.requestMapping = requestMapping;
	}

	public TransactionController getTarget() {
		return target;
	}

	public void setTarget(TransactionController target) {
		this.target = target;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
}
