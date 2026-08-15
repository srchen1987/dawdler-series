# dawdler-client-plug-jwt

## 模块介绍

jwt鉴权模块,基于dawdler-client-plug-web的web架构提供token的签发,解析,校验与请求鉴权.无需第三方依赖,使用JDK原生实现HMAC(HS256/384/512)与RSA(RS256/384/512)签名.

通过SPI自动注册`DawdlerJwtFilter`,其`web-fragment.xml`声明在web模块之前,确保在`ViewFilter`之前执行.

### 1. pom中引入依赖

```xml
<groupId>club.dawdler</groupId>
<artifactId>dawdler-client-plug-jwt</artifactId>
```

引入后过滤器自动生效,无需额外配置(无配置时使用jar内默认配置).

### 2. jwt.properties文件说明

在classpath下创建`jwt.properties`覆盖默认配置,支持统一配置中心与多环境(参考`dawdler.profiles.active`).

```properties
# 签名算法,支持 HS256 HS384 HS512 RS256 RS384 RS512
algorithm=HS256
# HMAC密钥(HS*算法使用),生产环境务必修改
secret=your-hmac-secret
# RSA私钥(RS*算法签发时使用),支持base64(DER)或PEM格式
rsaPrivateKey=
# RSA公钥(RS*算法验签时使用),支持base64(DER)或PEM格式
rsaPublicKey=

# token获取方式,可选 header cookie param,可组合用逗号分隔
tokenFrom=header
headerName=Authorization
headerPrefix=Bearer 
cookieName=token
paramName=token

# 签发者,校验时要求token的iss与之匹配,为空则不校验
issuer=
# 受众,校验时要求token的aud包含此项,为空则不校验
audience=
# token过期时间(秒),签发时未显式设置exp时使用
expireSeconds=7200
# 允许的时钟偏移(秒),用于exp/nbf校验
leewaySeconds=0

# token无效或缺失时是否拦截请求(返回401),false则仅解析填充上下文不拦截
rejectOnInvalid=true
# 无需校验的uri白名单,逗号分隔,支持antPath,如 /login,/public/**
excludePaths=/login,/public/**
# 拦截时返回的http状态码
unauthorizedStatus=401
# 拦截时返回的响应体
unauthorizedBody={"code":401,"msg":"unauthorized"}
```

### 3. 签发token

业务侧(如登录成功后)通过`JwtConfig`获取`JwtOperator`签发token.

```java
import java.time.Instant;
import club.dawdler.clientplug.web.jwt.JwtConfig;
import club.dawdler.clientplug.web.jwt.claim.JwtClaims;
import club.dawdler.clientplug.web.jwt.operator.JwtOperator;

JwtOperator operator = JwtConfig.getInstance().getJwtOperator();
String token = operator.sign(JwtClaims.create()
        .setSubject(userId)
        .setIssuer("dawdler")
        .setAudience("web")
        .setExpiration(Instant.now().plusSeconds(7200)));
// 将token返回给前端,前端通过Authorization: Bearer <token>携带
```

说明: `sign`时若未设置`iat`会自动补充为当前时间,若未设置`exp`且`expireSeconds>0`会自动补充过期时间.

### 4. 获取当前登录用户

鉴权通过后,过滤器会自动将claims绑定到当前线程与请求属性,业务侧通过`JwtContext`获取.

```java
import club.dawdler.clientplug.web.jwt.context.JwtContext;
import club.dawdler.clientplug.web.jwt.claim.JwtClaims;

JwtClaims claims = JwtContext.get();          // 线程级,可为null
String userId = claims.getSubject();
Object role = claims.getClaim("role");
```

### 5. 鉴权行为

- 命中`excludePaths`白名单(支持antPath)的请求直接放行,不做校验.
- 其余请求按`tokenFrom`顺序提取token,校验签名,`exp`,`nbf`,`iss`,`aud`.
- 校验通过: 绑定`JwtContext`,继续执行后续过滤器与Controller.
- 校验失败或缺失: 若`rejectOnInvalid=true`(默认)则返回`unauthorizedStatus`与`unauthorizedBody`并终止; 若为`false`则不绑定上下文直接放行,由业务自行判断`JwtContext.get()`是否为空.

### 6. RSA密钥说明

`rsaPrivateKey`为PKCS#8格式,`rsaPublicKey`为X.509格式,均可传base64(DER)或PEM字符串(自动去除`-----BEGIN/END-----`与换行).签发token需要私钥,校验token需要公钥,典型场景为网关/服务端只配置公钥做验签,认证中心配置私钥做签发.

### 7. 统一配置中心与多环境支持

参考[统一配置中心与多环境支持](../../doc/dawdler-profiles.active-README.md)
