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
 * 分页工厂类
 */
public class PageFactory {
	private PageFactory() {
	}

	public static Page getPage() {
		return new Page();
	}

	public static Page getPage(int pageOn, int row, int rowCount) {
		return new Page(pageOn, row, rowCount);
	}

	public static Page getPage(int pageOn, int row) {
		return new Page(pageOn, row);
	}
}
