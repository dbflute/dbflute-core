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
package org.seasar.dbflute.logic.jdbc.urlanalyzer;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class DfUrlAnalyzerH2Test extends PlainTestCase {

    public void test_extractCatalog_basic() throws Exception {
        // ## Arrange ##
        DfUrlAnalyzer analyzer = createTarget("jdbc:h2:file:../src/main/resources/ponpon/exampledb");

        // ## Act ##
        String catalog = analyzer.extractCatalog();

        // ## Assert ##
        assertEquals("exampledb", catalog);
    }

    public void test_extractCatalog_option() throws Exception {
        // ## Arrange ##
        DfUrlAnalyzer analyzer = createTarget("jdbc:h2:file:../src/main/resources/ponpon/exampledb;charSet=UTF-8");

        // ## Act ##
        String catalog = analyzer.extractCatalog();

        // ## Assert ##
        assertEquals("exampledb", catalog);
    }

    public void test_extractCatalog_nopath() throws Exception {
        // ## Arrange ##
        DfUrlAnalyzer analyzer = createTarget("jdbc:h2:file:exampledb");

        // ## Act ##
        String catalog = analyzer.extractCatalog();

        // ## Assert ##
        assertEquals("exampledb", catalog);
    }

    protected DfUrlAnalyzer createTarget(String url) {
        return new DfUrlAnalyzerH2(url);
    }
}
