package com.anywide.dawdler.breaker;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import com.anywide.dawdler.breaker.metric.Metric;
import com.anywide.dawdler.breaker.state.CircuitBreakerState;
import com.anywide.dawdler.breaker.state.CircuitBreakerState.State;
import com.anywide.dawdler.breaker.state.LocalCircuitBreadkerState;
import com.anywide.dawdler.util.JVMTimeProvider;

/**
 * 
 * @Title:  LocalCircuitBreaker.java
 * @Description:    本地熔断器实现
 * @author: jackson.song    
 * @date:   2018年3月16日  
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class LocalCircuitBreaker implements CircuitBreaker {
	private int sleepWindowInMilliseconds = 3000;
	private int requestVolumeThreshold = 5;
	private double errorThresholdPercentage = 0.3;
	private CircuitBreakerState circutiBreakerState;
	
	public LocalCircuitBreaker(com.anywide.dawdler.core.annotation.CircuitBreaker cb) {
		this.circutiBreakerState = new LocalCircuitBreadkerState(cb.intervalInMs(),cb.windowsCount());
		this.sleepWindowInMilliseconds = cb.sleepWindowInMilliseconds();
		this.requestVolumeThreshold = cb.requestVolumeThreshold();
		this.errorThresholdPercentage = cb.errorThresholdPercentage();
	}

	@Override
	public boolean check() {
		if (circutiBreakerState != null) {
			AtomicReference<State> state = circutiBreakerState.getState();
			switch (state.get()) {
			case CLOSE:
				return true;
			case OPEN:
				if (circutiBreakerState.getStartTime() + sleepWindowInMilliseconds < JVMTimeProvider.currentTimeMillis()) {
					return state.compareAndSet(State.OPEN, State.HALF_OPEN);
				}
				return false;
			default:
				return false;
			}

		}
		return true;
	}

	@Override
	public void fail() {
		AtomicReference<State> state = circutiBreakerState.getState();
		if (state.get() == State.CLOSE) {
			List<Metric> list = circutiBreakerState.getStw().listCurrentMetrics();
			int totalCount = 0;
			int failCount = 0;
			for (Metric metric : list) {
				totalCount += metric.totalCount();
				failCount += metric.failCount();
			}
			if (totalCount < requestVolumeThreshold)
				return;
			if ((double) failCount / (double) totalCount >= errorThresholdPercentage && state.compareAndSet(State.CLOSE, State.OPEN))
				circutiBreakerState.resetStartTime();
		} else if (state.get() == State.HALF_OPEN && state.compareAndSet(State.HALF_OPEN, State.OPEN)) {
			circutiBreakerState.resetStartTime();
		}

	}

	@Override
	public void pass() {
		AtomicReference<State> state = circutiBreakerState.getState();
		if (state.get() == State.HALF_OPEN) {
			state.compareAndSet(State.HALF_OPEN, State.CLOSE);
			circutiBreakerState.resetStartTime();
		}
	}

	@Override
	public CircuitBreakerState getState() {
		return circutiBreakerState;
	}
}
