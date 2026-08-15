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
package club.dawdler.server.thread.processor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jackson.song
 * @version V1.0
 * 线程池过载时的拒绝策略,对 DataProcessor 回写 cause=busy 的 ResponseBean,
 * 让客户端拿到明确的繁忙语义做退避重试或故障转移,而非被动重连。
 * rejectBusy 内部失败时已自行兜底关连接,此处不再重复处理。
 */
public class BusyResponseRejectedExecutionHandler implements RejectedExecutionHandler {
	private static final Logger logger = LoggerFactory.getLogger(BusyResponseRejectedExecutionHandler.class);

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		if (r instanceof DataProcessor) {
			((DataProcessor) r).rejectBusy();
			return;
		}
		logger.warn("Rejected non-DataProcessor task, discarded. task={}, pool={}", r, executor);
	}
}
