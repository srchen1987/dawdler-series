package com.anywide.dawdler.breaker.state;
import java.util.concurrent.atomic.AtomicReference;
import com.anywide.dawdler.breaker.SlideTimeWindows;
import com.anywide.dawdler.util.JVMTimeProvider;
/**
 * 
 * @Title:  LocalCircuitBreadkerState.java
 * @Description:    熔断器状态（LOCAL）
 * @author: jackson.song    
 * @date:   2018年3月16日  
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class LocalCircuitBreadkerState implements CircuitBreakerState {
	private volatile long startTime;
	private SlideTimeWindows stw;
	public LocalCircuitBreadkerState(int intervalInMs, int windowsCount) {
		stw = new SlideTimeWindows(intervalInMs, windowsCount);
	}

	public SlideTimeWindows getStw() {
		return stw;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	private final AtomicReference<State> state = new AtomicReference<State>(State.CLOSE);

	public AtomicReference<State> getState() {
		return state;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	@Override
	public void resetStartTime() {
		startTime = JVMTimeProvider.currentTimeMillis();
	}

}
