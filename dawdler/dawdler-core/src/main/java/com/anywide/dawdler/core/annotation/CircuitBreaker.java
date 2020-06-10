package com.anywide.dawdler.core.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface CircuitBreaker {
	
	
	
	public String breakerKey() default "";
	
	/**
	 * 
	 * @Title: intervalInMs   
	 * @Description: 统计时长 intervalInMs/windowsCount 最好为整数
	 * @return: int    
	 * @throws 
	 * @author: srchen     
	 * @date:   2018年03月10日
	 */
	public int intervalInMs() default 3000;
	
	/**
	 * 
	 * @Title: windowsCount   
	 * @Description: 窗口大小
	 * @return: int    
	 * @throws 
	 * @author: srchen     
	 * @date:   2018年03月10日
	 */
	public int windowsCount() default 2; 
	
	/**
	 * 
	 * @Title: sleepWindowInMilliseconds   
	 * @Description: 熔断器打开后，所有的请求都会直接失败，熔断器打开时会在经过一段时间后就放行一条请求成功则关闭熔断器，此配置就为指定的这段时间，默认值是 5000。
	 * @return: int    
	 * @throws 
	 * @author: srchen     
	 * @date:   2018年03月10日 
	 */
	public int sleepWindowInMilliseconds() default 5000;


	/**
	 * 
	 * @Title: requestVolumeThreshold   
	 * @Description: 启用熔断器功能窗口时间内的最小请求数。
	 * @return: int    
	 * @throws 
	 * @author: srchen     
	 * @date:   2018年03月10日 
	 */

	public int requestVolumeThreshold() default 5; 


	/**
	 * 
	 * @Title: errorThresholdPercentage   
	 * @Description: 错误百分比，默认为40%  达到40%的错误率会触发熔断（大于requestVolumeThreshold）
	 * @return: double    
	 * @throws 
	 * @author: srchen     
	 * 
	 * 
	 * @date:   2018年03月10日  
	 */
	public double errorThresholdPercentage() default 0.4; 
	/**
	 * 
	 * @Title: fallbackMethod   
	 * @Description: 熔断后执行的方法 参数与返回值与执行的方法相同
	 * @return: String    
	 * @throws 
	 * @author: srchen     
	 * @date:   2018年03月10日  
	 */
	public String fallbackMethod() default "";

	
	
	
 
	

}
