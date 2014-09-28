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
package org.apache.torque.engine.database.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class TableTest extends PlainTestCase {

    public void test_Table_extractMinimumRelationIndex() throws Exception {
        final Table table = new Table();
        final Map<String, Integer> relationIndexMap = new LinkedHashMap<String, Integer>();
        {
            relationIndexMap.clear();
            relationIndexMap.put("0", 0);
            relationIndexMap.put("1", 1);
            relationIndexMap.put("2", 2);
            relationIndexMap.put("3", 3);
            final int minimumRelationIndex = table.extractMinimumRelationIndex(relationIndexMap);
            Assert.assertEquals(4, minimumRelationIndex);
        }
        {
            relationIndexMap.clear();
            relationIndexMap.put("1", 1);
            relationIndexMap.put("2", 2);
            relationIndexMap.put("3", 3);
            final int minimumRelationIndex = table.extractMinimumRelationIndex(relationIndexMap);
            Assert.assertEquals(0, minimumRelationIndex);
        }
        {
            relationIndexMap.clear();
            relationIndexMap.put("0", 0);
            relationIndexMap.put("1", 1);
            relationIndexMap.put("3", 3);
            final int minimumRelationIndex = table.extractMinimumRelationIndex(relationIndexMap);
            Assert.assertEquals(2, minimumRelationIndex);
        }
        {
            relationIndexMap.clear();
            relationIndexMap.put("0", 0);
            relationIndexMap.put("1", 1);
            relationIndexMap.put("3", 3);
            relationIndexMap.put("5", 5);
            final int minimumRelationIndex = table.extractMinimumRelationIndex(relationIndexMap);
            Assert.assertEquals(2, minimumRelationIndex);
        }
    }

    public void test_Table_buildVersionNoUncapitalisedJavaName() throws Exception {
        // ## Arrange ##
        final Table table = new Table();

        // ## Act & Assert ##
        Assert.assertEquals("versionNo", table.buildVersionNoUncapitalisedJavaName("VersionNo"));
        Assert.assertEquals("versionNo", table.buildVersionNoUncapitalisedJavaName("versionNo"));
        Assert.assertEquals("versionno", table.buildVersionNoUncapitalisedJavaName("versionno"));
    }
}
