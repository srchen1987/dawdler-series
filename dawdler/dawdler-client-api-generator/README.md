# dawdler-client-api-generator

## 模块介绍

基于java源码doc生成兼容swagger-ui的OpenAPI 3.0的json工具,对源代码零侵入,上手简单,生成效率高,使用非常方便.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-client-api-generator</artifactId>
```

### 2. dawdler-web-api.yml配置文件说明

例:

```yml
version: 1.0
title: "演示服务"
description: "用于演示的文档"
contact:
 name: "jackson.song"
 email: "suxuan696@gmail.com"
 url: "https://github.com/srchen1987"
swagger: "2.0"
host: "localhost"
basePath: "/"
scanPath:
 - "/home/srchen/github/api-demo"
outPath: "/home/srchen/github/api-demo/demo-api.json"
```

说明：

| 名称 | 说明 |
| :-: | :-: |
| version | 版本号 |
| title | 标题 |
| description | 描述 |
| contact | 联系人 相关信息|
| swagger | swagger版本号 |
| host | api地址,例如: 192.168.1.55:8080 |
| basePath | web的basePath |
| scanPath | 扫描路径,Controller或实体对象(数组结构) |
| outPath | 输出json的路径 |

### 3. 使用方法

#### 3.1 基础配置

参考dawdler-web-api.yml文件进行配置,确保scanPath配置正确.

#### 3.2 必要条件

scanPath配置一定要配置正确,确保路径下有Controller(必须使用@Controller注解的才会生效).

方法返回值需要使用@ResponseBody标注,如果非基础类型或String或BigDecimal则需要在scanPath内才会生效.

方法参数值使用@RequestBody标注的对象需要在scanPath内才会生效.

返回的对象需要有@ResponseBody标注,如果不支持的类型则不会解析,比如Map.

#### 3.3 生成api文件

```shell
java -jar dawdler-client-api-generator-0.0.2-RELEASES.jar   /home/srchen/github/api-demo/dawdler-web-api.yml
```

运行后会生成demo-api.json(outPath配置的路径).

#### 3.4 启动swagger-ui

拉取docker镜像

```shell
docker pull swaggerapi/swagger-ui
```

启动

```shell
docker run -p 80:8080 -e BASE_URL=/swagger -e SWAGGER_JSON=/foo/demo-api.json -v /home/srchen/github/api-demo:/foo swaggerapi/swagger-ui
```

访问 <http://localhost/swagger> 既可使用.

#### 4. 已支持javaDoc的Tag/注解/对象

##### 4.1 JavaDoc的Tag

1. @Description 用于类或方法的描述信息,支持放在类上或方法上.(同时支持javadoc标准 方法上注释,可不编写@Description)

2. @param 用于方法参数对应的注释信息,只支持方法上.

注释上有 @param userId 用户ID, 方法参数列表中的userId就会被备注为名字.

例如:

```java
 /**
  * 
 * @Title: get 
 * @author jackson.song 
 * @date 2022年3月23日
 * @Description 根据用户ID查询用户
 * @param userId 用户ID
 *
  */
 @RequestMapping(value = "/user/get",method = RequestMethod.GET)
 @ResponseBody
 public User get(String userId){
  return null;
 }

```

##### 4.2 dawdler-client-plug的注解

1. @Controller 标识一个类为Controller,只有此标识才会被扫描生成文档,用于类上.

2. @RequestMapping 标识设置请求api的path,只有此标识才会被扫描生成文档,支持设置类上和方法上.

3. @RequestParam 指定request请求参数名的注解,可以用搭配@param来做注释,注意RequestParam中的value需要与@param的值对应才生效.

4. @PathVariable 获取pathVariable的变量,用于方法参数列表中,可以用搭配@param来做注释.

5. @RequestHeader 获取head中的值,用于方法参数列表中,可以搭配@param来做注释.

6. @RequestBody 标识一个对象通过http body方式传入.

7. @ResponseBody 标识返回对象.

##### 4.3 方法参数列表支持的对象类型(dawdler-client-plug中支持的对象)

1. UploadFile 用于上传文件时使用的对象,可以搭配@param来做注释.

2. java8大基础类型 用于获取http请求参数,可以搭配@param来做注释.

3. String类型 用于获取http请求参数,可以搭配@param来做注释.

4. BigDecimal 用于获取http请求参数,可以搭配@param来做注释.

5. 自定义对象(通过@RequestBody标识时需要http body,如果没有@RequestBody标识,则自定义对象的属性会作为http param参数) 注释只支持在自定义对象中加入注解,注释采用/**注释**/ 例如:

```java
public class User {
 /**
  * 用户ID
  */
 private Integer userId;
 /**
  * 用户名
  */
 private String userName;
 /**
  * 地址
  */
 private String address;

 public Integer getUserId() {
  return userId;
 }
 public void setUserId(Integer userId) {
  this.userId = userId;
 }
 public String getUserName() {
  return userName;
 }
 public void setUserName(String userName) {
  this.userName = userName;
 }
 public String getAddress() {
  return address;
 }
 public void setAddress(String address) {
  this.address = address;
 }
}

```

#### 5. 返回数据类型

1. 8大基础类型及数组

2. String类型及数组

3. BigDecimal及数组

4. List<类型>/Set<类型>/Collection<类型>/Vector<类型>

5. 自定义类,支持泛型.

#### 6. 演示demo

DemoController是Controller的一个例子.

```java

package com.anywide.dawdler.demo.api.controller;

import java.util.List;

import com.anywide.dawdler.clientplug.annotation.Controller;
import com.anywide.dawdler.clientplug.annotation.PathVariable;
import com.anywide.dawdler.clientplug.annotation.RequestBody;
import com.anywide.dawdler.clientplug.annotation.RequestHeader;
import com.anywide.dawdler.clientplug.annotation.RequestMapping;
import com.anywide.dawdler.clientplug.annotation.RequestMapping.RequestMethod;
import com.anywide.dawdler.clientplug.annotation.ResponseBody;
import com.anywide.dawdler.clientplug.web.upload.UploadFile;
import com.anywide.dawdler.demo.api.BaseResult;
import com.anywide.dawdler.demo.api.InnerResult;
import com.anywide.dawdler.demo.api.entity.User;
import com.anywide.dawdler.demo.api.entity.UserBank;

/**
 * 
 * @ClassName: DemoController
 * @Description 演示的demo
 * @author jackson.song
 * @date 2022年3月23日
 *
 */
@Controller
public class DemoController {

 /**
  * 根据用户ID查询用户
  * @Title: get
  * @author jackson.song
  * @date 2022年3月23日
  * @param userId 用户ID
  **/

 @RequestMapping(value = "/user/get", method = RequestMethod.GET)
 @ResponseBody
 public User get(String userId) {
  return null;
 }

 /**
  * 
  * @Title: getUserByUserId
  * @author jackson.song
  * @date 2022年3月23日
  * @Description 根据用户id查询用户,antPath方式
  * @param id 用户ID
  **/

 @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
 @ResponseBody
 public User getUserByUserId(@PathVariable("id") int id) {
  return null;
 }

 /**
  * 
  * @Title: getUserComposite
  * @author jackson.song
  * @date 2022年3月23日
  * @Description 根据用户ID查询并返回复合bean
  * @param userId 用户ID
  *
  */

 @RequestMapping(value = "/user/getUserComposite", method = RequestMethod.POST)
 @ResponseBody
 public BaseResult<InnerResult<InnerResult<User>>, InnerResult<UserBank>> getUserComposite(int userId) {
  return null;
 }

 /**
  * 
  * @Title: getUserComposite2
  * @author jackson.song
  * @date 2022年3月23日
  * @Description 根据用户ID查询并返回复合bean 返回结果List结构
  * @param userId
  * @return
  *
  */
 @RequestMapping(value = "/user/getUserComposite2", method = RequestMethod.POST)
 @ResponseBody
 public BaseResult<List<User>, UserBank> getUserComposite2(int userId) {
  return null;
 }

 /**
  * 
  * @Title: create
  * @author jackson.song
  * @date 2022年3月23日
  * @Description 创建一个用户
  **/
 @RequestMapping(value = "/user/create", method = RequestMethod.POST)
 @ResponseBody
 public boolean create(@RequestBody User user) {
  return false;
 }

 /**
  * 
  * @Title: headTest
  * @author jackson.song
  * @date 2022年3月23日
  * @Description 头部获取信息测试
  * @param token 请求头信息
  * 
  **/

 @RequestMapping(value = "/head/test", method = RequestMethod.GET)
 public String headTest(@RequestHeader("token") String token) {
  return token;
 }

 /**
  * 
  * @Title: testFileUpload
  * @author jackson.song
  * @date 2022年3月23日
  * @Description 测试文件上传
  * @param id   业务Id
  * @param file 文件
  * @return
  * 
  **/

 @RequestMapping(value = "/testFileUpload", method = RequestMethod.POST)
 @ResponseBody
 public boolean testFileUpload(Integer id, UploadFile[] file) {
  return false;
 }

 /**
  * 
  * @Title: createUser
  * @author jackson.song
  * @date 2022年3月23日
  * @Description 根据User实体对象的属性生成对应的Http param参数
  *
  */
 @RequestMapping(value = "/createUser", method = RequestMethod.POST)
 @ResponseBody
 public User createUser(User user) {
  return null;
 }

}


```

User 实体类

```java
package com.anywide.dawdler.demo.api.entity;

/**
 * 
* @ClassName: User 
* @Description: 用户实体
* @author jackson.song
* @date 2022年3月23日
*
 */
public class User {
 /**
  * 用户ID
  */
 private Integer userId;
 
 /**
  * 用户名
  */
 private String userName;
 
 /**
  * 地址
  */
 private String address;

 public Integer getUserId() {
  return userId;
 }

 public void setUserId(Integer userId) {
  this.userId = userId;
 }

 public String getUserName() {
  return userName;
 }

 public void setUserName(String userName) {
  this.userName = userName;
 }

 public String getAddress() {
  return address;
 }

 public void setAddress(String address) {
  this.address = address;
 }

}

```

UserBank实体类

```java
package com.anywide.dawdler.demo.api.entity;

public class UserBank {

 /** 银行卡ID */
 private int cardid;
 /** userid */
 private int userid;
 /** 银行类型id */
 private int bankid;
 /** 开户行 */
 private String openBank;
 /** 持卡人姓名 */
 private String username;
 /** 银行卡号 */
 private String cardNumber;
 /** 是否有效，1为有效 */
 private Boolean visible;
 /** 创建时间 */
 private int createDate;
 /** 创建者 */
 private int creator;
 
 public int getCardid() {
  return cardid;
 }
 public void setCardid(int cardid) {
  this.cardid = cardid;
 }
 public int getUserid() {
  return userid;
 }
 public void setUserid(int userid) {
  this.userid = userid;
 }
 public int getBankid() {
  return bankid;
 }
 public void setBankid(int bankid) {
  this.bankid = bankid;
 }
 public String getOpenBank() {
  return openBank;
 }
 public void setOpenBank(String openBank) {
  this.openBank = openBank;
 }
 public String getUsername() {
  return username;
 }
 public void setUsername(String username) {
  this.username = username;
 }
 public String getCardNumber() {
  return cardNumber;
 }
 public void setCardNumber(String cardNumber) {
  this.cardNumber = cardNumber;
 }
 public Boolean getVisible() {
  return visible;
 }
 public void setVisible(Boolean visible) {
  this.visible = visible;
 }
 public int getCreateDate() {
  return createDate;
 }
 public void setCreateDate(int createDate) {
  this.createDate = createDate;
 }
 public int getCreator() {
  return creator;
 }
 public void setCreator(int creator) {
  this.creator = creator;
 }


}


```

BaseResult 返回基础类

```java
package com.anywide.dawdler.demo.api;

public class BaseResult<T, S> {

 private T data;

 private S dataS;

 /**
  * 信息
  */
 private String message;

 /**
  * 是否成功
  */
 private Boolean success;

 public S getDataS() {
  return dataS;
 }

 public void setDataS(S dataS) {
  this.dataS = dataS;
 }

 public T getData() {
  return data;
 }

 public void setData(T data) {
  this.data = data;
 }

 public String getMessage() {
  return message;
 }

 public void setMessage(String message) {
  this.message = message;
 }

 public Boolean getSuccess() {
  return success;
 }

 public void setSuccess(Boolean success) {
  this.success = success;
 }

}


```

InnerResult 为了测试多层泛型建的临时测试类

```java

package com.anywide.dawdler.demo.api;

import java.util.List;

import com.anywide.dawdler.demo.api.entity.UserBank;

public class InnerResult<F> {
 private F datas;
 /**
  * 状态
  */
 private int status;
 private List<UserBank> userBanks;

 public int getStatus() {
  return status;
 }

 public void setStatus(int status) {
  this.status = status;
 }

 public F getDatas() {
  return datas;
 }

 public void setDatas(F datas) {
  this.datas = datas;
 }

 public List<UserBank> getUserBanks() {
  return userBanks;
 }

 public void setUserBanks(List<UserBank> userBanks) {
  this.userBanks = userBanks;
 }
}


```
