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
package com.anywide.dawdler.util.reflectasm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ParameterNameReader.java
 * @Description 参数名称读取类，基于asm实现读取localVariableNodes来实现
 * @date 2021年3月27日
 * @email suxuan696@gmail.com
 */
public class ParameterNameReader {

	private static final Map<Class<?>, Map<Method, String[]>> parameterNamesCache = new ConcurrentHashMap<>(64);

	public static String[] getMethodParameterNames(Method method) throws IOException {
		boolean statics = Modifier.isStatic(method.getModifiers());
		String name = method.getName();
		String desc = Type.getMethodDescriptor(method);
		Class<?> clazz = method.getDeclaringClass();
		ClassReader classReader = null;
		try (InputStream input = clazz.getClassLoader()
				.getResourceAsStream(clazz.getName().replace(".", File.separator) + ".class")) {
			classReader = new ClassReader(input);
		}
		ClassNode classNode = new ClassNode();
		classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
		MethodNode methodNode = findMethod(classNode, name, desc);
		return findLocalVars(methodNode, statics, method.getParameterCount());
	}

	/**
	 * 
	 * <p>
	 * Title: loadAllDeclaredMethodsParameterNames
	 * </p>
	 * 
	 * @author jackson.song
	 * @date 2021年3月27日
	 * @return void
	 *         <p>
	 *         Description:
	 *         Controller初始化时会加载此类，所以不需要考虑线程安全,其他场景使用需要考虑线程安全，防止多次调用影响性能，瞬间增加io和系统负载
	 *         初始化所有本类的方法 不考虑 bridge、static、syn 、private、 父类的方法
	 *         </p>
	 * @param clazz
	 * @throws IOException
	 *
	 */
	public static void loadAllDeclaredMethodsParameterNames(Class<?> clazz, byte[] classCodes) throws IOException {
		Map<Method, String[]> methodsParameterNames = getParameterNames(clazz);
		if (methodsParameterNames != null)
			return;
		ClassReader classReader = new ClassReader(classCodes);
		ClassNode classNode = new ClassNode();
		classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
		Method[] methods = clazz.getDeclaredMethods();
		if (methods.length == 0)
			methodsParameterNames = Collections.emptyMap();
		else
			methodsParameterNames = new HashMap<>(32);

		for (Method method : methods) {
			boolean statics = Modifier.isStatic(method.getModifiers());
			String name = method.getName();
			String desc = Type.getMethodDescriptor(method);
			MethodNode methodNode = findMethod(classNode, name, desc);
			int parameterCount = method.getParameterCount();
			if (parameterCount > 0)
				methodsParameterNames.put(method, findLocalVars(methodNode, statics, parameterCount));
		}
		parameterNamesCache.put(clazz, methodsParameterNames);
	}

	public static Map<Method, String[]> getParameterNames(Class<?> clazz) {
		return parameterNamesCache.get(clazz);
	}

	public static void removeParameterNames(Class<?> clazz) {
		parameterNamesCache.remove(clazz);
	}

	public static MethodNode findMethod(ClassNode cn, String name, String desc) {
		for (Object node : cn.methods) {
			MethodNode methodNode = (MethodNode) node;
			if (methodNode.name.equals(name) && methodNode.desc.equals(desc)) {
				return methodNode;
			}
		}
		return null;
	}

	public static String[] findLocalVars(MethodNode methodNode, boolean statics, int parameterCount) {
		List<LocalVariableNode> localVariableNodes = methodNode.localVariables;
		if (localVariableNodes.isEmpty())
			return null;
		String[] parameterNames = null;
		if (statics) {
			parameterNames = new String[parameterCount];
		} else {
			parameterNames = new String[parameterCount];
		}
		for (LocalVariableNode variableNode : localVariableNodes) {
			int index = variableNode.index;
			String name = variableNode.name;
			if (statics) {
				if (index == parameterCount-1)
					return parameterNames;
				parameterNames[index] = name;
			} else {
				if (index == parameterCount)
					return parameterNames;
				if (index == 0)
					continue;// skip this
				parameterNames[index - 1] = name;
			}
		}
		return parameterNames;
	}
	
}
