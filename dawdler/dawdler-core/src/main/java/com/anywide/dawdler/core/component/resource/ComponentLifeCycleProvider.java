package com.anywide.dawdler.core.component.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.order.OrderComparator;
import com.anywide.dawdler.core.order.OrderData;

public class ComponentLifeCycleProvider {
	private static final List<OrderData<ComponentLifeCycle>> componentLifeCycles = new ArrayList<>();
	static {
		ServiceLoader.load(ComponentLifeCycle.class).forEach(ComponentLifeCycleProvider::addLifeCycle);
		OrderComparator.sort(componentLifeCycles);
	}
	
	public static List<OrderData<ComponentLifeCycle>> getComponentlifecycles() {
		return componentLifeCycles;
	}

	static void addLifeCycle(ComponentLifeCycle lifeCycle) {
		Order co = lifeCycle.getClass().getAnnotation(Order.class);
		OrderData<ComponentLifeCycle> od = new OrderData<>();
		od.setData(lifeCycle);
		if (co != null) {
			od.setOrder(co.value());
		}
		componentLifeCycles.add(od);
	}
	
}
