# dawdler-db-core

## 模块介绍

事务核心模块.提供事务管理器,提供与spring基本一致的事务应用,支持读写分离.

### 1. pom中引入依赖

```xml
<groupId>club.dawdler</groupId>
<artifactId>dawdler-db-core</artifactId>
```

### 2. DBTransaction注解

@DBTransaction应用到Service的实现方法上,注意不支持远程调用传播(一般也不建议使用服务端调用服务端).

```java
public @interface DBTransaction {

 MODE mode() default MODE.deferToConfig;
// MODE.forceReadOnWrite, // 强制读从写连接上，在做读写分离时需要根据插入数据做业务不能保证从库数据的实时性可以采用这种方式
// MODE.deferToConfig, // 根据本方法的注解定义,默认为此配置,支持读写分离.
// MODE.readOnly// 只传入读连接

 READ_CONFIG readConfig() default READ_CONFIG.idem;
// READ_CONFIG.idem, // 同上层定义
// READ_CONFIG.deferToConfig// 根据本方法的注解定义

 Class<? extends Throwable>[] noRollbackFor() default {};//指定的异常不回滚,注意是不回滚.

 Propagation propagation() default Propagation.REQUIRED;
//  REQUIRED(TransactionDefinition.PROPAGATION_REQUIRED),// 支持当前事务，如果当前没有事务，就新建一个事务。
//  SUPPORTS(TransactionDefinition.PROPAGATION_SUPPORTS),// 支持当前事务，如果当前没有事务，就以非事务方式执行。
//  MANDATORY(TransactionDefinition.PROPAGATION_MANDATORY),// 支持当前事务，如果当前没有事务，就抛出异常。
//  REQUIRES_NEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW),// 新建事务，如果当前存在事务，把当前事务挂起。
//  NOT_SUPPORTED(TransactionDefinition.PROPAGATION_NOT_SUPPORTED),// 以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。
//  NEVER(TransactionDefinition.PROPAGATION_NEVER),// 以非事务方式执行，如果当前存在事务，则抛出异常。
//  NESTED(TransactionDefinition.PROPAGATION_NESTED);// 如果当前存在事务，则在嵌套事务内执行。如果当前没有事务，则进行与PROPAGATION_REQUIRED类似的操作。

 Isolation isolation() default Isolation.DEFAULT;//数据库的隔离级别
//  DEFAULT(TransactionDefinition.TRANSACTION_DEFAULT), //默认隔离级别
//  READ_UNCOMMITTED(TransactionDefinition.TRANSACTION_READ_UNCOMMITTED),//未提交读
//  READ_COMMITTED(TransactionDefinition.TRANSACTION_READ_COMMITTED),//读已提交 oracle默认
//  REPEATABLE_READ(TransactionDefinition.TRANSACTION_REPEATABLE_READ),// 可重复读 mysql 默认
//  SERIALIZABLE(TransactionDefinition.TRANSACTION_SERIALIZABLE);//序列化读


 int timeOut() default -1;//超时时间,当设置此值并小于queryTimeout时生效
  /*  逻辑如下：
  *  if (queryTimeout == null || queryTimeout == 0 || transactionTimeout < queryTimeout) {
  *   statement.setQueryTimeout(transactionTimeout);
  *  }
  * mysql驱动代码如下：
  *  public int getQueryTimeout() throws SQLException {
  *  synchronized (checkClosed().getConnectionMutex()) {
  *   return  getTimeoutInMillis() / 1000; 
  *  } 
  *  }
  */
}
```

### 3. 数据源配置

数据源支持配置在本地properties配置文件,也支持配置在统一配置中心.(优先找本地配置文件,支持多环境配置.)

示例:

userDataSource_read.properties

```properties
type: com.zaxxer.hikari.HikariDataSource
jdbcUrl: jdbc:mysql://127.0.0.1:3306/mydb?characterEncoding=utf8&amp;useSSL=false
driverClassName: com.mysql.cj.jdbc.Driver
username: root
password: 
maximumPoolSize: 20
minimumPoolSize: 2
 ```

参数说明:

type 数据库连接池类,其余属性为连接池中的属性,不同池有不同实现.

### 4. 数据源规则配置

dawdler支持读写分离的配置,由datasource-expression节点来进行配置.

```xml
 <datasource-expressions>
  <datasource-expression id="userDataSource"
  latent-expression="write=[userDataSource_write],read=[userDataSource_read|userDataSource_read1]" />
  <!--数据源表达式配置 id为标识 latent_expression为读写配置 其中write为写连接, read为读连接. 读连接可以配置多个用|分开,以轮询方式调用,示例中的userDataSource_write,userDataSource_read,userDataSource_read1为数据源配置的id.注意优先找本项目的配置. -->
 </datasource-expressions>
```

### 5. 数据源绑定服务配置

通过节点decision指定包中的service使用配置的数据源.

```xml
<decisions>
  <decision mapping="com.anywide.demo.user.service.impl" latent-expression-id="userDataSource" />
  <!-- mapping为指定的包,指定的包下所有的service实现类都会采用latent-expression-id所配置的规则,优先级高于antpath匹配规则 -->

  <!-- 也可以采用antpath模式 -->
  <decision mapping="com.anywide.demo.**.service.impl" latent-expression-id="userDataSource" />
  <!-- mapping支持antpath,被匹配到的包下所有的service实现类都会采用latent-expression-id所配置的规则,优先级没有直接指定非antpath的高 -->
</decisions>
```

### 6. 水平分库配置

dawdler支持水平分库的配置,由SubDatabase注解来进行配置.

注意: 需要配置分库规则对应的数据源,否则会导找不到对应的数据库. 如 userDataSource_write_0,userDataSource_write_1,userDataSource_write_2. userDataSource_read_0,userDataSource_read_1,userDataSource_read_2. 如果没有用读写分离,则只需要配置userDataSource_0,userDataSource_1,userDataSource_2.

```java
/**
 * @author jackson.song
 * @version V1.0
 * 分库注解,应用于service方法中
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Inherited
public @interface SubDatabase {

    String expression();

    String configPath();

    Class<? extends SubRule> subRuleType();

}
```

参数说明:

expression: 用于指定分库参数的名称,例如:分库字段id,则该参数名称为id,同时支持对象参数,例如:分库字段为对象user,则该参数名称为user.id.

configPath: 用于指定分库规则配置文件的路径,该配置文件为yml格式(支持统一配置中心).

subRuleType: 用于指定分库规则的实现类,该类必须实现SubRule接口.


dawdler提供的分库分表规则实现类有: ConsistentHashSubRule,RemainderSubRule.有其他需求可以自行扩展.

ConsistentHashSubRule: 一致性hash算法实现的分库规则,该规则需要配置副本数量与节点列表.

示例: 

```yml
numberOfReplicas: 3
nodes: 
  - 0
  - 1
  - 2
```

RemainderSubRule: 取模算法实现的分库规则,该规则需要配置除数.

示例:

```yml
divisor: 3
```

以下演示通过用户id进行余3取模分库的例子.

userById.yml

```yml
divisor: 3
```

```java
@Service
public class UserServiceImpl implements UserService {
    @Repository
    private UserMapper userMapper;

    @Override
    @DBTransaction(mode=MODE.readOnly)
    @SubDatabase(expression = "id",configPath = "userById",subRuleType = RemainderSubRule.class)
    public BaseResult<User> selectByPrimaryKey(Integer id) {
        User user = userMapper.selectByPrimaryKey(id);
        return new BaseResult<>(user);
    }

    @Override
    @DBTransaction(mode=MODE.readOnly)
    @SubDatabase(expression = "user.id",configPath = "userById",subRuleType = RemainderSubRule.class)
    public BaseResult<User> selectByPrimaryKey(User user) {
        User result = userMapper.selectByPrimaryKey(user);
        return new BaseResult<>(result);
    }
}
```
