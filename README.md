# dawdler-series

## 项目介绍

dawdler-series 是一站式分布式应用、微服务架构的解决方案,其特点简单、小巧、高效、易扩展.

简单: 技术栈全面,入门门槛低.

小巧: 项目java源代码2万行,占用空间30M,最小依赖三方组件(个数不超过20个,大小在10M以内).

高效: 启动运行速度快,一般一个服务大约在100ms到3s之间,多个服务如果部署在一个dawdler容器中是并行加载,启动时长为最长的那个服务的加载时间.

易扩展: 提供各种扩展接口与扩展方式.如: 负载均衡,配置中心,事务执行器,拦截器,监听器,服务创建监听器,序列化扩展,视图插件等.

部分功能说明:

mvc框架: 与spring mvc几乎一致.

分布式session框架: 高性能分布式session的实现.

验证框架: 前后台通用表达式支持后台生成前端表达式,灵活扩展.

RPC框架及容器: 容器部署方式,高效急速稳定的rpc实现,支持服务动态注册,心跳探测,断网重连,优雅关机,自定义序列化协议,rpc请求负载均衡,容器级生命周期监听器、过滤器、服务创建监听器,服务权限管理(支持全局用户、特定模块用户,可控制调用者调用指定服务),jndi数据源,高性能编译型aop实现,多服务部署(公用一个dawdler服务器).

事务框架: 与spring transaction一致并支持读写分离.

熔断器: 熔断器,支持熔断配置、降级,采用时间滑动窗口方式统计.

分布式事务框架: 高性能异步tcc实现.

数据库操作框架: 封装jdbc的一套操作框架和集成mybatis的一套

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

### dawdler模块介绍

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

通过mybatis实现的数据库操作插件,注入mapper到service.

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

#### 13. [dawdler-config](dawdler/dawdler-config/README.md)

统一配置中心,clientside应用到web端,serverside应用到dawdler服务端.

#### 14. [dawdler-distributed-transaction](dawdler/dawdler-distributed-transaction/README.md)

分布式事务模块,client应用到web端,server应用到dawdler端,compensator补偿器模块.

#### 15. [dawdler-serialization](dawdler/dawdler-serialization/README.md)

序列化模块.

#### 16. [dawdler-load-bean](dawdler/dawdler-load-bean/README.md)

dawdler中需要序列化的类,dawdler内部使用.

#### 17. [dawdler-util](dawdler/dawdler-util/README.md)

常用工具模块.

#### 18. [dawdler-rabbitmq-plug](dawdler/dawdler-rabbitmq-plug/README.md)

rabbitmq连接池插件,通过pool2实现池.

#### 19. [dawdler-redis-plug](dawdler/dawdler-redis-plug/README.md)

redis池插件,通过jedis实现.

#### 20. [dawdler-es-plug](dawdler/dawdler-es-plug/README.md)

通过pool2对elasticsearch-rest-high-level-client进行封装实现的一套es连接池.

### dawdler服务器结构说明

        --bin 存放dawdler启动的jar包与脚本.

        --conf dawdler的配置信息,server-conf是服务器配置,datasources数据源配置.

        --deploys 部署dawdler的项目,如写了一个 user模块,admin模块,那下面就是user目录,admin目录,这与tomcat的webapps很像,更像jboss/WildFly的deploy的概念.每个目录的资源是独立使用的,包括类加载器.

        --lib 这里面的jar包是所有deploys下面的模块项目通用的,比如mysql驱动,数据库连接池等等相关的jar包 可以放到这里.

        --logs 存放日志

### 安装教程

1. 下载dawdler服务器

2. 启动dawdler,  
进入dawdler的bin目录,win环境`dawdler.bat`
linux或mac环境`sh dawdler.sh`

   `sh dawdler.sh`会有以下输出

        commands:
        run               Start dawdler in the current window 在当前窗口启动dawdler

        start             Start dawdler in a separate window 在后台启动dawdler 

        stop              Stop dawdler 停止dawdler(关闭dawdler之前会拒绝所有的请求,同时等待处理完客户端的请求之后停止服务器)

        stopnow           Stop dawdler immediately 立刻停止dawdler,如果客户端有请求为处理完会收到一个强制停止的异常

3. 启动zookeeper.执行 `sh zkServer.sh start`  启动zookeeper.

#### 快速入门
