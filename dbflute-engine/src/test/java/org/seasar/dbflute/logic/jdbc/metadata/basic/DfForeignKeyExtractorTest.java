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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.seasar.dbflute.logic.jdbc.metadata.info.DfForeignKeyMeta;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 1.0.5F (2014/05/07 Wednesday)
 */
public class DfForeignKeyExtractorTest extends PlainTestCase {

    public void test_immobilizeOrder_basic() throws Exception {
        // ## Arrange ##
        DfForeignKeyExtractor extractor = new DfForeignKeyExtractor();
        Map<String, DfForeignKeyMeta> fkMap = newLinkedHashMap();
        {
            DfForeignKeyMeta meta = new DfForeignKeyMeta();
            meta.putColumnNameAll(newLinkedHashMap("b", "1", "c", "2"));
            fkMap.put("foo", meta);
        }
        {
            DfForeignKeyMeta meta = new DfForeignKeyMeta();
            meta.putColumnNameAll(newLinkedHashMap("a", "1", "b", "2"));
            fkMap.put("bar", meta);
        }
        {
            DfForeignKeyMeta meta = new DfForeignKeyMeta();
            meta.putColumnNameAll(newLinkedHashMap("c", "1", "b", "2"));
            fkMap.put("qux", meta);
        }
        {
            DfForeignKeyMeta meta = new DfForeignKeyMeta();
            meta.putColumnNameAll(newLinkedHashMap("c", "7", "a", "2"));
            fkMap.put("corge", meta);
        }
        {
            DfForeignKeyMeta meta = new DfForeignKeyMeta();
            meta.putColumnNameAll(newLinkedHashMap("c", "8"));
            fkMap.put("grault", meta);
        }

        // ## Act ##
        Map<String, DfForeignKeyMeta> sortedMap = extractor.immobilizeOrder(fkMap);

        // ## Assert ##
        assertEquals(5, sortedMap.size());
        Set<Entry<String, DfForeignKeyMeta>> entrySet = sortedMap.entrySet();
        Iterator<Entry<String, DfForeignKeyMeta>> iterator = entrySet.iterator();
        {
            Entry<String, DfForeignKeyMeta> entry = iterator.next();
            assertEquals("bar", entry.getKey());
            assertEquals("1", entry.getValue().getColumnNameMap().get("a"));
            assertEquals("2", entry.getValue().getColumnNameMap().get("b"));
        }
        {
            Entry<String, DfForeignKeyMeta> entry = iterator.next();
            assertEquals("foo", entry.getKey());
            assertEquals("1", entry.getValue().getColumnNameMap().get("b"));
            assertEquals("2", entry.getValue().getColumnNameMap().get("c"));
        }
        {
            Entry<String, DfForeignKeyMeta> entry = iterator.next();
            assertEquals("grault", entry.getKey());
            assertEquals("8", entry.getValue().getColumnNameMap().get("c"));
        }
        {
            Entry<String, DfForeignKeyMeta> entry = iterator.next();
            assertEquals("corge", entry.getKey());
            assertEquals("7", entry.getValue().getColumnNameMap().get("c"));
            assertEquals("2", entry.getValue().getColumnNameMap().get("a"));
        }
        {
            Entry<String, DfForeignKeyMeta> entry = iterator.next();
            assertEquals("qux", entry.getKey());
            assertEquals("1", entry.getValue().getColumnNameMap().get("c"));
            assertEquals("2", entry.getValue().getColumnNameMap().get("b"));
        }
    }
}
