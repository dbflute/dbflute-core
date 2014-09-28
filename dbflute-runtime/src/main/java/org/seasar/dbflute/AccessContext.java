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
package org.seasar.dbflute;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.exception.AccessContextNoValueException;
import org.seasar.dbflute.exception.AccessContextNotFoundException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.resource.DBFluteSystem;

/**
 * The context of DB access. (basically for CommonColumnAutoSetup)
 * @author jflute
 */
public class AccessContext {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(AccessContext.class);

    // ===================================================================================
    //                                                                        Thread Local
    //                                                                        ============
    // -----------------------------------------------------
    //                                         Thread Object
    //                                         -------------
    /** The default thread-local for this. */
    protected static final ThreadLocal<AccessContext> _defaultThreadLocal = new ThreadLocal<AccessContext>();

    /** The default holder for access context, using thread local. (NotNull) */
    protected static final AccessContextHolder _defaultHolder = new AccessContextHolder() {

        public AccessContext provide() {
            return _defaultThreadLocal.get();
        }

        public void save(AccessContext context) {
            _defaultThreadLocal.set(context);
        }
    };

    /** The holder for access context, might be changed. (NotNull: null setting is not allowed) */
    protected static AccessContextHolder _holder = _defaultHolder; // as default

    /** Is this static world locked? e.g. you should unlock it to set your own provider. */
    protected static boolean _locked = true; // at first locked

    /**
     * The holder of for access context. <br />
     * Basically for asynchronous of web framework e.g. Play2.
     */
    public static interface AccessContextHolder {

        /**
         * Provide access context. <br />
         * You should return same instance in same request.
         * @return The instance of access context. (NullAllowed: when no context, but should exist in real handling)
         */
        AccessContext provide();

        /**
         * Hold access context and save it in holder.
         * @param accessContext The access context set by static setter. (NullAllowed: if null, context is removed)
         */
        void save(AccessContext accessContext);
    }

    // -----------------------------------------------------
    //                                        Basic Handling
    //                                        --------------
    /**
     * Get access-context on thread.
     * @return The context of DB access. (NullAllowed)
     */
    public static AccessContext getAccessContextOnThread() {
        return getActiveHolder().provide();
    }

    /**
     * Set access-context on thread.
     * @param accessContext The context of DB access. (NotNull)
     */
    public static void setAccessContextOnThread(AccessContext accessContext) {
        if (accessContext == null) {
            String msg = "The argument 'accessContext' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        getActiveHolder().save(accessContext);
    }

    /**
     * Is existing access-context on thread?
     * @return The determination, true or false.
     */
    public static boolean isExistAccessContextOnThread() {
        return getActiveHolder().provide() != null;
    }

    /**
     * Clear access-context on thread.
     */
    public static void clearAccessContextOnThread() {
        getActiveHolder().save(null);
    }

    /**
     * Get the active holder for access context.
     * @return The holder instance to handle access context. (NotNull)
     */
    protected static AccessContextHolder getActiveHolder() {
        return _holder;
    }

    // -----------------------------------------------------
    //                                            Management
    //                                            ----------
    /**
     * Use the surrogate holder for access context. (automatically locked after setting) <br />
     * You should call this in application initialization if it needs.
     * @param holder The holder instance. (NullAllowed: if null, use default holder)
     */
    public static void useSurrogateHolder(AccessContextHolder holder) {
        assertNotLocked();
        if (_log.isInfoEnabled()) {
            _log.info("...Setting surrogate holder for access context: " + holder);
        }
        if (holder != null) {
            _holder = holder;
        } else {
            _holder = _defaultHolder;
        }
        _locked = true;
    }

    /**
     * Is this static world locked?
     * @return The determination, true or false.
     */
    public static boolean isLocked() {
        return _locked;
    }

    /**
     * Lock this static world, e.g. not to set the holder of thread-local.
     */
    public static void lock() {
        if (_log.isInfoEnabled()) {
            _log.info("...Locking the static world of the access context!");
        }
        _locked = true;
    }

    /**
     * Unlock this static world, e.g. to set the holder of thread-local.
     */
    public static void unlock() {
        if (_log.isInfoEnabled()) {
            _log.info("...Unlocking the static world of the access context!");
        }
        _locked = false;
    }

    /**
     * Assert this is not locked.
     */
    protected static void assertNotLocked() {
        if (!isLocked()) {
            return;
        }
        String msg = "The access context is locked! Don't access at this timing!";
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                  Access Information
    //                                                                  ==================
    // -----------------------------------------------------
    //                                           Access Date
    //                                           -----------
    /**
     * Get access date on thread. <br />
     * If it couldn't get access date from access-context, it returns current date of {@link DBFluteSystem}.
     * @return The date that specifies access time. (NotNull)
     */
    public static Date getAccessDateOnThread() {
        if (isExistAccessContextOnThread()) {
            final AccessContext context = getAccessContextOnThread();
            final java.util.Date accessDate = context.getAccessDate();
            if (accessDate != null) {
                return accessDate;
            }
            final AccessDateProvider provider = context.getAccessDateProvider();
            if (provider != null) {
                final Date provided = provider.getAccessDate();
                if (provided != null) {
                    return provided;
                }
            }
        }
        return DBFluteSystem.currentDate();
    }

    // -----------------------------------------------------
    //                                      Access Timestamp
    //                                      ----------------
    /**
     * Get access time-stamp on thread. <br />
     * If it couldn't get access time-stamp from access-context, it returns current time-stamp of {@link DBFluteSystem}.
     * @return The time-stamp that specifies access time. (NotNull)
     */
    public static Timestamp getAccessTimestampOnThread() {
        if (isExistAccessContextOnThread()) {
            final AccessContext context = getAccessContextOnThread();
            final Timestamp accessTimestamp = context.getAccessTimestamp();
            if (accessTimestamp != null) {
                return accessTimestamp;
            }
            final AccessTimestampProvider provider = context.getAccessTimestampProvider();
            if (provider != null) {
                final Timestamp provided = provider.getAccessTimestamp();
                if (provided != null) {
                    return provided;
                }
            }
        }
        return DBFluteSystem.currentTimestamp();
    }

    // -----------------------------------------------------
    //                                           Access User
    //                                           -----------
    /**
     * Get access user on thread.
     * @return The expression for access user. (NotNull)
     */
    public static String getAccessUserOnThread() {
        if (isExistAccessContextOnThread()) {
            final AccessContext context = getAccessContextOnThread();
            final String accessUser = context.getAccessUser();
            if (accessUser != null) {
                return accessUser;
            }
            final AccessUserProvider provider = context.getAccessUserProvider();
            if (provider != null) {
                final String user = provider.getAccessUser();
                if (user != null) {
                    return user;
                }
            }
        }
        final String methodName = "getAccessUserOnThread()";
        if (isExistAccessContextOnThread()) {
            throwAccessContextNoValueException(methodName, "AccessUser", "user");
        } else {
            throwAccessContextNotFoundException(methodName);
        }
        return null; // unreachable
    }

    // -----------------------------------------------------
    //                                        Access Process
    //                                        --------------
    /**
     * Get access process on thread.
     * @return The expression for access module. (NotNull)
     */
    public static String getAccessProcessOnThread() {
        if (isExistAccessContextOnThread()) {
            final AccessContext context = getAccessContextOnThread();
            final String accessProcess = context.getAccessProcess();
            if (accessProcess != null) {
                return accessProcess;
            }
            final AccessProcessProvider provider = context.getAccessProcessProvider();
            if (provider != null) {
                final String provided = provider.getAccessProcess();
                if (provided != null) {
                    return provided;
                }
            }
        }
        final String methodName = "getAccessProcessOnThread()";
        if (isExistAccessContextOnThread()) {
            throwAccessContextNoValueException(methodName, "AccessProcess", "process");
        } else {
            throwAccessContextNotFoundException(methodName);
        }
        return null; // unreachable
    }

    // -----------------------------------------------------
    //                                         Access Module
    //                                         -------------
    /**
     * Get access module on thread.
     * @return The expression for access module. (NotNull)
     */
    public static String getAccessModuleOnThread() {
        if (isExistAccessContextOnThread()) {
            final AccessContext context = getAccessContextOnThread();
            final String accessModule = context.getAccessModule();
            if (accessModule != null) {
                return accessModule;
            }
            final AccessModuleProvider provider = context.getAccessModuleProvider();
            if (provider != null) {
                final String provided = provider.getAccessModule();
                if (provided != null) {
                    return provided;
                }
            }
        }
        final String methodName = "getAccessModuleOnThread()";
        if (isExistAccessContextOnThread()) {
            throwAccessContextNoValueException(methodName, "AccessModule", "module");
        } else {
            throwAccessContextNotFoundException(methodName);
        }
        return null; // unreachable
    }

    // -----------------------------------------------------
    //                                          Access Value
    //                                          ------------
    /**
     * Get access value on thread.
     * @param key Key. (NotNull)
     * @return The object of access value. (NotNull)
     */
    public static Object getAccessValueOnThread(String key) {
        if (isExistAccessContextOnThread()) {
            final AccessContext context = getAccessContextOnThread();
            final Map<String, Object> accessValueMap = context.getAccessValueMap();
            if (accessValueMap != null) {
                final Object value = accessValueMap.get(key);
                if (value != null) {
                    return value;
                }
            }
        }
        final String methodName = "getAccessValueOnThread(\"" + key + "\")";
        if (isExistAccessContextOnThread()) {
            throwAccessContextNoValueException(methodName, "AccessValue", "value");
        } else {
            throwAccessContextNotFoundException(methodName);
        }
        return null; // unreachable
    }

    // -----------------------------------------------------
    //                                             Exception
    //                                             ---------
    protected static void throwAccessContextNotFoundException(String methodName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The access context was not found on thread.");
        br.addItem("Advice");
        br.addElement("Set up the value before DB access (using common column auto set-up)");
        br.addElement("You should set it up at your application's interceptor or filter.");
        br.addElement("For example:");
        br.addElement("  try {");
        br.addElement("      AccessContext context = new AccessContext();");
        br.addElement("      context.setAccessTimestamp(accessTimestamp);");
        br.addElement("      context.setAccessUser(accessUser);");
        br.addElement("      context.setAccessProcess(accessProcess);");
        br.addElement("      AccessContext.setAccessContextOnThread(context);");
        br.addElement("      return invocation.proceed();");
        br.addElement("  } finally {");
        br.addElement("      AccessContext.clearAccessContextOnThread();");
        br.addElement("  }");
        final String msg = br.buildExceptionMessage();
        throw new AccessContextNotFoundException(msg);
    }

    protected static void throwAccessContextNoValueException(String methodName, String capPropName, String aliasName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to get the access " + aliasName + " in access context on thread.");
        br.addItem("Advice");
        br.addElement("Set up the value before DB access (using common column auto set-up)");
        br.addElement("You should set it up at your application's interceptor or filter.");
        br.addElement("For example:");
        br.addElement("  try {");
        br.addElement("      AccessContext context = new AccessContext();");
        br.addElement("      context.setAccessTimestamp(accessTimestamp);");
        br.addElement("      context.setAccessUser(accessUser);");
        br.addElement("      context.setAccessProcess(accessProcess);");
        br.addElement("      AccessContext.setAccessContextOnThread(context);");
        br.addElement("      return invocation.proceed();");
        br.addElement("  } finally {");
        br.addElement("      AccessContext.clearAccessContextOnThread();");
        br.addElement("  }");
        final String msg = br.buildExceptionMessage();
        throw new AccessContextNoValueException(msg);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected static String ln() {
        return DBFluteSystem.getBasicLn();
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Date _accessDate;
    protected AccessDateProvider _accessDateProvider;

    protected Timestamp _accessTimestamp;
    protected AccessTimestampProvider _accessTimestampProvider;

    protected String _accessUser;
    protected AccessUserProvider _accessUserProvider;

    protected String _accessProcess;
    protected AccessProcessProvider _accessProcessProvider;

    protected String _accessModule;
    protected AccessModuleProvider _accessModuleProvider;

    /** The map of access value, you can freely add your item value. */
    protected Map<String, Object> _accessValueMap;

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _accessDate + ", " + _accessTimestamp + ", " + _accessUser + ", " + _accessProcess + ", "
                + _accessModule + ", " + _accessValueMap + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                           Access Date
    //                                           -----------
    public Date getAccessDate() {
        return _accessDate;
    }

    public void setAccessDate(Date accessDate) {
        this._accessDate = accessDate;
    }

    public AccessDateProvider getAccessDateProvider() {
        return _accessDateProvider;
    }

    public void setAccessDateProvider(AccessDateProvider accessDateProvider) {
        this._accessDateProvider = accessDateProvider;
    }

    // -----------------------------------------------------
    //                                      Access Timestamp
    //                                      ----------------
    public Timestamp getAccessTimestamp() {
        return _accessTimestamp;
    }

    public void setAccessTimestamp(Timestamp accessTimestamp) {
        this._accessTimestamp = accessTimestamp;
    }

    public AccessTimestampProvider getAccessTimestampProvider() {
        return _accessTimestampProvider;
    }

    public void setAccessTimestampProvider(AccessTimestampProvider accessTimestampProvider) {
        this._accessTimestampProvider = accessTimestampProvider;
    }

    // -----------------------------------------------------
    //                                           Access User
    //                                           -----------
    public String getAccessUser() {
        return _accessUser;
    }

    public void setAccessUser(String accessUser) {
        this._accessUser = accessUser;
    }

    public AccessUserProvider getAccessUserProvider() {
        return _accessUserProvider;
    }

    public void setAccessUserProvider(AccessUserProvider accessUserProvider) {
        this._accessUserProvider = accessUserProvider;
    }

    // -----------------------------------------------------
    //                                        Access Process
    //                                        --------------
    public String getAccessProcess() {
        return _accessProcess;
    }

    public void setAccessProcess(String accessProcess) {
        this._accessProcess = accessProcess;
    }

    public AccessProcessProvider getAccessProcessProvider() {
        return _accessProcessProvider;
    }

    public void setAccessProcessProvider(AccessProcessProvider accessProcessProvider) {
        this._accessProcessProvider = accessProcessProvider;
    }

    // -----------------------------------------------------
    //                                         Access Module
    //                                         -------------
    public String getAccessModule() {
        return _accessModule;
    }

    public void setAccessModule(String accessModule) {
        this._accessModule = accessModule;
    }

    public AccessModuleProvider getAccessModuleProvider() {
        return _accessModuleProvider;
    }

    public void setAccessModuleProvider(AccessModuleProvider accessModuleProvider) {
        this._accessModuleProvider = accessModuleProvider;
    }

    // -----------------------------------------------------
    //                                          Access Value
    //                                          ------------
    public Map<String, Object> getAccessValueMap() {
        return _accessValueMap;
    }

    /**
     * Register the access value by the key.
     * @param key The key of the access value. (NotNull)
     * @param value The value as object. (NullAllowed)
     */
    public void registerAccessValue(String key, Object value) {
        if (_accessValueMap == null) {
            _accessValueMap = new HashMap<String, Object>();
        }
        _accessValueMap.put(key, value);
    }

    // ===================================================================================
    //                                                                  Provider Interface
    //                                                                  ==================
    /**
     * The provider interface of access date.
     */
    public static interface AccessDateProvider {

        /**
         * Get access date.
         * @return The date that specifies access time. (NotNull)
         */
        Date getAccessDate();
    }

    /**
     * The provider interface of access time-stamp.
     */
    public static interface AccessTimestampProvider {

        /**
         * Get access time-stamp.
         * @return The time-stamp that specifies access time. (NotNull)
         */
        Timestamp getAccessTimestamp();
    }

    /**
     * The provider interface of access user.
     */
    public static interface AccessUserProvider {

        /**
         * Get access user.
         * @return The expression for access user. (NotNull)
         */
        String getAccessUser();
    }

    /**
     * The provider interface of access process.
     */
    public static interface AccessProcessProvider {

        /**
         * Get access process.
         * @return The expression for access process. (NotNull)
         */
        String getAccessProcess();
    }

    /**
     * The provider interface of access module.
     */
    public static interface AccessModuleProvider {

        /**
         * Get access module.
         * @return The expression for access module. (NotNull)
         */
        String getAccessModule();
    }
}
