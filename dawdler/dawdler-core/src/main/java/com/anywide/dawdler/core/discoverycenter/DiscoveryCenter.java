package com.anywide.dawdler.core.discoverycenter;

import java.util.List;
/**
 * 
 * @Title:DiscoveryCenter.java 
 * @Description:代替 PropertiesCenter.java 当年写的着急，先用zk实现 不考虑扩展其他的，目前支持用这个接口来扩展
 * @author: jackson.song
 * @date: 2018年08月13日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public interface DiscoveryCenter {
	public static final String OFFLINESTATUS="Offline";
	public List<String> getServiceList(String path) throws Exception;
	public void init()throws Exception;
	public void destroy()throws Exception;
	public boolean addProvider(String path,String value) throws Exception;
	public boolean updateProvider(String path,String value) throws Exception;
}
