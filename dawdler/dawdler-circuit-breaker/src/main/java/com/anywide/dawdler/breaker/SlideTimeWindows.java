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
package com.anywide.dawdler.breaker;

import com.anywide.dawdler.breaker.metric.Metric;
import com.anywide.dawdler.breaker.metric.MetricBase;
import com.anywide.dawdler.util.JVMTimeProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author jackson.song
 * @version V1.0
 * @Title SlideTimeWindows.java
 * @Description 滑动窗口
 * @date 2018年3月16日
 * @email suxuan696@gmail.com
 */
public class SlideTimeWindows {
    private final int intervalInMs;
    private final int windowsCount;
    private final int windowLengthInMs;

    private final AtomicReferenceArray<Metric> array;

    private final ReentrantLock lock = new ReentrantLock();

    public SlideTimeWindows(int intervalInMs, int windowsCount) {
        this.windowsCount = windowsCount;
        this.intervalInMs = intervalInMs;
        windowLengthInMs = intervalInMs / windowsCount;
        array = new AtomicReferenceArray<>(windowsCount);
    }

    public Metric currentMetrics() {
        long now = JVMTimeProvider.currentTimeMillis();
        int index = getCurrentIdx(now);
        while (true) {
            Metric metrics = array.get(index);
            long start = now - now % windowLengthInMs;
            if (metrics == null) {
                metrics = new MetricBase(start);
                if (array.compareAndSet(index, null, metrics))
                    return metrics;
                return array.get(index);
            }

            if (metrics.getStartTime() == start)
                return metrics;
            else {
                if (lock.tryLock())
                    try {
                        metrics.reset(start);
                        return metrics;
                    } finally {
                        lock.unlock();
                    }
            }
        }
    }

    public List<Metric> listCurrentMetrics() {
        List<Metric> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Metric mb = array.get(i);
            if (mb == null || JVMTimeProvider.currentTimeMillis() - intervalInMs > mb.getStartTime()) {
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
