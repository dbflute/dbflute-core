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
package org.seasar.dbflute.s2dao.extension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author jflute
 */
public class TnSqlLogRegistry {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String PKG_ORG_SEASAR = "org.seasar.";
    protected static final String NAME_SqlLogRegistryLocator = PKG_ORG_SEASAR + "extension.jdbc.SqlLogRegistryLocator";
    protected static final String NAME_getInstance = "getInstance";
    protected static final String NAME_setInstance = "setInstance";
    protected static final String NAME_SqlLogRegistry = PKG_ORG_SEASAR + "extension.jdbc.SqlLogRegistry";
    protected static final String NAME_SqlLogRegistryImpl = PKG_ORG_SEASAR + "extension.jdbc.impl.SqlLogRegistryImpl";
    protected static final String NAME_SqlLog = PKG_ORG_SEASAR + "extension.jdbc.SqlLog";
    protected static final String NAME_SqlLogImpl = PKG_ORG_SEASAR + "extension.jdbc.impl.SqlLogImpl";
    protected static final boolean exists;
    protected static final Class<?> locatorType;
    protected static final Method locatorInstanceMethod;
    protected static final Class<?> registryType;
    protected static final Class<?> registryImplType;
    protected static final Class<?> sqlLogType;
    protected static final Class<?> sqlLogImplType;
    static {
        locatorType = forNameContainerSqlLogRegistryLocator();
        if (locatorType != null) {
            exists = true;
            final String methodName = NAME_getInstance;
            final Method foundMethod;
            try {
                foundMethod = locatorType.getMethod(methodName, (Class[]) null);
            } catch (Exception e) {
                String msg = "Failed to get the method to get the instance: " + methodName;
                throw new IllegalStateException(msg);
            }
            locatorInstanceMethod = foundMethod;
            registryType = forNameContainerSqlLogRegistry();
            registryImplType = forNameContainerSqlLogRegistryImpl();
            sqlLogType = forNameContainerSqlLog();
            sqlLogImplType = forNameContainerSqlLogImpl();
        } else {
            exists = false;
            locatorInstanceMethod = null;
            registryType = null;
            registryImplType = null;
            sqlLogType = null;
            sqlLogImplType = null;
        }
    }

    // ===================================================================================
    //                                                                        Public Entry
    //                                                                        ============
    public static boolean exists() {
        return exists;
    }

    public static boolean setupSqlLogRegistry() {
        if (!exists) {
            return false;
        }
        final Object sqlLogRegistryImpl = createContainerSqlLogRegistryImpl();
        final String methodName = NAME_setInstance;
        try {
            final Method method = locatorType.getMethod(methodName, new Class[] { registryType });
            method.invoke(null, new Object[] { sqlLogRegistryImpl });
            return true;
        } catch (Exception e) {
            String msg = "Failed to set the locator instance:";
            msg = msg + " locatorType=" + locatorType;
            msg = msg + " methodName=" + methodName;
            throw new IllegalStateException(msg, e);
        }
    }

    public static void clearSqlLogRegistry() {
        if (!exists) {
            return;
        }
        final String methodName = "clear";
        try {
            final Object sqlLogRegistry = findContainerSqlLogRegistry();
            final Method method = locatorType.getMethod(methodName, new Class[] {});
            method.invoke(sqlLogRegistry, new Object[] {});
        } catch (Exception e) {
            String msg = "Failed to clear registry of the locator:";
            msg = msg + " locatorType=" + locatorType;
            msg = msg + " methodName=" + methodName;
            throw new IllegalStateException(msg, e);
        }
    }

    public static Object findContainerSqlLogRegistry() {
        if (!exists) {
            return null;
        }
        try {
            return locatorInstanceMethod.invoke(null, (Object[]) null);
        } catch (Exception e) {
            String msg = "Failed to get the locator instance:";
            msg = msg + " locatorType=" + locatorType;
            msg = msg + " methodName=" + NAME_getInstance;
            throw new IllegalStateException(msg, e);
        }
    }

    public static void closeRegistration() {
        if (!exists) {
            return;
        }
        final String methodName = NAME_setInstance;
        try {
            final Method method = locatorType.getMethod(methodName, new Class[] { registryType });
            method.invoke(null, new Object[] { null });
        } catch (Exception e) {
            String msg = "Failed to handle the locator method:";
            msg = msg + " locatorType=" + locatorType;
            msg = msg + " methodName=" + methodName;
            throw new IllegalStateException(msg, e);
        }
    }

    public static void push(String rawSql, String completeSql, Object[] bindArgs, Class<?>[] bindArgTypes,
            Object sqlLogRegistry) {
        if (!exists) {
            return;
        }
        if (sqlLogRegistry == null) {
            throw new IllegalArgumentException("sqlLogRegistry should not be null.");
        }
        final Object sqlLogImpl = createContainerSqlLogImpl(rawSql, completeSql, bindArgs, bindArgTypes);
        reflectSqlLogToContainerSqlLogRegistry(sqlLogImpl, sqlLogRegistry);
    }

    public static String peekCompleteSql() {
        if (!exists) {
            return null;
        }
        final Object sqlLogRegistry = findContainerSqlLogRegistry();
        final Object sqlLog = findLastContainerSqlLog(sqlLogRegistry);
        return extractCompleteSqlFromContainerSqlLog(sqlLog);
    }

    // ===================================================================================
    //                                                                Container Reflection
    //                                                                ====================
    protected static Object createContainerSqlLogRegistryImpl() {
        try {
            final Constructor<?> constructor = registryImplType.getConstructor(int.class);
            return constructor.newInstance(new Object[] { 3 });
        } catch (Exception e) {
            String msg = "Failed to create registryImpl:";
            msg = msg + " registryImplType=" + registryImplType;
            throw new IllegalStateException(msg, e);
        }
    }

    protected static Object createContainerSqlLogImpl(String rawSql, String completeSql, Object[] bindArgs,
            Class<?>[] bindArgTypes) {
        try {
            final Class<?>[] argTypes = new Class[] { String.class, String.class, Object[].class, Class[].class };
            final Constructor<?> constructor = sqlLogImplType.getConstructor(argTypes);
            return constructor.newInstance(new Object[] { rawSql, completeSql, bindArgs, bindArgTypes });
        } catch (Exception e) {
            String msg = "Failed to create sqlLogImpl:";
            msg = msg + " completeSql=" + completeSql;
            msg = msg + " sqlLogImplType=" + sqlLogImplType;
            throw new IllegalStateException(msg, e);
        }
    }

    protected static void reflectSqlLogToContainerSqlLogRegistry(Object sqlLog, Object sqlLogRegistry) {
        if (sqlLog == null || sqlLogRegistry == null) {
            return;
        }
        final String methodName = "add";
        try {
            final Method method = registryType.getMethod(methodName, new Class[] { sqlLogType });
            method.invoke(sqlLogRegistry, new Object[] { sqlLog });
        } catch (Exception e) {
            String msg = "Failed to reflect sqlLogRegistry:";
            msg = msg + " sqlLog=" + sqlLog + " sqlLogRegistry=" + sqlLogRegistry;
            msg = msg + " registryType=" + registryType;
            msg = msg + " methodName=" + methodName;
            throw new IllegalStateException(msg, e);
        }
    }

    protected static Object findLastContainerSqlLog(Object sqlLogRegistry) {
        if (sqlLogRegistry == null) {
            return null;
        }
        final String methodName = "getLast";
        try {
            final Method method = registryType.getMethod(methodName, (Class[]) null);
            return method.invoke(sqlLogRegistry, (Object[]) null);
        } catch (Exception e) {
            String msg = "Failed to find sqlLog:";
            msg = msg + " sqlLogRegistry=" + sqlLogRegistry;
            msg = msg + " methodName=" + methodName;
            throw new IllegalStateException(msg, e);
        }
    }

    protected static String extractCompleteSqlFromContainerSqlLog(Object sqlLog) {
        if (sqlLog == null) {
            return null;
        }
        final String methodName = "getCompleteSql";
        try {
            final Method method = sqlLogType.getMethod(methodName, (Class[]) null);
            return (String) method.invoke(sqlLog, (Object[]) null);
        } catch (Exception e) {
            String msg = "Failed to extract completeSql:";
            msg = msg + " sqlLog=" + sqlLog;
            msg = msg + " methodName=" + methodName;
            throw new IllegalStateException(msg, e);
        }
    }

    protected static Class<?> forNameContainerSqlLogRegistryLocator() {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(NAME_SqlLogRegistryLocator);
        } catch (Exception ignored) {
            return null;
        }
        return clazz;
    }

    protected static Class<?> forNameContainerSqlLogRegistry() {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(NAME_SqlLogRegistry);
        } catch (Exception ignored) {
            return null;
        }
        return clazz;
    }

    protected static Class<?> forNameContainerSqlLogRegistryImpl() {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(NAME_SqlLogRegistryImpl);
        } catch (Exception ignored) {
            return null;
        }
        return clazz;
    }

    protected static Class<?> forNameContainerSqlLog() {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(NAME_SqlLog);
        } catch (Exception ignored) {
            return null;
        }
        return clazz;
    }

    protected static Class<?> forNameContainerSqlLogImpl() {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(NAME_SqlLogImpl);
        } catch (Exception ignored) {
            return null;
        }
        return clazz;
    }
}
