# dawdler-services-dependencies

## 模块介绍

服务端引用的maven依赖.

### 1. pom中引入依赖

```xml
<dependencyManagement>
  <dependencies>
   <dependency>
    <groupId>io.github.dawdler-series</groupId>
    <artifactId>dawdler-services-dependencies</artifactId>
    <version>${dawdler.build.version}</version>
    <type>pom</type>
    <scope>import</scope>
   </dependency>
  </dependencies>
 </dependencyManagement>
```
