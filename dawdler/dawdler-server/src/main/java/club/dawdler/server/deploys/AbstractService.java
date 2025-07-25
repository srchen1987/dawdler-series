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
package club.dawdler.server.deploys;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.core.annotation.ListenerConfig;
import club.dawdler.core.component.injector.CustomComponentInjectionProvider;
import club.dawdler.core.component.injector.CustomComponentInjector;
import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.core.component.resource.ComponentLifeCycleProvider;
import club.dawdler.core.health.Health;
import club.dawdler.core.health.HealthIndicator;
import club.dawdler.core.health.HealthIndicatorProvider;
import club.dawdler.core.health.ServiceHealth;
import club.dawdler.core.health.Status;
import club.dawdler.core.loader.DeployClassLoader;
import club.dawdler.core.order.OrderData;
import club.dawdler.core.scan.DawdlerComponentScanner;
import club.dawdler.core.scan.component.reader.ClassStructureParser;
import club.dawdler.core.scan.component.reader.ClassStructureParser.ClassStructure;
import club.dawdler.core.service.ServicesManager;
import club.dawdler.core.service.bean.ServicesBean;
import club.dawdler.core.service.processor.DefaultServiceExecutor;
import club.dawdler.core.service.processor.ServiceExecutor;
import club.dawdler.server.conf.ServerConfig;
import club.dawdler.server.conf.ServerConfig.HealthCheck;
import club.dawdler.server.context.DawdlerContext;
import club.dawdler.server.filter.FilterProvider;
import club.dawdler.server.listener.DawdlerListenerProvider;
import club.dawdler.server.listener.DawdlerServiceListener;
import club.dawdler.server.service.conf.ServicesConfig;
import club.dawdler.util.SunReflectionFactoryInstantiator;
import club.dawdler.util.spring.antpath.AntPathMatcher;
import club.dawdler.util.spring.antpath.Resource;

/**
 * @author jackson.song
 * @version V1.0
 * 抽象服务
 */
public abstract class AbstractService implements Service {
	private static final Logger logger = LoggerFactory.getLogger(AbstractService.class);
	protected DeployClassLoader classLoader;
	protected DawdlerContext dawdlerContext = null;
	protected String deployName;
	private final ServiceExecutor defaultServiceExecutor = new DefaultServiceExecutor();
	private final DawdlerListenerProvider dawdlerListenerProvider = new DawdlerListenerProvider();
	private final ServicesManager servicesManager = new ServicesManager();
	private final FilterProvider filterProvider = new FilterProvider();
	private ServiceExecutor serviceExecutor = defaultServiceExecutor;
	protected AntPathMatcher antPathMatcher;
	protected volatile String status;
	protected Throwable cause;
	protected HealthCheck healthCheck;
	protected ExecutorService healthCheckExecutor;

	protected AbstractService(ServerConfig serverConfig, String deployName, Semaphore startSemaphore,
			AtomicBoolean started) throws Exception {
		this.deployName = deployName;
		this.healthCheck = serverConfig.getHealthCheck();
		if (healthCheck.isCheck()) {
			healthCheckExecutor = Executors.newCachedThreadPool();
		}
		String host = serverConfig.getServer().getHost();
		int port = serverConfig.getServer().getTcpPort();
		this.antPathMatcher = serverConfig.getAntPathMatcher();
		this.status = Status.STARTING;
		dawdlerContext = new DawdlerContext(deployName, host, port, servicesManager, antPathMatcher, healthCheck,
				startSemaphore, started, status);
	}

	@Override
	public void start() throws Throwable {
		List<OrderData<ComponentLifeCycle>> lifeCycleList = ComponentLifeCycleProvider.getInstance(deployName)
				.getComponentLifeCycles();
		dawdlerContext.setAttribute(FILTER_PROVIDER, filterProvider);
		dawdlerContext.setAttribute(DAWDLER_LISTENER_PROVIDER, dawdlerListenerProvider);
		for (int i = 0; i < lifeCycleList.size(); i++) {
			OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
			lifeCycle.getData().prepareInit();
		}
		ServicesConfig servicesConfig = dawdlerContext.getServicesConfig();

		Set<String> preLoadClasses = dawdlerContext.getServicesConfig().getPreLoads();
		if (preLoadClasses != null) {
			for (String preLoadClass : preLoadClasses) {
				classLoader.findClassForDawdler(preLoadClass, true, false);
			}
		}

		Map<String, Resource> removeDuplicates = new LinkedHashMap<>();

		List<OrderData<CustomComponentInjector>> customComponentInjectorList = CustomComponentInjectionProvider
				.getInstance(deployName).getCustomComponentInjectors();
		Set<String> packagePaths = servicesConfig.getPackagePaths();
		if (packagePaths != null) {
			for (String packageInClasses : packagePaths) {
				Resource[] resources = DawdlerComponentScanner.getClasses(packageInClasses);
				for (Resource resource : resources) {
					removeDuplicates.putIfAbsent(resource.getURL().toString(), resource);
				}
			}
		}

		Collection<Resource> resources = removeDuplicates.values();
		for (Resource resource : resources) {
			InputStream input = null;
			try {
				input = resource.getInputStream();
				ClassStructure classStructure = ClassStructureParser.parser(input);
				if (classStructure != null) {
					for (OrderData<CustomComponentInjector> data : customComponentInjectorList) {
						CustomComponentInjector customComponentInjector = data.getData();
						inject(resource, classStructure, customComponentInjector);
					}
				}
			} finally {
				if (input != null) {
					input.close();
				}
			}
		}

		for (OrderData<CustomComponentInjector> data : customComponentInjectorList) {
			CustomComponentInjector customComponentInjector = data.getData();
			String[] scanLocations = customComponentInjector.scanLocations();
			if (scanLocations != null) {
				for (String scanLocation : scanLocations) {
					Resource[] resourcesArray = DawdlerComponentScanner.getClasses(scanLocation);
					for (Resource resource : resourcesArray) {
						InputStream input = null;
						try {
							input = resource.getInputStream();
							ClassStructure classStructure = ClassStructureParser.parser(input);
							inject(resource, classStructure, customComponentInjector);
						} finally {
							if (input != null) {
								input.close();
							}
						}
					}
				}
			}
		}

		removeDuplicates.clear();
		servicesManager.getDawdlerServiceCreateProvider().order();
		servicesManager.fireCreate();
		dawdlerListenerProvider.order();
		filterProvider.orderAndBuildChain();

		for (int i = 0; i < lifeCycleList.size(); i++) {
			OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
			lifeCycle.getData().init();
		}
		if (healthCheck.isCheck()) {
			checkHealth();
		}
		dawdlerContext.removeAttribute(FILTER_PROVIDER);
		dawdlerContext.removeAttribute(DAWDLER_LISTENER_PROVIDER);
		for (OrderData<DawdlerServiceListener> orderData : dawdlerListenerProvider.getListeners()) {
			ListenerConfig listenerConfig = orderData.getClass().getAnnotation(ListenerConfig.class);
			if (listenerConfig != null && listenerConfig.asyn()) {
				new Thread(() -> {
					if (listenerConfig.delayMsec() > 0) {
						try {
							Thread.sleep(listenerConfig.delayMsec());
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						try {
							orderData.getData().contextInitialized(dawdlerContext);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}, "listenerThread").start();
			} else {
				orderData.getData().contextInitialized(dawdlerContext);
			}
		}

		for (int i = 0; i < lifeCycleList.size(); i++) {
			OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
			lifeCycle.getData().afterInit();
		}
	}

	private void inject(Resource resource, ClassStructure classStructure,
			CustomComponentInjector customComponentInjector) throws Throwable {
		boolean match = false;
		Class<?>[] matchTypes = customComponentInjector.getMatchTypes();
		if (matchTypes != null) {
			for (Class<?> matchType : matchTypes) {
				if (classStructure.getInterfaces().contains(matchType.getName())) {
					match = true;
					break;
				}
				if (classStructure.getClassName().equals(matchType.getName())
						|| classStructure.getSuperClasses().contains(matchType.getName())) {
					match = true;
					break;
				}
			}
		}
		if (!match) {
			final Set<? extends Class<? extends Annotation>> annotationSet = customComponentInjector
					.getMatchAnnotations();
			if (annotationSet != null) {
				for (Class<? extends Annotation> annotationType : annotationSet) {
					if (classStructure.getAnnotationNames().contains(annotationType.getName())) {
						match = true;
						break;
					}
				}
			}
		}
		if (match) {
			Class<?> clazz = classLoader.findClassForDawdler(classStructure.getClassName(), resource,
					customComponentInjector.useAop(), customComponentInjector.storeVariableNameByASM());
			if (customComponentInjector.isInject() && !classStructure.isAbstract() && !classStructure.isAnnotation()
					&& !classStructure.isInterface()) {
				Object target = SunReflectionFactoryInstantiator.newInstance(clazz);
				customComponentInjector.inject(clazz, target);
			}
		}
	}

	public FilterProvider getFilterProvider() {
		return filterProvider;
	}

	@Override
	public void prepareStop() {
		resetContextClassLoader();
		List<OrderData<ComponentLifeCycle>> lifeCycleList = ComponentLifeCycleProvider.getInstance(deployName)
				.getComponentLifeCycles();
		for (int i = lifeCycleList.size() - 1; i >= 0; i--) {
			try {
				OrderData<ComponentLifeCycle> liftCycle = lifeCycleList.get(i);
				liftCycle.getData().prepareDestroy();
			} catch (Throwable e) {
				logger.error("", e);
			}
		}
	}

	@Override
	public void stop() {
		status(Status.DOWN);
		if (healthCheck.isCheck()) {
			healthCheckExecutor.shutdown();
		}
		resetContextClassLoader();
		if (dawdlerListenerProvider.getListeners() != null) {
			for (int i = dawdlerListenerProvider.getListeners().size(); i > 0; i--) {
				try {
					dawdlerListenerProvider.getListeners().get(i - 1).getData().contextDestroyed(dawdlerContext);
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
		servicesManager.clear();
		List<OrderData<ComponentLifeCycle>> lifeCycleList = ComponentLifeCycleProvider.getInstance(deployName)
				.getComponentLifeCycles();
		for (int i = lifeCycleList.size() - 1; i >= 0; i--) {
			try {
				OrderData<ComponentLifeCycle> liftCycle = lifeCycleList.get(i);
				liftCycle.getData().destroy();
			} catch (Throwable e) {
				logger.error("", e);
			}
		}
		for (int i = lifeCycleList.size() - 1; i >= 0; i--) {
			try {
				OrderData<ComponentLifeCycle> liftCycle = lifeCycleList.get(i);
				liftCycle.getData().afterDestroy();
			} catch (Throwable e) {
				logger.error("", e);
			}
		}
	}

	public DawdlerContext getDawdlerContext() {
		return dawdlerContext;
	}

	@Override
	public ServiceExecutor getServiceExecutor() {
		return serviceExecutor;
	}

	protected void resetContextClassLoader() {
		Thread.currentThread().setContextClassLoader((ClassLoader) classLoader);
	}

	private void checkHealth() throws Throwable {
		getServiceHealth();
		if (cause != null) {
			throw cause;
		}
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public Throwable getCause() {
		return cause;
	}

	@Override
	public void status(String status) {
		dawdlerContext.setServiceStatus(status);
		this.status = status;
	}

	@Override
	public void cause(Throwable cause) {
		this.cause = cause;
	}

	@Override
	public ClassLoader getClassLoader() {
		return (ClassLoader) classLoader;
	}

	@Override
	public ServicesBean getServicesBean(String name) {
		return servicesManager.getService(name);
	}

	@Override
	public ServiceHealth getServiceHealth() {
		if (status.equals(Status.DOWN)) {
			ServiceHealth serviceHealth = new ServiceHealth(deployName);
			serviceHealth.setStatus(status);
			serviceHealth.addComponent("error", cause.getClass().getName() + ":" + cause.getMessage());
			return serviceHealth;
		}
		resetContextClassLoader();
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
					cause(e);
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
