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
package com.anywide.dawdler.local.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jackson.song
 * @version V1.0
 * 注入服务的注解(本地服务 用于代替老版本的@Service 新版本的@Service用于标注接口或实现类)
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface LocalService {
	//服务的类名,默认为空,则为注解所在父类或接口的全称. 与@Service中serviceName对应
	String serviceName() default "";
}
