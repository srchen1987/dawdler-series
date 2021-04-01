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
package com.anywide.dawdler.clientplug.web.session;

import com.anywide.dawdler.clientplug.web.session.http.DawdlerHttpSession;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.TimeUnit;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DistributedCaffeineSessionManager.java
 * @Description caffeine session管理器的一个实现
 * @date 2016年6月16日
 * @email suxuan696@gmail.com
 */
public class DistributedCaffeineSessionManager extends AbstractDistributedSessionManager {
    private final int maxInactiveInterval;
    LoadingCache<String, DawdlerHttpSession> sessions;

    public DistributedCaffeineSessionManager(int maxInactiveInterval, int maxSize) {
        this.maxInactiveInterval = maxInactiveInterval;
        sessions = Caffeine.newBuilder().maximumSize(maxSize)
                .expireAfterAccess(maxInactiveInterval, TimeUnit.SECONDS)
                .build(this::createExpensiveGraph);
    }

    public DawdlerHttpSession getSession(String sessionKey) {
        return sessions.getIfPresent(sessionKey);
    }

    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    private DawdlerHttpSession createExpensiveGraph(@NonNull String key) {
        return null;
    }

    @Override
    public void close() {
        sessions.cleanUp();
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
}
