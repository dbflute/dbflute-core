/*
 * Copyright 2014-2026 the original author or authors.
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
package org.dbflute.hook.thread;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.dbflute.hook.AccessContext;
import org.dbflute.hook.AccessContext.AccessContextHolder;
import org.dbflute.unit.RuntimeTestCase;
import org.dbflute.util.DfReflectionUtil;

/**
 * @author jflute
 * @since 1.3.2 (2026/02/21 Saturday at ichihara)
 */
public class ThreadLocalClearConfirmTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final ThreadLocal<AccessContext> _surrogateThreadLocal = new ThreadLocal<AccessContext>();

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    @Override
    protected void setUp() throws Exception {
        log("[1] before setUp(): sizeObj=" + extractLocalMapSize());

        AccessContext.unlock();
        AccessContext.useSurrogateHolder(new AccessContextHolder() {
            @Override
            public void save(AccessContext accessContext) {
                if (accessContext != null) {
                    _surrogateThreadLocal.set(accessContext);
                } else {
                    //_surrogateThreadLocal.set(null); // sizeObj=4 (from 4)
                    _surrogateThreadLocal.remove(); // sizeObj=3 (from 3)
                }
            }

            @Override
            public AccessContext provide() {
                return _surrogateThreadLocal.get();
            }
        });

        super.setUp();
        log("[2] after setUp(): sizeObj=" + extractLocalMapSize());
    }

    @Override
    protected void tearDown() throws Exception {
        log("[4] before tearDown(): sizeObj=" + extractLocalMapSize());
        super.tearDown();
        log("[5] after tearDown(): sizeObj=" + extractLocalMapSize());
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public void test_demo() {
        /*
        - [1] before setUp(): sizeObj=3
        - <<< ThreadLocalClearTest.test_demo() >>>
        - [2] after setUp(): sizeObj=4
        - [3] in test: sizeObj=4
        - [4] before tearDown(): sizeObj=4
        if set(null)():
          - [5] after tearDown(): sizeObj=4
        if remove():
          - [5] after tearDown(): sizeObj=3
         */
        log("[3] in test: sizeObj=" + extractLocalMapSize());
    }

    // ===================================================================================
    //                                                                     Deep Reflection
    //                                                                     ===============
    private int extractLocalMapSize() {
        Object threadLocalMap = extractThreadLocalMap();
        Field sizeField = DfReflectionUtil.getWholeField(threadLocalMap.getClass(), "size");
        return (int) DfReflectionUtil.getValueForcedly(sizeField, threadLocalMap);
    }

    private Object extractThreadLocalMap() {
        Method mapMethod = DfReflectionUtil.getWholeMethod(ThreadLocal.class, "getMap", new Class[] { Thread.class });
        mapMethod.setAccessible(true);
        return DfReflectionUtil.invoke(mapMethod, _surrogateThreadLocal, new Object[] { Thread.currentThread() });
    }
}
