# dawdler-mybatis-core

## 模块介绍

通过mybatis3.5.6进行改造,session变更为单例模式,支持读写分离,去除cache功能,一般不会单独使用,由dawdler-server-plug-mybatis依赖引入此模块即可.

### 1. pom中引入依赖

```xml
<groupId>dawdler</groupId>
<artifactId>dawdler-mybatis-core</artifactId>
```
