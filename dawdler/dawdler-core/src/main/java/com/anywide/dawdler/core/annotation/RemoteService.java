/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anywide.dawdler.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
/**
 *
 * @Title RemoteService.java
 * @Description 标注一个类是远程提供服务的注解
 * @author jackson.song
 * @date 2015年4月26日
 * @version V1.0
 * @email suxuan696@gmail.com
 */
public @interface RemoteService {
	String value() default "";// 服务调用

	String group() default "";// 在调用端有效,指定服务端部署的服务名.

	boolean single() default true;// 在服务端有效,标识一个服务的实现类是否为单例.默认为单例.

	boolean async() default false;// 在客户端有效,是否为异步执行.

	boolean remote() default false;// 在服务端有效,标识是否是一个远程服务,一般不建议在服务端再次调用另一个服务,默认为否,调用本服务中的服务（适用事务传播）.

	int timeout() default 120;// 在调用端有效,调用远程服务的超时事件,单位为秒,默认120秒.

	boolean fuzzy() default true;// 在调用端有效,是否模糊匹配方法,默认为true,模糊匹配根据方法名与参数个数进行匹配,非模糊匹配会根据方法名与参数类型进行精确匹配.模糊匹配效率高,如果一个服务实现类中存在相同方法相同参数个数时需要设置此参数为true.

	String loadBalance() default "roundRobin";// 调用端有效,负载方式
}
