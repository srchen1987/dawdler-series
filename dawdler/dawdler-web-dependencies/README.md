# dawdler-web-dependencies

## 模块介绍

web端引用的maven依赖.

### 1. pom中引入依赖

```xml
<dependencyManagement>
  <dependencies>
   <dependency>
    <groupId>dawdler</groupId>
    <artifactId>dawdler-web-dependencies</artifactId>
    <version>${dawdler.build.version}</version>
    <type>pom</type>
    <scope>import</scope>
   </dependency>
  </dependencies>
 </dependencyManagement>
```
