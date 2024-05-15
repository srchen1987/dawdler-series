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
package com.anywide.dawdler.remote.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jackson.song
 * @version V1.0
 * @Title RemoteServiceAssistant.java
 * @Description 应用在服务接口的方法上. 为服务的方法做特定的异步,超时时间,模糊匹配,负载均衡方式的设置.
 *  一般不会使用,有特殊需要时配置即可.
 * @date 2015年4月26日
 * @email suxuan696@gmail.com
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface RemoteServiceAssistant {
	boolean async() default false;// 在客户端有效,是否为异步执行.

	int timeout() default 120;// 在调用端有效,调用远程服务的超时事件,单位为秒,默认120秒.

	boolean fuzzy() default true;// 在调用端有效,是否模糊匹配方法,默认为true,模糊匹配根据方法名与参数个数进行匹配,非模糊匹配会根据方法名与参数类型进行精确匹配.模糊匹配效率高,如果一个服务实现类中存在相同方法相同参数个数时需要设置此参数为true.

	String loadBalance() default "roundRobin";// 调用端有效,负载方式
}
