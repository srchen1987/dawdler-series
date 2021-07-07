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
 * @date 2007年04月05日
 * @email suxuan696@gmail.com
 */
public class Page implements Serializable {
	private static final long serialVersionUID = 7024911260241474159L;
	private int pageon;
	private int rowcount;
	private int pagecount;
	private int row;
	private int start;
	private int end;
	private int pageNumber = 11;

	public Page(int pageon, int row, int rowcount) {
		this.pageon = pageon;
		this.row = row;
		this.rowcount = rowcount;
		compute();
	}

	public Page(int pageon, int row) {
		this.pageon = pageon;
		this.row = row;
	}

	public Page() {
	}

	public int getPageon() {
		return pageon;
	}

	public void setPageon(int pageon) {
		this.pageon = pageon;
	}

	public int getRowcount() {
		return rowcount;
	}

	public void setRowcount(int rowcount) {
		this.rowcount = rowcount;
	}

	public int getPagecount() {
		return pagecount;
	}

	public void setPagecount(int pagecount) {
		this.pagecount = pagecount;
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
		if (rowcount <= 0)
			return;
		if (row <= 0)
			row = 10;
		pagecount = rowcount % row == 0 ? rowcount / row : rowcount / row + 1;
		if (pageon > pagecount)
			pageon = pagecount;
		if (pageon < 1)
			pageon = 1;
		start = (pageon - 1) * row;
		end = pageon * row;
		if (end > rowcount)
			end = rowcount;
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

	public void setRowcountAndCompute(int rowcount) {
		this.rowcount = rowcount;
		compute();
	}
}
