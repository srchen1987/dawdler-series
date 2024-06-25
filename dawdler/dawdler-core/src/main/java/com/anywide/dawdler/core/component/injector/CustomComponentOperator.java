package com.anywide.dawdler.core.component.injector;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.loader.DeployClassLoader;
import com.anywide.dawdler.core.order.OrderComparator;
import com.anywide.dawdler.core.order.OrderData;
import com.anywide.dawdler.core.scan.DawdlerComponentScanner;
import com.anywide.dawdler.core.scan.component.reader.ClassStructureParser;
import com.anywide.dawdler.core.scan.component.reader.ClassStructureParser.ClassStructure;
import com.anywide.dawdler.util.SunReflectionFactoryInstantiator;
import com.anywide.dawdler.util.spring.antpath.Resource;

/**
 * @author jackson.song
 * @version V1.0
 * 组件注入操作者
 */
public class CustomComponentOperator {

	public static void scanAndInject(DeployClassLoader classLoader,
			List<OrderData<CustomComponentInjector>> customComponentInjectorList, Set<String> packagePaths)
			throws Throwable {
		Map<String, Resource> removeDuplicates = new LinkedHashMap<>();
		for (String packageInClasses : packagePaths) {
			Resource[] resources = DawdlerComponentScanner.getClasses(packageInClasses);
			for (Resource resource : resources) {
				removeDuplicates.putIfAbsent(resource.getURL().toString(), resource);
			}
		}
		Collection<Resource> resources = removeDuplicates.values();
		List<OrderData<CustomComponentData>> customComponentDataList = new ArrayList<>();
		for (Resource resource : resources) {
			InputStream input = null;
			try {
				input = resource.getInputStream();
				ClassStructure classStructure = ClassStructureParser.parser(input);
				if (classStructure != null) {
					for (OrderData<CustomComponentInjector> data : customComponentInjectorList) {
						CustomComponentInjector customComponentInjector = data.getData();
						OrderData<CustomComponentData> orderData = new OrderData<>();
						orderData.setData(new CustomComponentData(resource, classStructure, customComponentInjector));
						Order order = customComponentInjector.getClass().getAnnotation(Order.class);
						if (order != null) {
							orderData.setOrder(order.value());
						}
						customComponentDataList.add(orderData);
					}
				}
			} finally {
				if (input != null) {
					input.close();
				}
			}
		}
		if (!customComponentDataList.isEmpty()) {
			OrderComparator.sort(customComponentDataList);
			for (OrderData<CustomComponentData> data : customComponentDataList) {
				CustomComponentData customComponentData = data.getData();
				inject(classLoader, customComponentData.getResource(), customComponentData.getClassStructure(),
						customComponentData.getCustomComponentInjector());
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
							inject(classLoader, resource, classStructure, customComponentInjector);
						} finally {
							if (input != null) {
								input.close();
							}
						}
					}
				}
			}
		}
	}

	public static Class<?> inject(DeployClassLoader classLoader, Resource resource, ClassStructure classStructure,
			CustomComponentInjector customComponentInjector) throws Throwable {
		return inject(classLoader, resource, null, classStructure, customComponentInjector);
	}

	public static Class<?> inject(DeployClassLoader classLoader, byte[] codeBytes, ClassStructure classStructure,
			CustomComponentInjector customComponentInjector) throws Throwable {
		return inject(classLoader, null, codeBytes, classStructure, customComponentInjector);
	}

	private static Class<?> inject(DeployClassLoader classLoader, Resource resource, byte[] codeBytes,
			ClassStructure classStructure, CustomComponentInjector customComponentInjector) throws Throwable {
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
			Set<? extends Class<? extends Annotation>> annotationSet = customComponentInjector.getMatchAnnotations();
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
			Class<?> c;
			if (codeBytes != null) {
				c = classLoader.findClassForDawdlerByte(classStructure.getClassName(), codeBytes,
						customComponentInjector.useAop(), customComponentInjector.storeVariableNameByASM());
			} else {
				c = classLoader.findClassForDawdler(classStructure.getClassName(), resource,
						customComponentInjector.useAop(), customComponentInjector.storeVariableNameByASM());
			}
			if (customComponentInjector.isInject() && !classStructure.isAbstract() && !classStructure.isAnnotation()
					&& !classStructure.isInterface()) {
				Object target = SunReflectionFactoryInstantiator.newInstance(c);
				customComponentInjector.inject(c, target);
			}
			return c;
		}
		return null;
	}

	private static class CustomComponentData {
		private Resource resource;
		private ClassStructure classStructure;
		private CustomComponentInjector customComponentInjector;

		public CustomComponentData(Resource resource, ClassStructure classStructure,
				CustomComponentInjector customComponentInjector) {
			this.resource = resource;
			this.classStructure = classStructure;
			this.customComponentInjector = customComponentInjector;

		}

		public Resource getResource() {
			return resource;
		}

		public ClassStructure getClassStructure() {
			return classStructure;
		}

		public CustomComponentInjector getCustomComponentInjector() {
			return customComponentInjector;
		}

	}

}
