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
package com.anywide.dawdler.core.scan.component.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import com.anywide.dawdler.core.scan.DawdlerComponentScanner;
import com.anywide.dawdler.util.spring.antpath.Resource;

/**
 * @author jackson.song
 * @version V1.0
 * asm实现的类解析器
 */
public class ClassStructureParser {

	public static ClassStructure parser(InputStream inputStream) throws IOException {
		return parser(new ClassReader(inputStream), null);
	}

	public static ClassStructure parser(byte[] data) throws IOException {
		return parser(new ClassReader(data), null);
	}

	private static ClassStructure parser(ClassReader cr, ClassStructure classStructure) throws IOException {
		ClassNode cn = new ClassNode();
		cr.accept(cn, ClassReader.SKIP_DEBUG);
		if (classStructure == null) {
			classStructure = new ClassStructure();
			classStructure.className = cr.getClassName().replace("/", ".");
			int access = cn.access;
			classStructure.isInterface = ((access & Opcodes.ACC_INTERFACE) != 0);
			classStructure.isAnnotation = ((access & Opcodes.ACC_ANNOTATION) != 0);
			classStructure.isAbstract = ((access & Opcodes.ACC_ABSTRACT) != 0);
			classStructure.isFinal = ((access & Opcodes.ACC_FINAL) != 0);
			if (classStructure.isInterface || classStructure.isAnnotation || classStructure.isAbstract) {
				return classStructure;
			}
		}
		if (cn.visibleAnnotations != null) {
			for (AnnotationNode annotationNode : cn.visibleAnnotations) {
				String annotationName = Type.getType(annotationNode.desc).getClassName();
				classStructure.annotationNames.add(annotationName);
			}
		}
		for (String classInterface : cn.interfaces) {
			if (!classInterface.startsWith("java")) {
				Resource resource = DawdlerComponentScanner.getClass(classInterface);
				if (resource != null) {
					InputStream input = null;
					try {
						parser(new ClassReader(resource.getBytes()), classStructure);
					} finally {
						if (input != null) {
							input.close();
						}
					}
				}
				classStructure.interfaces.add(classInterface.replace("/", "."));
			}
		}
		if (!cn.superName.startsWith("java")) {
			Resource resource = DawdlerComponentScanner.getClass(cn.superName);
			if (resource != null) {
				InputStream input = null;
				try {
					parser(new ClassReader(resource.getBytes()), classStructure);
				} finally {
					if (input != null) {
						input.close();
					}
				}
			}
			classStructure.superClasses.add(cn.superName.replace("/", "."));
		}
		return classStructure;
	}

	public static class ClassStructure {
		private boolean isInterface;
		private boolean isAnnotation;
		private boolean isAbstract;
		private boolean isFinal;
		private Set<String> annotationNames = new HashSet<>(8);
		private Set<String> interfaces = new HashSet<>(4);
		private Set<String> superClasses = new HashSet<>(4);
		private String className;

		public Set<String> getAnnotationNames() {
			return annotationNames;
		}

		public Set<String> getInterfaces() {
			return interfaces;
		}

		public Set<String> getSuperClasses() {
			return superClasses;
		}

		public String getClassName() {
			return className;
		}

		public boolean isInterface() {
			return isInterface;
		}

		public boolean isAnnotation() {
			return isAnnotation;
		}

		public boolean isAbstract() {
			return isAbstract;
		}

		public boolean isFinal() {
			return isFinal;
		}

		@Override
		public String toString() {
			return className + ": interface:" + isInterface + ": abstract:" + isAbstract + ": final:" + isFinal
					+ ": annotation:" + isAnnotation;
		}

	}
}
