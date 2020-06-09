package com.anywide.service.user;

import java.sql.SQLException;
import java.util.Map;

import com.anywide.bean.user.User;
import com.anywide.dawdler.core.annotation.CircuitBreaker;
import com.anywide.dawdler.core.annotation.RemoteService;
 
@RemoteService
public interface UserService {

	/**
	 * 查询列表
	 * @param map
	 * @return
	 * @throws SQLException
	 */
	@CircuitBreaker(breakerKey = "test",fallbackMethod = "selectListFallback")
	public Map<String,Object> selectList(Map<String,Object> map) throws SQLException;
	
	
	 static public Map<String,Object> selectListFallback(Map<String,Object> map) throws SQLException{
		System.out.println("invoke..."+map);
		map.put("fallback","true");
		return map;
	}
	/**
	 * 查询详情
	 * @param map
	 * @return
	 * @throws SQLException
	 */
	public Map<String,Object> selectInit(int userId) throws SQLException;
	
	/**
	 * 保存或修改数据
	 * @param map
	 * @return
	 * @throws SQLException
	 */
	public Map<String,Object> insert(User user) throws SQLException;
	
	/**
	 * 删除数据
	 * @param map
	 * @return
	 * @throws SQLException
	 */
	public Map<String,Object> delete(int userId) throws SQLException;
	
	public String hello(int userId) throws SQLException;
	
}
