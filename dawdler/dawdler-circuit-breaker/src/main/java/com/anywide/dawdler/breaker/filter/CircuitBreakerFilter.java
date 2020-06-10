package com.anywide.dawdler.breaker.filter;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.breaker.CircuitBreaker;
import com.anywide.dawdler.breaker.LocalCircuitBreaker;
import com.anywide.dawdler.breaker.metric.Metric;
import com.anywide.dawdler.breaker.state.CircuitBreakerState;
import com.anywide.dawdler.client.filter.DawdlerClientFilter;
import com.anywide.dawdler.client.filter.FilterChain;
import com.anywide.dawdler.client.filter.RequestWrapper;
import com.anywide.dawdler.core.bean.RequestBean;
public class CircuitBreakerFilter implements DawdlerClientFilter {
	private static Logger logger = LoggerFactory.getLogger(DawdlerClientFilter.class);
	private ConcurrentHashMap<String, CircuitBreaker> breakers = new ConcurrentHashMap<String, CircuitBreaker>();

	@Override
	public Object doFilter(RequestBean request, FilterChain chain) throws Exception {
		RequestWrapper rw = (RequestWrapper) request;
		com.anywide.dawdler.core.annotation.CircuitBreaker cb = rw.getCircuitBreaker();
		if (cb == null)
			return chain.doFilter(request);
		String unique = "".equals(cb.breakerKey())
				? (request.getPath() + request.getServiceName() + request.getMethodName())
				: cb.breakerKey();
		CircuitBreaker circuitBreaker = breakers.get(unique);
		if (circuitBreaker == null) {
			circuitBreaker = new LocalCircuitBreaker(cb);
			CircuitBreaker pre = breakers.putIfAbsent(unique, circuitBreaker);
			if (pre != null)
				circuitBreaker = pre;
		}
		CircuitBreakerState state = circuitBreaker.getState();
		Metric metric = state.getStw().currentMetrics();
		metric.totalIncrt();
		if (circuitBreaker.check()) {
			try {
				Object result = chain.doFilter(request);
				circuitBreaker.pass();
				return result;
			} catch (Exception e) {
				metric.failIncrt();
				circuitBreaker.fail();
				throw e;
			}
		} else {
			Class<?> c = rw.getProxyInterface();
			String fallbackMethod = cb.fallbackMethod();
			if (c != null && !"".equals(fallbackMethod)) {
				try {
					Method method = c.getMethod(fallbackMethod,request.getTypes());
					return method.invoke(null, request.getArgs());
				} catch (Exception e) {
					logger.error("", e);
					throw e;
				}
//				ReflectionUtil.getMethodAccess(c).getIndex(methodName, paramTypes)
//				methodAccess.getIndex(methodName, paramTypes);
			}

			throw new IllegalAccessException(
					"CircuitBreaker State : " + state.getState().get().name() + ", unique : " + unique);
		}
	}
	

}
