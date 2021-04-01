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
package com.anywide.dawdler.client;

import com.anywide.dawdler.client.filter.FilterProvider;
import com.anywide.dawdler.client.filter.RequestWrapper;
import com.anywide.dawdler.client.net.aio.session.SocketSession;
import com.anywide.dawdler.core.annotation.CircuitBreaker;
import com.anywide.dawdler.core.bean.RequestBean;
import com.anywide.dawdler.core.thread.InvokeFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author jackson.song
 * @version V2.0
 * @Title Transaction.java
 * @Description 客户端调用服务器端的行为，不考虑异步
 * @date 2008年03月13日
 * @email suxuan696@gmail.com modify 2015年03月22日 脱离jboss 采用自有容器
 */
public class Transaction {
    private final List<Class> types = new ArrayList<>();
    private final List values = new ArrayList<>();
    private final DawdlerConnection con;
    private String serviceName;
    private String method;
    private int serializer;
    private boolean fuzzy;
    private boolean single = true;
    private int timeout = 15;
    private CircuitBreaker circuitBreaker;
    private Class proxyInterface;

    public Transaction(DawdlerConnection con) {
        this.serializer = con.getSerializer();
        this.con = con;
    }

    public Class getProxyInterface() {
        return proxyInterface;
    }

    public void setProxyInterface(Class proxyInterface) {
        this.proxyInterface = proxyInterface;
    }

    public void setCircuitBreaker(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public int getSerializer() {
        return serializer;
    }

    public void setSerializer(int serializer) {
        this.serializer = serializer;
    }

    public void setSingle(boolean single) {
        this.single = single;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public <T extends Object> void addParam(Class<T> type, T value) {
        types.add(type);
        values.add(value);
    }

    void addObjectParam(Class type, Object value) {
        types.add(type);
        values.add(value);
    }

    public void addString(String value) {
        types.add(String.class);
        values.add(value);
    }

    public void addLong(long value) {
        types.add(long.class);
        values.add(value);
    }

    public void addInt(int value) {
        types.add(int.class);
        values.add(value);
    }

    public void addShort(short value) {
        types.add(short.class);
        values.add(value);
    }

    public void addByte(byte value) {
        types.add(byte.class);
        values.add(value);
    }

    public void addChar(char value) {
        types.add(char.class);
        values.add(value);
    }

    public void addBoolean(boolean value) {
        types.add(boolean.class);
        values.add(value);
    }

    public void addDouble(double value) {
        types.add(double.class);
        values.add(value);
    }

    public void addFloat(float value) {
        types.add(float.class);
        values.add(value);
    }

    public void addObject(Object value) {
        types.add(value.getClass());
        values.add(value);
    }

    public void setFuzzy(boolean fuzzy) {
        this.fuzzy = fuzzy;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean execute() throws Exception {
        Object obj = innerExecute(false);
        if (obj instanceof Boolean)
            return (Boolean) obj;
        return true;
    }

    public Object executeResult() throws Exception {
        return innerExecute(false);
    }

    public Object pureExecuteResult() throws Exception {
        return innerExecute(true);
    }

    private Object innerExecute(boolean pure) throws Exception {
        validate();
        SocketSession socketSession = con.getSession();
        RequestBean request = new RequestBean();
        request.setSeq(socketSession.getSequence());
        request.setServiceName(serviceName);
        request.setMethodName(method);
        request.setTypes(types.toArray(new Class[]{}));
        request.setArgs(values.toArray());
        request.setFuzzy(fuzzy);
        request.setSingle(single);
        Object obj = null;
        if (pure) {
            InvokeFuture<Object> future = new InvokeFuture<>();
            socketSession.getFutures().put(request.getSeq(), future);
            socketSession.getDawdlerConnection().write(request, socketSession);
            obj = future.getResult(timeout, TimeUnit.SECONDS);
        } else {
            obj = FilterProvider.doFilter(new RequestWrapper(request, socketSession, circuitBreaker, proxyInterface, timeout));
        }
        return obj;
    }

    private void validate() {
        if (serviceName == null || method == null)
            throw new IllegalArgumentException("serviceName,method can't be null!");
    }
}
