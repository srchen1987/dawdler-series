package com.anywide.dawdler.server.context;

import com.anywide.dawdler.server.filter.RequestWrapper;

public class RpcServerContext {

	private RequestWrapper request;

	private static final InheritableThreadLocal<RpcServerContext> THREAD_LOCAL = new InheritableThreadLocal<RpcServerContext>() {
		@Override
		protected RpcServerContext initialValue() {
			return new RpcServerContext();
		}
	};

	public static RpcServerContext getContext() {
		return THREAD_LOCAL.get();
	}

	public static void removeContext() {
		THREAD_LOCAL.remove();
	}

	public void setRequest(RequestWrapper request) {
		if (this.request == null)
			this.request = request;
	}

	public RequestWrapper getRequest() {
		return request;
	}

}
