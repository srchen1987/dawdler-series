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
package com.anywide.dawdler.schedule;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ScheduleOperator.java
 * @Description Schedule操作者
 * @date 2022年7月12日
 * @email suxuan696@gmail.com
 */
public class ScheduleOperator {
	private static Logger logger = LoggerFactory.getLogger(ScheduleOperator.class);
	private static final String TARGET = "target";
	private static final String TARGET_METHOD = "target_method";

	public static void shutdown() {
		try {
			SchedulerFactory.getInstance().scheduler.shutdown(true);
		} catch (Exception e) {
			logger.error("", e);
		}

		SchedulerFactory.getInstance().getInstances().forEach((k, v) -> {
			try {
				v.getScheduler().shutdown(true);
			} catch (Exception e) {
				logger.error("", e);
			}
		});
	}

	public static void addJob(String fileName, String cron, boolean concurrent, Object target, Method method)
			throws SchedulerException {
		Scheduler scheduler = SchedulerFactory.getInstance().getScheduler(fileName);
		JobDetail jobDetail;
		if (concurrent) {
			jobDetail = JobBuilder.newJob(ConcurrentInnnerJob.class).build();
		} else {
			jobDetail = JobBuilder.newJob(InnnerJob.class).build();
		}
		jobDetail.getJobDataMap().put(TARGET, target);
		jobDetail.getJobDataMap().put(TARGET_METHOD, method);
		CronTrigger cronTrigger = TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(cron))
				.build();
		scheduler.scheduleJob(jobDetail, cronTrigger);
	}

	public static void start() {
		try {
			SchedulerFactory.getInstance().scheduler.start();
		} catch (Exception e) {
			logger.error("", e);
		}
		SchedulerFactory.getInstance().getInstances().forEach((k, v) -> {
			try {
				v.getScheduler().start();
			} catch (Exception e) {
				logger.error("", e);
			}
		});
	}

	public static class SchedulerFactory {
		private Scheduler scheduler;
		private static final String PREFIX = ".properties";
		private final static SchedulerFactory INSTANCE = new SchedulerFactory();
		private Map<String, org.quartz.SchedulerFactory> INSTANCES = new ConcurrentHashMap<String, org.quartz.SchedulerFactory>(
				8);

		private SchedulerFactory() {
			org.quartz.SchedulerFactory schedulerFactory = new StdSchedulerFactory();
			try {
				scheduler = schedulerFactory.getScheduler();
			} catch (SchedulerException e) {
				logger.error("", e);
			}
		}

		public Scheduler getScheduler(String fileName) throws SchedulerException {
			if ("".equals(fileName)) {
				return scheduler;
			}
			org.quartz.SchedulerFactory factory = INSTANCES.get(fileName);
			if (factory == null) {
				factory = new StdSchedulerFactory(fileName + PREFIX);
				org.quartz.SchedulerFactory pre = INSTANCES.putIfAbsent(fileName, factory);
				if (pre != null) {
					factory = pre;
				}
			}
			return factory.getScheduler();
		}

		public Map<String, org.quartz.SchedulerFactory> getInstances() {
			return INSTANCES;
		}

		public void setInstances(Map<String, org.quartz.SchedulerFactory> instances) {
			this.INSTANCES = instances;
		}

		public static SchedulerFactory getInstance() {
			return INSTANCE;
		}

	}

	@DisallowConcurrentExecution
	public static class InnnerJob implements Job {

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			JobDataMap dataMap = context.getJobDetail().getJobDataMap();
			Method method = (Method) dataMap.get(TARGET_METHOD);
			try {
				method.invoke(dataMap.get(TARGET));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger.error("", e);
			}
		}

	}

	public static class ConcurrentInnnerJob implements Job {

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			JobDataMap dataMap = context.getJobDetail().getJobDataMap();
			Method method = (Method) dataMap.get(TARGET_METHOD);
			try {
				method.invoke(dataMap.get(TARGET));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger.error("", e);
			}
		}

	}
}
