/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.logic.replaceschema.loaddata.delimiter.secretary;

import org.dbflute.unit.EngineTestCase;

/**
 * @author jflute
 * @since 1.2.7 (2023/02/06 Monday at roppongi japanese)
 */
public class DfDelimiterDataTableDbNameExtractorTest extends EngineTestCase {

    public void test_extractTableDbName_basic() {
        // orthodox
        assertEquals("HANGAR_MYSTIC", createExtractor("./HANGAR_MYSTIC.tsv").extractTableDbName());
        assertEquals("HANGAR_MYSTIC", createExtractor("./sea/HANGAR_MYSTIC.tsv").extractTableDbName());
        assertEquals("hangar_mystic", createExtractor("./sea/hangar_mystic.tsv").extractTableDbName());
        assertEquals("MYSTIC", createExtractor("./sea/MYSTIC.tsv").extractTableDbName());

        // no slash
        assertEquals("HANGAR_MYSTIC", createExtractor("HANGAR_MYSTIC.tsv").extractTableDbName());
        assertEquals("MYSTIC", createExtractor("MYSTIC.tsv").extractTableDbName());

        // no extension
        assertException(StringIndexOutOfBoundsException.class, () -> { // unexpected but no problem
            createExtractor("HANGAR_MYSTIC").extractTableDbName();
        });

        // hyphenated (first one is delimiter)
        assertEquals("HANGAR_MYSTIC", createExtractor("./sea-HANGAR_MYSTIC.tsv").extractTableDbName());
        assertEquals("lostriver-HANGAR_MYSTIC", createExtractor("./sea-lostriver-HANGAR_MYSTIC.tsv").extractTableDbName());
    }

    protected DfDelimiterDataTableDbNameExtractor createExtractor(String filePath) {
        return new DfDelimiterDataTableDbNameExtractor(filePath);
    }
}
