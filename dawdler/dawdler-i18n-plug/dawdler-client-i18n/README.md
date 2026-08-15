# dawdler-client-i18n

## 模块介绍

客户端代码，用于在微服务的架构中的web端引用此模块传递国际化参数到服务端。

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-client-i18n</artifactId>
```

该模块通过Java SPI机制自动注册相关组件，无需额外配置。在web.xml中会自动注册相关的过滤器和监听器。

### 2. 使用方式

一般情况下，开发者无需直接使用此模块，而是通过[dawdler-client-plug-i18n](../dawdler-client-plug-i18n/README.md)模块提供的@I18nInjector注解来使用国际化功能。
