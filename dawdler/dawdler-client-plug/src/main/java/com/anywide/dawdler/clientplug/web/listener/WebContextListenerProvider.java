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
package com.anywide.dawdler.clientplug.web.listener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.ServletContext;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.order.OrderComparator;
import com.anywide.dawdler.core.order.OrderData;
/**
 * 
 * @Title:  WebContextListenerProvider.java   
 * @Description:    监听器提供者   
 * @author: jackson.song    
 * @date:   2007年04月19日   
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class WebContextListenerProvider {
	private static AtomicBoolean order = new AtomicBoolean(false);
	public static List<OrderData<WebContextListener>> getwebContextListeners() {
		return webContextListeners;
	}
	public static void order() {
		if(order.compareAndSet(false, true))
			OrderComparator.sort(webContextListeners);
	}

	public static void addWebContextListeners(WebContextListener webContextListener) {
		Order co = webContextListener.getClass().getAnnotation(Order.class);
		OrderData<WebContextListener> od = new OrderData<WebContextListener>();
		od.setData(webContextListener);
		if(co!=null){
			od.setOrder(co.value());
		}
		webContextListeners.add(od);
	}

	private static List<OrderData<WebContextListener>>  webContextListeners = new ArrayList<OrderData<WebContextListener>>();
	public static void listenerRun(boolean init,ServletContext servletContext){
		List<OrderData<WebContextListener>> listeners = WebContextListenerProvider.getwebContextListeners();
		if(listeners!=null){
			if(init){
				for(OrderData<WebContextListener> listener : listeners){
					listener.getData().contextInitialized(servletContext);
				}
			}else{
				for(int i=listeners.size()-1;i>=0;i--){
					listeners.get(i).getData().contextDestroyed(servletContext);
				}
			}
			
		}
		
	}
}

