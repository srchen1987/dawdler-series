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
package club.dawdler.clientplug.web.session;

import java.util.concurrent.TimeUnit;

import club.dawdler.clientplug.web.session.http.DawdlerHttpSession;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * @author jackson.song
 * @version V1.0
 * caffeine session管理器的一个实现
 */
public class DistributedCaffeineSessionManager extends AbstractDistributedSessionManager {
	private final int maxInactiveInterval;
	LoadingCache<String, DawdlerHttpSession> sessions;
	LoadingCache<String, Boolean> ipBlacklist;

	public DistributedCaffeineSessionManager(int maxInactiveInterval, int maxSize, boolean defense,
			int ipMaxInactiveInterval, int ipMaxSize) {
		this.maxInactiveInterval = maxInactiveInterval;
		sessions = Caffeine.newBuilder().maximumSize(maxSize).expireAfterAccess(maxInactiveInterval, TimeUnit.SECONDS)
				.build(key -> null);
		if (defense) {
			ipBlacklist = Caffeine.newBuilder().maximumSize(maxSize)
					.expireAfterWrite(maxInactiveInterval, TimeUnit.SECONDS).build(key -> null);
		}
	}

	public DawdlerHttpSession getSession(String sessionKey) {
		return sessions.get(sessionKey);
	}

	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	@Override
	public void close() {
		invalidateAll();
	}

	@Override
	public void removeSession(String sessionKey) {
		DawdlerHttpSession session = sessions.get(sessionKey);
		if (session != null) {
			session.clear();
			sessions.invalidate(sessionKey);
		}
	}

	@Override
	public void addSession(String sessionKey, DawdlerHttpSession dawdlerHttpSession) {
		sessions.put(sessionKey, dawdlerHttpSession);
	}

	@Override
	public void removeSession(DawdlerHttpSession dawdlerHttpSession) {
		if (dawdlerHttpSession != null) {
			dawdlerHttpSession.clear();
			sessions.invalidate(dawdlerHttpSession.getId());
		}
	}

	@Override
	public void invalidateAll() {
		sessions.invalidateAll();
	}

	public void addIpToBlacklist(String ip) {
		ipBlacklist.put(ip, true);
	}

	public boolean getIpBlack(String ip) {
		return ipBlacklist.get(ip) != null;
	}
}
