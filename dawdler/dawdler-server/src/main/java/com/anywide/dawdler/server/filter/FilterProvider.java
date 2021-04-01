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

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.bean.RequestBean;
import com.anywide.dawdler.core.bean.ResponseBean;
import com.anywide.dawdler.core.exception.DawdlerOperateException;
import com.anywide.dawdler.core.order.OrderComparator;
import com.anywide.dawdler.core.order.OrderData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jackson.song
 * @version V1.0
 * @Title FilterProvider.java
 * @Description 过滤器提供者 采用spi方式配置 update 改为实现DawdlerFilter接口即可
 * @date 2015年04月08日
 * @email suxuan696@gmail.com
 */
public class FilterProvider {
    private static final Logger logger = LoggerFactory.getLogger(FilterProvider.class);
    private final List<OrderData<DawdlerFilter>> filters = new ArrayList<>();
    public FilterChain lastChain;

    public FilterProvider() {
    }

    public List<OrderData<DawdlerFilter>> getFilters() {
        return filters;
    }

    public void addFilter(DawdlerFilter dawdlerFilter) {
        Order co = dawdlerFilter.getClass().getAnnotation(Order.class);
        OrderData<DawdlerFilter> od = new OrderData<>();
        od.setData(dawdlerFilter);
        if (co != null) {
            od.setOrder(co.value());
        }
        filters.add(od);
    }

    public void orderAndBuildChain() {
        OrderComparator.sort(filters);
        FilterChain chain = new DefaultFilterChain();
        lastChain = buildChain(chain);
    }

    FilterChain buildChain(final FilterChain chain) {
        FilterChain last = chain;
        List<OrderData<DawdlerFilter>> filters = this.filters;
        if (!filters.isEmpty()) {
            for (int i = filters.size() - 1; i >= 0; i--) {
                final DawdlerFilter filter = filters.get(i).getData();
                final FilterChain next = last;
                last = new FilterChain() {
                    @Override
                    public void doFilter(RequestBean request, ResponseBean response) throws Exception {
                        filter.doFilter(request, response, next);
                    }
                };
            }
        }
        return last;
    }

    public void doFilter(RequestBean request, ResponseBean response) {
        try {
            lastChain.doFilter(request, response);
        } catch (Exception e) {
            response.setCause(new DawdlerOperateException(new RuntimeException(e.toString())));
            logger.error("", e);
        }

    }

}
