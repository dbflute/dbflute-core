/*
 * Copyright 2014-2014 the original author or authors.
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
package org.dbflute.bhv.core.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbflute.Entity;
import org.dbflute.bhv.readable.EntityRowHandler;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.system.DBFluteSystem;

/**
 * The context of condition-bean.
 * @author jflute
 */
public class ConditionBeanContext {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(ConditionBeanContext.class);

    // ===================================================================================
    //                                                             ConditionBean on Thread
    //                                                             =======================
    /** The thread-local for condition-bean. */
    private static final ThreadLocal<ConditionBean> _conditionBeanLocal = new ThreadLocal<ConditionBean>();

    /**
     * Get condition-bean on thread.
     * @return Condition-bean. (NullAllowed)
     */
    public static ConditionBean getConditionBeanOnThread() {
        return (ConditionBean) _conditionBeanLocal.get();
    }

    /**
     * Set condition-bean on thread.
     * @param cb Condition-bean. (NotNull)
     */
    public static void setConditionBeanOnThread(ConditionBean cb) {
        if (cb == null) {
            String msg = "The argument[cb] must not be null.";
            throw new IllegalArgumentException(msg);
        }
        _conditionBeanLocal.set(cb);
    }

    /**
     * Is existing condition-bean on thread?
     * @return The determination, true or false.
     */
    public static boolean isExistConditionBeanOnThread() {
        return (_conditionBeanLocal.get() != null);
    }

    /**
     * Clear condition-bean on thread.
     */
    public static void clearConditionBeanOnThread() {
        _conditionBeanLocal.set(null);
    }

    // ===================================================================================
    //                                                          EntityRowHandler on Thread
    //                                                          ==========================
    /** The thread-local for entity row handler. */
    private static final ThreadLocal<EntityRowHandler<? extends Entity>> _entityRowHandlerLocal = new ThreadLocal<EntityRowHandler<? extends Entity>>();

    /**
     * Get the handler of entity row. on thread.
     * @return The handler of entity row. (NullAllowed)
     */
    public static EntityRowHandler<? extends Entity> getEntityRowHandlerOnThread() {
        return (EntityRowHandler<? extends Entity>) _entityRowHandlerLocal.get();
    }

    /**
     * Set the handler of entity row on thread.
     * @param handler The handler of entity row. (NotNull)
     */
    public static void setEntityRowHandlerOnThread(EntityRowHandler<? extends Entity> handler) {
        if (handler == null) {
            String msg = "The argument[handler] must not be null.";
            throw new IllegalArgumentException(msg);
        }
        _entityRowHandlerLocal.set(handler);
    }

    /**
     * Is existing the handler of entity row on thread?
     * @return The determination, true or false.
     */
    public static boolean isExistEntityRowHandlerOnThread() {
        return (_entityRowHandlerLocal.get() != null);
    }

    /**
     * Clear the handler of entity row on thread.
     */
    public static void clearEntityRowHandlerOnThread() {
        _entityRowHandlerLocal.set(null);
    }

    // ===================================================================================
    //                                                                        Cool Classes
    //                                                                        ============
    public static void loadCoolClasses() {
        boolean debugEnabled = false; // If you watch the log, set this true.
        // Against the ClassLoader Headache for S2Container's HotDeploy!
        // However, These classes are in Library since 0.9.0
        // so this process may not be needed...
        final StringBuilder sb = new StringBuilder();
        {
            final Class<?> clazz = org.dbflute.outsidesql.paging.SimplePagingBean.class;
            if (debugEnabled) {
                sb.append("  ...Loading class of " + clazz.getName() + " by " + clazz.getClassLoader().getClass())
                        .append(ln());
            }
        }
        {
            loadClass(org.dbflute.hook.AccessContext.class);
            loadClass(org.dbflute.hook.CallbackContext.class);
            loadClass(org.dbflute.bhv.readable.EntityRowHandler.class);
            loadClass(org.dbflute.cbean.coption.FromToOption.class);
            loadClass(org.dbflute.cbean.coption.LikeSearchOption.class);
            loadClass(org.dbflute.cbean.result.grouping.GroupingOption.class);
            loadClass(org.dbflute.cbean.result.grouping.GroupingRowEndDeterminer.class);
            loadClass(org.dbflute.cbean.result.grouping.GroupingRowResource.class);
            loadClass(org.dbflute.cbean.result.grouping.GroupingRowSetupper.class);
            loadClass(org.dbflute.cbean.paging.PageNumberLink.class);
            loadClass(org.dbflute.cbean.paging.PageNumberLinkSetupper.class);
            loadClass(org.dbflute.jdbc.CursorHandler.class);
            if (debugEnabled) {
                sb.append("  ...Loading class of ...and so on");
            }
        }
        if (debugEnabled) {
            _log.debug("{Initialize against the ClassLoader Headache}" + ln() + sb);
        }
    }

    protected static void loadClass(Class<?> clazz) { // for avoiding Find-Bugs warnings
        // do nothing
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected static String ln() {
        return DBFluteSystem.ln();
    }
}
