package com.anywide.dawdler.breaker.metric;

import java.util.concurrent.atomic.LongAdder;
import com.anywide.dawdler.util.JVMTimeProvider;
/**
 * 
 * @Title:  MetricBase.java
 * @Description:    基础度量接口
 * @author: jackson.song    
 * @date:   2018年3月16日  
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class MetricBase implements Metric {

	private LongAdder total = new LongAdder();

	private LongAdder fail = new LongAdder();
	
	private long startTime;
	public MetricBase(long startTime) {
		this.startTime = startTime;
	}
	

	public void totalIncrt() {
		total.increment();
	}

	public void failIncrt() {
		fail.increment();
	}

	public long totalCount() {
		return total.longValue();
	}

	public long failCount() {
		return fail.longValue();
	}
	
	@Override
	public long getStartTime() {
		return startTime;
	}

	@Override
	public long restStartTime() {
		return JVMTimeProvider.currentTimeMillis();
	}

	@Override
	public void reset(long startTime) {
		total.reset();
		fail.reset();
		this.startTime=startTime;
	}

}
