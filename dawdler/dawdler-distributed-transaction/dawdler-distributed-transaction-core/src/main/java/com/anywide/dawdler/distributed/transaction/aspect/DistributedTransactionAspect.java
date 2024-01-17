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
package com.anywide.dawdler.distributed.transaction.aspect;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.rpc.context.RpcContext;
import com.anywide.dawdler.distributed.transaction.TransactionStatus;
import com.anywide.dawdler.distributed.transaction.annotation.DistributedTransaction;
import com.anywide.dawdler.distributed.transaction.context.DistributedTransactionContext;
import com.anywide.dawdler.distributed.transaction.interceptor.TransactionInterceptInvoker;
import com.anywide.dawdler.distributed.transaction.interceptor.TransactionInterceptInvokerHolder;
import com.anywide.dawdler.distributed.transaction.message.MessageSender;
import com.anywide.dawdler.distributed.transaction.message.amqp.rabbitmq.AMQPSender;
import com.anywide.dawdler.distributed.transaction.repository.RedisRepository;
import com.anywide.dawdler.distributed.transaction.repository.TransactionRepository;
import com.anywide.dawdler.util.JsonProcessUtil;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DistributedTransactionAspect.java
 * @Description aop拦截器 根据事务注解来做事务处理
 * @date 2021年4月10日
 * @email suxuan696@gmail.com
 */
@Aspect
public class DistributedTransactionAspect {
	private static Logger logger = LoggerFactory.getLogger(DistributedTransactionAspect.class);

	private TransactionRepository transactionRepository = new RedisRepository();

	private MessageSender messageSender = new AMQPSender();

	@Pointcut("@annotation(com.anywide.dawdler.distributed.transaction.annotation.DistributedTransaction)")
	public void compensableService() {
	}

	@Around("compensableService()")
	public Object interceptCompensableMethod(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
		Method method = methodSignature.getMethod();
		DistributedTransaction dt = method.getAnnotation(DistributedTransaction.class);
		DistributedTransactionContext dc = DistributedTransactionContext.getDistributedTransactionContext();
		if (logger.isDebugEnabled()) {
			logger.debug("transaction start method:{} action:{}", method.getName(), dt.action());
		}
		if (dt.sponsor()) {
			if (dc != null) {
				throw new IllegalStateException(
						"This transaction have the sponsor # globalTxId:\t" + dc.getGlobalTxId());
			}
			String globalTxId = UUID.randomUUID().toString();
			dc = new DistributedTransactionContext(globalTxId);
			dc.init();
			DistributedTransactionContext.setDistributedTransactionContext(dc);
			String action = dt.action();
			dc.setAction(action);
			Object obj = null;
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("transaction proceed sponsor:{} action:{}", dc.getGlobalTxId(), action);
				}
				obj = pjp.proceed();
			} catch (Throwable e) {
				if (!dc.isIntervene()) {
					cancel(action, globalTxId);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("transaction proceed exception sponsor:{} action:{} ", dc.getGlobalTxId(), action);
				}
				throw e;
			} finally {
				DistributedTransactionContext.setDistributedTransactionContext(null);
			}
			if (!dc.isIntervene()) {
				if (dc.isCancel()) {
					try {
						if (logger.isDebugEnabled()) {
							logger.debug("transaction proceed cancel sponsor:{} action:{} ", dc.getGlobalTxId(),
									action);
						}
						cancel(action, globalTxId);
					} catch (Throwable e) {
						logger.error("distributed_transaction_cancel ", e);
					}
				} else {
					try {
						if (logger.isDebugEnabled()) {
							logger.debug("transaction proceed confirm sponsor:{} action:{} ", dc.getGlobalTxId(),
									action);
						}
						confirm(action, globalTxId);
					} catch (Throwable e) {
						logger.error("distributed_transaction_confirm ", e);
					}
				}
			}
			return obj;
		} else {
			if (dc != null) {
				if (!dc.isCancel()) {
					String globalTxId = dc.getGlobalTxId();
					DistributedTransactionContext branchContext = new DistributedTransactionContext(globalTxId);
					branchContext.init();
					branchContext.setAction(dt.action());
					branchContext.setStatus(TransactionStatus.TRYING);
					RpcContext.getContext().setAttachment(
							DistributedTransactionContext.DISTRIBUTED_TRANSACTION_CONTEXT_KEY, branchContext);
					Object[] args = pjp.getArgs();
					Parameter[] parameters = method.getParameters();
					if (args.length > 0) {
						Map<String, Object> data = new HashMap<>(16);
						for (int i = 0; i < parameters.length; i++) {
							data.put(parameters[i].getName(), args[i]);
						}
						branchContext.setDatas(data);
					}
					Object obj = null;
					Throwable error = null;
					boolean success = true;
					try {
						if (logger.isDebugEnabled()) {
							logger.debug("transaction proceed globalTxid:{} branchTxId:{} action:{} create to redis",
									branchContext.getGlobalTxId(), branchContext.getBranchTxId(),
									branchContext.getAction());
						}
						transactionRepository.create(branchContext);
						TransactionInterceptInvoker invoker = TransactionInterceptInvokerHolder
								.getTransactionInterceptInvoker();
						if (invoker != null) {
							obj = invoker.invoke(pjp, dc);
						} else {
							obj = pjp.proceed();
						}
					} catch (Throwable e) {
						error = e;
						success = false;
					}
					if (!success) {
						if (logger.isDebugEnabled()) {
							logger.debug(
									"transaction proceed failed globalTxid:{} branchTxId:{} action:{} create to redis",
									branchContext.getGlobalTxId(), branchContext.getBranchTxId(),
									branchContext.getAction());
						}
						dc.setCancel(true);
						if (error != null) {
							throw error;
						}
					}
					return obj;
				}
				logger.warn("this transaction was cancel,globalTxid:{}.", dc.getGlobalTxId());
				return null;
			} else {
				try {
					return pjp.proceed();
				} catch (Throwable e) {
					throw e;
				}
			}
		}

	}

	public void confirm(String action, String globalTxId) throws Exception {
		Map<String, Object> data = new HashMap<>(8);
		data.put("status", TransactionStatus.CONFIRM);
		data.put("action", action);
		data.put("globalTxId", globalTxId);
		transactionRepository.updateDataByGlobalTxId(globalTxId, data);
		String msg = JsonProcessUtil.beanToJson(data);
		messageSender.sent(msg);
	}

	public void cancel(String action, String globalTxId) throws Exception {
		Map<String, Object> data = new HashMap<>(8);
		data.put("status", TransactionStatus.CANCEL);
		data.put("action", action);
		data.put("globalTxId", globalTxId);
		transactionRepository.updateDataByGlobalTxId(globalTxId, data);
		String msg = JsonProcessUtil.beanToJson(data);
		messageSender.sent(msg);
	}

}
