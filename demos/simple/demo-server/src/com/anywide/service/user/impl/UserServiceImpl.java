package com.anywide.service.user.impl;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import com.anywide.bean.user.User;
import com.anywide.dao.user.UserDAO;
import com.anywide.dawdler.serverplug.annotation.DBTransaction;
import com.anywide.dawdler.serverplug.annotation.DBTransaction.MODE;
import com.anywide.dawdler.serverplug.annotation.Propagation;
import com.anywide.dawdler.serverplug.load.bean.Page;
import com.anywide.service.user.UserService;
public class UserServiceImpl implements UserService{
	@Resource
	private UserDAO userDAO;
	@Override
	@DBTransaction(mode=MODE.forceReadOnWrite,propagation=Propagation.REQUIRED)
	public Map<String, Object> selectList(Map<String, Object> map) throws SQLException {
		int pageon = Integer.parseInt(map.get("pageon")+"");
		int row  = Integer.parseInt(map.get("row")+"");
		Page page = new Page(pageon, row);
		List<User> userList = userDAO.selectUserList(page);
		Map<String,Object> resultMap =new HashMap<String,Object>();
		resultMap.put("userList", userList);
		resultMap.put("page", page);
		return resultMap;
	}

	@Override
	@DBTransaction(mode=MODE.forceReadOnWrite,propagation=Propagation.REQUIRED)
	public Map<String, Object> selectInit(int userId) throws SQLException {
		User user = userDAO.selectUser(userId);
		Map<String,Object> resultMap =new HashMap<String,Object>();
		resultMap.put("user", user);
		return resultMap;
	}

	@Override
	@DBTransaction(mode=MODE.forceReadOnWrite,propagation=Propagation.REQUIRED)
	public Map<String, Object> insert(User user) throws SQLException {
		int count = 0;
		if(user.getUserid()>0)count = userDAO.selectExistCount(user.getUserid());
		if(count > 0) {
			userDAO.updateUser(user);
		}else {
			userDAO.insertUser(user);
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("status", 1);
		result.put("message", "操作成功!");
		return result;
	}

	@Override
	@DBTransaction(mode=MODE.forceReadOnWrite,propagation=Propagation.REQUIRED)
	public Map<String, Object> delete(int userId) throws SQLException {
		Map<String, Object> result = new HashMap<String, Object>();
		int count = userDAO.deleteUser(userId);
		if(count <= 0) {
			result.put("status", 0);
			result.put("message", "操作失败!");
			return result; 
		}
		result.put("status", 1);
		result.put("message", "操作成功!");
		return result;
	}

}
