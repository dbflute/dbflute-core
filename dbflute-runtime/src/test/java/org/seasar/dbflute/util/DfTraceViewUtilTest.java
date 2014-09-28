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

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.9.5.1 (2009/06/20 Saturday)
 */
public class DfTraceViewUtilTest extends PlainTestCase {

    public void test_convertToPerformanceView_millis_basic() throws Exception {
        // ## Arrange & Act ##
        String view = DfTraceViewUtil.convertToPerformanceView(10000L);

        // ## Assert ##
        log(view);
        assertEquals("00m10s000ms", view);
    }

    public void test_convertToPerformanceView_millis_min() throws Exception {
        // ## Arrange & Act ##
        String view = DfTraceViewUtil.convertToPerformanceView(100012L);

        // ## Assert ##
        log(view);
        assertEquals("01m40s012ms", view);
    }

    public void test_convertToPerformanceView_millis_hour() throws Exception {
        // ## Arrange & Act ##
        String view = DfTraceViewUtil.convertToPerformanceView(10000000L);

        // ## Assert ##
        log(view);
        assertEquals("166m40s000ms", view);
    }

    public void test_convertToPerformanceView_millis_minus() throws Exception {
        // ## Arrange & Act ##
        String view = DfTraceViewUtil.convertToPerformanceView(-10000000L);

        // ## Assert ##
        log(view);
        assertEquals("-10000000", view);
    }

    public void test_convertToPerformanceView_millis_various() throws Exception {
        assertEquals("00m00s000ms", DfTraceViewUtil.convertToPerformanceView(0L));
        assertEquals("00m00s000ms", DfTraceViewUtil.convertToPerformanceView(-0L));
        assertEquals("00m00s001ms", DfTraceViewUtil.convertToPerformanceView(1L));
        assertEquals("00m01s000ms", DfTraceViewUtil.convertToPerformanceView(1000L));
        assertEquals("01m00s000ms", DfTraceViewUtil.convertToPerformanceView(60000L));
        assertEquals("100m00s000ms", DfTraceViewUtil.convertToPerformanceView(6000000L));
        assertEquals("166m47s789ms", DfTraceViewUtil.convertToPerformanceView(10007789L));
        assertEquals("1666m40s000ms", DfTraceViewUtil.convertToPerformanceView(100000000L));
        assertEquals("16666m40s000ms", DfTraceViewUtil.convertToPerformanceView(1000000000L));
        assertEquals("16666m42s000ms", DfTraceViewUtil.convertToPerformanceView(1000002000L));
    }
}
