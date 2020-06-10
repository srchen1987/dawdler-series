package com.anywide.dawdler.breaker.metric;
/**
 * 
 * @Title:  Metric.java
 * @Description:    度量接口
 * @author: jackson.song    
 * @date:   2018年3月16日  
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public interface Metric {
	public void totalIncrt();

	public void failIncrt();

	public long totalCount();

	public long failCount();
	
	public long getStartTime();
	
	public void reset(long startTime);
	
	public long restStartTime();
}
