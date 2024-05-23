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
/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.anywide.dawdler.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author jackson.song
 * @version V1.0
 * netty中挪用过来的定时器
 */
public class HashedWheelTimer implements Timer {

	public static final int WORKER_STATE_INIT = 0;
	public static final int WORKER_STATE_STARTED = 1;
	public static final int WORKER_STATE_SHUTDOWN = 2;
	private static boolean windows = System.getProperty("os.name").toLowerCase(Locale.US).contains("win");
	final Thread workerThread;
	final AtomicInteger workerState = new AtomicInteger(); // 0 - init, 1 - started, 2 - shut down

	public AtomicInteger getWorkerState() {
		return workerState;
	}

	final long tickDuration;
	final Set<HashedWheelTimeout>[] wheel;
	final int mask;
	final ReadWriteLock lock = new ReentrantReadWriteLock();
	final CountDownLatch startTimeInitialized = new CountDownLatch(1);
	private final Worker worker = new Worker();
	volatile long startTime;
	volatile long tick;

	/**
	 * Creates a new timer with the default thread factory
	 * ({@link Executors#defaultThreadFactory()}), default tick duration, and
	 * default number of ticks per wheel.
	 */
	public HashedWheelTimer() {
		this(Executors.defaultThreadFactory());
	}

	/**
	 * Creates a new timer with the default thread factory
	 * ({@link Executors#defaultThreadFactory()}) and default number of ticks per
	 * wheel.
	 *
	 * @param tickDuration the duration between tick
	 * @param unit         the time unit of the {@code tickDuration}
	 * @throws NullPointerException     if {@code unit} is {@code null}
	 * @throws IllegalArgumentException if {@code tickDuration} is <= 0
	 */
	public HashedWheelTimer(long tickDuration, TimeUnit unit) {
		this(Executors.defaultThreadFactory(), tickDuration, unit);
	}

	/**
	 * Creates a new timer with the default thread factory
	 * ({@link Executors#defaultThreadFactory()}).
	 *
	 * @param tickDuration  the duration between tick
	 * @param unit          the time unit of the {@code tickDuration}
	 * @param ticksPerWheel the size of the wheel
	 * @throws NullPointerException     if {@code unit} is {@code null}
	 * @throws IllegalArgumentException if either of {@code tickDuration} and
	 *                                  {@code ticksPerWheel} is <= 0
	 */
	public HashedWheelTimer(long tickDuration, TimeUnit unit, int ticksPerWheel) {
		this(Executors.defaultThreadFactory(), tickDuration, unit, ticksPerWheel);
	}

	/**
	 * Creates a new timer with the default tick duration and default number of
	 * ticks per wheel.
	 *
	 * @param threadFactory a {@link ThreadFactory} that creates a background
	 *                      {@link Thread} which is dedicated to {@link TimerTask}
	 *                      execution.
	 * @throws NullPointerException if {@code threadFactory} is {@code null}
	 */
	public HashedWheelTimer(ThreadFactory threadFactory) {
		this(threadFactory, 100, TimeUnit.MILLISECONDS);
	}

	/**
	 * Creates a new timer with the default number of ticks per wheel.
	 *
	 * @param threadFactory a {@link ThreadFactory} that creates a background
	 *                      {@link Thread} which is dedicated to {@link TimerTask}
	 *                      execution.
	 * @param tickDuration  the duration between tick
	 * @param unit          the time unit of the {@code tickDuration}
	 * @throws NullPointerException     if either of {@code threadFactory} and
	 *                                  {@code unit} is {@code null}
	 * @throws IllegalArgumentException if {@code tickDuration} is <= 0
	 */
	public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit) {
		this(threadFactory, tickDuration, unit, 512);
	}

	/**
	 * Creates a new timer.
	 *
	 * @param threadFactory a {@link ThreadFactory} that creates a background
	 *                      {@link Thread} which is dedicated to {@link TimerTask}
	 *                      execution.
	 * @param tickDuration  the duration between tick
	 * @param unit          the time unit of the {@code tickDuration}
	 * @param ticksPerWheel the size of the wheel
	 * @throws NullPointerException     if either of {@code threadFactory} and
	 *                                  {@code unit} is {@code null}
	 * @throws IllegalArgumentException if either of {@code tickDuration} and
	 *                                  {@code ticksPerWheel} is <= 0
	 */
	public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit, int ticksPerWheel) {

		if (threadFactory == null) {
			throw new NullPointerException("threadFactory");
		}
		if (unit == null) {
			throw new NullPointerException("unit");
		}
		if (tickDuration <= 0) {
			throw new IllegalArgumentException("tickDuration must be greater than 0: " + tickDuration);
		}
		if (ticksPerWheel <= 0) {
			throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);
		}
		// Normalize ticksPerWheel to power of two and initialize the wheel.
		wheel = createWheel(ticksPerWheel);
		mask = wheel.length - 1;

		// Convert tickDuration to nanos.
		this.tickDuration = unit.toNanos(tickDuration);

		// Prevent overflow.
		if (this.tickDuration >= Long.MAX_VALUE / wheel.length) {
			throw new IllegalArgumentException(
					String.format("tickDuration: %d (expected: 0 < tickDuration in nanos < %d", tickDuration,
							Long.MAX_VALUE / wheel.length));
		}

		workerThread = threadFactory.newThread(worker);
	}

	@SuppressWarnings("unchecked")
	private static Set<HashedWheelTimeout>[] createWheel(int ticksPerWheel) {
		if (ticksPerWheel <= 0) {
			throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);
		}
		if (ticksPerWheel > 1073741824) {
			throw new IllegalArgumentException("ticksPerWheel may not be greater than 2^30: " + ticksPerWheel);
		}

		ticksPerWheel = normalizeTicksPerWheel(ticksPerWheel);
		Set<HashedWheelTimeout>[] wheel = new Set[ticksPerWheel];
		for (int i = 0; i < wheel.length; i++) {
			wheel[i] = Collections.newSetFromMap(new ConcurrentHashMap<HashedWheelTimeout, Boolean>());
		}
		return wheel;
	}

	private static int normalizeTicksPerWheel(int ticksPerWheel) {
		int normalizedTicksPerWheel = 1;
		while (normalizedTicksPerWheel < ticksPerWheel) {
			normalizedTicksPerWheel <<= 1;
		}
		return normalizedTicksPerWheel;
	}

	/**
	 * Starts the background thread explicitly. The background thread will start
	 * automatically on demand even if you did not call this method.
	 *
	 * @throws IllegalStateException if this timer has been {@linkplain #stop()
	 *                               stopped} already
	 */
	public void start() {
		switch (workerState.get()) {
		case WORKER_STATE_INIT:
			if (workerState.compareAndSet(WORKER_STATE_INIT, WORKER_STATE_STARTED)) {
				workerThread.start();
			}
			break;
		case WORKER_STATE_STARTED:
			break;
		case WORKER_STATE_SHUTDOWN:
			throw new IllegalStateException("cannot be started once stopped");
		default:
			throw new Error("Invalid WorkerState");
		}

		// Wait until the startTime is initialized by the worker.
		while (startTime == 0) {
			try {
				startTimeInitialized.await();
			} catch (InterruptedException ignore) {
				// Ignore - it will be ready very soon.
			}
		}
	}

	@Override
	public Set<Timeout> stop() {
		if (Thread.currentThread() == workerThread) {
			throw new IllegalStateException(HashedWheelTimer.class.getSimpleName() + ".stop() cannot be called from "
					+ TimerTask.class.getSimpleName());
		}

		if (!workerState.compareAndSet(WORKER_STATE_STARTED, WORKER_STATE_SHUTDOWN)) {
			workerState.set(WORKER_STATE_SHUTDOWN);
			return Collections.emptySet();
		}

		boolean interrupted = false;
		while (workerThread.isAlive()) {
			workerThread.interrupt();
			try {
				workerThread.join(100);
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}

		if (interrupted) {
			Thread.currentThread().interrupt();
		}
		Set<Timeout> unprocessedTimeouts = new HashSet<Timeout>();
		for (Set<HashedWheelTimeout> bucket : wheel) {
			unprocessedTimeouts.addAll(bucket);
			bucket.clear();
		}
		return Collections.unmodifiableSet(unprocessedTimeouts);
	}

	@Override
	public Timeout newTimeout(TimerTask task, long delay, TimeUnit unit) {
		start();

		if (task == null) {
			throw new NullPointerException("task");
		}
		if (unit == null) {
			throw new NullPointerException("unit");
		}

		long deadline = System.nanoTime() + unit.toNanos(delay) - startTime;

		// Add the timeout to the wheel.
		HashedWheelTimeout timeout;
		lock.readLock().lock();
		try {
			timeout = new HashedWheelTimeout(task, deadline);
			if (workerState.get() == WORKER_STATE_SHUTDOWN) {
				throw new IllegalStateException("Cannot enqueue after shutdown");
			}
			wheel[timeout.stopIndex].add(timeout);
		} finally {
			lock.readLock().unlock();
		}

		return timeout;
	}

	private final class Worker implements Runnable {

		Worker() {
		}

		@Override
		public void run() {
			// Initialize the startTime.
			startTime = System.nanoTime();
			if (startTime == 0) {
				// We use 0 as an indicator for the uninitialized value here, so make sure it's
				// not 0 when initialized.
				startTime = 1;
			}

			// Notify the other threads waiting for the initialization at start().
			startTimeInitialized.countDown();

			List<HashedWheelTimeout> expiredTimeouts = new ArrayList<HashedWheelTimeout>();

			do {
				final long deadline = waitForNextTick();
				if (deadline > 0) {
					fetchExpiredTimeouts(expiredTimeouts, deadline);
					notifyExpiredTimeouts(expiredTimeouts);
				}
			} while (workerState.get() == WORKER_STATE_STARTED);
		}

		private void fetchExpiredTimeouts(List<HashedWheelTimeout> expiredTimeouts, long deadline) {

			// Find the expired timeouts and decrease the round counter
			// if necessary. Note that we don't send the notification
			// immediately to make sure the listeners are called without
			// an exclusive lock.
			lock.writeLock().lock();
			try {
				fetchExpiredTimeouts(expiredTimeouts, wheel[(int) (tick & mask)].iterator(), deadline);
			} finally {
				// Note that the tick is updated only while the writer lock is held,
				// so that newTimeout() and consequently new HashedWheelTimeout() never see an
				// old value
				// while the reader lock is held.
				tick++;
				lock.writeLock().unlock();
			}
		}

		private void fetchExpiredTimeouts(List<HashedWheelTimeout> expiredTimeouts, Iterator<HashedWheelTimeout> i,
				long deadline) {

			while (i.hasNext()) {
				HashedWheelTimeout timeout = i.next();
				if (timeout.remainingRounds <= 0) {
					i.remove();
					if (timeout.deadline <= deadline) {
						expiredTimeouts.add(timeout);
					} else {
						// The timeout was placed into a wrong slot. This should never happen.
						throw new Error(
								String.format("timeout.deadline (%d) > deadline (%d)", timeout.deadline, deadline));
					}
				} else {
					timeout.remainingRounds--;
				}
			}
		}

		private void notifyExpiredTimeouts(List<HashedWheelTimeout> expiredTimeouts) {
			// Notify the expired timeouts.
			for (int i = expiredTimeouts.size() - 1; i >= 0; i--) {
				expiredTimeouts.get(i).expire();
			}

			// Clean up the temporary list.
			expiredTimeouts.clear();
		}

		/**
		 * calculate goal nanoTime from startTime and current tick number, then wait
		 * until that goal has been reached.
		 *
		 * @return Long.MIN_VALUE if received a shutdown request, current time otherwise
		 *         (with Long.MIN_VALUE changed by +1)
		 */
		private long waitForNextTick() {
			long deadline = tickDuration * (tick + 1);

			for (;;) {
				final long currentTime = System.nanoTime() - startTime;
				long sleepTimeMs = (deadline - currentTime + 999999) / 1000000;

				if (sleepTimeMs <= 0) {
					if (currentTime == Long.MIN_VALUE) {
						return -Long.MAX_VALUE;
					} else {
						return currentTime;
					}
				}

				// Check if we run on windows, as if thats the case we will need
				// to round the sleepTime as workaround for a bug that only affect
				// the JVM if it runs on windows.
				//
				if (windows) {
					sleepTimeMs = sleepTimeMs / 10 * 10;
				}

				try {
					Thread.sleep(sleepTimeMs);
				} catch (InterruptedException e) {
					if (workerState.get() == WORKER_STATE_SHUTDOWN) {
						return Long.MIN_VALUE;
					}
				}
			}
		}
	}

	private final class HashedWheelTimeout implements Timeout {

		private static final int ST_INIT = 0;
		private static final int ST_CANCELLED = 1;
		private static final int ST_EXPIRED = 2;
		final long deadline;
		final int stopIndex;
		private final TimerTask task;
		private final AtomicInteger state = new AtomicInteger(ST_INIT);
		volatile long remainingRounds;

		HashedWheelTimeout(TimerTask task, long deadline) {
			this.task = task;
			this.deadline = deadline;

			long calculated = deadline / tickDuration;
			final long ticks = Math.max(calculated, tick); // Ensure we don't schedule for past.
			stopIndex = (int) (ticks & mask);
			remainingRounds = (calculated - tick) / wheel.length;
		}

		@Override
		public Timer timer() {
			return HashedWheelTimer.this;
		}

		@Override
		public TimerTask task() {
			return task;
		}

		@Override
		public boolean cancel() {
			if (!state.compareAndSet(ST_INIT, ST_CANCELLED)) {
				return false;
			}

			wheel[stopIndex].remove(this);
			return true;
		}

		@Override
		public boolean isCancelled() {
			return state.get() == ST_CANCELLED;
		}

		@Override
		public boolean isExpired() {
			return state.get() != ST_INIT;
		}

		public void expire() {
			if (!state.compareAndSet(ST_INIT, ST_EXPIRED)) {
				return;
			}

			try {
				task.run(this);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		@Override
		public String toString() {
			final long currentTime = System.nanoTime();
			long remaining = deadline - currentTime + startTime;

			StringBuilder buf = new StringBuilder(192);
			buf.append(getClass().getName());
			buf.append('(');

			buf.append("deadline: ");
			if (remaining > 0) {
				buf.append(remaining);
				buf.append(" ns later");
			} else if (remaining < 0) {
				buf.append(-remaining);
				buf.append(" ns ago");
			} else {
				buf.append("now");
			}

			if (isCancelled()) {
				buf.append(", cancelled");
			}

			buf.append(", task: ");
			buf.append(task());

			return buf.append(')').toString();
		}
	}
}
