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

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
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

	private static final Map<Class<?>, Map<Method, String[]>> PARAMETER_NAMES_CACHE = new ConcurrentHashMap<>(64);

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
	 *         初始化所有本类的方法 不考虑 bridge、static、syn 、private、varArgs、abstract 及其父类的方法
	 *         </p>
	 * @param clazz
	 * @throws IOException
	 *
	 */
	public static void loadAllDeclaredMethodsParameterNames(Class<?> clazz, byte[] codeBytes) throws IOException {
		Map<Method, String[]> methodsParameterNames = getParameterNames(clazz);
		if (methodsParameterNames != null) {
			return;
		}
		ClassReader classReader = new ClassReader(codeBytes);
		ClassNode classNode = new ClassNode();
		classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
		Method[] methods = clazz.getDeclaredMethods();
		if (methods.length == 0) {
			methodsParameterNames = Collections.emptyMap();
		} else {
			methodsParameterNames = new HashMap<>(32);
		}
		for (Method method : methods) {
			boolean statics = Modifier.isStatic(method.getModifiers());
			if (statics || method.isBridge() || method.isVarArgs() || Modifier.isPrivate(method.getModifiers())
					|| Modifier.isSynchronized(method.getModifiers()) || Modifier.isAbstract(method.getModifiers())) {
				continue;
			}
			int parameterCount = method.getParameterCount();
			if (parameterCount == 0) {
				continue;
			}
			String name = method.getName();
			String desc = Type.getMethodDescriptor(method);
			MethodNode methodNode = findMethod(classNode, name, desc);
			methodsParameterNames.put(method, findLocalVars(methodNode, parameterCount));
		}
		PARAMETER_NAMES_CACHE.put(clazz, methodsParameterNames);
	}

	public static Map<Method, String[]> getParameterNames(Class<?> clazz) {
		return PARAMETER_NAMES_CACHE.get(clazz);
	}

	public static void removeParameterNames(Class<?> clazz) {
		PARAMETER_NAMES_CACHE.remove(clazz);
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

	public static String[] findLocalVars(MethodNode methodNode, int parameterCount) {
		List<LocalVariableNode> localVariableNodes = methodNode.localVariables;
		if (localVariableNodes.isEmpty()) {
			return null;
		}
		String[] parameterNames = new String[parameterCount];
		Comparator<LocalVariableNode> indexComparator = (o1, o2) -> o1.index - o2.index;
		Collections.sort(localVariableNodes, indexComparator);
		for (LocalVariableNode variableNode : localVariableNodes) {
			int index = variableNode.index;
			String name = variableNode.name;
			if (index - 1 >= parameterCount) {
				return parameterNames;
			} else if (index == 0) {
				continue;// skip this
			}
			parameterNames[index - 1] = name;
		}
		return parameterNames;
	}

}
