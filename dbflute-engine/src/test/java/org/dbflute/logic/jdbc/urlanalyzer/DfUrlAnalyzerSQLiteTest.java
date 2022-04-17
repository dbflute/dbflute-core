/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.logic.jdbc.urlanalyzer;

import org.dbflute.unit.EngineTestCase;

/**
 * @author jflute
 */
public class DfUrlAnalyzerSQLiteTest extends EngineTestCase {

    public void test_extractCatalog_basic() throws Exception {
        // ## Arrange ##
        DfUrlAnalyzer analyzer = createTarget("jdbc:sqlite:../src/main/resources/ponpon/exampledb.db");

        // ## Act ##
        String catalog = analyzer.extractCatalog();

        // ## Assert ##
        assertEquals("exampledb", catalog);
    }

    public void test_extractCatalog_option() throws Exception {
        // ## Arrange ##
        DfUrlAnalyzer analyzer = createTarget("jdbc:sqlite:../src/main/resources/ponpon/exampledb.db;charSet=UTF-8");

        // ## Act ##
        String catalog = analyzer.extractCatalog();

        // ## Assert ##
        assertEquals("exampledb", catalog);
    }

    public void test_extractCatalog_nopath() throws Exception {
        // ## Arrange ##
        DfUrlAnalyzer analyzer = createTarget("jdbc:sqlite:exampledb.db");

        // ## Act ##
        String catalog = analyzer.extractCatalog();

        // ## Assert ##
        assertEquals("exampledb", catalog);
    }

    protected DfUrlAnalyzer createTarget(String url) {
        return new DfUrlAnalyzerSQLite(url);
    }
}
