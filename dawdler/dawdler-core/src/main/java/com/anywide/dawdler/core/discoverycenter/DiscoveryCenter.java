package com.anywide.dawdler.core.discoverycenter;

import java.util.List;

/**
 * @author jackson.song
 * @version V1.0
 * @TitleDiscoveryCenter.java
 * @Description代替 PropertiesCenter.java(已删除) 当年写的着急，先用zk实现 不考虑扩展其他的，目前支持用这个接口来扩展
 * @date 2018年8月13日
 * @email suxuan696@gmail.com
 */
public interface DiscoveryCenter {
	String OFFLINESTATUS = "Offline";

	List<String> getServiceList(String path) throws Exception;

	void init() throws Exception;

	void destroy() throws Exception;

	boolean addProvider(String path, String value) throws Exception;

	boolean updateProvider(String path, String value) throws Exception;
}
