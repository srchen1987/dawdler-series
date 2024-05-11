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
package com.anywide.dawdler.client.filter;

import com.anywide.dawdler.core.thread.InvokeFuture;

/**
 * @author jackson.song
 * @version V1.0
 * @Title AsyncInvokeFutureHolder.java
 * @Description 异步调用结果持有者
 * @date 2020年4月22日
 * @email suxuan696@gmail.com
 */
public class AsyncInvokeFutureHolder {

	private InvokeFuture<?> invokeFuture;
	private static final ThreadLocal<AsyncInvokeFutureHolder> THREAD_LOCAL = new ThreadLocal<AsyncInvokeFutureHolder>() {
		@Override
		protected AsyncInvokeFutureHolder initialValue() {
			return new AsyncInvokeFutureHolder();
		}
	};

	public static AsyncInvokeFutureHolder getContext() {
		return THREAD_LOCAL.get();
	}

	private static void removeContext() {
		THREAD_LOCAL.remove();
	}

	<T> void setInvokeFuture(InvokeFuture<T> invokeFuture) {
		this.invokeFuture = invokeFuture;
	}

	@SuppressWarnings("unchecked")
	public <T> InvokeFuture<T> getInvokeFuture() {
		removeContext();
		return (InvokeFuture<T>) invokeFuture;
	}

}
