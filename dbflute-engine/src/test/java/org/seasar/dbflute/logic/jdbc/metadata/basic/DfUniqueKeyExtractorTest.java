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
package org.seasar.dbflute.logic.jdbc.metadata.basic;

import java.util.List;
import java.util.Map;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class DfUniqueKeyExtractorTest extends PlainTestCase {

    public void test_removePkMatchUniqueKey_simple() throws Exception {
        // ## Arrange ##
        DfUniqueKeyExtractor extractor = new DfUniqueKeyExtractor();
        List<String> pkList = newArrayList("FOO");
        Map<String, Map<Integer, String>> uniqueKeyMap = newHashMap();
        {
            Map<Integer, String> elementMap = newLinkedHashMap();
            elementMap.put(1, "1ST");
            elementMap.put(2, "2ND");
            elementMap.put(3, "3RD");
            uniqueKeyMap.put("FIRST", elementMap);
        }
        {
            Map<Integer, String> elementMap = newLinkedHashMap();
            elementMap.put(1, "FOO");
            uniqueKeyMap.put("SECOND", elementMap);
        }

        // ## Act ##
        extractor.removePkMatchUniqueKey("TEST", pkList, uniqueKeyMap);

        // ## Assert ##
        log(uniqueKeyMap);
        assertTrue(uniqueKeyMap.containsKey("FIRST"));
        assertFalse(uniqueKeyMap.containsKey("SECOND"));
    }

    public void test_removePkMatchUniqueKey_compound() throws Exception {
        // ## Arrange ##
        DfUniqueKeyExtractor extractor = new DfUniqueKeyExtractor();
        List<String> pkList = newArrayList("FOO", "BAR");
        Map<String, Map<Integer, String>> uniqueKeyMap = newHashMap();
        {
            Map<Integer, String> elementMap = newLinkedHashMap();
            elementMap.put(1, "1ST");
            elementMap.put(2, "2ND");
            elementMap.put(3, "3RD");
            uniqueKeyMap.put("FIRST", elementMap);
        }
        {
            Map<Integer, String> elementMap = newLinkedHashMap();
            elementMap.put(1, "FOO");
            elementMap.put(2, "BAR");
            uniqueKeyMap.put("SECOND", elementMap);
        }

        // ## Act ##
        extractor.removePkMatchUniqueKey("TEST", pkList, uniqueKeyMap);

        // ## Assert ##
        assertTrue(uniqueKeyMap.containsKey("FIRST"));
        assertFalse(uniqueKeyMap.containsKey("SECOND"));
    }
}
