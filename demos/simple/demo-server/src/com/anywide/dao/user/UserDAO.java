package com.anywide.dao.user;
import java.sql.SQLException;
import java.util.List;
import com.anywide.bean.user.User;
import com.anywide.dawdler.serverplug.db.dao.SuperDAO;
import com.anywide.dawdler.serverplug.load.bean.Page;

/**
 * 继承SuperDAO获取公用DAO 
 * 也可在此类扩展
 *
 */
public class UserDAO extends SuperDAO{

	/**
	 * 列表
	 * @param page
	 * @return
	 * @throws SQLException
	 */
	public List<User> selectUserList(Page page) throws SQLException{
		String countsql = "SELECT count(1) FROM user";
		String sql = "SELECT * FROM user";
		List<User> userList = queryListPagePrepare(countsql, sql, page.getPageon(), page.getRow(),page, User.class);
		return userList;
	}
	
	/**
	 * 详情
	 * @param userId
	 * @return
	 * @throws SQLException
	 */
	public User selectUser(int userId) throws SQLException{
		String sql = "SELECT  * FROM user where userid = ?";
		User user = queryObjectPrepare(sql, User.class, userId);
		return user;
	}
	
	/**
	 * 添加
	 * @param userName
	 * @param age
	 * @return
	 * @throws SQLException
	 */
	public int insertUser(User user) throws SQLException{
		int count = insertPrepare("insert into user (username,age) values (?,?)", user.getUsername(),user.getAge());
		return count; 
	} 
	
	/**
	 * 修改
	 * @param userId
	 * @param userName
	 * @param age
	 * @return
	 * @throws SQLException
	 */
	public int updateUser(User user) throws SQLException{
		int count = updatePrepare("update user set username=?,age=? where userid = ?",user.getUsername(),user.getAge(),user.getUserid());
		return count;
	}
	
	/**
	 * 获取用户是否存在
	 * @param userId
	 * @return
	 * @throws SQLException
	 */
	public int selectExistCount(int userId) throws SQLException {
		String countSql = "select count(1) from user where userid=?";
		int count = queryCountPrepare(countSql,userId);
		return count;
	}
	
	/**
	 * 删除
	 * @param userId
	 * @return
	 * @throws SQLException
	 */
	public int deleteUser(int userId) throws SQLException {
		String sql = "DELETE FROM `user` WHERE `userid`=?";
		int count = deletePrepare(sql, userId);
		return count;
	}
}
