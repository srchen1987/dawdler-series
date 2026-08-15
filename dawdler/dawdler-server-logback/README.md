# dawdler-server-logback

## 模块介绍

dawdler-server-logback 是dawdler服务端的日志实现模块,基于logback扩展,通过自定义SLF4J ServiceProvider实现多模块(多类加载器)日志隔离.

dawdler-server以隔离的类加载器(DeployClassLoader)部署多个服务,标准logback使用全局唯一的LoggerContext,会导致各服务之间日志配置相互覆盖、冲突.本模块以当前线程上下文类加载器为key,为每个部署模块维护独立的LoggerContext,使每个服务可独立加载自己的logback.xml配置,互不影响.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-server-logback</artifactId>
```

### 2. 实现说明

本模块通过SPI机制注册自定义的SLF4J ServiceProvider,注册文件位于:

```java
src/main/resources/META-INF/services/org.slf4j.spi.SLF4JServiceProvider
```

内容为:

```java
club.dawdler.server.log.DawdlerLogbackServiceProvider
```

核心类说明:

#### 2.1 DawdlerLogbackServiceProvider

实现了`org.slf4j.spi.SLF4JServiceProvider`接口,核心逻辑在`getLoggerFactory()`方法中:

- 以当前线程上下文类加载器(Thread.currentThread().getContextClassLoader())为key.
- 通过`Map<ClassLoader, LoggerContext>`缓存每个类加载器对应的LoggerContext.
- 首次获取时创建LoggerContext,调用`DawdlerLogbackContextInitializer`完成自动配置,并启动该上下文.
- 同一类加载器多次获取返回同一个LoggerContext,保证模块内日志上下文一致.

#### 2.2 DawdlerLogbackContextInitializer

负责单个LoggerContext的初始化与配置加载,配置文件查找顺序与标准logback一致:

1. 系统属性`logback.configurationFile`指定的配置文件.
2. classpath下的`logback-test.xml`.
3. classpath下的`logback.xml`.

找到配置文件后通过`JoranConfigurator`进行解析;若未找到任何配置文件,则回退到`BasicConfigurator`提供基础控制台输出.

### 3. 配置方式

各部署模块在自身classpath根目录放置`logback.xml`即可,配置方式与标准logback完全一致.

示例:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

### 4. 使用前提

- 本模块仅适用于dawdler-server服务端,dawdler-server部署各服务时使用隔离的类加载器,本模块依赖该机制实现日志隔离.
- classpath中不要同时引入标准`logback-classic`的SLF4J绑定(其默认ServiceProvider为`ch.qos.logback.classic.spi.LogbackServiceProvider`),以免与本模块产生SPI冲突.本模块已通过`logback-classic`提供logback核心能力,无需额外绑定.
