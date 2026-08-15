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
package club.dawdler.jedis.lock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import club.dawdler.util.HashedWheelTimer;
import club.dawdler.util.HashedWheelTimerSingleCreator;
import club.dawdler.util.Timeout;
import club.dawdler.util.TimerTask;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.params.SetParams;

/**
 * @author jackson.song
 * @version V1.0
 * jedis实现的分布式锁,支持watch dog与重入锁
 */
public class JedisDistributedLock {

	private int intervalInMillis;
	private UnifiedJedis unifiedJedis;
	private final String LOCK_KEY;
	private long lockExpiryInMillis;
	private volatile Timeout timeout;
	private volatile boolean released = true;
	private final boolean useWatchDog;
	private final ThreadLocal<Lock> lockThreadLocal = new ThreadLocal<Lock>();

	private static final HashedWheelTimer HASHED_WHEEL_TIMER = HashedWheelTimerSingleCreator.getHashedWheelTimer();

	public JedisDistributedLock(UnifiedJedis unifiedJedis, String lockKey, long lockExpiryInMillis,
					int intervalInMillis,
			boolean useWatchDog) {
		if (unifiedJedis == null) {
			throw new IllegalArgumentException("unifiedJedis can not be null");
		}
		if (lockKey == null || lockKey.isEmpty()) {
			throw new IllegalArgumentException("lockKey can not be empty");
		}
		this.unifiedJedis = unifiedJedis;
		this.LOCK_KEY = lockKey;
		this.lockExpiryInMillis = lockExpiryInMillis;
		this.intervalInMillis = intervalInMillis;
		this.useWatchDog = useWatchDog;
	}

	public boolean isUseWatchDog() {
		return useWatchDog;
	}

	public String getLockKey() {
		return this.LOCK_KEY;
	}

	public long getLockExpiryInMillis() {
		return lockExpiryInMillis;
	}

	private String nextUid() {
		return Thread.currentThread().getName() + ":" + UUID.randomUUID().toString();
	}

	private UnifiedJedis getClient() {
		return unifiedJedis;
	}

	private boolean tryLock(UnifiedJedis unifiedJedis) {
		Lock lock = lockThreadLocal.get();
		if (lock == null || released) {
			lock = new Lock(nextUid());
			final String lockUid = lock.uid;
			SetParams setParams = SetParams.setParams().px(lockExpiryInMillis).nx();
			String result = unifiedJedis.set(this.LOCK_KEY, lock.toString(), setParams);
			if ("OK".equals(result)) {
				released = false;
				if (useWatchDog) {
					HASHED_WHEEL_TIMER.newTimeout(new TimerTask() {
						@Override
						public void run(Timeout timeout) throws Exception {
							if (timeout.isCancelled() || released) {
								return;
							}
							try {
								List<String> args = new ArrayList<>(2);
								args.add(lockUid);
								args.add(lockExpiryInMillis + "");
								UnifiedJedis unifiedJedis = getClient();
								String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('pexpire', KEYS[1],  ARGV[2]) else return 0 end";
								Object result = unifiedJedis.eval(luaScript,
										Collections.singletonList(JedisDistributedLock.this.LOCK_KEY), args);
								if (result != null && ((Long) result) == 1L) {
									JedisDistributedLock.this.timeout = timeout.timer().newTimeout(this,
											lockExpiryInMillis / 3, TimeUnit.MILLISECONDS);
								}
							} catch (Exception e) {
							}
						}
					}, lockExpiryInMillis / 3, TimeUnit.MILLISECONDS);
				}
				lockThreadLocal.set(lock);
				return true;
			}
			return false;
		} else {
			String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('pexpire', KEYS[1], ARGV[2]) else return 0 end";
			Object renewResult = unifiedJedis.eval(luaScript,
					Collections.singletonList(this.LOCK_KEY),
					Arrays.asList(lock.toString(), String.valueOf(lockExpiryInMillis)));
			if (renewResult != null && ((Long) renewResult) == 1L) {
				lock.incrementLockTime();
				return true;
			}
			lockThreadLocal.remove();
			released = true;
			return tryLock(unifiedJedis);
		}

	}

	public boolean tryLock() {
		UnifiedJedis unifiedJedis = getClient();
		return tryLock(unifiedJedis);
	}

	public boolean lock(long acquireTimeoutInMillis) throws InterruptedException {
		long expiryTime = System.currentTimeMillis() + acquireTimeoutInMillis; // 锁的请求到期时间
		while (expiryTime >= System.currentTimeMillis()) {
			UnifiedJedis unifiedJedis = getClient();
			boolean result = tryLock(unifiedJedis);
			if (result) {
				return true;
			}
			Thread.sleep(intervalInMillis);
		}
		return false;
	}

	public boolean lock() throws InterruptedException {
		return lock(Integer.MAX_VALUE);
	}

	public boolean unlock() {
		UnifiedJedis unifiedJedis = getClient();
		return unlock(unifiedJedis);
	}

	private boolean unlock(UnifiedJedis unifiedJedis) {
		Lock lock = lockThreadLocal.get();
		if (lock == null) {
			return false;
		} else {
			if (lock.decrementAndGet() == 0) {
				released = true;
				if (this.timeout != null) {
					this.timeout.cancel();
				}
				lockThreadLocal.remove();
				String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
				Object result = unifiedJedis.eval(luaScript, Collections.singletonList(this.LOCK_KEY),
						Collections.singletonList(lock.toString()));
				if (result != null && ((Long) result) == 1L) {
					return true;
				}
				return false;
			} else {
				return true;
			}
		}
	}

	protected static class Lock {
		private String uid;
		private int lockTime;

		Lock(String uid) {
			this.uid = uid;
			this.lockTime = 1;
		}

		public String getUid() {
			return uid;
		}

		public int decrementAndGet() {
			return --lockTime;
		}

		public void incrementLockTime() {
			lockTime++;
		}

		public int getLockTime() {
			return lockTime;
		}

		@Override
		public String toString() {
			return uid;
		}
	}

}
