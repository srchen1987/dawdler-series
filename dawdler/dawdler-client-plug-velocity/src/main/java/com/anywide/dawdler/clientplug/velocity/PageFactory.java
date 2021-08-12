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
package com.anywide.dawdler.clientplug.velocity;

import com.anywide.dawdler.serverplug.load.bean.Page;

/**
 * @author jackson.song
 * @version V1.0
 * @Title PageFactory.java
 * @Description 分页工厂类 （注释后补的）
 * @date 2006年8月10日
 * @email suxuan696@gmail.com
 */
public class PageFactory {
	// private static PageFactory pageFactory = new PageFactory();
	private PageFactory() {
	}

	public static Page getPage() {
		return new Page();
	}

	public static Page getPage(int pageon, int row, int rowcount) {
		return new Page(pageon, row, rowcount);
	}

	public static Page getPage(int pageon, int row) {
		return new Page(pageon, row);
	}
}
