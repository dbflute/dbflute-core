/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.dbflute.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author modified by jflute (originated in Seasar2)
 */
public class DfReflectionUtil {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static Map<Class<?>, Class<?>> wrapperToPrimitiveMap = new HashMap<Class<?>, Class<?>>();
    private static Map<Class<?>, Class<?>> primitiveToWrapperMap = new HashMap<Class<?>, Class<?>>();
    private static Map<String, Class<?>> primitiveClsssNameMap = new HashMap<String, Class<?>>();
    static {
        wrapperToPrimitiveMap.put(Character.class, Character.TYPE);
        wrapperToPrimitiveMap.put(Byte.class, Byte.TYPE);
        wrapperToPrimitiveMap.put(Short.class, Short.TYPE);
        wrapperToPrimitiveMap.put(Integer.class, Integer.TYPE);
        wrapperToPrimitiveMap.put(Long.class, Long.TYPE);
        wrapperToPrimitiveMap.put(Double.class, Double.TYPE);
        wrapperToPrimitiveMap.put(Float.class, Float.TYPE);
        wrapperToPrimitiveMap.put(Boolean.class, Boolean.TYPE);

        primitiveToWrapperMap.put(Character.TYPE, Character.class);
        primitiveToWrapperMap.put(Byte.TYPE, Byte.class);
        primitiveToWrapperMap.put(Short.TYPE, Short.class);
        primitiveToWrapperMap.put(Integer.TYPE, Integer.class);
        primitiveToWrapperMap.put(Long.TYPE, Long.class);
        primitiveToWrapperMap.put(Double.TYPE, Double.class);
        primitiveToWrapperMap.put(Float.TYPE, Float.class);
        primitiveToWrapperMap.put(Boolean.TYPE, Boolean.class);

        primitiveClsssNameMap.put(Character.TYPE.getName(), Character.TYPE);
        primitiveClsssNameMap.put(Byte.TYPE.getName(), Byte.TYPE);
        primitiveClsssNameMap.put(Short.TYPE.getName(), Short.TYPE);
        primitiveClsssNameMap.put(Integer.TYPE.getName(), Integer.TYPE);
        primitiveClsssNameMap.put(Long.TYPE.getName(), Long.TYPE);
        primitiveClsssNameMap.put(Double.TYPE.getName(), Double.TYPE);
        primitiveClsssNameMap.put(Float.TYPE.getName(), Float.TYPE);
        primitiveClsssNameMap.put(Boolean.TYPE.getName(), Boolean.TYPE);
    }

    private static final Method IS_BRIDGE_METHOD = getIsBridgeMethod();
    private static final Method IS_SYNTHETIC_METHOD = getIsSyntheticMethod();

    private static Method getIsBridgeMethod() {
        try {
            return Method.class.getMethod("isBridge", (Class[]) null);
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

    private static Method getIsSyntheticMethod() {
        try {
            return Method.class.getMethod("isSynthetic", (Class[]) null);
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

    // ===================================================================================
    //                                                                               Class
    //                                                                               =====
    public static Class<?> forName(String className) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            return Class.forName(className, true, loader);
        } catch (ClassNotFoundException e) {
            String msg = "The class was not found: class=" + className + " loader=" + loader;
            throw new ReflectionFailureException(msg, e);
        }
    }

    public static Object newInstance(Class<?> clazz) {
        assertObjectNotNull("clazz", clazz);
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            String msg = "Failed to instantiate the class: " + clazz;
            throw new ReflectionFailureException(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "Illegal access to the class: " + clazz;
            throw new ReflectionFailureException(msg, e);
        }
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>[] argTypes) {
        try {
            return clazz.getConstructor(argTypes);
        } catch (NoSuchMethodException e) {
            String msg = "Such a method was not found:";
            msg = msg + " class=" + clazz + " argTypes=" + Arrays.asList(argTypes);
            throw new ReflectionFailureException(msg, e);
        }
    }

    public static Object newInstance(Constructor<?> constructor, Object[] args) {
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException e) {
            String msg = "Failed to instantiate the class: " + constructor;
            throw new ReflectionFailureException(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "Illegal access to the constructor: " + constructor;
            throw new ReflectionFailureException(msg, e);
        } catch (InvocationTargetException e) {
            String msg = "The InvocationTargetException occurred: " + constructor;
            throw new ReflectionFailureException(msg, e.getTargetException());
        }
    }

    public static boolean isAssignableFrom(Class<?> toClass, Class<?> fromClass) {
        if (toClass == Object.class && !fromClass.isPrimitive()) {
            return true;
        }
        if (toClass.isPrimitive()) {
            fromClass = getPrimitiveClassIfWrapper(fromClass);
        }
        return toClass.isAssignableFrom(fromClass);
    }

    public static Class<?> getPrimitiveClass(Class<?> clazz) {
        return (Class<?>) wrapperToPrimitiveMap.get(clazz);
    }

    public static Class<?> getPrimitiveClassIfWrapper(Class<?> clazz) {
        Class<?> ret = getPrimitiveClass(clazz);
        if (ret != null) {
            return ret;
        }
        return clazz;
    }

    public static Class<?> getWrapperClass(Class<?> clazz) {
        return (Class<?>) primitiveToWrapperMap.get(clazz);
    }

    // ===================================================================================
    //                                                                               Field
    //                                                                               =====
    public static Field getAccessibleField(Class<?> clazz, String fieldName) {
        assertObjectNotNull("clazz", clazz);
        return findField(clazz, fieldName, VisibilityType.ACCESSIBLE);
    }

    public static Field getPublicField(Class<?> clazz, String fieldName) {
        assertObjectNotNull("clazz", clazz);
        return findField(clazz, fieldName, VisibilityType.PUBLIC);
    }

    public static Field getWholeField(Class<?> clazz, String fieldName) {
        assertObjectNotNull("clazz", clazz);
        return findField(clazz, fieldName, VisibilityType.WHOLE);
    }

    protected static Field findField(Class<?> clazz, String fieldName, VisibilityType visibilityType) {
        assertObjectNotNull("clazz", clazz);
        for (Class<?> target = clazz; target != null && target != Object.class; target = target.getSuperclass()) {
            final Field declaredField;
            try {
                declaredField = target.getDeclaredField(fieldName);
            } catch (SecurityException e) {
                String msg = "The security violation was found: " + fieldName;
                throw new IllegalStateException(msg, e);
            } catch (NoSuchFieldException continued) {
                continue;
            }
            final int modifier = declaredField.getModifiers();
            if (isOutOfTargetForPublic(visibilityType, modifier)) {
                continue;
            }
            if (isOutOfTargetForAccessible(visibilityType, modifier, clazz, target)) {
                continue;
            }
            return declaredField;
        }
        return null;
    }

    public static Object getValue(Field field, Object target) {
        assertObjectNotNull("field", field);
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            String msg = "Illegal access to the field: field=" + field + " target=" + target;
            throw new ReflectionFailureException(msg, e);
        }
    }

    public static Object getValueForcedly(Field field, Object target) {
        assertObjectNotNull("field", field);
        field.setAccessible(true);
        return getValue(field, target);
    }

    public static void setValue(Field field, Object target, Object value) {
        assertObjectNotNull("field", field);
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            String msg = "Illegal access to the field: field=" + field + " target=" + target + " value=" + value;
            throw new ReflectionFailureException(msg, e);
        }
    }

    public static void setValueForcedly(Field field, Object target, Object value) {
        assertObjectNotNull("field", field);
        field.setAccessible(true);
        setValue(field, target, value);
    }

    public static boolean isStaticFinalField(Field field) {
        final int mod = field.getModifiers();
        return Modifier.isStatic(mod) && Modifier.isFinal(mod);
    }

    public static boolean isStaticVariableField(Field field) {
        final int mod = field.getModifiers();
        return Modifier.isStatic(mod) && !Modifier.isFinal(mod);
    }

    public static boolean isInstanceFinalField(Field field) {
        final int mod = field.getModifiers();
        return !Modifier.isStatic(mod) && Modifier.isFinal(mod);
    }

    public static boolean isInstanceVariableField(Field field) {
        final int mod = field.getModifiers();
        return !Modifier.isStatic(mod) && !Modifier.isFinal(mod);
    }

    public static boolean isPublicField(Field field) {
        final int mod = field.getModifiers();
        return Modifier.isPublic(mod);
    }

    // ===================================================================================
    //                                                                              Method
    //                                                                              ======
    /**
     * Get the accessible method that means as follows:
     * <pre>
     * o target class's methods = all
     * o superclass's methods   = public or protected
     * </pre>
     * @param clazz The type of class that defines the method. (NotNull)
     * @param methodName The name of method. (NotNull)
     * @param argTypes The type of argument. (NotNull)
     * @return The instance of method. (NullAllowed: if null, not found)
     */
    public static Method getAccessibleMethod(Class<?> clazz, String methodName, Class<?>[] argTypes) {
        assertObjectNotNull("clazz", clazz);
        assertStringNotNullAndNotTrimmedEmpty("methodName", methodName);
        return findMethod(clazz, methodName, argTypes, VisibilityType.ACCESSIBLE, false);
    }

    /**
     * Get the accessible method that means as follows:
     * <pre>
     * o target class's methods = all
     * o superclass's methods   = public or protected
     * </pre>
     * And it has the flexibly searching so you can specify types of sub-class to argTypes.
     * But if overload methods exist, it returns the first-found method.
     * @param clazz The type of class that defines the method. (NotNull)
     * @param methodName The name of method. (NotNull)
     * @param argTypes The type of argument. (NotNull)
     * @return The instance of method. (NullAllowed: if null, not found)
     */
    public static Method getAccessibleMethodFlexibly(Class<?> clazz, String methodName, Class<?>[] argTypes) {
        assertObjectNotNull("clazz", clazz);
        assertStringNotNullAndNotTrimmedEmpty("methodName", methodName);
        return findMethod(clazz, methodName, argTypes, VisibilityType.ACCESSIBLE, true);
    }

    /**
     * Get the public method.
     * @param clazz The type of class that defines the method. (NotNull)
     * @param methodName The name of method. (NotNull)
     * @param argTypes The type of argument. (NotNull)
     * @return The instance of method. (NullAllowed: if null, not found)
     */
    public static Method getPublicMethod(Class<?> clazz, String methodName, Class<?>[] argTypes) {
        assertObjectNotNull("clazz", clazz);
        assertStringNotNullAndNotTrimmedEmpty("methodName", methodName);
        return findMethod(clazz, methodName, argTypes, VisibilityType.PUBLIC, false);
    }

    /**
     * Get the public method. <br />
     * And it has the flexibly searching so you can specify types of sub-class to argTypes. <br />
     * But if overload methods exist, it returns the first-found method. <br />
     * And no cache so you should cache it yourself if you call several times. <br />
     * @param clazz The type of class that defines the method. (NotNull)
     * @param methodName The name of method. (NotNull)
     * @param argTypes The type of argument. (NotNull)
     * @return The instance of method. (NullAllowed: if null, not found)
     */
    public static Method getPublicMethodFlexibly(Class<?> clazz, String methodName, Class<?>[] argTypes) {
        assertObjectNotNull("clazz", clazz);
        assertStringNotNullAndNotTrimmedEmpty("methodName", methodName);
        return findMethod(clazz, methodName, argTypes, VisibilityType.PUBLIC, true);
    }

    /**
     * Get the method in whole methods that means as follows:
     * <pre>
     * o target class's methods = all
     * o superclass's methods   = all (also contains private)
     * </pre>
     * And no cache so you should cache it yourself if you call several times.
     * @param clazz The type of class that defines the method. (NotNull)
     * @param methodName The name of method. (NotNull)
     * @param argTypes The type of argument. (NotNull)
     * @return The instance of method. (NullAllowed: if null, not found)
     */
    public static Method getWholeMethod(Class<?> clazz, String methodName, Class<?>[] argTypes) {
        assertObjectNotNull("clazz", clazz);
        assertStringNotNullAndNotTrimmedEmpty("methodName", methodName);
        return findMethod(clazz, methodName, argTypes, VisibilityType.WHOLE, false);
    }

    /**
     * Get the method in whole methods that means as follows:
     * <pre>
     * o target class's methods = all
     * o superclass's methods   = all (also contains private)
     * </pre>
     * And it has the flexibly searching so you can specify types of sub-class to argTypes. <br />
     * But if overload methods exist, it returns the first-found method. <br />
     * And no cache so you should cache it yourself if you call several times.
     * @param clazz The type of class that defines the method. (NotNull)
     * @param methodName The name of method. (NotNull)
     * @param argTypes The type of argument. (NotNull)
     * @return The instance of method. (NullAllowed: if null, not found)
     */
    public static Method getWholeMethodFlexibly(Class<?> clazz, String methodName, Class<?>[] argTypes) {
        assertObjectNotNull("clazz", clazz);
        assertStringNotNullAndNotTrimmedEmpty("methodName", methodName);
        return findMethod(clazz, methodName, argTypes, VisibilityType.WHOLE, true);
    }

    protected static Method findMethod(Class<?> clazz, String methodName, Class<?>[] argTypes,
            VisibilityType visibilityType, boolean flexibly) {
        final Method method = doFindMethodBasic(clazz, methodName, argTypes, visibilityType);
        if (method != null) {
            return method;
        } else {
            if (flexibly && argTypes.length >= 1) { // only when argument exists
                return doFindMethodFlexibly(clazz, methodName, argTypes, visibilityType);
            } else {
                return null;
            }
        }
    }

    protected static Method doFindMethodBasic(Class<?> clazz, String methodName, Class<?>[] argTypes,
            VisibilityType visibilityType) {
        for (Class<?> target = clazz; target != null && target != Object.class; target = target.getSuperclass()) {
            final Method declaredMethod;
            try {
                declaredMethod = target.getDeclaredMethod(methodName, argTypes);
            } catch (SecurityException e) {
                String msg = "The security violation was found: " + methodName;
                throw new IllegalStateException(msg, e);
            } catch (NoSuchMethodException continued) {
                continue;
            } catch (NoClassDefFoundError e) {
                String msg = "No class definition: specified=" + clazz.getName() + "#" + methodName + "()";
                throw new IllegalStateException(msg, e);
            }
            final int modifier = declaredMethod.getModifiers();
            if (isOutOfTargetForPublic(visibilityType, modifier)) {
                continue;
            }
            if (isOutOfTargetForAccessible(visibilityType, modifier, clazz, target)) {
                continue;
            }
            return declaredMethod;
        }
        return null;
    }

    protected static Method doFindMethodFlexibly(Class<?> clazz, String methodName, Class<?>[] argTypes,
            VisibilityType visibilityType) {
        for (Class<?> target = clazz; target != null && target != Object.class; target = target.getSuperclass()) {
            final Method[] methods = target.getDeclaredMethods();
            for (int methodIndex = 0; methodIndex < methods.length; ++methodIndex) {
                final Method current = methods[methodIndex];
                final int modifier = current.getModifiers();
                if (isOutOfTargetForPublic(visibilityType, modifier)) {
                    continue;
                }
                if (isOutOfTargetForAccessible(visibilityType, modifier, clazz, target)) {
                    continue;
                }
                if (methodName.equals(current.getName())) {
                    final Class<?>[] types = current.getParameterTypes();
                    if ((types == null || types.length == 0) && (argTypes == null || argTypes.length == 0)) {
                        return current;
                    }
                    if (types.length != argTypes.length) {
                        continue;
                    }
                    boolean diff = false;
                    for (int argIndex = 0; argIndex < types.length; argIndex++) {
                        if (!types[argIndex].isAssignableFrom(argTypes[argIndex])) {
                            diff = true;
                            break;
                        }
                    }
                    if (!diff) {
                        return current;
                    }
                }
            }
        }
        return null;
    }

    protected static boolean isOutOfTargetForPublic(VisibilityType visibilityType, int modifier) {
        return visibilityType == VisibilityType.PUBLIC && !Modifier.isPublic(modifier);
    }

    protected static boolean isOutOfTargetForAccessible(VisibilityType visibilityType, int modifier, Class<?> clazz,
            Class<?> target) {
        return visibilityType == VisibilityType.ACCESSIBLE && clazz != target && isDefaultOrPrivate(modifier);
    }

    /**
     * Invoke the method by reflection.
     * @param method The instance of method. (NotNull)
     * @param target The invocation target instance. (NullAllowed: if null, it means static method)
     * @param args The array of arguments. (NullAllowed)
     * @return The return value of the method. (NullAllowed)
     * @throws ReflectionFailureException When invocation failure and illegal access
     */
    public static Object invoke(Method method, Object target, Object[] args) {
        assertObjectNotNull("method", method);
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            if (t instanceof Error) {
                throw (Error) t;
            }
            String msg = "The InvocationTargetException occurred: ";
            msg = msg + " method=" + method + " target=" + target;
            msg = msg + " args=" + (args != null ? Arrays.asList(args) : "");
            throw new ReflectionFailureException(msg, t);
        } catch (IllegalArgumentException e) {
            String msg = "Illegal argument for the method:";
            msg = msg + " method=" + method + " target=" + target;
            msg = msg + " args=" + (args != null ? Arrays.asList(args) : "");
            throw new ReflectionFailureException(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "Illegal access to the method:";
            msg = msg + " method=" + method + " target=" + target;
            msg = msg + " args=" + (args != null ? Arrays.asList(args) : "");
            throw new ReflectionFailureException(msg, e);
        }
    }

    public static Object invokeForcedly(Method method, Object target, Object[] args) {
        assertObjectNotNull("method", method);
        if (!isPublicMethod(method) && !method.isAccessible()) {
            method.setAccessible(true);
        }
        return invoke(method, target, args);
    }

    public static Object invokeStatic(Method method, Object[] args) {
        assertObjectNotNull("method", method);
        return invoke(method, null, args);
    }

    public static boolean isPublicMethod(Method method) {
        final int mod = method.getModifiers();
        return Modifier.isPublic(mod);
    }

    public static boolean isBridgeMethod(final Method method) {
        if (IS_BRIDGE_METHOD == null) {
            return false;
        }
        return ((Boolean) invoke(IS_BRIDGE_METHOD, method, null)).booleanValue();
    }

    public static boolean isSyntheticMethod(final Method method) {
        if (IS_SYNTHETIC_METHOD == null) {
            return false;
        }
        return ((Boolean) invoke(IS_SYNTHETIC_METHOD, method, null)).booleanValue();
    }

    // ===================================================================================
    //                                                                            Modifier
    //                                                                            ========
    public static enum VisibilityType {
        ACCESSIBLE, PUBLIC, WHOLE
    }

    public static boolean isPublic(int modifier) {
        return Modifier.isPublic(modifier);
    }

    protected static boolean isDefaultOrPrivate(int modifier) {
        return !Modifier.isPublic(modifier) && !Modifier.isProtected(modifier);
    }

    public static boolean isStatic(int modifier) {
        return Modifier.isStatic(modifier);
    }

    // ===================================================================================
    //                                                                             Generic
    //                                                                             =======
    public static Class<?> getGenericType(Type type) {
        return getRawClass(getGenericParameter(type, 0));
    }

    protected static boolean isTypeOf(Type type, Class<?> clazz) {
        if (Class.class.isInstance(type)) {
            return clazz.isAssignableFrom(Class.class.cast(type));
        }
        if (ParameterizedType.class.isInstance(type)) {
            final ParameterizedType parameterizedType = ParameterizedType.class.cast(type);
            return isTypeOf(parameterizedType.getRawType(), clazz);
        }
        return false;
    }

    protected static Class<?> getRawClass(Type type) {
        if (Class.class.isInstance(type)) {
            return Class.class.cast(type);
        }
        if (ParameterizedType.class.isInstance(type)) {
            final ParameterizedType parameterizedType = ParameterizedType.class.cast(type);
            return getRawClass(parameterizedType.getRawType());
        }
        if (WildcardType.class.isInstance(type)) {
            final WildcardType wildcardType = WildcardType.class.cast(type);
            final Type[] types = wildcardType.getUpperBounds();
            return getRawClass(types[0]);
        }
        if (GenericArrayType.class.isInstance(type)) {
            final GenericArrayType genericArrayType = GenericArrayType.class.cast(type);
            final Class<?> rawClass = getRawClass(genericArrayType.getGenericComponentType());
            return Array.newInstance(rawClass, 0).getClass();
        }
        return null;
    }

    protected static Type getGenericParameter(Type type, int index) {
        if (!ParameterizedType.class.isInstance(type)) {
            return null;
        }
        final List<Type> genericParameter = getGenericParameterList(type);
        if (genericParameter.isEmpty()) {
            return null;
        }
        return genericParameter.get(index);
    }

    protected static List<Type> getGenericParameterList(Type type) {
        if (ParameterizedType.class.isInstance(type)) {
            final ParameterizedType paramType = ParameterizedType.class.cast(type);
            return Arrays.asList(paramType.getActualTypeArguments());
        }
        if (GenericArrayType.class.isInstance(type)) {
            final GenericArrayType arrayType = GenericArrayType.class.cast(type);
            return getGenericParameterList(arrayType.getGenericComponentType());
        }
        @SuppressWarnings("unchecked")
        List<Type> emptyList = Collections.EMPTY_LIST;
        return emptyList;
    }

    public static class ReflectionFailureException extends RuntimeException {

        /** Serial version UID. (Default) */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         * @param msg Exception message. (NotNull)
         */
        public ReflectionFailureException(String msg) {
            super(msg);
        }

        /**
         * Constructor.
         * @param msg Exception message. (NotNull)
         * @param cause Throwable. (NotNull)
         */
        public ReflectionFailureException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    /**
     * Assert that the object is not null.
     * @param variableName Variable name. (NotNull)
     * @param value Value. (NotNull)
     * @exception IllegalArgumentException
     */
    protected static void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Assert that the entity is not null and not trimmed empty.
     * @param variableName Variable name. (NotNull)
     * @param value Value. (NotNull)
     */
    public static void assertStringNotNullAndNotTrimmedEmpty(String variableName, String value) {
        assertObjectNotNull("variableName", variableName);
        assertObjectNotNull("value", value);
        if (value.trim().length() == 0) {
            String msg = "The value should not be empty: variableName=" + variableName + " value=" + value;
            throw new IllegalArgumentException(msg);
        }
    }
}
