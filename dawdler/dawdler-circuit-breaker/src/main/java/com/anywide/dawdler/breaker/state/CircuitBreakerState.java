package com.anywide.dawdler.breaker.state;
import java.util.concurrent.atomic.AtomicReference;
import com.anywide.dawdler.breaker.SlideTimeWindows;
/**
 * 
 * @Title:  CircuitBreakerState.java
 * @Description:    熔断器状态
 * @author: jackson.song    
 * @date:   2018年3月16日  
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public interface CircuitBreakerState {

	public AtomicReference<State> getState();
	
	public long getStartTime();
	
	public void resetStartTime();

	public SlideTimeWindows getStw();
	
	public enum State {
		CLOSE, OPEN, HALF_OPEN,
	}

}
