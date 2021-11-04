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
package com.anywide.dawdler.distributed.transaction.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;

import com.anywide.dawdler.distributed.transaction.context.DistributedTransactionContext;

/**
 *
 * @Title TransactionInterceptInvoker.java
 * @Description 事务执行拦截器，主要是针对不同的响应结果做拦截
 * @author jackson.song
 * @date 2021年4月10日
 * @version V1.0
 * @email suxuan696@gmail.com
 */
public interface TransactionInterceptInvoker {

	Object invoke(ProceedingJoinPoint invocation, DistributedTransactionContext distributedTransactionContext) throws Throwable;

}
