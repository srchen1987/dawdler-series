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
package club.dawdler.core.thread;

import java.nio.channels.Channel;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.core.exception.DawdlerOperateException;

/**
 * @author jackson.song
 * @version V1.0
 * 执行结果
 */
public class InvokeFuture<V> {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected V result;
	protected AtomicBoolean done = new AtomicBoolean(false);
	protected AtomicBoolean success = new AtomicBoolean(false);
	protected Semaphore semaphore = new Semaphore(0);
	protected Throwable cause;
	protected Channel channel;

	public InvokeFuture() {
	}

	public boolean isDone() {
		return done.get();
	}

	public V getResult() throws DawdlerOperateException {
		if (!isDone()) {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

		}
		if (cause != null) {
			if (cause instanceof DawdlerOperateException) {
				throw ((RuntimeException) cause);
			}
			throw new DawdlerOperateException(cause);
		}
		return this.result;
	}

	public void setResult(V result) {
		this.result = result;
		done.set(true);
		success.set(true);
		semaphore.release();
	}

	public V getResult(long timeout, TimeUnit unit) {
		if (!isDone()) {
			try {
				if (!semaphore.tryAcquire(timeout, unit)) {
					setCause(new TimeoutException("get result time out," + timeout + "\t" + unit + "!"));
				}
			} catch (InterruptedException e) {
				throw new DawdlerOperateException(e);
			}
		}
		if (cause != null) {
			if (cause instanceof DawdlerOperateException) {
				throw ((DawdlerOperateException) cause);
			}
			throw new DawdlerOperateException(cause);
		}
		return this.result;
	}

	public boolean isSuccess() {
		return success.get();
	}

	public Throwable getCause() {
		return cause;
	}

	public void setCause(Throwable cause) {
		this.cause = cause;
		done.set(true);
		success.set(false);
		semaphore.release();
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

}
