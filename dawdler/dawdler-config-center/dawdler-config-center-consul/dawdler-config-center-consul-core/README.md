# dawdler-config-center-consul-core

## 模块介绍

统一配置中心核心模块

### 1. dawdler-config.yml配置文件

配置不同类型的配置中心与相关配置,目前只支持consul,支持自定义扩展.

dawdler-config.yml 支持多环境配置 参考[统一配置中心与多环境支持](../../../../doc/dawdler-profiles.active-README.md).

```yml
consul:
 host: localhost
 port: 8500
 separator: 
 token: 
 wait-time: 10
 watch-keys: 
 - /orderConfig
 - /user
 #TLSConfig:
  #keyStoreInstanceType: JKS, JCEKS, PKCS12, PKCS11, DKS
  #certificatePath:
  #certificatePassword:
  #keyStorePath:
  #keyStorePassword:
```

host: consul服务器的ip

port: consul服务器暴露的端口

separator:分割符 一般无须配置,只在keys的场景有意义,如以下请求:
 设 目前已有目录 /config/config-uat /config/config-dev /config/config
访问 ```http://localhost:8500/v1/kv/config?keys&separator=-&wait=5s&index=2``` 返回 [
"config/config", "config/config-" ].

token:用于身份校验

wait-time:轮询超时长,单位秒数

 watch-keys: 监控的key,是一个list列表,只有被监控的key有变化才会刷新相关配置,也可以通过 ```/``` 来监控所有的keys.

 TLSConfig:证书相关配置,如果有敏感数据并且consul需要部署在互联网中使用,建议采用此配置. 具体参考:[consul encryption](https://www.consul.io/docs/security/encryption)

### 2. 安装consul

参考[consul downloads](https://www.consul.io/downloads) 即可完成安装.

#### 2.1 consul在生产环境下的使用

Consul支持多DataCenter,数据中心可以通过Internet互联,为了提高通信效率,只有Server节点才加入跨数据中心的通信.

在单个数据中心中,Consul分为Client和Server两种节点(所有的节点称为Agent),Server节点保存数据,Client负责健康检查及转发数据请求到Server,Server节点有一个Leader和多个Follower,Leader节点会将数据同步到Follower,Server的数量推荐是3个或者5个,在Leader挂掉的时候会启动选举机制产生一个新的Leader.

集群内的Consul节点通过gossip协议维护成员关系,某个节点了解集群内现在还有哪些节点,这些节点是Client还是Server.单个数据中心的流言协议同时使用TCP和UDP通信,并且都使用8301端口.跨数据中心的gossip也同时使用TCP和UDP通信,端口使用8302.

集群内数据的读写请求既可以直接发到Server,也可以通过Client使用RPC转发到Server,请求最终会到达Leader节点,在允许数据轻微陈旧的情况下,读请求也可以在普通的Server节点完成,集群内数据的读写和复制都是通过TCP的8300端口完成.

注意: consul 只暴露局域网ip,如果使用配置中心这种需要ui来操作,可以通过nginx来暴露外网IP转发到consul局域网暴露的IP,同时通过auth_basic方式来认证授权访问ui.

##### 2.1.1  nginx负载均衡多server节点集群

consul在生产环境下一般使用集群模式,集群模式自行参考官方文档完成即可,建议通过nginx来负载多个集群节点.

服务注册(配置中心的client端也采用这种方式)调用关系如下：

service->nginx(可以考虑多个nginx+lvs保证高可用)->(负载多个)consul_server

##### 2.1.2  单client连接server节点集群

服务注册(配置中心的client端也采用这种方式)调用关系如下：

service->consul_client->consul_server

#### 2.2 单机测试环境配置启动

```shell
consul agent -data-dir=/data/consul/data -server -bootstrap-expect 1 -bind=192.168.43.128 -client=0.0.0.0 -ui &
```

dev:用于本地开发环境.

server: 以server身份启动,默认是client.

data-dir:data存放的目录.

bind:监听的ip地址,默认绑定0.0.0.0.

client: 客户端的ip地址,0.0.0.0对外公开任何ip都可以访问.

ui: 开启访问web管理界面.

启动完成 访问[consul管理界面](http://localhost:8500/) 验证是否成功.

#### 2.3 集群环境

以下提供一个三个服务端集群的示例:

```shell
#配置三台机器并启动

consul agent -server -bootstrap-expect 3 -data-dir=/data/consul/data -node=n1 -bind=192.168.43.131 -client=0.0.0.0 -ui&

consul agent -server -bootstrap-expect 3 -data-dir=/data/consul/data -node=n2 -bind=192.168.43.130 -client=0.0.0.0 -ui&

consul agent -server -bootstrap-expect 3 -data-dir=/data/consul/data -node=n3 -bind=192.168.43.137 -client=0.0.0.0 -ui&

#n1 n3 节点加入 n2,分别在 43.137与43.131中执行

consul join 192.168.43.130

#通过 consul members 可以查看节点状态

返回信息如下:
Node  Address              Status  Type    Build   Protocol  DC   Partition  Segment
n1    192.168.43.137:8301  alive   server  1.11.3  2         dc1  default    <all>
n2    192.168.43.131:8301  alive   server  1.11.3  2         dc1  default    <all>
n3    192.168.43.130:8301  alive   server  1.11.3  2         dc1  default    <all>

```

部分参数说明:

server: 以server身份启动.默认是client

bootstrap-expect:集群要求的最少server数量,当低于这个数量,集群即失效

data-dir:data存放的目录,更多信息请参阅consul数据同步机制

node:节点id,集群中的每个node必须有一个唯一的名称.默认情况下,Consul使用机器的hostname

bind:监听的ip地址.默认绑定0.0.0.0,可以不指定.表示Consul监听的地址,而且它必须能够被集群中的其他节点访问.Consul默认会监听第一个private IP,但最好还是提供一个.生产设备上的服务器通常有好几个网卡,所以指定一个不会出错

client: 客户端的ip地址,0.0.0.0是指不限定ip（不加这个,下面的ui :8500无法访问）

ui: 可以访问UI界面

-config-dir指定配置文件夹,Consul会加载其中的所有文件

-datacenter 指定数据中心名称,默认是dc1

#### 2.4 安全方面

要注意consul不要开放端口给互联网使用,建议通过nginx Basic HTTP authentication做授权后反向代理到局域网的consul端口上.
