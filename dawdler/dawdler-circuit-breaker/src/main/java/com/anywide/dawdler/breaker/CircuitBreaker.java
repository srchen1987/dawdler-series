package com.anywide.dawdler.breaker;
import com.anywide.dawdler.breaker.state.CircuitBreakerState;
/**
 * 
 * @Title:  CircuitBreaker.java
 * @Description:    熔断器接口
 * @author: jackson.song    
 * @date:   2018年3月16日  
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public interface CircuitBreaker {
	public boolean check();
	
	public void fail();
	
	public void pass();
	
	public CircuitBreakerState getState();
	
}
