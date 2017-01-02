/*
 * Copyright 2014-2017 the original author or authors.
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
package org.dbflute.bhv.proposal.callback;

import java.lang.reflect.Method;

import org.dbflute.unit.RuntimeTestCase;
import org.dbflute.util.DfReflectionUtil;

/**
 * @author jflute
 * @since 1.1.0-sp1 (2015/01/19 Monday)
 */
public class SimpleTraceableSqlStringFilterTest extends RuntimeTestCase {

    public void test_buildInvokeMark_noMethod() throws Exception {
        // ## Arrange ##
        Method method = DfReflectionUtil.getPublicMethod(getClass(), "test_buildInvokeMark", null);
        SimpleTraceableSqlStringFilter filter = new SimpleTraceableSqlStringFilter(method, null);

        // ## Act ##
        String invokeMark = filter.buildInvokeMark();

        // ## Assert ##
        log(invokeMark);
        assertEquals("", invokeMark);
    }

    public void test_buildInvokeMark_noMethod_bothOverride() throws Exception {
        // ## Arrange ##
        Method method = DfReflectionUtil.getPublicMethod(getClass(), "test_buildInvokeMark", null);
        SimpleTraceableSqlStringFilter filter = new SimpleTraceableSqlStringFilter(method, null) {
            protected String buildDeclaringClass() {
                return "maihama";
            }

            protected String buildMethodName() {
                return "sea";
            }
        };

        // ## Act ##
        String invokeMark = filter.buildInvokeMark();

        // ## Assert ##
        log(invokeMark);
        assertEquals("maihama@sea()", invokeMark);
    }

    public void test_buildInvokeMark_noMethod_overrideDeclaringClassOnly() throws Exception {
        // ## Arrange ##
        Method method = DfReflectionUtil.getPublicMethod(getClass(), "test_buildInvokeMark", null);
        SimpleTraceableSqlStringFilter filter = new SimpleTraceableSqlStringFilter(method, null) {
            protected String buildDeclaringClass() {
                return "maihama";
            }
        };

        // ## Act ##
        String invokeMark = filter.buildInvokeMark();

        // ## Assert ##
        log(invokeMark);
        assertEquals("maihama", invokeMark);
    }

    public void test_buildInvokeMark_noMethod_overrideMethodNameOnly() throws Exception {
        // ## Arrange ##
        Method method = DfReflectionUtil.getPublicMethod(getClass(), "test_buildInvokeMark", null);
        SimpleTraceableSqlStringFilter filter = new SimpleTraceableSqlStringFilter(method, null) {
            protected String buildMethodName() {
                return "sea";
            }
        };

        // ## Act ##
        String invokeMark = filter.buildInvokeMark();

        // ## Assert ##
        log(invokeMark);
        assertEquals("sea()", invokeMark);
    }
}
