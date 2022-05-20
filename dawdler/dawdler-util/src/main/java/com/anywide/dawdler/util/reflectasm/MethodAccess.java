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

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACC_VARARGS;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author jackson.song
 * @version V1.0
 * @Title MethodAccess.java
 * @Description asm的改进版 已升到asm支持jdk9
 * @date 2012年5月16日
 * @email suxuan696@gmail.com
 */
public abstract class MethodAccess {
	private String[] methodNames;
	private Class<?>[][] parameterTypes;
	private Class<?>[] returnTypes;
	private Annotation[][] annotations;
	private Parameter[][] parameters;

	public static boolean equals(Object[] a, Object[] a2) {
		if (a == a2)
			return true;
		if (a == null || a2 == null)
			return false;

		int length = a.length;
		if (a2.length != length)
			return false;

		for (int i = 0; i < length; i++) {
			Object o1 = a[i];
			Object o2 = a2[i];
			if (!(o1 == null ? o2 == null : (o1.equals(o2))))
				return false;
		}

		return true;
	}

	/**
	 * @param type Must not be the Object class, an interface, a primitive type, or
	 *             void.
	 */
	static public MethodAccess get(Class<?> type) {
		if (type.getSuperclass() == null)
			throw new IllegalArgumentException(
					"The type must not be the Object class, an interface, a primitive type, or void.");

		ArrayList<Method> methods = new ArrayList<Method>();
		boolean isInterface = type.isInterface();
		if (!isInterface) {
			Class<?> nextClass = type;
			while (nextClass != Object.class) {
				addDeclaredMethodsToList(nextClass, methods);
				nextClass = nextClass.getSuperclass();
				if (nextClass == Object.class)
					addDeclaredMethodsToList(nextClass, methods);
			}
		} else {
			recursiveAddInterfaceMethodsToList(type, methods);
		}

		int n = methods.size();
		String[] methodNames = new String[n];
		Class<?>[][] parameterTypes = new Class<?>[n][];
		Class<?>[] returnTypes = new Class<?>[n];
		Annotation[][] annotations = new Annotation[n][0];
		for (int i = 0; i < n; i++) {
			Method method = methods.get(i);
			methodNames[i] = method.getName();
			parameterTypes[i] = method.getParameterTypes();
			returnTypes[i] = method.getReturnType();
			annotations[i] = method.getAnnotations();
		}

		String className = type.getName();
		String accessClassName = className + "MethodAccess";
		if (accessClassName.startsWith("java."))
			accessClassName = "reflectasm." + accessClassName;
		Class<?> accessClass;

		AccessClassLoader loader = AccessClassLoader.get(type);
		try {
			accessClass = loader.loadClass(accessClassName);
		} catch (ClassNotFoundException ignored) {
			synchronized (loader) {
				try {
					accessClass = loader.loadClass(accessClassName);
				} catch (ClassNotFoundException ignored2) {
					String accessClassNameInternal = accessClassName.replace('.', '/');
					String classNameInternal = className.replace('.', '/');

					ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
					MethodVisitor mv;
					cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, accessClassNameInternal, null,
							"com/anywide/dawdler/util/reflectasm/MethodAccess", null);
					{
						mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
						mv.visitCode();
						mv.visitVarInsn(ALOAD, 0);
						mv.visitMethodInsn(INVOKESPECIAL, "com/anywide/dawdler/util/reflectasm/MethodAccess", "<init>",
								"()V", false);
						mv.visitInsn(RETURN);
						mv.visitMaxs(0, 0);
						mv.visitEnd();
					}
					{
						mv = cw.visitMethod(ACC_PUBLIC + ACC_VARARGS, "invoke",
								"(Ljava/lang/Object;I[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
						mv.visitCode();

						if (!methods.isEmpty()) {
							mv.visitVarInsn(ALOAD, 1);
							mv.visitTypeInsn(CHECKCAST, classNameInternal);
							mv.visitVarInsn(ASTORE, 4);

							mv.visitVarInsn(ILOAD, 2);
							Label[] labels = new Label[n];
							for (int i = 0; i < n; i++)
								labels[i] = new Label();
							Label defaultLabel = new Label();
							mv.visitTableSwitchInsn(0, labels.length - 1, defaultLabel, labels);

							StringBuilder buffer = new StringBuilder(128);
							for (int i = 0; i < n; i++) {
								mv.visitLabel(labels[i]);
								if (i == 0)
									mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] { classNameInternal }, 0, null);
								else
									mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
								mv.visitVarInsn(ALOAD, 4);

								buffer.setLength(0);
								buffer.append('(');

								Class<?>[] paramTypes = parameterTypes[i];
								Class<?> returnType = returnTypes[i];
								for (int paramIndex = 0; paramIndex < paramTypes.length; paramIndex++) {
									mv.visitVarInsn(ALOAD, 3);
									mv.visitIntInsn(BIPUSH, paramIndex);
									mv.visitInsn(AALOAD);
									Type paramType = Type.getType(paramTypes[paramIndex]);
									switch (paramType.getSort()) {
									case Type.BOOLEAN:
										mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
										mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z",
												false);
										break;
									case Type.BYTE:
										mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
										mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);
										break;
									case Type.CHAR:
										mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
										mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C",
												false);
										break;
									case Type.SHORT:
										mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
										mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S",
												false);
										break;
									case Type.INT:
										mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
										mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I",
												false);
										break;
									case Type.FLOAT:
										mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
										mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F",
												false);
										break;
									case Type.LONG:
										mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
										mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
										break;
									case Type.DOUBLE:
										mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
										mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D",
												false);
										break;
									case Type.ARRAY:
										mv.visitTypeInsn(CHECKCAST, paramType.getDescriptor());
										break;
									case Type.OBJECT:
										mv.visitTypeInsn(CHECKCAST, paramType.getInternalName());
										break;
									}
									buffer.append(paramType.getDescriptor());
								}

								buffer.append(')');
								buffer.append(Type.getDescriptor(returnType));
								int invoke;
								if (isInterface)
									invoke = INVOKEINTERFACE;
								else if (Modifier.isStatic(methods.get(i).getModifiers()))
									invoke = INVOKESTATIC;
								else
									invoke = INVOKEVIRTUAL;
								mv.visitMethodInsn(invoke, classNameInternal, methodNames[i], buffer.toString(), false);

								switch (Type.getType(returnType).getSort()) {
								case Type.VOID:
									mv.visitInsn(ACONST_NULL);
									break;
								case Type.BOOLEAN:
									mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf",
											"(Z)Ljava/lang/Boolean;", false);
									break;
								case Type.BYTE:
									mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;",
											false);
									break;
								case Type.CHAR:
									mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf",
											"(C)Ljava/lang/Character;", false);
									break;
								case Type.SHORT:
									mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf",
											"(S)Ljava/lang/Short;", false);
									break;
								case Type.INT:
									mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf",
											"(I)Ljava/lang/Integer;", false);
									break;
								case Type.FLOAT:
									mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf",
											"(F)Ljava/lang/Float;", false);
									break;
								case Type.LONG:
									mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;",
											false);
									break;
								case Type.DOUBLE:
									mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf",
											"(D)Ljava/lang/Double;", false);
									break;
								}

								mv.visitInsn(ARETURN);
							}

							mv.visitLabel(defaultLabel);
							mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
						}
						mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
						mv.visitInsn(DUP);
						mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
						mv.visitInsn(DUP);
						mv.visitLdcInsn("Method not found: ");
						mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V",
								false);
						mv.visitVarInsn(ILOAD, 2);
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
								"(I)Ljava/lang/StringBuilder;", false);
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;",
								false);
						mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>",
								"(Ljava/lang/String;)V", false);
						mv.visitInsn(ATHROW);
						mv.visitMaxs(0, 0);
						mv.visitEnd();
					}
					cw.visitEnd();
					byte[] data = cw.toByteArray();
					accessClass = loader.defineClass(accessClassName, data);
				}
			}
		}
		try {
			MethodAccess access = (MethodAccess) accessClass.newInstance();
			access.methodNames = methodNames;
			access.parameterTypes = parameterTypes;
			access.returnTypes = returnTypes;
			access.annotations = annotations;
			return access;
		} catch (Throwable t) {
			throw new RuntimeException("Error constructing method access class: " + accessClassName, t);
		}
	}

	private static void addDeclaredMethodsToList(Class<?> type, ArrayList<Method> methods) {
		Method[] declaredMethods = type.getDeclaredMethods();
		for (int i = 0, n = declaredMethods.length; i < n; i++) {
			Method method = declaredMethods[i];
			int modifiers = method.getModifiers();
			if (Modifier.isPrivate(modifiers))
				continue;
			methods.add(method);
		}
	}

	private static void recursiveAddInterfaceMethodsToList(Class<?> interfaceType, ArrayList<Method> methods) {
		addDeclaredMethodsToList(interfaceType, methods);
		for (Class<?> nextInterface : interfaceType.getInterfaces()) {
			recursiveAddInterfaceMethodsToList(nextInterface, methods);
		}
	}

	public Parameter[] getParametersWithIndex(int index) {
		return parameters[index];
	}

	public Annotation[] getAnnotationWithIndex(int index) {
		return annotations[index];
	}

	public <T extends Annotation> T getAnnotation(int methodIndex, Class<T> annotation) {
		Annotation[] annotationsArray = annotations[methodIndex];
		for (Annotation an : annotationsArray) {
			if (an.annotationType().isAssignableFrom(annotation))
				return (T) an;
		}
		return null;
	}

	abstract public Object invoke(Object object, int methodIndex, Object... args);

	/**
	 * Invokes the method with the specified name and the specified param types.
	 */
	public Object invoke(Object object, String methodName, Class<?>[] paramTypes, Object... args) {
		return invoke(object, getIndex(methodName, paramTypes), args);
	}

	/**
	 * Invokes the first method with the specified name and the specified number of
	 * arguments.
	 */
	public Object invoke(Object object, String methodName, Object... args) {
		return invoke(object, getIndex(methodName, args == null ? 0 : args.length), args);
	}

	/**
	 * Returns the index of the first method with the specified name.
	 */
	public int getIndex(String methodName) {
		for (int i = 0, n = methodNames.length; i < n; i++)
			if (methodNames[i].equals(methodName))
				return i;
		throw new IllegalArgumentException("Unable to find non-private method: " + methodName);
	}

	/**
	 * Returns the index of the first method with the specified name and param
	 * types.
	 */
	public int getIndex(String methodName, Class<?>... paramTypes) {
		for (int i = 0, n = methodNames.length; i < n; i++)
			if (methodNames[i].equals(methodName) && Arrays.equals(paramTypes, parameterTypes[i]))
				return i;
		throw new IllegalArgumentException(
				"Unable to find non-private method: " + methodName + " " + Arrays.toString(paramTypes));
	}

	/**
	 * Returns the index of the first method with the specified name and the
	 * specified number of arguments.
	 */
	public int getIndex(String methodName, int paramsCount) {
		for (int i = 0; i < methodNames.length; i++) {
			if (methodNames[i].equals(methodName) && parameterTypes[i].length == paramsCount)
				return i;
		}

		throw new IllegalArgumentException(
				"Unable to find non-private method: " + methodName + " with " + paramsCount + " params.");
	}

	public String[] getMethodNames() {
		return methodNames;
	}

	public Class<?>[][] getParameterTypes() {
		return parameterTypes;
	}

	public Class<?>[] getReturnTypes() {
		return returnTypes;
	}

	public Class<?> getReturnType(int methodIndex) {
		return returnTypes[methodIndex];
	}
}
