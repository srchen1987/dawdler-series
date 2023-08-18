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
package com.anywide.dawdler.jedis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.anywide.dawdler.util.HashedWheelTimer;
import com.anywide.dawdler.util.HashedWheelTimerSingleCreator;
import com.anywide.dawdler.util.Timeout;
import com.anywide.dawdler.util.TimerTask;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.util.Pool;

/**
 * @author jackson.song
 * @version V1.0
 * @Title JedisDistributedLock.java
 * @Description jedis实现的分布式锁,支持watch dog与重入锁
 * @date 2023年7月17日
 * @email suxuan696@gmail.com
 */
public class JedisDistributedLock {

	private int intervalInMillis; // 下一次重试等待，单位毫秒
	private static final int DEFAULT_INTERVAL_IN_MILLIS = 20;
	private Pool<Jedis> jedisPool;
	private final String LOCK_KEY;
	private long lockExpiryInMillis; // 锁的过期时长，单位毫秒
	private static final long DEFAULT_LOCK_EXPIRY_IN_MILLIS = 3000;
	private Timeout timeout;
	private boolean useWatchDog = true;
	private static final ThreadLocal<Lock> LOCK_THREADLOCAL = new ThreadLocal<Lock>();

	private static final HashedWheelTimer HASHED_WHEEL_TIMER = HashedWheelTimerSingleCreator.getHashedWheelTimer();

	public JedisDistributedLock(Pool<Jedis> jedisPool, String lockKey, long lockExpiryInMillis, int intervalInMillis) {
		this.jedisPool = jedisPool;
		this.LOCK_KEY = lockKey;
		this.lockExpiryInMillis = lockExpiryInMillis;
		this.intervalInMillis = intervalInMillis;
	}

	public JedisDistributedLock(Pool<Jedis> jedisPool, String lockKey, long lockExpiryInMillis) {
		this(jedisPool, lockKey,  lockExpiryInMillis,  DEFAULT_INTERVAL_IN_MILLIS);
	}

	public JedisDistributedLock(Pool<Jedis> jedisPool, String lockKey, int intervalInMillis) {
		this(jedisPool, lockKey,  DEFAULT_LOCK_EXPIRY_IN_MILLIS,  intervalInMillis);
	}

	public JedisDistributedLock(Pool<Jedis> jedisPool, String lockKey) {
		this(jedisPool, lockKey,  DEFAULT_LOCK_EXPIRY_IN_MILLIS,  DEFAULT_INTERVAL_IN_MILLIS);
	}
	
	public boolean isUseWatchDog() {
		return useWatchDog;
	}

	public void setUseWatchDog(boolean useWatchDog) {
		this.useWatchDog = useWatchDog;
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

	private Jedis getClient() {
		return jedisPool.getResource();
	}

	private boolean tryAcquire(Jedis jedis) {
		Lock lock = LOCK_THREADLOCAL.get();
		if (lock == null) {
			lock = new Lock(nextUid());
			final String lockUid = lock.uid;
			SetParams setParams = SetParams.setParams().px(lockExpiryInMillis).nx();
			String result = jedis.set(this.LOCK_KEY, lock.toString(), setParams);
			if ("OK".equals(result)) {
				if(useWatchDog) {
					HASHED_WHEEL_TIMER.newTimeout(new TimerTask() {
						@Override
						public void run(Timeout timeout) throws Exception {
							if (timeout.isCancelled()) {
								return;
							}
							List<String> args = new ArrayList<>(2);
							args.add(lockUid);
							args.add(lockExpiryInMillis + "");
							Jedis jedis = getClient();
							try {
								String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('pexpire', KEYS[1],  ARGV[2]) else return 0 end";
								Object result = jedis.eval(luaScript,
										Collections.singletonList(JedisDistributedLock.this.LOCK_KEY), args);
								if (((Long) result) == 1L) {
									JedisDistributedLock.this.timeout = timeout.timer().newTimeout(this, lockExpiryInMillis / 3,
											TimeUnit.MILLISECONDS);
								}
							} finally {
								if (jedis != null) {
									jedis.close();
								}
							}

						}
					}, lockExpiryInMillis / 3, TimeUnit.MILLISECONDS);
				}
				LOCK_THREADLOCAL.set(lock);
				return true;
			}
			return false;
		} else {
			lock.incrementLockTime();
			return true;
		}

	}

	public boolean tryAcquire() {
		Jedis jedis = null;
		try {
			jedis = getClient();
			return tryAcquire(jedis);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}

	public boolean acquire(long acquireTimeoutInMillis) throws InterruptedException {
		long expiryTime = System.currentTimeMillis() + acquireTimeoutInMillis; // 锁的请求到期时间
		while (expiryTime >= System.currentTimeMillis()) {
			Jedis jedis = null;
			try {
				jedis = getClient();
				boolean result = tryAcquire(jedis);
				if (result) {
					return true;
				}
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
			Thread.sleep(intervalInMillis);
		}
		return false;
	}

	public boolean acquire() throws InterruptedException {
		return acquire(Integer.MAX_VALUE);
	}

	public boolean release() {
		Jedis jedis = null;
		try {
			jedis = getClient();
			return release(jedis);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}

	private boolean release(Jedis jedis) {
		Lock lock = LOCK_THREADLOCAL.get();
		if (lock == null) {
			return false;
		} else {
			if (lock.decrementAndGet() == 0) {
				if (this.timeout != null) {
					this.timeout.cancel();
				}
				String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
				Object result = jedis.eval(luaScript, Collections.singletonList(this.LOCK_KEY),
						Collections.singletonList(lock.toString()));
				if (((Long) result) == 1L) {
					LOCK_THREADLOCAL.remove();
					return true;
				}
				return false;
			} else {
				return false;
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