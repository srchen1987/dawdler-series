# dawdler-server-plug-db

## 模块介绍

事务管理器模块,提供与spring基本一致的事务应用,支持读写分离.

### 1. pom中引入依赖

```xml
<groupId>dawdler</groupId>
<artifactId>dawdler-server-plug-db</artifactId>
```

### 2. DBTransaction注解

@DBTransaction应用到Service的实现方法上,注意不支持远程调用传播(一般也不建议使用服务端调用服务端).

```java
public @interface DBTransaction {

 MODE mode() default MODE.deferToConfig;
// MODE.forceReadOnWrite, // 强制读从写连接上，在做读写分离时需要根据插入数据做业务不能保证从库数据的实时性可以采用这种方式
// MODE.deferToConfig, // 根据本方法的注解定义
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

dawdler服务器支持jndi方式的数据源配置,部署在本服务器下所有服务都可以使用.参考[]()

在services-configlxml中本服务的数据源配置：

```xml
<datasources>
  <!-- 数据源配置 根据不同的数据库连接池进行配置即可 -->
  <datasource id="userDataSource_write"  
   code="com.mchange.v2.c3p0.ComboPooledDataSource"><!-- id 唯一标识 -->
   <attribute name="jdbcUrl">jdbc:mysql://127.0.0.1:3306/welife_community?characterEncoding=utf8&amp;useSSL=false
   </attribute>
   <attribute name="driverClass">com.mysql.cj.jdbc.Driver</attribute>
   <attribute name="user">root</attribute>
   <attribute name="password"></attribute>
   <attribute name="acquireIncrement">10</attribute>
   <attribute name="checkoutTimeout">30000</attribute>
   <attribute name="initialPoolSize">5</attribute>
   <attribute name="maxIdleTime">7200</attribute>
   <attribute name="maxIdleTimeExcessConnections">1800</attribute>
   <attribute name="maxPoolSize">200</attribute>
  </datasource>
 </datasources>
 ```

### 4. 数据源规则配置

dawdler支持读写分离的配置,由datasource-expression节点来进行配置.

```xml
 <datasource-expressions>
  <datasource-expression id="userDataSource"
  latent-expression="write=[userDataSource_write],read=[userDataSource_write|userDataSource_write]" />
  <!--数据源表达式配置 id为标识 latent_expression为读写配置 其中write为写连接 read为读连接 读连接可以配置多个用|分开,以轮询方式调用,示例中的userDataSource_write为数据源配置的id.注意优先找本项目的配置,如果找不到会根据id查找jndi中的配置. -->
 </datasource-expressions>
```

### 5. 数据源绑定服务配置

通过节点decision指定包中的service使用配置的数据源.

```xml
<decisions>
  <decision mapping="com.anywide.yyg.user.service.impl" latent-expression="userDataSource" />
  <!-- mapping为指定的包,指定的包下所有的service实现类都会采用latent-expression所配置的规则,优先级高于antpath匹配规则 -->
  <decision mapping="com.anywide.yyg.**.service.impl" latent-expression="userDataSource" />
  <!-- mapping支持antpath,被匹配到的包下所有的service实现类都会采用latent-expression所配置的规则,优先级没有直接指定非antpath的高 -->

</decisions>
```
