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
package club.dawdler.core.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author jackson.song
 * @version V1.0
 * 返回结果包装类
 */
public class BaseResult<T> {

	/**
	 * 数据
	 */
	@JsonInclude(Include.NON_NULL)
	protected T data;

	/**
	 * 是否成功
	 */
	@JsonInclude(Include.NON_NULL)
	protected Boolean success;

	/**
	 * 提示信息
	 */
	@JsonInclude(Include.NON_NULL)
	protected String message;

	/**
	 * 状态码
	 */
	@JsonInclude(Include.NON_NULL)
	protected Integer code;

	public BaseResult() {

	}

	public BaseResult(T data) {
		this.data = data;
		this.success = true;
	}

	public BaseResult(String message, boolean success) {
		this.message = message;
		this.success = success;
	}

	public BaseResult(boolean success) {
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

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

}
