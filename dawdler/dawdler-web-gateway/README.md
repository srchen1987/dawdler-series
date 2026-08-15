# dawdler-web-gateway

## 模块介绍

dawdler 的网关模块,基于 Servlet Filter 与 Vert.x 实现的轻量级反向代理网关.通过 `gateway.yml` 声明式路由,将请求按规则匹配后转发到上游服务,支持 HTTP/HTTPS 与 WebSocket/wss 协议代理,并可与注册中心联动实现服务发现与负载均衡.

核心特性:

- 声明式路由配置,基于 Ant 风格路径匹配(`**`、`*`、`?`),支持多段通配.
- HTTP/HTTPS 异步反向代理,基于 Servlet 3.0 异步与 Vert.x `HttpClient` 流式转发请求/响应体.
- WebSocket/wss 反向代理,基于 Servlet 3.1 HTTP Upgrade 机制 + Vert.x `NetClient` 原始 TCP 双向透传帧数据,不依赖 JSR-356 端点注册,绕开容器 `WsFilter` 单段路径模板限制.
- 服务发现集成,通过 `lb://` 协议从注册中心获取实例并随机负载均衡.
- 路由 Filter 机制,内置 `StripPrefix`、`RewritePath`、`SetPath`,基于 SPI 可扩展.

### 1. pom中引入依赖

```xml
<groupId>club.dawdler</groupId>
<artifactId>dawdler-web-gateway</artifactId>
```

### 2. 架构说明

入口为 `GatewayFilter`(`@WebFilter("/*")`),核心处理流程:

1. 解析请求路径(去除 `contextPath`),通过 `RouteResolver.matchRoute` 按 Ant 路径谓词匹配路由.
2. 未匹配到路由时放行至后续 Filter 链.
3. 匹配到路由后,根据 URI 协议(`RouteScheme`)分流:
   - WebSocket 类协议(`ws`、`wss`、`lb:ws`、`lb:wss`):校验升级请求头,交由 `WebSocketProxyHandler` 处理.
   - `forward` 协议(`forward://`):应用 Filter 改写路径后,交由 `ForwardRoutingHandler` 通过 `RequestDispatcher.forward` 分发到本地 beans/controllers.
   - HTTP 类协议(`http`、`https`、`lb`、`lb:http`、`lb:https`):解析目标地址,应用 Filter,通过 `HttpProxyHandler` 异步代理.
4. `lb://` 协议从 `ServiceInstancePool` 获取实例地址并随机选取,再拼接为具体协议地址转发.

#### 2.1 主要组件

| 组件 | 说明 |
| --- | --- |
| `filter.GatewayFilter` | 网关入口 Servlet Filter,负责路由分发与协议分流. |
| `config.GatewayConfigParser` | 启动时从 classpath 读取 `gateway.yml`(支持 `dawdler.profiles.active` profile 切换)并解析为 `GatewayConfig`,classpath 不存在时回退到统一配置中心加载. |
| `config.RouteDefinition` / `PredicateDefinition` / `FilterDefinition` | 路由、谓词、过滤器的配置定义. |
| `route.RouteResolver` | 路由缓存构建、路径匹配(`AntPathMatcher`)、目标地址解析、Filter 应用. |
| `route.filter.GatewayPathFilter` / `GatewayPathFilterFactory` | 路径过滤器 SPI 接口与工厂,通过 `ServiceLoader` 加载内置及第三方过滤器. |
| `route.RouteScheme` | 路由协议枚举与协议解析. |
| `route.ServiceInstancePool` | 服务实例池静态注册表,由注册中心网关插件维护,供负载均衡读取. |
| `handler.HttpProxyHandler` | 基于 Vert.x `HttpClient` 的 HTTP/HTTPS 异步代理. |
| `handler.WebSocketProxyHandler` | WebSocket 握手与桥接初始化.通过 `AsyncContext` 异步释放 servlet 线程,在 `handshakeExecutor` 上完成上游握手,完成后调用 `request.upgrade()` 注册 `WebSocketProxyUpgradeHandler` 并绑定上游 socket 与数据队列,帧数据的双向透传由 `WebSocketProxyUpgradeHandler` 在 `bridgeExecutor` 上执行. |
| `handler.WebSocketProxyUpgradeHandler` | Servlet 3.1 `HttpUpgradeHandler`,接管升级后连接,在 `bridgeExecutor` 上双向透传帧数据. |
| `handler.ForwardRoutingHandler` | `forward://` 协议处理器,通过 `RequestDispatcher.forward` 分发到本地 beans/controllers. |

### 3. 配置说明

路由配置位于 classpath 下的 `gateway.yml`,顶层包含 `routes` 列表与可选的 `options` 运行时选项段,每条路由包含:

| 字段 | 说明 | 示例 |
| --- | --- | --- |
| `id` | 路由唯一标识 | `user-facade` |
| `uri` | 目标地址(协议 + 地址) | `lb://user-web-api` |
| `predicates` | 断言列表,`Name=Args` 格式 | `Path=/api-user/**` |
| `filters` | 过滤器列表,`Name=Args` 格式 | `StripPrefix=1` |

#### 3.1 谓词 (Predicate)

| 谓词 | 说明 | 示例 |
| --- | --- | --- |
| `Path` | Ant 风格路径匹配 | `Path=/api-user/**` |

#### 3.2 过滤器 (Filter)

过滤器用于在转发前改写请求路径,按配置顺序依次应用,前一个过滤器的输出作为后一个的输入.通过 SPI 机制加载,内置以下过滤器,可自行扩展(见[过滤器扩展](#过滤器扩展)).

| 过滤器 | 参数格式 | 说明 |
| --- | --- | --- |
| `StripPrefix` | `N` | 去除请求路径前 N 段前缀(以 `/` 分段),后续段原样保留 |
| `RewritePath` | `正则, 替换` | 基于正则替换路径,替换串可引用捕获组(`$1`、`${name}`),以首个逗号分隔 |
| `SetPath` | `模板` | 基于模板**整体覆盖**输出路径,`{N}` 引用原路径第 N 段(1 基),丢弃原路径结构 |

三者对比(原路径 `/api-user/v1/user/list`):

```text
# StripPrefix=2                              → /user/list       (砍掉前 2 段,后面原样保留)
# RewritePath=/api-user/(?<s>.*), /${s}      → /v1/user/list    (正则替换,保留未匹配部分)
# SetPath=/user/{4}                          → /user/list       (无视结构,精确取第 4 段)
```

##### SetPath 用法详解

`SetPath` 适合在网关暴露路径与上游接口路径结构差异较大、需按段位精确重组时使用.处理步骤:

1. **分段**:原路径按 `/` 切分,忽略空段(开头 `/`、连续 `//` 不影响编号),得到段列表.
2. **1 基编号**:第 1 段、第 2 段……从 1 开始计数.
3. **占位符替换**:模板中的 `{N}` 替换为原路径第 N 段;**N 超出段数则替换为空串**.
4. **整体替换**:输出 = 模板替换结果,与原路径其余部分无关;若结果为空返回 `/`.

设原路径 `/api-user/123/details`,分段后 `[1]=api-user  [2]=123  [3]=details`:

| 模板 | 转发结果 | 说明 |
| --- | --- | --- |
| `SetPath=/user/{2}` | `/user/123` | 取第 2 段拼到固定前缀后 |
| `SetPath=/{1}/{3}` | `/api-user/details` | 跳过中间段重组 |
| `SetPath=/api/v1/{2}/{3}` | `/api/v1/123/details` | 模板自带前缀,再拼两段 |
| `SetPath=/{3}/{2}/{1}` | `/details/123/api-user` | 段顺序可任意重排 |
| `SetPath=/static/{1}` | `/static/api-user` | 只取第 1 段,丢弃其余 |
| `SetPath=/{1}/{5}` | `/api-user/` | 第 5 段不存在 → 替换为空串 |
| `SetPath=/fixed` | `/fixed` | 无占位符,所有请求都打到同一路径 |

边界情况:`SetPath=`(空参数)不生效,原样返回原路径并打印告警;原路径为空直接返回原值;连续斜杠 `/a//b///c` 分段为 `[a, b, c]`,空段不占编号位.

##### 过滤器扩展

过滤器通过 `java.util.ServiceLoader` 加载,实现 `club.dawdler.web.gateway.route.filter.GatewayPathFilter` 接口(`getName` 返回过滤器名,`apply(path, args)` 返回改写后的路径),并在 jar 的 `META-INF/services/club.dawdler.web.gateway.route.filter.GatewayPathFilter` 中登记全限定类名即可被自动发现.参考内置的 `StripPrefixFilter`、`RewritePathFilter`、`SetPathFilter`.

#### 3.3 运行时选项 (options)

`options` 段集中管理网关运行时可调参数,覆盖 Vert.x 线程模型、HTTP/HTTPS 代理、WebSocket/wss 代理三组组件.**所有字段均内置面向网关场景的合理默认值**,省略 `options` 段或单个字段时即可获得网关级开箱可用配置.

> 命名约定:键名须为 camelCase,与 Java 字段名逐字匹配(`YAMLMapper` 使用 Jackson 默认配置,无命名策略转换).

##### 3.3.1 Vert.x 运行时 (`options.vertx`)

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `eventLoopPoolSize` | `0` | 事件循环线程数,`0` = Vert.x 默认(2 × CPU 核心数).只负责非阻塞事件分发,一般无需调整 |
| `workerPoolSize` | `0` | worker 线程池大小,`0` = Vert.x 默认(20).阻塞的 Servlet I/O(请求体读取/响应体回写)经 `executeBlocking(ordered=true)` 在此池执行,保证同一请求 I/O 顺序执行、不同请求并行,由池规模决定并发上限.高并发下 20 偏小,建议按并发量调大(如 64~256) |

##### 3.3.2 HTTP/HTTPS 代理 (`options.httpProxy`)

> 单位注意:`connectTimeout` 为**毫秒**;`idleTimeout`、`keepAliveTimeout` 为**秒**(与 Vert.x `HttpClientOptions` 原生单位一致).值为 `0` 表示不覆盖,使用 Vert.x 默认值.

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `asyncTimeout` | `30000` | Servlet 异步上下文超时(毫秒),`0` = 不超时 |
| `connectTimeout` | `0` | 上游连接建立超时(毫秒),`0` = Vert.x 默认 |
| `idleTimeout` | `0` | 连接空闲超时(秒),`0` = 不超时 |
| `maxPoolSize` | `200` | 连接池最大连接数,**对吞吐影响最大**.HTTP/1.1 默认不开 pipelining,每条连接串行处理请求,在飞请求数被限制在 `maxPoolSize` 以内(经验估算 `吞吐 ≈ maxPoolSize / 单请求往返时延`).Vert.x 原生默认仅 5,网关默认设为 200;压测/高并发应按目标在飞请求数调大(如 500~1000),并配合 `keepAlive: true` 复用连接.设为 `0` 回退 Vert.x 默认(不推荐) |
| `keepAlive` | `true` | 是否启用 HTTP Keep-Alive |
| `keepAliveTimeout` | `0` | Keep-Alive 超时(秒),`0` = Vert.x 默认 |
| `trustAll` | `false` | HTTPS 是否信任所有证书(跳过证书校验),默认走 JDK truststore |
| `requestBufferSize` | `8192` | 请求体流式转发 buffer 大小(字节) |

##### 3.3.3 WebSocket/wss 代理 (`options.webSocketProxy`)

> 所有时间字段单位均为**毫秒**.

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `connectTimeout` | `12000` | 上游 TCP 连接等待超时(毫秒) |
| `handshakeTimeout` | `15000` | 上游 WebSocket 握手等待超时(毫秒) |
| `netConnectTimeout` | `10000` | NetClient 连接建立超时(毫秒) |
| `maxHeaderSize` | `65536` | 握手响应头最大字节,超限直接判定握手失败 |
| `trustAll` | `true` | wss 是否信任所有证书(跳过证书校验) |
| `readBufferSize` | `8192` | 客户端 → 上游读取 buffer 大小(字节) |
| `maxBridgeThreads` | `256` | WS 桥接读写线程池上限.每条 WS 连接占用 2 个线程(reader + writer)并在连接生命周期内一直持有,默认 256 可支撑 128 条并发 WS 连接,超出上限的新连接会被关闭.调大时需注意线程栈内存开销(每线程约 512KB~1MB) |
| `maxHandshakeThreads` | `64` | WS 握手异步执行线程池上限.握手期间释放 servlet 线程,在 worker 线程上等待上游连接与 101 响应,每次最长阻塞 `connectTimeout + handshakeTimeout`.该线程池核心数与最大数均为 `maxHandshakeThreads`,并使用同等容量的有界队列(`LinkedBlockingQueue`),因此实际 503 触发阈值为 `2 × maxHandshakeThreads`(线程数 + 队列槽位),默认 64 即最多容纳 128 个并发握手请求,超出返回 503 |

> **线程模型**:WebSocket 代理采用两个独立有界线程池,避免资源耗尽——
> - **握手阶段**通过 Servlet `AsyncContext` 释放 servlet 线程,在 `handshakeExecutor` 的 worker 线程上等待上游连接与 101 响应,完成后从 worker 线程调用 `request.upgrade()` 注册处理器.
> - **桥接阶段**的 reader/writer 在 `bridgeExecutor` 的 worker 线程上运行,双向透传帧数据.
> - 任一线程池饱和时,握手池返回 503,桥接池关闭已升级连接,避免无界线程增长导致的 DoS.

##### 3.3.4 配置示例

```yaml
options:
    vertx:
        eventLoopPoolSize: 4
        workerPoolSize: 32
    httpProxy:
        asyncTimeout: 60000
        connectTimeout: 5000
        idleTimeout: 30
        maxPoolSize: 200
        keepAlive: true
        keepAliveTimeout: 60
        trustAll: false
        requestBufferSize: 16384
    webSocketProxy:
        connectTimeout: 12000
        handshakeTimeout: 15000
        netConnectTimeout: 10000
        maxHeaderSize: 65536
        trustAll: false
        readBufferSize: 16384
        maxBridgeThreads: 256
        maxHandshakeThreads: 64
```

### 4. 支持的协议

通过路由 `uri` 字段的协议前缀决定转发方式:

| 协议 | 说明 | 示例 |
| --- | --- | --- |
| `lb://` | 负载均衡,经服务发现路由到微服务(默认 http) | `lb://user-web-api` |
| `lb:ws://` / `lb:wss://` | 负载均衡 + 指定 WebSocket 协议 | `lb:ws://websocket-service` |
| `lb:http://` / `lb:https://` | 负载均衡 + 指定 HTTP 协议 | `lb:https://secure-http-service` |
| `http://` / `https://` | 路由到静态外部/内部 HTTP(S) 地址 | `http://httpbin.org` |
| `ws://` / `wss://` | 路由到静态 WebSocket 端点 | `ws://echo.websocket.org` |
| `forward://` | 路由到本地 beans/controllers,通过 Servlet `RequestDispatcher.forward` 分发 | `forward:///local/api` |

### 5. 配置示例

```yaml
routes:
    # 负载均衡
    - id: user-facade
      uri: lb://user-web-api
      predicates:
        - Path=/api-user/**
      filters:
        - StripPrefix=1

    # 负载均衡 + WebSocket 协议
    - id: websocket-lb
      uri: lb:ws://websocket-service
      predicates:
        - Path=/ws-lb/**
      filters:
        - StripPrefix=1

    # 静态 HTTP / HTTPS
    - id: http-service
      uri: http://httpbin.org
      predicates:
        - Path=/http-service/**
      filters:
        - StripPrefix=1

    - id: https-service
      uri: https://httpbin.org
      predicates:
        - Path=/https-service/**
      filters:
        - StripPrefix=1

    # 静态 WebSocket / wss
    - id: websocket-service
      uri: ws://echo.websocket.org
      predicates:
        - Path=/ws-service/**
      filters:
        - StripPrefix=1

    - id: secure-websocket-service
      uri: wss://echo.websocket.org
      predicates:
        - Path=/wss-service/**
      filters:
        - StripPrefix=1

    # RewritePath 正则替换
    - id: rewrite-demo
      uri: lb://user-web-api
      predicates:
        - Path=/api-user/**
      filters:
        - RewritePath=/api-user/(?<segment>.*), /${segment}

    # SetPath 模板重构,{2} 引用原路径第 2 段
    - id: setpath-demo
      uri: lb://user-web-api
      predicates:
        - Path=/api-user/**
      filters:
        - SetPath=/user/{2}

    # 本地转发,分发到当前 Web 应用内的 beans/controllers
    - id: local-forward
      uri: forward:///local
      predicates:
        - Path=/api-local/**
      filters:
        - StripPrefix=1
```

#### 5.1 forward:// 路径解析

`forward://` 不经过反向代理,而是通过 Servlet 容器的 `RequestDispatcher.forward` 将请求分发到当前 Web 应用内的本地控制器,与 dawdler 自身的 `AnnotationUrlHandler` 转发方式一致.路径解析规则与 HTTP/HTTPS 代理保持一致:

1. 先对已去除 `contextPath` 的请求路径应用 Filter 改写.
2. 若 `forward://` URI 自带路径(如 `forward:///local`),则将其作为前缀与改写后的路径拼接.
3. 若 URI 不带路径(如 `forward:///`),则直接使用改写后的路径.
4. 原始请求的 query string 由 Servlet 容器在 forward 时自动保留.

```text
# uri: forward:///local, filters: [StripPrefix=1]
/api-local/user/list  ->  /local/user/list

# uri: forward:///, filters: [StripPrefix=1]
/api-local/user/list  ->  /user/list
```

### 6. 服务发现集成

`lb://` 协议依赖注册中心提供实例地址.网关本身不包含具体注册中心实现,需引入对应的注册中心网关插件,由其轮询线程维护 `ServiceInstancePool` 实例池(新增/下线),网关在请求时随机选取实例转发.

使用 Consul 作为注册中心:

```xml
<groupId>club.dawdler</groupId>
<artifactId>dawdler-gateway-discovery-center-consul</artifactId>
```

参考 [dawdler-gateway-discovery-center-consul](../dawdler-discovery-center/dawdler-discovery-center-consul/dawdler-gateway-discovery-center-consul/README.md).

使用 Zookeeper 作为注册中心:

```xml
<groupId>club.dawdler</groupId>
<artifactId>dawdler-gateway-discovery-center-zookeeper</artifactId>
```

参考 [dawdler-gateway-discovery-center-zookeeper](../dawdler-discovery-center/dawdler-discovery-center-zookeeper/dawdler-gateway-discovery-center-zookeeper/README.md).

### 7. 性能调优

吞吐受 `maxPoolSize`、`workerPoolSize`、`maxBridgeThreads` 三组参数共同制约(详见 [3.3 运行时选项](#33-运行时选项-options)).压测/高并发场景调优顺序:

1. 先把 `maxPoolSize` 调到目标并发量级别,验证吞吐回升;
2. 用 `jstack` 观察网关线程,若 Vert.x event-loop 线程长期阻塞于 `OutputStream.flush` / `InputStream.read`,说明阻塞隔离未生效或 worker 池过小,调大 `workerPoolSize`;
3. 若有大量 WebSocket 连接,按目标并发连接数 N 设 `maxBridgeThreads ≥ 2 × N`(并视上游响应速度配合调大 `maxHandshakeThreads`);
4. 逐步逼近直连上游的吞吐水平.

