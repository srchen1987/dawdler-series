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
package com.anywide.dawdler.core.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DataProcessWorkerPool.java
 * @Description 数据处理的线程池
 * @date 2015年4月21日
 * @email suxuan696@gmail.com
 */
public class DataProcessWorkerPool {
	private final ExecutorService executor;

	public DataProcessWorkerPool(int nThreads, int queueCapacity, long keepAliveMilliseconds) {
		if (queueCapacity <= 0) {
			queueCapacity = Integer.MAX_VALUE;
		}

		if (keepAliveMilliseconds < 0) {
			keepAliveMilliseconds = 0;
		}
		executor = new ThreadPoolExecutor(nThreads, nThreads, keepAliveMilliseconds, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(queueCapacity), new DefaultThreadFactory("dataProcessWorkerPool#"));
	}

	public void execute(Runnable command) {
		executor.execute(command);
	}

	public void shutdown() {
		executor.shutdown();
	}

	public void shutdownNow() {
		executor.shutdownNow();
	}
}
