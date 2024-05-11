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
package com.anywide.dawdler.core.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DBTransaction.java
 * @Description 事务注解，应用于service方法中
 * @date 2012年9月27日
 * @email suxuan696@gmail.com
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Inherited
public @interface DBTransaction {

	MODE mode() default MODE.deferToConfig;

	READ_CONFIG readConfig() default READ_CONFIG.idem;

	Class<? extends Throwable>[] noRollbackFor() default {};

	Propagation propagation() default Propagation.REQUIRED;

	Isolation isolation() default Isolation.DEFAULT;

	/**
	 * 
	 * <p>
	 * Title: timeOut
	 * </p>
	 * 
	 * @author jackson.song
	 * @date 2021年5月20日
	 * @return int Description: 设置事务超时时间 逻辑如下： if (queryTimeout == null ||
	 *         queryTimeout == 0 || transactionTimeout < queryTimeout) {
	 *         statement.setQueryTimeout(transactionTimeout); } mysql驱动代码如下： public
	 *         int getQueryTimeout() throws SQLException { synchronized
	 *         (checkClosed().getConnectionMutex()) { return getTimeoutInMillis() /
	 *         1000; } }
	 *
	 */
	int timeOut() default 0;

	enum MODE {
		forceReadOnWrite, // 强制读从写连接上，在做读写分离时需要根据插入数据做业务不能保证从库数据的实时性所以采用这种方式
		deferToConfig, // 根据本方法的注解定义
		readOnly// 只传入读连接
	}

	enum READ_CONFIG {
		idem, // 同上层定义
		deferToConfig// 根据本方法的注解定义
	}

}
