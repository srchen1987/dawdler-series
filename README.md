# dawdler-series
#### 项目介绍
dawdler-series 是一站式分布式应用、微服务架构的解决方案，其特点简单、高效、安全。


####dawdler源码模块介绍:


1.  dawdler-server(容器的服务端)
2.  dawdler-server-plug(服务端插件，提供远程加载服务，注入service到过滤器，监听器，远程加载客户端)
3.  dawdler-server-plug-db(服务端数据库事务，读写分离的插件)
4.  dawdler-server-plug-dao(通过反射实现的jdbc通用dao插件，注入dao到service)
5.  dawdler-server-plug-mybatis(通过mybatis实现的数据库操作插件，注入mapper到service)
6.  dawdler-client(客户端核心代码，服务发现，连接池，动态代理，aop实现，负载均衡等)
7.  dawdler-client-plug(客户端插件，webmvc，远程加载组件的客户端，远程加载组件的通知器，web监听器，web拦截器等)
8.  dawdler-client-plug-session（客户端高性能分布式session实现）
9.  dawdler-client-plug-validator(客户端前后端通用校验器，支持js和java后端通用表达式校验，支持扩展，支持后端校验规则生成前端表达式，java后端支持分组，继承，排除等特性，js支持校验扩展，各种事件扩展)
10.  dawdler-client-plug-velocity(客户端velocity模板插件，目前已升级到2.2，提供一行代码分页pages指令，动态表单指令)
11.  dawdler-circuit-breaker(熔断器，支持熔断配置，降级，采用时间滑动窗口方式统计)
12.  dawdler-config(统一配置中心，clientside应用到web端，serverside应用到dawdler服务端)
13.  dawdler-distributed-transaction(分布式事务模块，client应用到web端，server应用到dawdler端，compensator补偿器模块)
14.  dawdler-rabbitmq-plug(rabbitmq插件，通过pool2实现池)
15.  dawdler-redis-plug(redis插件)
16.  dawdler-serialization(序列化模块)
17.  dawdler-load-bean(dawdler中需要序列化的类)
18.  dawdler-util(常用工具模块)
为什么要重复的发明轮子？请看下面的dawdler之美，dawdler早期应用在linuxsir开源社区上（如今的www.linuxsir.org 由于公司原因已不再是java语言开发的了），2008年之前采用ejb3.0通过jboss4.x版本进行，2010年采用nio写了一个版本，但容器部分功能未做完整，直到2014年之后开始基于aio重新编写了容器dawdler。

dawdler成熟么？基于dawdler早先版本开发的linuxsir稳定运行在服务器上4年已久。dawdler还运行在了某一元购商城，某移动社区，某支付平台上，tps高峰时期可以达到上千，订单量每天在800-1000万条数据左右，稳定性方面表现的非常出色，没出现过问题。可放心使用，通过本地测试单机下的dawdler每秒可以处理60000多次调用。

dawdler之美
架构上支持容器数据源，数据库读写分离，心跳探测，断网重连，优雅关机，rpc请求负载均衡，web端与服务器端的filter、listener，web端验证器（根据后端验证配置自动生成前端js表达式）等功能。
以上是小功能，不够美吧？真正的独到之处在下面.
1. 支持模块化部署公用一个jvm，理解成多个服务可以部署在一个容器中，启动一个jvm即可.

2. 无需在客户端（消费者）定义服务端（服务器提供者）提供相同的接口定义，比如操作用户的service，UserService 这个接口客户端与服务器端需要存放两份，一般rpc框架或ejb3都用打jar包的方式来解决，在dawdler中完美的解决了此问题，dawdler的客户端（web端，服务调用者）没有代码，调用时无需打包.

3. controller 调试模式，不需要重启tomcat，dawdler容器，随时调试各种数据(重写了类加载器，千万别把这种实现方式与tomcat热加载搞混淆了，tomcat的相当于重新启动了，而dawdler不是). 

4. dawdler非常简单，启动神速，无需学习spring一样可以做研发。至于多简单？全民it总动员的年代你懂得.

#### 软件架构
基于java语言研发 采用aio研发的容器与客户端
dawdler文件结构说明

        --bin 存放dawdler启动的jar包与脚本.

        --conf dawdler的配置信息，server-conf是服务器配置,datasources数据源配置.

        --deploys 部署dawdler的项目，如写了一个 user模块，admin模块，那下面就是user目录，admin目录，这与tomcat的webapps很像，更像jboss/WildFly的deploy的概念。每个目录的资源是独立使用的，包括类加载器.

        --lib 这里面的jar包是所有deploys下面的模块项目通用的，比如mysql驱动，数据库连接池等等相关的jar包 可以放到这里.

        --logs 存放日志的
#### 安装教程

1. 下载myservers，servers为dawdler服务器，zookeeper-3.5.2-alpha，zooinspector-master为图形化管理zookeeper的一个工具
2. 启动dawdler，进入dawdler的bin目录，下面有dawdler.bat dawdler.sh win环境就用bat linux或Mac下请使用dawdler.sh.
   `sh dawdler.sh`会有以下输出

        commands:
        run               Start dawdler in the current window 在当前窗口启动dawdler

        start             Start dawdler in a separate window 在后台启动dawdler 

        stop              Stop dawdler 停止dawdler（关闭dawdler之前会拒绝所有的请求，同时等待处理完客户端的请求之后停止服务器）

        stopnow           Stop dawdler immediately 立刻停止dawdler，如果客户端有请求为处理完会收到一个强制停止的异常

3. 启动zookeeper。执行 `sh zkServer.sh start`  启动zookeeper。
4. 配置zookeeper，进入zooinspector-master的bin下执行 `sh zooinspector.sh run`可以看到dawdler注册的节点。



#### 快速入门
1. 下载demos/simple/demo-server（服务提供者，需要部署在dawdler的deploys下），demos/simple/demo-web（部署到tomcat端）
2. demo是用eclipse写的，分别导入到eclipse中，demo-web不用多说（小学生都会将它部署到tomcat中）。demo-server可以直接将编译好的classes拷贝到dawdler的deploys目录的项目中,在实际开发的情况下建议将eclipse下的dawdler服务端的classes直接编译到dawdler/deploys/模块/classes。这样可以方便研发调试。
3. 导入demo-server下的sql到数据库,同时注意项目下的数据源配置 参考使用说明中的 2.1 demo-server/src/src_config.xml [点击查看配置说明](https://gitee.com/srchen1987/dawdler-series/blob/master/demos/simple/demo-server/src/src_config.xml)
4. 由于demo中用到了容器的数据源所以需要配置下 参考dawdler-server 配置文件说明 1.2 dawdler-server/conf/datasources.xml [点击这里查详细说明](https://gitee.com/srchen1987/dawdler-series/blob/master/myserver/servers/dawdler1/conf/datasources.xml)
5. 先启动dawdler ，然后启动tomcat 访问controler即可体验（与spring mvc很相似)

#### 使用说明


1. dawdler-server 配置文件说明（容器端）
   
    1.1 dawdler-server/conf/server-conf.xml [点击这里查看详细说明](https://gitee.com/srchen1987/dawdler-series/blob/master/myserver/servers/dawdler1/conf/server-conf.xml)

    1.2 dawdler-server/conf/datasources.xml 此配置比较简单根据不同数据库的连接池配置来做即可 [点击这里查详细说明](https://gitee.com/srchen1987/dawdler-series/blob/master/myserver/servers/dawdler1/conf/datasources.xml)
   

2. demo-server端（服务提供者）

    
    2.1 demo-server/src/src_config.xml [点击查看配置说明](https://gitee.com/srchen1987/dawdler-series/blob/master/demos/simple/demo-server/src/src_config.xml)
    
    2.2  demo-server/src/com/anywide/load/loadconfig.xml [点击查看配置说明](https://gitee.com/srchen1987/dawdler-series/blob/master/demos/simple/demo-server/src/com/anywide/load/loadconfig.xml)
   
 
