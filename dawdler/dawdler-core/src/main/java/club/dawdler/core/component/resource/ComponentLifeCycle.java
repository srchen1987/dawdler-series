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
package club.dawdler.core.component.resource;

/**
 * @author jackson.song
 * @version V1.0
 * 组件(redis,es,rabbitmq)生命周期接口 初始化与销毁, web端、dawdler服务器端会在容器初始化之前、销毁后调用
 */
public interface ComponentLifeCycle {

	default void prepareInit() throws Throwable {
	};

	default void init() throws Throwable {
	};

	default void afterInit() throws Throwable {
	};

	default void prepareDestroy() throws Throwable {
	};

	default void destroy() throws Throwable {
	};

	default void afterDestroy() throws Throwable {
	};
	 
}
