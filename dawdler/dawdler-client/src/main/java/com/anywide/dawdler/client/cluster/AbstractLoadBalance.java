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
package com.anywide.dawdler.client.cluster;

import java.util.List;

import com.anywide.dawdler.core.bean.RequestBean;

/**
 * @author jackson.song
 * @version V1.0
 * @Title AbstractLoadBalance.java
 * @Description 负载均衡通用实现者（抽象）
 * @date 2019年8月16日
 * @email suxuan696@gmail.com
 */
public abstract class AbstractLoadBalance<T, K> implements LoadBalance<T, K> {
	protected String name;

	protected AbstractLoadBalance(String name) {
		this.name = name;
	}

	@Override
	public T select(RequestBean request, List<T> connections) {
		// 此处如果暴力下线 通过 服务端stopnow方式或者kill -9 方式 会有概率抛出数组下标越界异常
		T con = connections.size() > 1 ? doSelect(request, connections) : connections.get(0);
		return con;
	}

	public String getName() {
		return name;
	}

}
