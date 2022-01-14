# dawdler-server-plug-dao

## 模块介绍

通过反射实现的一套简易操作数据库的一套组件.支持基本的增删改查,读写分离.

### 1. pom中引入依赖

```xml
<groupId>dawdler</groupId>
<artifactId>dawdler-server-plug-dao</artifactId>
```

### 2. SupperDAO常用api

query系列为查询

insert系列为插入

update系列为修改

delete系列为删除

Object ... values 可变参数是有序的对应sql中预处理的?号.

Class\<T\> c 传入的参数为数据库实体对象entity.

insertMap 第一个参数为表名,第二个参数Map的key为表字段,value为值.

insertPrepareGetKey与insertMapGetKey 方法会返回插入信息生成的主键,主键类型必须为整型.

```java
 <T extends Object> List<T> queryList(String sql, Class<T> c) throws SQLException;

 <T extends Object> List<T> queryListPrepare(String sql, Class<T> c, Object... values) throws SQLException;

 <T extends Object> T queryObject(String sql, Class<T> c) throws SQLException;

 <T extends Object> T queryObjectPrepare(String sql, Class<T> c, Object... values) throws SQLException;

 List<Map<String, Object>> queryListMaps(String sql) throws SQLException;

 List<Map<String, Object>> queryListMapsPrepare(String sql, Object... values) throws SQLException;

 int update(String sql) throws SQLException;

 int updatePrepare(String sql, Object... values) throws SQLException;

 int insert(String sql) throws SQLException;

 int insertPrepare(String sql, Object... values) throws SQLException;

 long insertPrepareGetKey(String sql, Object... values) throws SQLException;

 int insertMap(String tableName, Map<String, Object> data) throws SQLException;

 long insertMapGetKey(String tableName, Map<String, Object> data) throws SQLException;

 int delete(String sql) throws SQLException;

 int deletePrepare(String sql, Object... values) throws SQLException;

 int queryCount(String sql) throws SQLException;

 int queryCountPrepare(String sql, Object... values) throws SQLException;
```

### 3. services-config.xml中mybatis的配置文件说明

services-config.xml是服务端核心配置文件,包含了数据源定义,指定目标包定义数据源,读写分离配置,服务端配置.

本模块中涉及mybatis的配置在mybatis的子节点mapper的值中,支持antPath语法进行配置.

示例：

```xml
<mybatis>
 <mappers>
  <mapper>classpath*:com/anywide/shop/*/mapper/xml/*.xml</mapper>
 </mappers>
 </mybatis>
```

### 3. 注入DAO

编写自定义的DAO继承SupperDAO即可，如UserDAO。

在service层通过@Repository注入dao,即可使用注入的dao.

示例：

```java
public class UserDAO extends SuperDAO{

 public int insertUser(User user) throws SQLException{
  return insertPrepare("insert into t_user(username,`password`,age)", user.getUsername(), user.getPassword(), user.getAge());
 }

}
```

```java
public class UserServiceImpl implements UserService{

 @Repository
 UserDAO userDAO;
 
 @Override
 @DBTransaction
 public boolean addUser(User user) {
  return userDAO.insertUser(user) > 0;
 }
```

### 4. 获取读连接与写连接

需要用到Connection时可以通过SupperDAO中的getReadConnection来获取读连接,getWriteConnection来获取写连接.

底层是通过LocalConnectionFactory.getReadConnection()与LocalConnectionFactory.getWriteConnection()实现.

关于配置读写分离可参考dawdler-server-plug-db模块的读写分离配置.
