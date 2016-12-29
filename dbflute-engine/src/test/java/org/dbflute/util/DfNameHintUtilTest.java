/*
 * Copyright 2014-2016 the original author or authors.
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
package org.dbflute.util;

import java.util.Arrays;
import java.util.List;

import org.dbflute.unit.EngineTestCase;

/**
 * @author jflute
 */
public class DfNameHintUtilTest extends EngineTestCase {

    // ===================================================================================
    //                                                                    isTargetByHint()
    //                                                                    ================
    public void test_isTargetByHint() {
        {
            List<String> targetList = Arrays.asList(new String[] { "prefix:MY_" });
            List<String> exceptList = Arrays.asList(new String[] {});
            assertTrue(DfNameHintUtil.isTargetByHint("MY_NAME", targetList, exceptList));
            assertFalse(DfNameHintUtil.isTargetByHint("MO_NAME", targetList, exceptList));
        }
        {
            List<String> targetList = Arrays.asList(new String[] {});
            List<String> exceptList = Arrays.asList(new String[] { "prefix:MY_" });
            assertFalse(DfNameHintUtil.isTargetByHint("MY_NAME", targetList, exceptList));
            assertTrue(DfNameHintUtil.isTargetByHint("MO_NAME", targetList, exceptList));
        }
        {
            List<String> targetList = Arrays.asList(new String[] { "prefix:MY_" });
            List<String> exceptList = Arrays.asList(new String[] { "prefix:MY_" });
            assertTrue(DfNameHintUtil.isTargetByHint("MY_NAME", targetList, exceptList));
            assertFalse(DfNameHintUtil.isTargetByHint("MO_NAME", targetList, exceptList));
        }
        {
            List<String> targetList = Arrays.asList(new String[] { "prefix:MY_" });
            List<String> exceptList = Arrays.asList(new String[] { "prefix:MO_" });
            assertTrue(DfNameHintUtil.isTargetByHint("MY_NAME", targetList, exceptList));
            assertFalse(DfNameHintUtil.isTargetByHint("MO_NAME", targetList, exceptList));
        }
    }

    // ===================================================================================
    //                                                                    isHitByTheHint()
    //                                                                    ================
    public void test_isHitByTheHint_basic() {
        assertTrue(DfNameHintUtil.isHitByTheHint("XXX_YN", DfNameHintUtil.SUFFIX_MARK + "_YN"));
        assertTrue(DfNameHintUtil.isHitByTheHint("L_XXX", DfNameHintUtil.PREFIX_MARK + "L_"));
        assertTrue(DfNameHintUtil.isHitByTheHint("XXX", "XXX"));
        assertFalse(DfNameHintUtil.isHitByTheHint("XXX_YN", DfNameHintUtil.PREFIX_MARK + "_YN"));
        assertFalse(DfNameHintUtil.isHitByTheHint("L_XXX", DfNameHintUtil.SUFFIX_MARK + "L_"));
    }

    public void test_isHitByTheHint_pattern() {
        assertTrue(DfNameHintUtil.isHitByTheHint("1234", DfNameHintUtil.PATTERN_MARK + "\\d{4}"));
        assertFalse(DfNameHintUtil.isHitByTheHint("123d", DfNameHintUtil.PATTERN_MARK + "\\d{4}"));
        assertFalse(DfNameHintUtil.isHitByTheHint("a234", DfNameHintUtil.PATTERN_MARK + "\\d{4}"));
        assertFalse(DfNameHintUtil.isHitByTheHint("123", DfNameHintUtil.PATTERN_MARK + "\\d{4}"));
    }
}
