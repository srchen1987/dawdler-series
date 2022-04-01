package com.anywide.dawdler.clientplug.web.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class BaseResult<T> {
	
	private T data;

	@JsonInclude(Include.NON_NULL)
	private Boolean success;

	public BaseResult(T data) {
		this.data = data;
	}
	public BaseResult(T data, boolean success) {
		this.data = data;
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
}
