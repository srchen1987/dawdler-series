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
package com.anywide.dawdler.serverplug.db.mybatis.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.anywide.dawdler.serverplug.db.mybatis.DawdlerMybatisTransaction;
import com.anywide.dawdler.serverplug.db.transaction.LocalConnectionFactory;
import com.anywide.dawdler.util.TLS;

/**
 * @author jackson.song
 * @version V1.0
 * @Title SwitchConnectionAspect.java
 * @Description 通过aop切换数据库连接
 * @date 2021年5月8日
 * @email suxuan696@gmail.com
 */
@Aspect
public class SwitchConnectionAspect {

	@Around("execution(*  org.apache.ibatis.session.defaults.DefaultSqlSession.selectList(..)) && args(String,Object,org.apache.ibatis.session.RowBounds)")
	public Object select(ProceedingJoinPoint pjp) throws Throwable {
		try {
			TLS.set(DawdlerMybatisTransaction.CURRENT_CONNECTION, LocalConnectionFactory.getReadConnection());
			return pjp.proceed();
		} catch (Throwable t) {
			throw t;
		} finally {
			TLS.remove(DawdlerMybatisTransaction.CURRENT_CONNECTION);
		}
	}

	@Around("execution(*  org.apache.ibatis.session.defaults.DefaultSqlSession.update(..)) && args(String,Object)")
	public Object update(ProceedingJoinPoint pjp) throws Throwable {
		try {
			TLS.set(DawdlerMybatisTransaction.CURRENT_CONNECTION, LocalConnectionFactory.getWriteConnection());
			return pjp.proceed();
		} catch (Throwable t) {
			throw t;
		} finally {
			TLS.remove(DawdlerMybatisTransaction.CURRENT_CONNECTION);
		}
	}

}