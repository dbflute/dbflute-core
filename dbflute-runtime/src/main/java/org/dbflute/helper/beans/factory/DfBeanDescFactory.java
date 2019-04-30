/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.helper.beans.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dbflute.helper.beans.DfBeanDesc;
import org.dbflute.helper.beans.impl.DfBeanDescImpl;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class DfBeanDescFactory {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final Map<Class<?>, DfBeanDesc> beanDescCache = new ConcurrentHashMap<Class<?>, DfBeanDesc>(1024);

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfBeanDescFactory() {
    }

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    public static DfBeanDesc getBeanDesc(Class<?> clazz) {
        DfBeanDesc beanDesc = beanDescCache.get(clazz);
        if (beanDesc == null) {
            beanDesc = new DfBeanDescImpl(clazz);
            beanDescCache.put(clazz, beanDesc);
        }
        return beanDesc;
    }

    public static void clear() {
        beanDescCache.clear();
    }
}
