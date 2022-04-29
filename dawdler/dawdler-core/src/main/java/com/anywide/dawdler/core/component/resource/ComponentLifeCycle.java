
package com.anywide.dawdler.core.component.resource;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ComponentLifeCycle.java
 * @Description 组件(redis,es,rabbitmq)生命周期接口 初始化与销毁,
 *              web端、dawdler服务器端会在容器初始化之前、销毁后调用
 * @date 2022年4月12日
 * @email suxuan696@gmail.com
 */
public interface ComponentLifeCycle {

	default public void init() throws Throwable {
	};

	default public void destroy() throws Throwable {
	};

}
