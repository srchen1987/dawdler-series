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
package com.anywide.dawdler.server.deploys;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.annotation.ListenerConfig;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.annotation.RemoteService;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycleProvider;
import com.anywide.dawdler.core.discoverycenter.DiscoveryCenter;
import com.anywide.dawdler.core.exception.NotSetRemoteServiceException;
import com.anywide.dawdler.core.health.Health;
import com.anywide.dawdler.core.health.HealthIndicator;
import com.anywide.dawdler.core.health.HealthIndicatorProvider;
import com.anywide.dawdler.core.health.ServiceHealth;
import com.anywide.dawdler.core.health.Status;
import com.anywide.dawdler.core.order.OrderData;
import com.anywide.dawdler.server.bean.ServicesBean;
import com.anywide.dawdler.server.conf.ServerConfig;
import com.anywide.dawdler.server.conf.ServerConfig.HealthCheck;
import com.anywide.dawdler.server.conf.ServerConfig.Scanner;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.filter.DawdlerFilter;
import com.anywide.dawdler.server.filter.FilterProvider;
import com.anywide.dawdler.server.listener.DawdlerListenerProvider;
import com.anywide.dawdler.server.listener.DawdlerServiceListener;
import com.anywide.dawdler.server.service.ServicesManager;
import com.anywide.dawdler.server.service.listener.DawdlerServiceCreateListener;
import com.anywide.dawdler.server.thread.processor.DefaultServiceExecutor;
import com.anywide.dawdler.server.thread.processor.ServiceExecutor;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.SunReflectionFactoryInstantiator;
import com.anywide.dawdler.util.XmlObject;
import com.anywide.dawdler.util.spring.antpath.AntPathMatcher;
import com.anywide.dawdler.util.spring.antpath.StringUtils;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServiceBase.java
 * @Description deploy下服务模块具体实现类
 * @date 2015年3月22日
 * @email suxuan696@gmail.com
 */
public class ServiceBase implements Service {
	private static final Logger logger = LoggerFactory.getLogger(ServiceBase.class);
	public static final String SERVICE_EXECUTOR_PREFIX = "serviceExecutorPrefix";
	public static final String ASPECT_SUPPORT_OBJ = "aspectSupportObj";// aspect 支持
	public static final String ASPECT_SUPPORT_METHOD = "aspectSupportMethod";
	public static final String FILTER_PROVIDER = "filterProvider";
	public static final String DAWDLER_LISTENER_PROVIDER = "dawdlerListenerProvider";
	private static final String CLASSES_PATH = "classes";
	private static final String LIB_PATH = "lib";
	private final DawdlerDeployClassLoader classLoader;
	private final File deploy;
	private final String deployName;
	private final DawdlerContext dawdlerContext;
	private final ServiceExecutor defaultServiceExecutor = new DefaultServiceExecutor();
	private final DawdlerListenerProvider dawdlerListenerProvider = new DawdlerListenerProvider();
	private final ServicesManager servicesManager = new ServicesManager();
	private final FilterProvider filterProvider = new FilterProvider();
	private ServiceExecutor serviceExecutor = defaultServiceExecutor;
	private final static int WAIT_TIME_MILLIS = 1000;
	private final Scanner scanner;
	private final DeployScanner deployScanner;
	private AntPathMatcher antPathMatcher;
	private String status;
	private Throwable cause;
	private HealthCheck healthCheck;

	public ServiceBase(ServerConfig serverConfig, File deploy, ClassLoader parent) throws MalformedURLException {
		this.healthCheck = serverConfig.getHealthCheck();
		URL binPath = serverConfig.getBinPath();
		String host = serverConfig.getServer().getHost();
		int port = serverConfig.getServer().getTcpPort();
		this.scanner = serverConfig.getScanner();
		this.deployScanner = new DeployScanner();
		this.antPathMatcher = serverConfig.getAntPathMatcher();
		this.deploy = deploy;
		this.deployName = deploy.getName();
		this.status = Status.STARTING;
		classLoader = DawdlerDeployClassLoader.createLoader(binPath, parent, getClassLoaderURL());
		Thread.currentThread().setContextClassLoader(classLoader);
		dawdlerContext = new DawdlerContext(classLoader, deploy.getName(), deploy.getPath(), getClassesDir().getPath(),
				host, port, servicesManager, antPathMatcher);
		classLoader.setDawdlerContext(dawdlerContext);
		dawdlerContext.setServicesConfig(loadXML());
		try {
			Class<?> clazz = classLoader.loadClass("org.aspectj.weaver.loadtime.Aj");
			Object obj = clazz.getDeclaredConstructor().newInstance();
			Method initializeMethod = clazz.getMethod("initialize");
			initializeMethod.invoke(obj);
			Method preProcessMethod = clazz.getMethod("preProcess", String.class, byte[].class, ClassLoader.class,
					ProtectionDomain.class);
			dawdlerContext.setAttribute(ASPECT_SUPPORT_METHOD, preProcessMethod);
			dawdlerContext.setAttribute(ASPECT_SUPPORT_OBJ, obj);
		} catch (Exception e) {
		}
	}

	public ServicesBean getServicesBean(String name) {
		return servicesManager.getService(name);
	}

	public ServicesBean getServicesBeanNoSafe(String name) {
		return servicesManager.getService(name);
	}

	private File getClassesDir() {
		return new File(deploy, CLASSES_PATH);
	}

	private URL[] getClassLoaderURL() throws MalformedURLException {
		File file = new File(deploy, LIB_PATH);
		return PathUtils.getLibURL(file, getClassesDir().toURI().toURL());
	}

	public Class<?> getClass(String className) throws ClassNotFoundException {
		return classLoader.loadClass(className);
	}

	@Override
	public void start() throws Throwable {
		List<OrderData<ComponentLifeCycle>> lifeCycleList = ComponentLifeCycleProvider.getInstance(deployName)
				.getComponentLifeCycles();
		for (int i = 0; i < lifeCycleList.size(); i++) {
			OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
			lifeCycle.getData().prepareInit();
		}
		Object definedServiceExecutor = dawdlerContext.getAttribute(SERVICE_EXECUTOR_PREFIX);
		if (definedServiceExecutor != null) {
			serviceExecutor = (ServiceExecutor) definedServiceExecutor;
		}

		Element root = dawdlerContext.getServicesConfig().getRoot();

		List<Node> preLoadClasses = root.selectNodes("scanner/loads/pre-load");

		for (Node node : preLoadClasses) {
			classLoader.findClassForDawdler(node.getText().trim());
		}
		List<Node> packagesInClasses = root.selectNodes("scanner/packages-in-classes/package-path");
		for (Node node : packagesInClasses) {
			deployScanner.splitAndAddPathInClasses(node.getText().trim());
		}
		List<Node> packagesInJars = root.selectNodes("scanner/packages-in-jar/package-path");
		for (Node node : packagesInJars) {
			deployScanner.splitAndAddPathInJar(node.getText().trim());
		}

		Set<Class<?>> classes;
		classes = DeployClassesScanner.getClassesInPath(scanner, deployScanner, deploy);
		Set<Class<?>> serviceClasses = new HashSet<>();
		for (Class<?> c : classes) {
			if (((c.getModifiers() & 1024) != 1024) && ((c.getModifiers() & 16) != 16)
					&& ((c.getModifiers() & 16384) != 16384) && ((c.getModifiers() & 8192) != 8192)
					&& ((c.getModifiers() & 512) != 512)) {
				if (DawdlerServiceListener.class.isAssignableFrom(c)) {
					DawdlerServiceListener listener = (DawdlerServiceListener) SunReflectionFactoryInstantiator
							.newInstance(c);
					dawdlerListenerProvider.addListener(listener);
				}
				if (DawdlerServiceCreateListener.class.isAssignableFrom(c)) {
					DawdlerServiceCreateListener dl = (DawdlerServiceCreateListener) SunReflectionFactoryInstantiator
							.newInstance(c);
					servicesManager.getDawdlerServiceCreateProvider().addServiceCreate(dl);
				}
				if (DawdlerFilter.class.isAssignableFrom(c)) {
					Order order = c.getAnnotation(Order.class);
					DawdlerFilter filter = (DawdlerFilter) SunReflectionFactoryInstantiator.newInstance(c);
					OrderData<DawdlerFilter> orderData = new OrderData<>();
					orderData.setData(filter);
					if (order != null) {
						orderData.setOrder(order.value());
					}
					filterProvider.addFilter(filter);
				}

				if (servicesManager.isService(c)) {
					serviceClasses.add(c);
				}
			}
		}
		for (Class<?> c : serviceClasses) {
			servicesManager.smartRegister(c);
		}
		servicesManager.getDawdlerServiceCreateProvider().order();
		servicesManager.fireCreate(dawdlerContext);

		if (healthCheck.isCheck()) {
			checkHealth();
		}

		dawdlerListenerProvider.order();
		filterProvider.orderAndBuildChain();

		for (OrderData<DawdlerServiceListener> data : dawdlerListenerProvider.getListeners()) {
			injectService(data.getData());
		}

		for (OrderData<DawdlerFilter> data : filterProvider.getFilters()) {
			injectService(data.getData());
		}
		dawdlerContext.setAttribute(FILTER_PROVIDER, filterProvider);
		dawdlerContext.setAttribute(DAWDLER_LISTENER_PROVIDER, dawdlerListenerProvider);

		for (int i = 0; i < lifeCycleList.size(); i++) {
			OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
			lifeCycle.getData().init();
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
						}
						try {
							orderData.getData().contextInitialized(dawdlerContext);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}).start();
			} else {
				orderData.getData().contextInitialized(dawdlerContext);
			}

			for (int i = 0; i < lifeCycleList.size(); i++) {
				try {
					OrderData<ComponentLifeCycle> lifeCycle = lifeCycleList.get(i);
					lifeCycle.getData().afterInit();
				} catch (Throwable e) {
					logger.error("", e);
				}
			}
		}
	}

	public FilterProvider getFilterProvider() {
		return filterProvider;
	}

	@Override
	public void prepareStop() {
		DiscoveryCenter discoveryCenter = (DiscoveryCenter) dawdlerContext.getAttribute(DiscoveryCenter.class);
		if (discoveryCenter != null) {
			try {
				discoveryCenter.destroy();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		try {
			Thread.sleep(WAIT_TIME_MILLIS);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	@Override
	public void stop() {
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
	}

	public DawdlerContext getDawdlerContext() {
		return dawdlerContext;
	}

	@Override
	public ServiceExecutor getServiceExecutor() {
		return serviceExecutor;
	}

	private void injectService(Object service) throws Throwable {
		Field[] fields = service.getClass().getDeclaredFields();
		for (Field field : fields) {
			com.anywide.dawdler.core.annotation.Service serviceAnnotation = field
					.getAnnotation(com.anywide.dawdler.core.annotation.Service.class);
			if (!field.getType().isPrimitive()) {
				Class<?> serviceClass = field.getType();
				field.setAccessible(true);
				try {
					Object obj = null;
					if (serviceAnnotation != null) {
						if (!serviceAnnotation.remote()) {
							obj = dawdlerContext.getServiceProxy(serviceClass);
						} else {
							RemoteService remoteService = serviceClass.getAnnotation(RemoteService.class);
							if (remoteService == null) {
								throw new NotSetRemoteServiceException(
										"not found @RemoteService on " + serviceClass.getName());
							}
							Class<?> serviceFactoryClass = classLoader
									.loadClass("com.anywide.dawdler.client.ServiceFactory");
							Method method = serviceFactoryClass.getMethod("getService", Class.class, String.class);
							String groupName = remoteService.value();
							obj = method.invoke(null, serviceClass, groupName);
						}
						if (obj != null) {
							field.set(service, obj);
						}
					}
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
	}

	private XmlObject loadXML() {
		try {
			String configPath;
			File file;
			String activeProfile = System.getProperty("dawdler.profiles.active");
			String prefix = "services-config";
			String subfix = ".xml";
			configPath = (prefix + (activeProfile != null ? "-" + activeProfile : "")) + subfix;
			file = new File(DawdlerTool.getCurrentPath() + configPath);
			if (!file.isFile()) {
				configPath = prefix + subfix;
				file = new File(DawdlerTool.getCurrentPath() + configPath);
			}
			if (!file.isFile()) {
				logger.error("not found services-config.xml");
			}
			return XmlObject.loadClassPathXML(configPath);
		} catch (DocumentException | IOException e) {
			logger.error("", e);
		}
		return null;
	}

	public class DeployScanner {
		private Set<String> packagePathInJar = new LinkedHashSet<String>();
		private Set<String> packageAntPathInJar = new LinkedHashSet<String>();
		private Set<String> packagePathInClasses = new LinkedHashSet<String>();
		private Set<String> packageAntPathInClasses = new LinkedHashSet<String>();

		public void splitAndAddPathInJar(String packagePath) {
			if (!StringUtils.hasLength(packagePath)) {
				return;
			}
			if (antPathMatcher.isPattern(packagePath)) {
				this.packageAntPathInJar.add(packagePath);
			} else {
				this.packagePathInJar.add(packagePath);
			}
		}

		public void splitAndAddPathInClasses(String packagePath) {
			if (!StringUtils.hasLength(packagePath)) {
				return;
			}
			if (antPathMatcher.isPattern(packagePath)) {
				this.packageAntPathInClasses.add(packagePath);
			} else {
				this.packagePathInClasses.add(packagePath);
			}
		}

		public boolean matchInClasses(String packagePath) {
			if (packagePathInClasses.contains(packagePath)) {
				return true;
			}
			for (String antPath : packageAntPathInClasses) {
				if (antPathMatcher.match(antPath, packagePath)) {
					return true;
				}
			}
			return false;
		}

		public boolean matchInJars(String packagePath) {
			if (packagePathInJar.contains(packagePath)) {
				return true;
			}
			for (String antPath : packageAntPathInJar) {
				if (antPathMatcher.match(antPath, packagePath)) {
					return true;
				}
			}
			return false;
		}

	}

	private void checkHealth() throws Throwable {
		getServiceHealth();
		if (cause != null) {
			throw cause;
		}
	}

	@Override
	public ServiceHealth getServiceHealth() {
		if (status == Status.DOWN) {
			ServiceHealth serviceHealth = new ServiceHealth(deployName);
			serviceHealth.setStatus(status);
			serviceHealth.addComponent("error", cause.getClass().getName() + ":" + cause.getMessage());
			return serviceHealth;
		}
		Thread.currentThread().setContextClassLoader(classLoader);
		ServiceHealth serviceHealth = new ServiceHealth(deployName);
		serviceHealth.setStatus(Status.STARTING);
		HealthIndicatorProvider healthChecker = HealthIndicatorProvider.getInstance(deployName);
		List<OrderData<HealthIndicator>> healthIndicators = healthChecker.getHealthIndicators();
		boolean down = false;
		for (OrderData<HealthIndicator> orderData : healthIndicators) {
			HealthIndicator healthIndicator = orderData.getData();
			if (!healthCheck.componentCheck(healthIndicator.name())) {
				continue;
			}
			Health.Builder builder = Health.up();
			try {
				builder.setName(healthIndicator.name());
				Health componentHealth = healthIndicator.check(builder);
				serviceHealth.addComponent(componentHealth);
			} catch (Exception e) {
				cause(e);
				serviceHealth.addComponent(Health.down(e).setName(builder.getName()).build());
				down = true;
			}
		}
		String status;
		if (down) {
			status = Status.DOWN;
		} else {
			status = Status.UP;
		}
		serviceHealth.setStatus(status);
		return serviceHealth;
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
		this.status = status;
	}

	@Override
	public void cause(Throwable cause) {
		this.cause = cause;
	}
}
