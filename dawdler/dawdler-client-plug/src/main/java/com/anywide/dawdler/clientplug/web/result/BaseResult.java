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
package com.anywide.dawdler.clientplug.web.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author jackson.song
 * @version V1.0
 * @Title BaseResult.java
 * @Description 返回vo的包装类
 * @date 2021年3月5日
 * @email suxuan696@gmail.com
 */
public class BaseResult<T> {

	@JsonInclude(Include.NON_NULL)
	private T data;

	@JsonInclude(Include.NON_NULL)
	private Boolean success;

	@JsonInclude(Include.NON_NULL)
	private String message;

	public BaseResult(T data) {
		this.data = data;
		this.success = true;
	}

	public BaseResult(String message, boolean success) {
		this.message = message;
		this.success = success;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
