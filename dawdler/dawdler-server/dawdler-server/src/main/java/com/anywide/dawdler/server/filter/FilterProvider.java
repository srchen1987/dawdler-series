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
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.bean.RequestBean;
import com.anywide.dawdler.core.bean.ResponseBean;
import com.anywide.dawdler.core.exception.DawdlerOperateException;
import com.anywide.dawdler.core.order.OrderComparator;
import com.anywide.dawdler.core.order.OrderData;
/**
 * 
 * @Title:  FilterProvider.java
 * @Description:    过滤器提供者 采用spi方式配置    update 改为实现DawdlerFilter接口即可
 * @author: jackson.song    
 * @date:   2015年04月08日      
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class FilterProvider {
	public FilterChain lastChain;
	private static Logger logger = LoggerFactory.getLogger(FilterProvider.class);
	private List<OrderData<DawdlerFilter>>	filters = new ArrayList<OrderData<DawdlerFilter>>(); 
//	private AtomicBoolean order = new AtomicBoolean(false);
	public FilterProvider() {
		// TODO Auto-generated constructor stub
	}
//	static{
//		ServiceLoader<DawdlerFilter> loader = ServiceLoader.load(DawdlerFilter.class);
//		loader.forEach((filter)->{
//			addFilter(filter);
//		});
//		order();
//		FilterChain chain = new DefaultFilterChain();
//		lastChain = buildChain(chain);
//	}
	public List<OrderData<DawdlerFilter>> getFilters() {
		return filters;
	}
	 public void addFilter(DawdlerFilter dawdlerFilter) {
		Order co = dawdlerFilter.getClass().getAnnotation(Order.class);
		OrderData<DawdlerFilter> od = new OrderData<DawdlerFilter>();
		od.setData(dawdlerFilter);
		if(co!=null){
			od.setOrder(co.value());
		}
		filters.add(od);
	}
	 public void orderAndbuildChain() {
//		if(order.compareAndSet(false, true))
			OrderComparator.sort(filters);
			FilterChain chain = new DefaultFilterChain();
			lastChain = buildChain(chain);
	}
	 FilterChain buildChain(final FilterChain chain) {
    	FilterChain last = chain;
    	 List<OrderData<DawdlerFilter>> filters = this.filters;
        if (!filters.isEmpty()) {
            for (int i = filters.size() - 1; i >= 0; i --) {
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
	public void doFilter(RequestBean request,ResponseBean response) {
		try {
			lastChain.doFilter(request, response);
		} catch (Exception e) {
			response.setCause(new DawdlerOperateException(new RuntimeException(e.toString())));
			logger.error("",e);
		}
		 
	}
	
}

