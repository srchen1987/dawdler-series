package com.anywide.util.reflectasm;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_1;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
/**
 * 
 * @Title:  ConstructorAccess.java
 * @Description:    asm的改进版   已升到asm支持jdk9
 * @author: jackson.song    
 * @date:   2012年05月16日    
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public abstract class ConstructorAccess<T> {
	boolean isNonStaticMemberClass;

	public boolean isNonStaticMemberClass () {
		return isNonStaticMemberClass;
	}
	abstract public T newInstance ();

	abstract public T newInstance (Object enclosingInstance);

	static public <T> ConstructorAccess<T> get (Class<T> type) {
		Class enclosingType = type.getEnclosingClass();
		boolean isNonStaticMemberClass = enclosingType != null && type.isMemberClass() && !Modifier.isStatic(type.getModifiers());

		String className = type.getName();
		String accessClassName = className + "ConstructorAccess";
		if (accessClassName.startsWith("java.")) accessClassName = "reflectasm." + accessClassName;
		Class accessClass;

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
					String enclosingClassNameInternal;
					Constructor<T> constructor = null;
					int modifiers = 0;
					if (!isNonStaticMemberClass) {
						enclosingClassNameInternal = null;
						try {
							constructor = type.getDeclaredConstructor((Class[])null);
							modifiers = constructor.getModifiers();
						} catch (Exception ex) {
							throw new RuntimeException("Class cannot be created (missing no-arg constructor): " + type.getName(), ex);
						}
						if (Modifier.isPrivate(modifiers)) {
							throw new RuntimeException("Class cannot be created (the no-arg constructor is private): " + type.getName());
						}
					} else {
						enclosingClassNameInternal = enclosingType.getName().replace('.', '/');
						try {
							constructor = type.getDeclaredConstructor(enclosingType); // Inner classes should have this.
							modifiers = constructor.getModifiers();
						} catch (Exception ex) {
							throw new RuntimeException("Non-static member class cannot be created (missing enclosing class constructor): "
								+ type.getName(), ex);
						}
						if (Modifier.isPrivate(modifiers)) {
							throw new RuntimeException(
								"Non-static member class cannot be created (the enclosing class constructor is private): " + type.getName());
						}
					}
					String superclassNameInternal = Modifier.isPublic(modifiers) ?
													"com/anywide/util/reflectasm/PublicConstructorAccess" :
													"com/anywide/util/reflectasm/ConstructorAccess";

					ClassWriter cw = new ClassWriter(0);
					cw.visit(V1_1, ACC_PUBLIC + ACC_SUPER, accessClassNameInternal, null, superclassNameInternal, null);

					insertConstructor(cw, superclassNameInternal);
					insertNewInstance(cw, classNameInternal);
					insertNewInstanceInner(cw, classNameInternal, enclosingClassNameInternal);

					cw.visitEnd();
					accessClass = loader.defineClass(accessClassName, cw.toByteArray());
				}
			}
		}
		ConstructorAccess<T> access;
		try {
			access = (ConstructorAccess<T>)accessClass.newInstance();
		} catch (Throwable t) {
			throw new RuntimeException("Exception constructing constructor access class: " + accessClassName, t);
		}
		if (!(access instanceof PublicConstructorAccess)  && !AccessClassLoader.areInSameRuntimeClassLoader(type, accessClass)) {
			throw new RuntimeException(
					(!isNonStaticMemberClass ?
					"Class cannot be created (the no-arg constructor is protected or package-protected, and its ConstructorAccess could not be defined in the same class loader): " :
					"Non-static member class cannot be created (the enclosing class constructor is protected or package-protected, and its ConstructorAccess could not be defined in the same class loader): ")
					+ type.getName());
		}
		access.isNonStaticMemberClass = isNonStaticMemberClass;
		return access;
	}

	static private void insertConstructor (ClassWriter cw, String superclassNameInternal) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, superclassNameInternal, "<init>", "()V");
		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	static void insertNewInstance (ClassWriter cw, String classNameInternal) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "newInstance", "()Ljava/lang/Object;", null, null);
		mv.visitCode();
		mv.visitTypeInsn(NEW, classNameInternal);
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, classNameInternal, "<init>", "()V");
		mv.visitInsn(ARETURN);
		mv.visitMaxs(2, 1);
		mv.visitEnd();
	}

	static void insertNewInstanceInner (ClassWriter cw, String classNameInternal, String enclosingClassNameInternal) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "newInstance", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
		mv.visitCode();
		if (enclosingClassNameInternal != null) {
			mv.visitTypeInsn(NEW, classNameInternal);
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitTypeInsn(CHECKCAST, enclosingClassNameInternal);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESPECIAL, classNameInternal, "<init>", "(L" + enclosingClassNameInternal + ";)V");
			mv.visitInsn(ARETURN);
			mv.visitMaxs(4, 2);
		} else {
			mv.visitTypeInsn(NEW, "java/lang/UnsupportedOperationException");
			mv.visitInsn(DUP);
			mv.visitLdcInsn("Not an inner class.");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/UnsupportedOperationException", "<init>", "(Ljava/lang/String;)V");
			mv.visitInsn(ATHROW);
			mv.visitMaxs(3, 2);
		}
		mv.visitEnd();
	}
}
