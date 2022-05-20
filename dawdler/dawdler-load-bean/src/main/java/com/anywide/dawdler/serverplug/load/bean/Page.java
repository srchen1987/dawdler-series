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
package com.anywide.dawdler.serverplug.load.bean;

import java.io.Serializable;

/**
 * @author jackson.song
 * @version V1.0
 * @Title Page.java
 * @Description 可序列化的page类
 * @date 2007年4月5日
 * @email suxuan696@gmail.com
 */
public class Page implements Serializable {
	private static final long serialVersionUID = 7024911260241474159L;
	/** 当前页 **/
	private int pageOn;
	/** 总行数 **/
	private int rowCount;
	/** 总页数 **/
	private int pageCount;
	/** 每页显示行数 **/
	private int row;
	private int start;
	private int end;
	private int pageNumber = 11;

	public Page(int pageOn, int row, int rowCount) {
		this.pageOn = pageOn;
		this.row = row;
		this.rowCount = rowCount;
		compute();
	}

	public Page(int pageOn, int row) {
		this.pageOn = pageOn;
		this.row = row;
	}

	public Page() {
	}

	public int getPageOn() {
		return pageOn;
	}

	public void setPageOn(int pageOn) {
		this.pageOn = pageOn;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public void compute() {
		if (rowCount <= 0) {
			return;
		}
		if (row <= 0) {
			row = 10;
		}
		pageCount = rowCount % row == 0 ? rowCount / row : rowCount / row + 1;
		if (pageOn > pageCount) {
			pageOn = pageCount;
		}
		if (pageOn < 1) {
			pageOn = 1;
		}
		start = (pageOn - 1) * row;
		end = pageOn * row;
		if (end > rowCount) {
			end = rowCount;
		}
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void setRowCountAndCompute(int rowCount) {
		this.rowCount = rowCount;
		compute();
	}
}
