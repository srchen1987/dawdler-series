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
package club.dawdler.clientplug.web.health;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import club.dawdler.core.health.Health;
import club.dawdler.core.health.HealthIndicator;
import club.dawdler.core.health.HealthIndicatorProvider;
import club.dawdler.core.health.ServiceHealth;
import club.dawdler.core.health.Status;
import club.dawdler.core.order.OrderData;

/**
 * @author jackson.song
 * @version V1.0
 * WebHealth 提供web端健康检测的功能
 */
public class WebHealth {

	private String deployName;
	
	private HealthCheck healthCheck;

	private ExecutorService healthCheckExecutor;

	public WebHealth(String deployName, HealthCheck healthCheck) {
		this.deployName = deployName;
		this.healthCheck = healthCheck;
		healthCheckExecutor = Executors.newCachedThreadPool();
	}

	public ServiceHealth getServiceHealth() {
		ServiceHealth serviceHealth = new ServiceHealth(deployName);
		serviceHealth.setStatus(Status.STARTING);
		HealthIndicatorProvider healthChecker = HealthIndicatorProvider.getInstance(deployName);
		List<OrderData<HealthIndicator>> healthIndicators = healthChecker.getHealthIndicators();
		List<Future<Boolean>> checkResult = new ArrayList<>();
		for (OrderData<HealthIndicator> orderData : healthIndicators) {
			HealthIndicator healthIndicator = orderData.getData();
			if (!healthCheck.componentCheck(healthIndicator.name())) {
				continue;
			}
			java.util.concurrent.Callable<Boolean> call = (() -> {
				Health.Builder builder = Health.up();
				try {
					builder.setName(healthIndicator.name());
					Health componentHealth = healthIndicator.check(builder);
					serviceHealth.addComponent(componentHealth);
				} catch (Exception e) {
					serviceHealth.addComponent(Health.down(e).setName(builder.getName()).build());
					return true;
				}
				return false;
			});
			checkResult.add(healthCheckExecutor.submit(call));
		}
		String checkedStatus = Status.UP;
		for (Future<Boolean> future : checkResult) {
			try {
				if (future.get().booleanValue()) {
					checkedStatus = Status.DOWN;
				}
			} catch (ExecutionException e) {
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		serviceHealth.setStatus(checkedStatus);
		return serviceHealth;
	}

}
