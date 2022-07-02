# dawdler-encrypt-generator

## 模块介绍

dawdler-encrypt-generator 用于生成密钥与加密内容的工具类.

### 1. 使用方法

#### 1.1 安装

通过mvn安装

```shell
mvn install 
```

#### 1.2 生成密钥文件并设置环境变量

安装后, 可以通过命令行运行dawdler-encrypt-generator生成密钥与加密内容.

```shell
java -jar dawdler-encrypt-generator-0.0.2-RELEASES.jar
```

运行后会生成加密用的密钥并存储在dawlder.password文件中.

提醒如下：

generated file:[/home/srchen/github/dawdler-series/dawdler/dawdler-encrypt-generator/target/dawdler.password]

please set DAWDLER_ENCRYP_FILE=/home/srchen/github/dawdler-series/dawdler/dawdler-encrypt-generator/target/dawdler.password to environment!

linux中设置环境变量

```shell
export DAWDLER_ENCRYP_FILE=/home/srchen/github/dawdler-series/dawdler/dawdler-encrypt-generator/target/dawdler.password
```

或者在.bashrc中添加

```shell
export DAWDLER_ENCRYP_FILE=/home/srchen/github/dawdler-series/dawdler/dawdler-encrypt-generator/target/dawdler.password
```

建议改变dawdler.password文件位置.

如：

```shell
mv /home/srchen/github/dawdler-series/dawdler/dawdler-encrypt-generator/target/dawdler.password /home/srchen/dawdler-server/dawdler.password
```

 环境变量设置在/home/srchen/dawdler-server/dawdler.password.

#### 1.3 加密内容

设置完成环境变量之后加密内容.

```shell
java -jar dawdler-encrypt-generator-0.0.2-RELEASES.jar 123456
#123456为原始密码
```

控制台输出

123456 -> [%2FkVZfdVQAuyD%2FXUNqzSQGw%3D%3D]

[]中为加密后的密码.

#### 1.4 在properties配置文件中使用加密后的密码

properties中支持加密内容需要使用 ENC(加密内容) 标识.

```properties
addr=localhost
port=6379
auth=ENC(%2FkVZfdVQAuyD%2FXUNqzSQGw%3D%3D)
database=15
max_active=20
max_idle=8
max_wait=10000
timeout=10000
test_on_borrow=true 
```

目前支持的模块有rabbitmq,redis,elasticSearch以及所有使用com.anywide.dawdler.util.PropertiesUtil来读取的自定义properties应用.

#### 1.5 在配置中心中使用加密后的密码

```yaml
type: com.zaxxer.hikari.HikariDataSource
jdbcUrl: jdbc:mysql://localhost:3306/sinopec?characterEncoding=utf8&allowPublicKeyRetrieval=true&useSSL=false
driverClassName: com.mysql.cj.jdbc.Driver
username: root
password: ENC(hTGici8M3cMAvJ6wrEXnwQ%3D%3D)
maximumPoolSize: 20
minimumIdle: 0
```
