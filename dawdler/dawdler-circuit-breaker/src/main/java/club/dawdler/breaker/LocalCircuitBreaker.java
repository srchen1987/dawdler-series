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
package club.dawdler.breaker;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import club.dawdler.breaker.metric.Metric;
import club.dawdler.breaker.state.CircuitBreakerState;
import club.dawdler.breaker.state.CircuitBreakerState.State;
import club.dawdler.breaker.state.LocalCircuitBreakerState;
import club.dawdler.util.JVMTimeProvider;

/**
 * @author jackson.song
 * @version V1.0
 * 本地熔断器实现
 */
public class LocalCircuitBreaker implements CircuitBreaker {
	private final int sleepWindowInMilliseconds;
	private final int requestVolumeThreshold;
	private final double errorThresholdPercentage;
	private final CircuitBreakerState circuitBreakerState;

	public LocalCircuitBreaker(club.dawdler.core.annotation.CircuitBreaker cb) {
		this.circuitBreakerState = new LocalCircuitBreakerState(cb.intervalInMs(), cb.windowsCount());
		this.sleepWindowInMilliseconds = cb.sleepWindowInMilliseconds();
		this.requestVolumeThreshold = cb.requestVolumeThreshold();
		this.errorThresholdPercentage = cb.errorThresholdPercentage();
	}

	@Override
	public boolean check() {
		if (circuitBreakerState != null) {
			AtomicReference<State> state = circuitBreakerState.getState();
			switch (state.get()) {
			case CLOSE:
				return true;
			case OPEN:
				if (circuitBreakerState.getStartTime() + sleepWindowInMilliseconds < JVMTimeProvider
						.currentTimeMillis()) {
					return state.compareAndSet(State.OPEN, State.HALF_OPEN);
				}
				return false;
			default:
				return false;
			}

		}
		return true;
	}

	@Override
	public void fail() {
		AtomicReference<State> state = circuitBreakerState.getState();
		if (state.get() == State.CLOSE) {
			List<Metric> list = circuitBreakerState.getStw().listCurrentMetrics();
			int totalCount = 0;
			int failCount = 0;
			for (Metric metric : list) {
				totalCount += metric.totalCount();
				failCount += metric.failCount();
			}
			if (totalCount < requestVolumeThreshold || totalCount == 0) {
				return;
			}
			if ((double) failCount / (double) totalCount >= errorThresholdPercentage
					&& state.compareAndSet(State.CLOSE, State.OPEN)) {
				circuitBreakerState.resetStartTime();
			}
		} else if (state.get() == State.HALF_OPEN && state.compareAndSet(State.HALF_OPEN, State.OPEN)) {
			circuitBreakerState.resetStartTime();
		}

	}

	@Override
	public void pass() {
		AtomicReference<State> state = circuitBreakerState.getState();
		if (state.get() == State.HALF_OPEN) {
			state.compareAndSet(State.HALF_OPEN, State.CLOSE);
			circuitBreakerState.resetStartTime();
		}
	}

	@Override
	public CircuitBreakerState getState() {
		return circuitBreakerState;
	}
}
