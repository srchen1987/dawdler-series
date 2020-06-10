package com.anywide.dawdler.breaker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;
import com.anywide.dawdler.breaker.metric.Metric;
import com.anywide.dawdler.breaker.metric.MetricBase;
import com.anywide.dawdler.util.JVMTimeProvider;

/**
 * 
 * @Title:  SlideTimeWindows.java
 * @Description:    滑动窗口
 * @author: jackson.song    
 * @date:   2018年3月16日  
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class SlideTimeWindows {
	private int intervalInMs;
	private int windowsCount;
	private int windowLengthInMs;
	
	private AtomicReferenceArray<Metric> array; 

	private final ReentrantLock lock = new ReentrantLock();

	public SlideTimeWindows(int intervalInMs,int windowsCount) {
		this.windowsCount=windowsCount;
		this.intervalInMs=intervalInMs;
		windowLengthInMs = intervalInMs / windowsCount;
		array = new AtomicReferenceArray<>(windowsCount);
	}

	public Metric currentMetrics() {
		long now = JVMTimeProvider.currentTimeMillis();
		int index = getCurrentIdx(now);
		while (true) {
			Metric metrics = array.get(index);
			long start = now - now % windowLengthInMs;
			if (metrics == null) {
				metrics = new MetricBase(start);
				if (array.compareAndSet(index, null, metrics))
					return metrics;
				return array.get(index);
			}
			
			if (metrics.getStartTime() == start)
				return metrics;
			else {
				if (lock.tryLock())
					try {
						metrics.reset(start);
						return metrics;
					} finally {
						lock.unlock();
					}
			}
		}
	}

	public List<Metric> listCurrentMetrics() {
		 List<Metric> list = new ArrayList<>();
	        for (int i = 0; i < array.length(); i++) {
	            Metric mb = array.get(i);
	            if (mb == null || JVMTimeProvider.currentTimeMillis() - intervalInMs > mb.getStartTime()) {
	                continue;
	            }
	            list.add(mb);
	        }
	        return list;
	}
	
	private int getCurrentIdx(long timeMillis) {
		long timeId = timeMillis / windowLengthInMs;
		return (int) (timeId % windowsCount);
	}
}
