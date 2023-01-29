import com.anywide.dawdler.breaker.filter.CircuitBreakerFilter;
import com.anywide.dawdler.client.filter.DawdlerClientFilter;

module dawdler.circuit.breaker {
	requires java.base;
	requires dawdler.util;
	requires dawdler.client;
	requires dawdler.core;
	requires org.slf4j;
	uses DawdlerClientFilter;
	provides DawdlerClientFilter with CircuitBreakerFilter;
}