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
package com.anywide.dawdler.server.context;

import com.anywide.dawdler.server.conf.ServerConfig;
import com.anywide.dawdler.server.deploys.ServiceRoot;

import java.nio.channels.AsynchronousServerSocketChannel;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerServerContext.java
 * @Description dawdler服务器上下文
 * @date 2015年3月21日
 * @email suxuan696@gmail.com
 */
public class DawdlerServerContext {
    private final ServiceRoot serviceRoot;
    private final ServerConfig serverConfig;
    private AsynchronousServerSocketChannel asynchronousServerSocketChannel;

    public DawdlerServerContext(ServerConfig serverConfig) {
        serviceRoot = new ServiceRoot();
        this.serverConfig = serverConfig;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public AsynchronousServerSocketChannel getAsynchronousServerSocketChannel() {
        return asynchronousServerSocketChannel;
    }

    public void setAsynchronousServerSocketChannel(AsynchronousServerSocketChannel asynchronousServerSocketChannel) {
        this.asynchronousServerSocketChannel = asynchronousServerSocketChannel;
    }

    public void initApplication() {
        serviceRoot.initApplication(this);
    }

    public void destroyedApplication() {
        serviceRoot.destroyedApplication();
    }

    public void prepareDestroyedApplication() {
        serviceRoot.prepareDestroyedApplication();
    }


}
