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
package com.anywide.dawdler.server.listener;
import java.util.ArrayList;
import java.util.List;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.order.OrderComparator;
import com.anywide.dawdler.core.order.OrderData;
/**
 * 
 * @Title:  DawdlerListenerProvider.java
 * @Description:    监听器提供者   
 * @author: jackson.song    
 * @date:   2015年04月19日    
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class DawdlerListenerProvider {
	public List<OrderData<DawdlerServiceListener>> getListeners() {
		return listeners;
	}
	public void order(){
			OrderComparator.sort(listeners);
	}
	public void addHandlerInterceptors(DawdlerServiceListener dawdlerServiceListener) {
		Order co = dawdlerServiceListener.getClass().getAnnotation(Order.class);
		OrderData<DawdlerServiceListener> od = new OrderData<>();
		od.setData(dawdlerServiceListener);
		if(co!=null){
			od.setOrder(co.value());
		}
		listeners.add(od);
	}

	private List<OrderData<DawdlerServiceListener>> listeners = new ArrayList<>();
}
