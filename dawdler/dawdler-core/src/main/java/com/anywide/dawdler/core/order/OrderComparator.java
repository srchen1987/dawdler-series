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
package com.anywide.dawdler.core.order;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author jackson.song
 * @version V1.0
 * @Title OrderComparator.java
 * @Description 排序
 * @date 2007年12月03日
 * @email suxuan696@gmail.com
 */
public class OrderComparator<T> implements Comparator<OrderData<T>> {
	public static final OrderComparator INSTANCE = new OrderComparator<>();

	public static <T> void sort(List<OrderData<T>> list) {
		if (list != null && list.size() > 1) {
			Collections.sort(list, INSTANCE);
		}
	}

	@Override
	public int compare(OrderData<T> o1, OrderData<T> o2) {
		return o1.getOrder() - o2.getOrder();
	}
}
