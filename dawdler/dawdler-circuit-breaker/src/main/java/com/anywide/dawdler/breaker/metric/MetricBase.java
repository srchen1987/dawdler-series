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
package com.anywide.dawdler.breaker.metric;

import java.util.concurrent.atomic.LongAdder;

import com.anywide.dawdler.util.JVMTimeProvider;

/**
 * @author jackson.song
 * @version V1.0
 * 基础度量
 */
public class MetricBase implements Metric {

	private final LongAdder total = new LongAdder();

	private final LongAdder fail = new LongAdder();

	private long startTime;

	public MetricBase(long startTime) {
		this.startTime = startTime;
	}

	public void totalIncrt() {
		total.increment();
	}

	public void failIncrt() {
		fail.increment();
	}

	public long totalCount() {
		return total.longValue();
	}

	public long failCount() {
		return fail.longValue();
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	@Override
	public long restStartTime() {
		return JVMTimeProvider.currentTimeMillis();
	}

	@Override
	public void reset(long startTime) {
		total.reset();
		fail.reset();
		this.startTime = startTime;
	}

}
