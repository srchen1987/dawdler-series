package com.anywide.dawdler.core.component.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.order.OrderComparator;
import com.anywide.dawdler.core.order.OrderData;

public class ComponentLifeCycleProvider {
	private static Map<String, ComponentLifeCycleProvider> instances = new ConcurrentHashMap<>();
	private final List<OrderData<ComponentLifeCycle>> componentLifeCycles = new ArrayList<>();
	
	public static ComponentLifeCycleProvider getInstance(String serviceName) {
		ComponentLifeCycleProvider componentLifeCycleProvider = instances.get(serviceName);
		if (componentLifeCycleProvider != null)
			return componentLifeCycleProvider;
		synchronized (instances) {
			componentLifeCycleProvider = instances.get(serviceName);
			if (componentLifeCycleProvider == null) {
				instances.put(serviceName, new ComponentLifeCycleProvider());
			}
		}
		return instances.get(serviceName);
	}

	private ComponentLifeCycleProvider() {
		ServiceLoader.load(ComponentLifeCycle.class).forEach(componentLifeCycle -> {
			OrderData<ComponentLifeCycle> orderData = new OrderData<>();
			Order order = componentLifeCycle.getClass().getAnnotation(Order.class);
			if (order != null) {
				orderData.setOrder(order.value());
			}
			orderData.setData(componentLifeCycle);
			componentLifeCycles.add(orderData);
		});
		OrderComparator.sort(componentLifeCycles);
	}

	public List<OrderData<ComponentLifeCycle>> getComponentLifeCycles() {
		return componentLifeCycles;
	}

}
