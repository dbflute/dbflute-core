/*
 * Copyright 2014-2018 the original author or authors.
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

import org.dbflute.logic.jdbc.urlanalyzer.DfUrlAnalyzer;
import org.dbflute.logic.jdbc.urlanalyzer.DfUrlAnalyzerSybase;
import org.dbflute.unit.EngineTestCase;

/**
 * @author jflute
 * @since 0.9.7.6 (2010/11/26 Friday)
 */
public class DfUrlAnalyzerSybaseTest extends EngineTestCase {

    public void test_extractCatalog_basic() throws Exception {
        // ## Arrange ##
        DfUrlAnalyzer analyzer = createTarget("jdbc:sybase:Tds:localhost:2638/exampledb");

        // ## Act ##
        String catalog = analyzer.extractCatalog();

        // ## Assert ##
        assertEquals("exampledb", catalog);
    }

    public void test_extractCatalog_option_ampersand() throws Exception {
        // ## Arrange ##
        DfUrlAnalyzer analyzer = createTarget("jdbc:sybase:Tds:localhost:2638/exampledb&charSet=UTF-8");

        // ## Act ##
        String catalog = analyzer.extractCatalog();

        // ## Assert ##
        assertEquals("exampledb", catalog);
    }

    public void test_extractCatalog_option_question() throws Exception {
        // ## Arrange ##
        DfUrlAnalyzer analyzer = createTarget("jdbc:sybase:Tds:localhost:5432/exampledb?charSet=UTF-8");

        // ## Act ##
        String catalog = analyzer.extractCatalog();

        // ## Assert ##
        assertEquals("exampledb", catalog);
    }

    protected DfUrlAnalyzer createTarget(String url) {
        return new DfUrlAnalyzerSybase(url);
    }
}
