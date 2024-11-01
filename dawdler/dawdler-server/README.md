# dawdler-server

## 模块介绍

dawdler-server 是dawdler容器端的具体实现,提供容器启动服务的监听器,过滤器,服务器配置,注入服务监听器,类加载器,aop实现.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-server</artifactId>
```

### 2. 服务器配置

#### 2.1 server-conf.xml说明

server-conf.xml 是dawdler服务器的核心配置文件.

配置文件示例：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
 <keyStore
  keyStorePath="dawdler.keystore"
  alias="srchen" password="jackson.song1948@anywide"></keyStore><!-- 
		keyStorePath 私钥路径 alias帐号 password 密码 -->
 <server host="0.0.0.0" tcp-port="9527"
  shutdownWhiteList="127.0.0.1,localhost" tcp-shutdownPort="19530"
  tcp-backlog="200" tcp-sendBuffer="163840" tcp-receiveBuffer="163840"
  tcp-keepAlive="false" tcp-noDelay="false" maxThreads="200" queueCapacity="65536" keepAliveMilliseconds="0">
 </server>
 <!-- tcp-port服务器启动端口号， shutdownWhiteList允许关闭服务的白名单， tcp-shutdownPort关闭服务器的端口号 
		maxThreads 处理业务线程池的大小 默认200，queueCapacity 线程池队列大小 默认65536，keepAliveMilliseconds 线程池中空闲线程等待工作的超时时间 默认0，其他为tcp的配置 -->
 <global-auth>
  <user username="global_user" password="global_password" />
  <user username="global_user_1" password="global_password" />
 </global-auth>
 <!-- 全局通用的用户,user节点中username属性第是用户名,password是密码. -->
 <module-auth>
  <module name="user-service">
   <user username="user1" password="user1password" />
   <user username="user2" password="user2password" />
  </module>

  <module name="order-service">
   <user username="order_user1" password="user1password" />
  </module>
 </module-auth><!-- 模块下的用户,module中的name指定模块名,user节点中username属性第是用户名,password是密码 -->

 <health-check check="on" scheme="https" port="19001" backlog="0" username="jackson" password="jackson.song">
 <elasticSearch check="on" />
 <jedis check="on" />
 <rabbit check="on" />
 <dataSource check="on" />
 <config check="on" />
 </health-check>
</config>

```

属性说明：

##### keyStore节点

是证书的私钥配置,客户端连接服务器需要账号密码,账号密码在客户端通过公钥进行加密,需要提前颁发公钥给客户端.keyStorePath 私钥路径,alias 帐号,password 密码.

##### server节点配置

```txt
host="0.0.0.0" server端的ip地址支持IPV6,IPV6="::",IPV4="0.0.0.0".dawdler会根据配置自动绑定IP,如果需要指定IP直接配置IP即可.

tcp-port="9527" 端口号

shutdownWhiteList="127.0.0.1,localhost" shutdownWhiteList允许关闭服务的白名单ip,用逗号隔开,一般无须更改.除非有远程停服务的需求.

tcp-shutdownPort="19530" 关闭服务的端口号

tcp-backlog="200" 指定socket链接队列的长度

tcp-sendBuffer="163840" TCP发送缓存区

tcp-receiveBuffer="163840" TCP接收缓存区

tcp-keepAlive="false" 保持长链

tcp-noDelay="false" 禁用纳格算法

maxThreads=200 处理业务线程池的大小
```

##### global-auth节点

用于配置整个dawdler服务器的全局用户,user节点中username属性第是用户名,password是密码,全局用户可以用于客户端调用本服务器内部部署的所有服务的认证.

##### module-auth节点

 用户配置指定模块下的用户,module中的name指定模块名,user节点中username属性第是用户名,password是密码.

##### health-check节点

用于做健康检测的配置,可以为k8s的liveness,readiness提供该服务.如果设有带(Basic Authentication)的认证,请通过head头加入Authorization头信息.

check="on" 为开启健康检测,off为关闭.关闭后不会开启http/https服务.

scheme="http" 提供http服务,支持设为https,使用keyStore中的keystore为https证书.

port="19001" 提供http/https 服务的端口号

backlog="0"  指定socket链接队列的长度,默认为0,系统级限制.

username="jackson" 用户名,未填写该属性则不开启认证模块.

password="jackson.song" 密码,未填写该属性则不开启认证模块.

uri="/health" 健康检测的uri,默认是/health.

以下为已支持健康检测的组件节点,check设为off或不填此节点则不会触发健康检测.

elasticSearch es检测

jedis redis检测

rabbit rabbitmq检测

dataSource 数据源检测

config 配置中心检测

#### 2.2 采用keytool制作证书

dawdler示例中采用keytool制作的证书,服务器端配置在server-conf文件中的keyStore节点. 客户端配置在client-conf文件中的certificatePath节点.

keytool制作的证书的命令如下：

```shell
keytool -validity 65535 -genkey -v -alias srchen -keyalg RSA -keystore dawdler.keystore -dname "CN=jackson,OU=互联网事业部,O=anywide,L=DALIAN,ST=LIAONING,c=CN" -storepass suxuan696@gmail.com -keypass jackson.song

keytool -export -v -alias srchen -keystore dawdler.keystore -storepass suxuan696@gmail.com -rfc -file dawdler.cer
```

##### keytool命令说明

Keytool是一个Java数据证书的管理工具,以下是简要说明.

-genkey 生成秘钥

-alias 别名

-keyalg 秘钥算法

-keysize 秘钥长度

-validity 有效期

-keystore 生成秘钥库的存储路径和名称

-keypass 秘钥口令

-storepass 秘钥库口令

-dname 拥有者信息,CN：姓名；OU：组织单位名称；O：组织名称；L：省/市/自治区名称；C：国家/地区代码

### 3. dawdler服务器启动销毁监听器

实现DawdlerServiceListener接口,支持@Order注解进行升序排序.contextInitialized方法是服务器初始化调用的方法 ,contextDestroyed方法是服务器销毁时调用的方法.这两个方法都是部署在dawdler服务器deploys下的服务启动与销毁时所调用的.

@ListenerConfig注解用于DawdlerServiceListener,可以标识是否为异步执行,并支持设置延迟时间.

应用场景: DawdlerServiceListener实现类会在dawdler启动和销毁时运行,可以用来初始化和销毁资源.涉及异步执行的情况是防止容器启动时初始化资源造成卡顿,延迟时间是为了控制其他资源未加载完成时可以延迟调用当前的监听器.

```java
public @interface ListenerConfig {
 long delayMsec() default 0L;// delayTime 毫秒级,只有异步执行的条件下生效

 boolean asyn() default false;// 是否为异步执行
}
```

示例1：

```java
public class UserServiceStartupListener implements DawdlerServiceListener{

 @Override
 public void contextDestroyed(DawdlerContext dawdlerContext) throws Exception {
  System.out.println("UserServiceStartupListener contextDestroyed："+dawdlerContext.getDeployClassPath());
 }

 @Override
 public void contextInitialized(DawdlerContext dawdlerContext) throws Exception {
  System.out.println("UserServiceStartupListener contextInitialized："+dawdlerContext.getDeployClassPath());
 }

}
```

示例2：

```java
//异步延迟3秒启动
@ListenerConfig(asyn=true,delayMsec=3000)
public class UserServiceAsyncStartupListener implements DawdlerServiceListener{

 @Override
 public void contextDestroyed(DawdlerContext dawdlerContext) throws Exception {
  System.out.println("UserServiceAsyncStartupListener contextDestroyed："+dawdlerContext.getDeployClassPath());
 }

 @Override
 public void contextInitialized(DawdlerContext dawdlerContext) throws Exception {
  System.out.println("UserServiceAsyncStartupListener contextInitialized："+dawdlerContext.getDeployClassPath());
 }

}
```

### 4. dawdler服务过滤器

实现DawdlerFilter接口,实现doFilter方法,支持@Order注解进行升序排序.所有服务调用都会经过过滤器.

示例：

```java
public class MyFilter implements DawdlerFilter{

 @Override
 public void doFilter(RequestBean request, ResponseBean response, FilterChain chain) throws Exception {
  chain.doFilter(request, response);
 }

}
```

### 5. dawdler服务创建监听器

实现DawdlerServiceCreateListener接口,实现create方法.支持@Order注解进行升序排序.服务实体类被初始化会调用此方法,单例情况下会在服务器启动时创建,如果是多例情况下则在被调用时调用.是否是单例是在服务端的@Service注解中进行设置,single默认为true是单例.

dawdler-server-plug-mybatis中使用InjectServiceCreateListener的示例：

```java
public class InjectServiceCreateListener implements DawdlerServiceCreateListener {
 private static final Logger logger = LoggerFactory.getLogger(InjectServiceCreateListener.class);
 private SqlSession sqlSession = SingleSqlSessionFactory.getInstance().getSqlSession();

 @Override
 public void create(Object service, boolean single, DawdlerContext dawdlerContext) {
  inject(service, dawdlerContext);
 }

 private void inject(Object service, DawdlerContext dawdlerContext) {
    Field[] fields = service.getClass().getDeclaredFields();
    for (Field field : fields) {
    Resource resource = field.getAnnotation(Resource.class);
    if (!field.getType().isPrimitive()) {
        Class<?> serviceClass = field.getType();
        field.setAccessible(true);
        try {
        if (resource != null && serviceClass.isInterface()) {
        field.set(service, sqlSession.getMapper(serviceClass));
        }
        } catch (Exception e) {
        logger.error("", e);
        }
    }
    }
 }
}
```

### 6. aop使用方式

dawdler的aop支持采用aspjectJ来实现,没有采用Load-time weaving和cglib(spring的实现)方式.

适用范围：dawdler服务端部署的所有类

示例(拦截ServiceImpl)：

注意: 以下两个文件都需要在服务端创建

1、创建META-INF\aop.xml

```xml
<aspectj>
  <aspects>
    <aspect name="com.anywide.yyg.user.aop.UserServiceAspect"/>
  </aspects>
</aspectj>
```

2、创建com.anywide.yyg.user.controller.UserControllerAspect

```java
@Aspect
public class UserServiceAspect {

 @Around("execution(*  com.anywide.yyg.user.service.impl .*.selectUserList(..))")
 public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
  System.out.println("start ...\tlogAround" + pjp.getSignature().getName());
  Object o = null;
  try {
   o = pjp.proceed();
  } catch (Throwable t) {
   throw t;
  }
  Object[] args = pjp.getArgs();
  System.out.println("over:\t" + args);
  return o;
 }
}
```

dawdler-server-plug-mybatis中的读写分离也是基于aop实现的,请参考[通过aop切换数据库连接](../dawdler-server-plug-mybatis/src/main/java/com/anywide/dawdler/serverplug/db/mybatis/aspect/SwitchConnectionAspect.java).

### 7. 健康检测

dawdler服务端提供健康检测功能,uri为/health,参考[health-check节点](#health-check节点)配置.

示例:

```shell
wget https://192.168.43.137:19001/health
```

返回:

```json

{"status":"UP","chain-service":{"status":"UP","rabbit":{"status":"UP"},"jedis":{"status":"UP"},"dataSource":{"status":"UP"}},"wallet-service":{"status":"UP","rabbit":{"rabbitmq":{"version":"3.9.10","status":"UP"},"status":"UP"},"jedis":{"jedis":{"version":"redis_version:6.2.1","status":"UP"},"status":"UP"},"elasticSearch":{"myEs":{"clusterName":"elasticsearch","numberOfNodes":1,"numberOfDataNodes":1,"activePrimaryShards":2,"activeShards":2,"relocatingShards":0,"initializingShards":0,"unassignedShards":0,"status":"UP"},"status":"UP"},"dataSource":{"write1":{"status":"UP"},"read1":{"status":"UP"},"status":"UP"},"config":{"consul":{"info":"fedora-dc1-1.12.0","status":"UP"},"status":"UP"}}}

```
