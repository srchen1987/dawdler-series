package com.anywide.controller.user;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.anywide.bean.user.User;
import com.anywide.dawdler.clientplug.annotation.RequestMapping;
import com.anywide.dawdler.clientplug.annotation.RequestMapping.ViewType;
import com.anywide.dawdler.clientplug.web.TransactionController;
import com.anywide.dawdler.core.annotation.RemoteService;
import com.anywide.service.user.UserService;
/**
 * Controller 需要继承TransactionController
 *
 */
//value对应访问地址 
@RequestMapping(value="/user")
public class UserController extends TransactionController{

	@RemoteService
	private UserService userService;
	
	/**
	 * 查询列表带分页
	 * @throws Exception
	 */
	//viewType 有三种展现方式用于前端显示 json velocity jsp 
	@RequestMapping(value="/list.html" ,viewType=ViewType.velocity)
	public void userList() throws Exception  {
		System.out.println("hello world");
		
		int pageon = paramInt("pageon");
		int row = paramInt("row",10);
		Map<String,Object>  map=new HashMap<String,Object>();
		map.put("pageon", pageon);
		map.put("row", row);
		Map<String,Object> resultMap = userService.selectList(map);
		setData(resultMap);
		setTemplatePath("user/list.html");
	}
	
	//viewType 有三种展现方式用于前端显示 json velocity jsp 
	@RequestMapping(value="/listJson.html" ,viewType=ViewType.json)
	public void userListJson() throws Exception  {
		System.out.println("hello world");
		
		int pageon = paramInt("pageon");
		int row = paramInt("row",10);
		Map<String,Object>  map=new HashMap<String,Object>();
		map.put("pageon", pageon);
		map.put("row", row);
		Map<String,Object> resultMap = userService.selectList(map);
		setData(resultMap);
	}
	
	
	/**
	 * 查询详情
	 * @throws SQLException
	 */
	@RequestMapping(value="/init.html")
	public void init() throws SQLException{
		int userId = paramInt("userid");
		Map<String, Object> result = userService.selectInit(userId);
		setData(result);
		setTemplatePath("user/init.html");
	}
	
	/**
	 * 保存或修改数据
	 * @throws SQLException
	 */
	@RequestMapping(value="/save.do",viewType=ViewType.json,generateValidator=true)
	public void add() throws SQLException {
		User user = paramClass(User.class);
		Map<String, Object> result = userService.insert(user);
		setData(result);
	}
	
	/**
	 * 删除数据
	 * @throws SQLException
	 */
	@RequestMapping(value="/del.do",viewType=ViewType.json)
	public void del() throws SQLException {
		int userId = paramInt("userid");
		Map<String, Object> result = userService.delete(userId);
		setData(result);
		setTemplatePath("user/list.html");
	}
}
