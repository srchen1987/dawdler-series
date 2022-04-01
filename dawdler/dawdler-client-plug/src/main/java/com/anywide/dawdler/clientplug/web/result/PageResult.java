package com.anywide.dawdler.clientplug.web.result;

import com.anywide.dawdler.serverplug.load.bean.Page;

public class PageResult<T> extends BaseResult<T> {
	private Page page;
	public PageResult(T data) {
		super(data);
	}
	
	public PageResult(T data, Page page, boolean success) {
		super(data, success);
		this.page = page;
	}
	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}


}
