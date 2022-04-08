package com.anywide.dawdler.clientplug.web.result;

import com.anywide.dawdler.serverplug.load.bean.Page;

/**
 * @author jackson.song
 * @version V1.0
 * @Title PageResult.java
 * @Description 返回支持分页的vo包装类
 * @date 2021年3月5日
 * @email suxuan696@gmail.com
 */
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
