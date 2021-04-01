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

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.order.OrderComparator;
import com.anywide.dawdler.core.order.OrderData;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author jackson.song
 * @version V1.0
 * @Title WebContextListenerProvider.java
 * @Description 监听器提供者
 * @date 2007年04月19日
 * @email suxuan696@gmail.com
 */
public class WebContextListenerProvider {
    private static final AtomicBoolean order = new AtomicBoolean(false);
    private static final List<OrderData<WebContextListener>> webContextListeners = new ArrayList<>();

    public static List<OrderData<WebContextListener>> getWebContextListeners() {
        return webContextListeners;
    }

    public static void order() {
        if (order.compareAndSet(false, true))
            OrderComparator.sort(webContextListeners);
    }

    public static void addWebContextListeners(WebContextListener webContextListener) {
        Order co = webContextListener.getClass().getAnnotation(Order.class);
        OrderData<WebContextListener> od = new OrderData<>();
        od.setData(webContextListener);
        if (co != null) {
            od.setOrder(co.value());
        }
        webContextListeners.add(od);
    }


    public static void listenerRun(boolean init, ServletContext servletContext) {
        List<OrderData<WebContextListener>> listeners = WebContextListenerProvider.getWebContextListeners();
        if (listeners != null) {
            if (init) {
                for (OrderData<WebContextListener> listener : listeners) {
                    listener.getData().contextInitialized(servletContext);
                }
            } else {
                for (int i = listeners.size() - 1; i >= 0; i--) {
                    listeners.get(i).getData().contextDestroyed(servletContext);
                }
            }

        }

    }
}
