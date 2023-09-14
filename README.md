# dawdler-series

![version](https://img.shields.io/badge/dawdler--series-0.0.2--RELEASES-brightgreen)&nbsp;
[![License](https://img.shields.io/badge/license-apache2.0-green)](LICENSE)&nbsp;
![jdk](https://img.shields.io/badge/jdk-1.8%2B-green)

## 项目介绍

dawdler-series 是一站式分布式应用、微服务架构的解决方案,其特点简单、小巧、高效、易扩展.

简单: 技术栈全面,入门门槛低.

小巧: 项目java源代码2万行,占用空间4.5M,最小依赖三方组件(个数不超过20个,大小在10M以内).

高效: 启动运行速度快,一般一个服务大约在50ms到300ms之间,多个服务如果部署在一个dawdler容器中是并行加载,启动时长为最长的那个服务的加载时间.

易扩展: 提供各种扩展接口与扩展方式.如: 负载均衡,配置中心,事务执行器,拦截器,监听器,服务创建监听器,序列化扩展,视图插件等.

部分功能说明:

mvc框架: 与spring mvc几乎一致.

分布式session框架: 高性能分布式session的实现.

验证框架: 前后台通用表达式支持后台生成前端表达式,灵活扩展.

RPC框架及容器: 容器部署方式,高效急速稳定的rpc实现,支持服务动态注册,心跳探测,断网重连,优雅关机,自定义序列化协议,rpc请求负载均衡,容器级生命周期监听器、过滤器、服务创建监听器,服务权限管理(支持全局用户、特定模块用户,可控制调用者调用指定服务),jndi数据源,高性能编译型aop实现,多服务部署(公用一个dawdler服务器).

事务框架: 与spring transaction一致并支持读写分离.

熔断器: 熔断器,支持熔断配置、降级,采用时间滑动窗口方式统计.

分布式事务框架: 高性能异步tcc实现.

数据库操作框架: 提供封装jdbc的一套操作框架同时支持集成mybatis来操作数据库.

统一配置中心: 支持扩展的统一配置中心.

链路追踪: 基于pinpoint实现的链路追踪.

常用组件的plug: redis,elastic-search,rabbitmq.

### 为什么开发dawdler

背景: dawdler早期应用在linuxsir开源社区上(如今的linuxsir由于公司原因已不再是java语言开发的了),2008年之前采用ejb3.0通过jboss4.x版本运行,之后切过jboss7.x、wildfly.

问题1: 运行在jboss中有很多问题是解决不了的,比如长链接心跳,断线重连,服务注册与发现,自定义序列化协议,调用性能,启动速度等等问题.

问题2: 很多应用使用了spring,启动速度和运行速度都不是很理想,特别是启动速度不方便调试开发.

由于以上问题2010年采用nio写了一个版本,但容器部分功能未做完整,直到2014年之后开始基于aio重新编写了容器dawdler.

### dawdler的稳定性

基于dawdler早先版本开发的linuxsir稳定运行在服务器上4年已久.dawdler还运行在了某一元购商城,某移动社区,国内某大型2b电商平台(应用部分组件),某支付平台上(tps高峰时期可以达到上千,订单量每天在800-1000万条数据左右,稳定性表现的非常出色,没出现过问题.可放心使用),通过本地测试单机下的dawdler每秒可以处理60000多次调用.

### dawdler之美

1. 支持模块化部署公用一个jvm,理解成多个服务可以部署在一个容器中,启动一个jvm即可.

2. 无需在客户端(消费者)定义服务端(服务器提供者)提供相同的接口定义,比如操作用户的service,UserService 这个接口客户端与服务器端需要存放两份,一般rpc框架或ejb3都用打jar包的方式来解决,在dawdler中完美的解决了此问题,dawdler的客户端(web端,服务调用者)没有代码,调用时无需打包(如今有了maven,可以无须将bean和api通过这种方式加载).

3. controller 调试模式,不需要重启tomcat,dawdler容器,随时调试api接口(只限于web端,这种实现方式与tomcat热加载完全不同,tomcat的相当于重新启动了,而dawdler是直接通过类加载器热更新类的方式实现).

4. dawdler启动速度快,运行效率高,依赖三方jar包少,体积小.

5. 高效的编译型aop实现(spring是通过cglib实现).

6. 功能齐全,学习成本低,几乎兼容springmvc与事务管理方式,无须使用spring也能使用同样的springmvc与事务的功能,同时提供各种常用组件的能力,请参考dawdler模块.

7. 动态加载类Filter实现网关提升性能(减少一次网关到聚合服务的调用),非常适合对性能要求高的互联网应用.

### dawdler模块介绍(具体文档可以点击标题连接进入子模块查看详细说明)

#### 1. [dawdler-server](dawdler/dawdler-server/README.md)

容器的服务端.

#### 2. [dawdler-core](dawdler/dawdler-core/README.md)

dawdler-server与dawdler-client公用的核心模块.包含网络,服务发现实现,线程池,注解,压缩算法等.

#### 3. [dawdler-server-plug](dawdler/dawdler-server-plug/README.md)

服务端插件,提供远程加载服务,注入service到过滤器,监听器,远程加载客户端.

#### 4. [dawdler-server-plug-db](dawdler/dawdler-server-plug-db/README.md)

服务端数据库事务,读写分离的插件.

#### 5. [dawdler-server-plug-dao](dawdler/dawdler-server-plug-dao/README.md)

通过反射实现的jdbc通用dao插件,注入dao到service.

#### 6. [dawdler-server-plug-mybatis](dawdler/dawdler-server-plug-mybatis/README.md)

+通过mybatis实现的数据库操作插件,注入mapper到service,session变更为单例模式,支持读写分离.

#### 7. [dawdler-client](dawdler/dawdler-client/README.md)

客户端核心代码,服务发现,连接池,动态代理,aop实现,负载均衡等.

#### 8. [dawdler-client-plug](dawdler/dawdler-client-plug/README.md)

客户端插件,webmvc,远程加载组件的客户端,远程加载组件的通知器,web监听器,web拦截器等.

#### 9. [dawdler-client-plug-session](dawdler/dawdler-client-plug-session/README.md)

客户端高性能分布式session实现.

#### 10. [dawdler-client-plug-validator](dawdler/dawdler-client-plug-validator/README.md)

是一个强大的前后端通用校验器,支持js和java后端通用表达式校验,支持扩展,支持后端校验规则生成前端表达式,java后端支持分组,继承,排除等特性,js支持校验扩展,各种事件扩展.

#### 11. [dawdler-client-plug-velocity](dawdler/dawdler-client-plug-velocity/README.md)

客户端velocity模板插件,目前已升级到2.3,提供一行代码分页pages指令,动态表单指令.

#### 12. [dawdler-circuit-breaker](dawdler/dawdler-circuit-breaker/README.md)

熔断器,支持熔断配置,降级,采用时间滑动窗口方式统计.

#### 13. [dawdler-config-center](dawdler/dawdler-config-center/README.md)

统一配置中心,支持扩展、注入.

#### 14. [dawdler-distributed-transaction](dawdler/dawdler-distributed-transaction/README.md)

分布式事务模块,client应用到web端,server应用到dawdler端,compensator补偿器模块.

#### 15. [dawdler-serialization](dawdler/dawdler-serialization/README.md)

序列化模块.

#### 16. [dawdler-load-bean](dawdler/dawdler-load-bean/README.md)

dawdler中需要序列化的类,dawdler内部使用.

#### 17. [dawdler-util](dawdler/dawdler-util/README.md)

常用工具模块.

#### 18. [dawdler-rabbitmq-plug](dawdler/dawdler-rabbitmq-plug/README.md)

rabbitmq模块的支持,包含客户端,服务器端,rabbitmq核心模块.

#### 19. [dawdler-redis-plug](dawdler/dawdler-redis-plug/README.md)

dawdler-redis-plug redis模块的支持,包含客户端,服务器端,redis核心模块.

#### 20. [dawdler-es-plug](dawdler/dawdler-es-plug/README.md)

dawdler-es-plug es模块的支持,包含客户端,服务器端,es核心模块.

#### 21. [dawdler-pinpoint-plug](dawdler/dawdler-pinpoint-plug/README.md)

dawdler实现pinpoint链路追踪插件.

#### 22. [dawdler-schedule-plug](dawdler/dawdler-schedule-plug/README.md)

基于quartz实现,schedule模块的支持,包含客户端,服务器端,schedule核心模块.

#### 23. [dawdler-discovery-center](dawdler/dawdler-discovery-center/README.md)

注册中心的根模块,提供服务注册,服务下线,服务发现等功能.目前提供zookeeper与consul的实现.

#### 24. [dawdler-encrypt-generator](dawdler/dawdler-encrypt-generator/README.md)

用于生成properties与统一配置中心使用加密的密钥工具类.

#### 25. [dawdler-dependencies](dawdler/dawdler-dependencies/README.md)

构建项目时需要用的maven依赖声明.

#### 26. [dawdler-client-api-generator](dawdler/dawdler-client-api-generator/README.md)

基于java源码doc生成兼容swagger-ui的OpenAPI 3.0的json工具,对源代码零侵入,上手简单,生成效率高,使用非常方便.

#### 27. [dawdler-cache-plug](dawdler/dawdler-cache-plug/README.md)

可以用在web端和服务端的缓存模块.

### dawdler-runtime介绍

参考[dawdler-runtime](https://github.com/srchen1987/dawdler-runtime/blob/main/README.md)

### 安装教程

1. 下载[dawdler-runtime-jdk1.8](https://github.com/srchen1987/dawdler-runtime/archive/refs/tags/dawdler-runtime-jdk1.8.zip)

2. 启动注册中心,zookeeper执行 `sh zkServer.sh start`  启动zookeeper或启动consul.

3. 进入dawdler的bin目录,通过 `sh dawdler.sh run` 启动(win环境`dawdler.bat` linux或mac环境`sh dawdler.sh`).

    运行`sh dawdler.sh`会有以下输出:

```shell
commands:
run               Start dawdler in the current window 在当前窗口启动dawdler

start             Start dawdler in a separate window 在后台启动dawdler 

stop              Stop dawdler 停止dawdler(关闭dawdler之前会从注册中心下线本容器下所有服务,不再接受请求,同时等待处理完客户端的请求之后停止服务器)

stopnow           Stop dawdler immediately 立刻停止dawdler,如果客户端有请求为处理完会收到一个强制停止的异常
```

### 快速入门

#### 1. 软环境

dawdler需要三方组件的支持,如下:

| 软件 | 是否必须 | 备注 |
| :-: | :-: | :-: |  
| jdk-1.8 | √ | 建议使用openjdk1.8x |
| apache-zookeeper-3.6+ | √ | 注册中心 |
| tomcat-8.5+ | x | web服务时需要 |
| redis5x \| 6x | x | 缓存服务时需要 |
| mysql5x \| 8x | x | 数据库服务时需要 |
| elastic-seach 7x | x | es服务时需要 |
| rabbitmq 3.8x | x | 消息服务时需要 |
| consul 1.10.x | x | 统一配置中心或注册中心时需要 |

#### 2. 项目结构说明(建议采用此规范定义项目结构)

dawdler为分布式调用,微服务而生,所以项目的结构也是以服务提供者->调用者(webapi提供者)这种方式构建的(服务拆分原则请自行了解,不在此进行说明).

##### 2.1 dto层或entity层

dto用于调用或响应时传输的序列化对象.

entity用于数据库查询返回的实体对象.

一般情况下可以用entity代替dto,但如果字段相差很多,还是建议单独创建dto.

##### 2.2 服务接口层

service接口用于声明服务的接口,并用于提供者与调用者的项目中.

##### 2.3 服务提供者

用于提供service定义接口的具体实现,部署在dawdler的deplays目录下运行.

涉及数据库调用的也部署在这层,如mybatis的Mapper.直接注入到ServiceImpl即可.

##### 2.4 服务调用者

用于调用服务提供者提供的服务,可以通过api调用,也可以通过动态代理对象调用.

一般在web应用中会将动态代理对象注入到web的Controller中进行远程调用.

##### 2.5 项目结构图

以电商一个项目为例:

```shell
--shop
    --api #api接口,包类型为pom的子模块工程.
    |   --user-api 
    |   --order-api #存放接口定义,dto,entity.
    |   --product-api
    --load-web #远程加载服务,包类型为pom的子模块工程.
    |   --user-load-web 
    |   --order-load-web #存放controller,listener,filter组件,用于被web-api模块远程加载.
    |   --product-load-web
    |   --core-load-web #用与加载公用组件,如服代替网关的过滤器,用于被web-api模块远程加载.
    --service #具体服务实现,部署在dawdler中,包类型为pom的子模块工程.
    |   --user-service 
    |   --order-service #存放服务实现,dao,mapper,服务启动监听器.
    |   --product-service
    --web-api #提供web服务,一般部署在web容器中,包类型为pom的子模块工程.
    |   --user-web-api
    |   --order-web-api
    |   --product-web-api
```

更多实例请参考[dawdler-chapter](https://github.com/srchen1987/dawdler-chapter).

需要帮助可以发送email到 <suxuan696@gmail.com> .
