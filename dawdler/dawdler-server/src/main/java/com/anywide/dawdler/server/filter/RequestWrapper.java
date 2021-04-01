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
package com.anywide.dawdler.server.filter;

import com.anywide.dawdler.core.bean.RequestBean;
import com.anywide.dawdler.server.bean.ServicesBean;
import com.anywide.dawdler.server.thread.processor.ServiceExecutor;

/**
 * @author jackson.song
 * @version V1.0
 * @Title RequestWrapper.java
 * @Description request的包装类
 * @date 2015年04月08日
 * @email suxuan696@gmail.com
 */
public class RequestWrapper extends RequestBean {
    private static final long serialVersionUID = 2807385594696214109L;
    private final RequestBean request;
    private final ServicesBean services;
    private final ServiceExecutor serviceExecutor;

    public RequestWrapper(RequestBean request, ServicesBean services, ServiceExecutor serviceExecutor) {
        super.setSeq(request.getSeq());
        super.setServiceName(request.getServiceName());
        super.setMethodName(request.getMethodName());
        super.setTypes(request.getTypes());
        super.setArgs(request.getArgs());
        super.setFuzzy(request.isFuzzy());
        this.request = request;
        this.services = services;
        this.serviceExecutor = serviceExecutor;
    }

    public ServiceExecutor getServiceExecutor() {
        return serviceExecutor;
    }

    public ServicesBean getServices() {
        return services;
    }

    RequestBean getRequest() {
        return request;
    }

    @Override
    public void setFuzzy(boolean fuzzy) {
    }

    @Override
    public void setPath(String path) {
    }

    @Override
    public void setSingle(boolean single) {
    }

    @Override
    public void setSeq(long seq) {
    }

    @Override
    public void setServiceName(String serviceName) {
    }

    @Override
    public void setMethodName(String methodName) {
    }

    @Override
    public void setTypes(Class[] types) {
    }

    @Override
    public void setArgs(Object... args) {
    }

}
