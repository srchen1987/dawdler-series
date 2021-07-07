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
package com.anywide.dawdler.breaker.state;

import java.util.concurrent.atomic.AtomicReference;

import com.anywide.dawdler.breaker.SlideTimeWindows;
import com.anywide.dawdler.util.JVMTimeProvider;

/**
 * @author jackson.song
 * @version V1.0
 * @Title LocalCircuitBreadkerState.java
 * @Description 熔断器状态（LOCAL）
 * @date 2018年3月16日
 * @email suxuan696@gmail.com
 */
public class LocalCircuitBreakerState implements CircuitBreakerState {
	private final SlideTimeWindows stw;
	private final AtomicReference<State> state = new AtomicReference<State>(State.CLOSE);
	private volatile long startTime;

	public LocalCircuitBreakerState(int intervalInMs, int windowsCount) {
		stw = new SlideTimeWindows(intervalInMs, windowsCount);
	}

	public SlideTimeWindows getStw() {
		return stw;
	}

	public AtomicReference<State> getState() {
		return state;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	@Override
	public void resetStartTime() {
		startTime = JVMTimeProvider.currentTimeMillis();
	}

}
