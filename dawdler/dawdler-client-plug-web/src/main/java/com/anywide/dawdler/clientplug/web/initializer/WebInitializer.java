package com.anywide.dawdler.clientplug.web.initializer;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.anywide.dawdler.clientplug.web.filter.ViewFilter;
import com.anywide.dawdler.clientplug.web.listener.WebListener;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycleProvider;
import com.anywide.dawdler.core.order.OrderData;

@Order(0)
public class WebInitializer implements ServletContainerInitializer {
	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		String contextPath = ctx.getContextPath();
		List<OrderData<ComponentLifeCycle>> lifeCycleList = ComponentLifeCycleProvider.getInstance(contextPath)
				.getComponentLifeCycles();
		for (int i = 0; i < lifeCycleList.size(); i++) {
			OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
			try {
				lifeCycle.getData().prepareInit();
			} catch (Throwable e) {
				throw new ServletException(e);
			}
		}
		ctx.addListener(WebListener.class);
		EnumSet<DispatcherType> dispatcherType = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD,
				DispatcherType.ERROR, DispatcherType.INCLUDE);
		ctx.addFilter(ViewFilter.class.getName(), ViewFilter.class).addMappingForUrlPatterns(dispatcherType, true,
				"/*");
	}
}
