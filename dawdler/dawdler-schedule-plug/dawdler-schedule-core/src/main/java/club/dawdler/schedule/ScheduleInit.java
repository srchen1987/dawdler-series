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
package club.dawdler.schedule;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.schedule.annotation.Schedule;

/**
 * @author jackson.song
 * @version V1.0
 * Schedule初始化
 */
public class ScheduleInit {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleInit.class);
	private static Map<String, Object> scheduleCache = new ConcurrentHashMap<>();

	public static void initScheduler(Object target, Class<?> clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			Schedule schedule = method.getAnnotation(Schedule.class);
			if (schedule != null) {
				String key = method.toGenericString();
				Object object = scheduleCache.get(key);
				if (object == null) {
					scheduleCache.put(key, target);
					try {
						ScheduleOperator.addJob(schedule.fileName(), schedule.cron(), schedule.concurrent(), target,
								method);
					} catch (Throwable e) {
						logger.error("", e.getCause());
					}
				}
			}
		}

	}

}
