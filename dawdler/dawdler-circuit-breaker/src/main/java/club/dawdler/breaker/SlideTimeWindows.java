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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;

import club.dawdler.breaker.metric.Metric;
import club.dawdler.breaker.metric.MetricBase;
import club.dawdler.util.JVMTimeProvider;

/**
 * @author jackson.song
 * @version V1.0
 * 滑动窗口
 */
public class SlideTimeWindows {
	private final int intervalInMs;
	private final int windowsCount;
	private final int windowLengthInMs;

	private final AtomicReferenceArray<Metric> array;

	private final ReentrantLock lock = new ReentrantLock();

	public SlideTimeWindows(int intervalInMs, int windowsCount) {
		if (intervalInMs <= 0) {
			throw new IllegalArgumentException("intervalInMs must be positive: " + intervalInMs);
		}
		if (windowsCount <= 0) {
			throw new IllegalArgumentException("windowsCount must be positive: " + windowsCount);
		}
		if (intervalInMs % windowsCount != 0) {
			throw new IllegalArgumentException(
					"intervalInMs must be divisible by windowsCount: intervalInMs=" + intervalInMs
							+ ", windowsCount=" + windowsCount);
		}
		this.windowsCount = windowsCount;
		this.intervalInMs = intervalInMs;
		windowLengthInMs = intervalInMs / windowsCount;
		array = new AtomicReferenceArray<>(windowsCount);
	}

	public Metric currentMetrics() {
		long now = JVMTimeProvider.currentTimeMillis();
		int index = getCurrentIdx(now);
		long start = now - now % windowLengthInMs;

		Metric metrics = array.get(index);
		if (metrics != null && metrics.getStartTime() == start) {
			return metrics;
		}

		lock.lock();
		try {
			now = JVMTimeProvider.currentTimeMillis();
			index = getCurrentIdx(now);
			start = now - now % windowLengthInMs;

			metrics = array.get(index);

			if (metrics != null && metrics.getStartTime() == start) {
				return metrics;
			}

			if (metrics == null) {
				metrics = new MetricBase(start);
				array.set(index, metrics);
			} else if (start > metrics.getStartTime()) {
				metrics.reset(start);
			}

			return metrics;
		} finally {
			lock.unlock();
		}
	}

	public List<Metric> listCurrentMetrics() {
		long currentTimeMillis = JVMTimeProvider.currentTimeMillis();
		List<Metric> list = new ArrayList<>();
		for (int i = 0; i < array.length(); i++) {
			Metric mb = array.get(i);
			if (mb == null || currentTimeMillis - intervalInMs > mb.getStartTime()) {
				continue;
			}
			list.add(mb);
		}
		return list;
	}

	private int getCurrentIdx(long timeMillis) {
		long timeId = timeMillis / windowLengthInMs;
		return (int) (timeId % windowsCount);
	}
}
