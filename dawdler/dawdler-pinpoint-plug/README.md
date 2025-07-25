# dawdler-pinpoint-plug

## 模块介绍

pinpoint链路追踪插件,升级到2.5.2 不需要对tomcat插件单独处理,[pinpoint-tomcat10-plugin](https://github.com/srchen1987/pinpoint-tomcat10-plugin/blob/main/README.md)不再维护了.

### 1. 下载pinpoint相关资源

下载pinpoint agent资源包

[pinpoint-agent-2.5.2.tar.gz](https://github.com/pinpoint-apm/pinpoint/releases/download/v2.5.2/pinpoint-agent-2.5.2.tar.gz)

下载两个jar包,分别为收集器与web-ui

hbase1.x 版本请下载:

[pinpoint-collector-boot-2.5.2.jar](https://github.com/pinpoint-apm/pinpoint/releases/download/v2.5.2/pinpoint-collector-boot-2.5.2.jar)

[pinpoint-web-boot-2.5.2.jar](https://github.com/pinpoint-apm/pinpoint/releases/download/v2.5.2/pinpoint-web-boot-2.5.2.jar)

hbase2.x 版本请下载:

[pinpoint-hbase2-collector-boot-2.5.2.jar](https://github.com/pinpoint-apm/pinpoint/releases/download/v2.5.2/pinpoint-hbase2-collector-boot-2.5.2.jar)

[pinpoint-hbase2-web-boot-2.5.2.jar](https://github.com/pinpoint-apm/pinpoint/releases/download/v2.5.2/pinpoint-hbase2-web-boot-2.5.2.jar)

### 2. 下载hbase1.x或hbase2.x

[hbase-1.7.1-bin.tar.gz](https://www.apache.org/dyn/closer.lua/hbase/1.7.1/hbase-1.7.1-bin.tar.gz)

[hbase-2.4.12-bin.tar.gz](https://www.apache.org/dyn/closer.lua/hbase/2.4.12/hbase-2.4.12-bin.tar.gz)

### 3. 将插件加入到收集器与web-ui的jar中

#### 3.1 下载pinpoint-dawdler-plugin

[dawdler-plugin](https://raw.githubusercontent.com/srchen1987/pinpoint-plugins/2.5.2/pinpoint-dawdler-plugin-2.5.2.jar)  此包是构建后的dawdler-pinpoint-plug插件.

#### 3.2 hbase1版本

将3.1下载好的dawdler插件加入到 pinpoint-web-boot-2.5.2.jar与pinpoint-collector-boot-2.5.2.jar

```shell
#创建BOOT-INF/lib/ 

mkdir -p BOOT-INF/lib/ 

cp pinpoint-dawdler-plugin-2.5.2.jar BOOT-INF/lib/

# tomcat10需要此jar包 如果用的tomcat9或之前请忽略此步骤
cp pinpoint-tomcat-plugin-2.5.2.jar BOOT-INF/lib/

#将插件加入到 pinpoint-web-boot-2.5.2.jar
jar -uvf0 pinpoint-web-boot-2.5.2.jar BOOT-INF/lib

#如果是zip 请使用 
zip -r0 pinpoint-web-boot-2.5.2.jar BOOT-INF/lib 

#将插件加入到 pinpoint-collector-boot-2.5.2.jar
jar -uvf0 pinpoint-collector-boot-2.5.2.jar BOOT-INF/lib

#如果是zip 请使用 
zip -r0 pinpoint-collector-boot-2.5.2.jar BOOT-INF/lib

```

#### 3.3 hbase2版本

将构建好的dawdler插件加入到 pinpoint-hbase2-web-boot-2.5.2.jar与pinpoint-hbase2-collector-boot-2.5.2.jar

```shell
#创建BOOT-INF/lib/ 

mkdir -p BOOT-INF/lib/ 

cp pinpoint-dawdler-plugin-2.5.2.jar BOOT-INF/lib/

# tomcat10需要此jar包 如果用的tomcat9或之前请忽略此步骤
cp pinpoint-tomcat-plugin-2.5.2.jar BOOT-INF/lib/

#将插件加入到 pinpoint-web-boot-2.5.2.jar
jar -uvf0 pinpoint-hbase2-web-boot-2.5.2.jar BOOT-INF/lib

#如果是zip 请使用 
zip -r0 pinpoint-hbase2-web-boot-2.5.2.jar BOOT-INF/lib 

#将插件加入到 pinpoint-collector-boot-2.5.2.jar
jar -uvf0 pinpoint-hbase2-collector-boot-2.5.2.jar BOOT-INF/lib

#如果是zip 请使用 
zip -r0 pinpoint-hbase2-collector-boot-2.5.2.jar  BOOT-INF/lib 

```

#### 3.4 添加logo(非必须,未添加只是不显示logo)

将DAWDLER.png,DAWDLER_CONSUMER.png,DAWDLER_PROVIDER.png添加到 pinpoint-web-boot-2.5.2.jar 或 pinpoint-hbase2-web-boot-2.5.2.jar

pinpoint-web-boot-2.5.2.jar 位置在 META-INF/resources/img/icons 与 META-INF/resources/img/servermap

pinpoint-hbase2-web-boot-2.5.2.jar 位置在 /lib/pinpoint-web-2.5.2.jar META-INF/resources/img/icons 与 META-INF/resources/img/servermap

添加方式通过 jar -uvf0 或 zip -r0 方式

### 4. 配置dawdler与tomcat的启动脚本

pinpoint是通过javaagent方式运行,所以需要配置相关的启动参数,下面给出具体操作步骤.

将下载的pinpoint-agent-2.5.2解压,获得pinpoint-agent-2.5.2目录.

将pinpoint-dawdler-plugin-2.5.2.jar 放入pinpoint-agent-2.5.2/plugin下.

编辑pinpoint-agent-2.5.2/profiles/release/pinpoint.config

dawdler-jdk1.8版本加入:

```txt
###########################################################
# DAWDLER                                                   #
###########################################################
profiler.dawdler.enable=true
# Classes for detecting application server type. Comma separated list of fully qualified class names. Wildcard not supported.
profiler.dawdler.bootstrap.main=club.dawdler.server.bootstrap.Bootstrap

```

将添加好配置文件的pinpoint-agent-2.5.2 分别复制到tomcat和dawdler的bin目录下.

编辑tomcat的启动脚本

```shell

JAVA_OPTS="-javaagent:pinpoint-agent-2.5.2/pinpoint-bootstrap.jar -Dpinpoint.config=pinpoint-agent-2.5.2/pinpoint-root.config -Dpinpoint.agentId=user-api-01 -Dpinpoint.applicationName=user-api -Dpinpoint.applicationName=user-api";


```

编辑dawdler的启动脚本

```shell

vm_arguments="-javaagent:pinpoint-agent-2.5.2/pinpoint-bootstrap.jar -Dpinpoint.config=pinpoint-agent-2.5.2/pinpoint-root.config -Dpinpoint.agentId=user-service-01  -Dpinpoint.applicationName=user-service -Dpinpoint.agentName=user-service";

```

### 5. 启动hbase,pinpoint收集器,web-ui,dawdler,tomcat

启动 hbase (出现端口问题请自行解决,需要做hbase,zookeeper集群请自行搭建)

```shell

sh start-hbase.sh

```

启动后 执行初始化脚本  [具体参考](https://github.com/pinpoint-apm/pinpoint/tree/2.5.x/hbase/scripts)

启动 collector  (收集器)

```shell

/usr/lib/jvm/java-11-openjdk-11.0.15.0.10-1.fc36.x86_64/bin/java -jar -Dpinpoint.zookeeper.address=127.0.0.1 pinpoint-hbase2-collector-boot-2.5.2.jar

```

启动 web 控制台 (web-ui)

```shell

/usr/lib/jvm/java-11-openjdk-11.0.15.0.10-1.fc36.x86_64/bin/java -jar -Dpinpoint.zookeeper.address=127.0.0.1 pinpoint-hbase2-web-boot-2.5.2.jar

```

启动 dawdler

```shell

sh dawdler.sh start

```

启动 tomcat

```shell

sh catalina.sh start

```
