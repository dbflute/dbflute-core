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
package org.dbflute.dbmeta.valuemap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dbflute.jdbc.Classification;
import org.dbflute.jdbc.ClassificationMeta;
import org.dbflute.unit.RuntimeTestCase;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 */
public class MetaHandlingMapToEntityMapperTest extends RuntimeTestCase {

    public void test_MapStringValueAnalyzer_analyzeOther_normalValue() throws Exception {
        // ## Arrange ##
        Map<String, Object> valueMap = new HashMap<String, Object>();
        Object value = new Object();
        valueMap.put("FOO_NAME", value);
        MetaHandlingMapToEntityMapper analyzer = new MetaHandlingMapToEntityMapper(valueMap);
        analyzer.init("FOO_NAME", "fooName", "FooName");

        // ## Act ##
        Object actual = analyzer.analyzeOther(Object.class);

        // ## Assert ##
        assertEquals(value, actual);
    }

    public void test_MapStringValueAnalyzer_analyzeOther_classification() throws Exception {
        // ## Arrange ##
        Map<String, String> valueMap = new HashMap<String, String>();
        valueMap.put("FOO_NAME", "bar");
        MetaHandlingMapToEntityMapper analyzer = new MetaHandlingMapToEntityMapper(valueMap);
        analyzer.init("FOO_NAME", "fooName", "FooName");

        // ## Act ##
        MockClassification actual = analyzer.analyzeOther(MockClassification.class);

        // ## Assert ##
        assertEquals(MockClassification.BAR, actual);
    }

    protected static enum MockClassification implements Classification {
        FOO, BAR;
        public String alias() {
            return null;
        }

        public String code() {
            return null;
        }

        public Set<String> sisterSet() {
            return DfCollectionUtil.emptySet();
        }

        public static MockClassification codeOf(Object obj) {
            return obj instanceof String && obj.equals("bar") ? BAR : null;
        }

        public boolean inGroup(String groupName) {
            return false;
        }

        public Map<String, Object> subItemMap() {
            return DfCollectionUtil.emptyMap();
        }

        public ClassificationMeta meta() {
            return null;
        }
    }
}
