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
package org.dbflute.infra.core;

import java.util.Map;
import java.util.Set;

import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 */
public class DfDatabaseNameMappingTest extends RuntimeTestCase {

    public void test_getDatabaseBaseInfo() {
        final DfDatabaseNameMapping config = DfDatabaseNameMapping.getInstance();
        final Map<String, Map<String, String>> databaseBaseInfo = config.analyze();
        assertNotNull(databaseBaseInfo);
        final Set<String> keySet = databaseBaseInfo.keySet();
        for (String key : keySet) {
            assertNotNull(key);

            log("[" + key + "]");

            final Map<String, String> infoElement = (Map<String, String>) databaseBaseInfo.get(key);
            assertNotNull(infoElement);
            final Set<String> elementKeySet = infoElement.keySet();
            for (String elementKey : elementKeySet) {
                final String elementValue = infoElement.get(elementKey);
                log("    " + elementKey + "=" + elementValue);
                assertNotNull(elementKey);
                assertNotNull(elementValue);
            }
        }
    }

    public void test_analyze() {
        final DfDatabaseNameMapping config = DfDatabaseNameMapping.getInstance();
        final Map<String, Map<String, String>> databaseBaseInfo = config.analyze();
        assertNotNull(databaseBaseInfo);
        log("databaseBaseInfoTest=" + databaseBaseInfo);
    }
}
