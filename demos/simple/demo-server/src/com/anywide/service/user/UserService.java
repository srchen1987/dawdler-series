package com.anywide.service.user;

import java.sql.SQLException;
import java.util.Map;

import com.anywide.bean.user.User;
import com.anywide.dawdler.core.annotation.RemoteService;
/**
 * 
 * @author lws
 *
 */
@RemoteService
public interface UserService {

	/**
	 * 查询列表
	 * @param map
	 * @return
	 * @throws SQLException
	 */
	public Map<String,Object> selectList(Map<String,Object> map) throws SQLException;
	
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
}
