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
package org.seasar.dbflute.logic.jdbc.urlanalyzer.factory;

import org.seasar.dbflute.logic.jdbc.urlanalyzer.DfUrlAnalyzer;
import org.seasar.dbflute.logic.jdbc.urlanalyzer.DfUrlAnalyzerDerby;
import org.seasar.dbflute.logic.jdbc.urlanalyzer.DfUrlAnalyzerH2;
import org.seasar.dbflute.logic.jdbc.urlanalyzer.DfUrlAnalyzerMySQL;
import org.seasar.dbflute.logic.jdbc.urlanalyzer.DfUrlAnalyzerPostgreSQL;
import org.seasar.dbflute.logic.jdbc.urlanalyzer.DfUrlAnalyzerSQLServer;
import org.seasar.dbflute.logic.jdbc.urlanalyzer.DfUrlAnalyzerSQLite;
import org.seasar.dbflute.logic.jdbc.urlanalyzer.DfUrlAnalyzerSybase;
import org.seasar.dbflute.properties.DfBasicProperties;

/**
 * @author jflute
 * @since 0.8.1 (2008/10/10 Friday)
 */
public class DfUrlAnalyzerFactory {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DfBasicProperties _basicProperties;
    protected String _url;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfUrlAnalyzerFactory(DfBasicProperties basicProperties, String url) {
        _basicProperties = basicProperties;
        _url = url;
    }

    // ===================================================================================
    //                                                                              Create
    //                                                                              ======
    /**
     * @return The analyzer of URL. (NotNull)
     */
    public DfUrlAnalyzer createAnalyzer() {
        if (_basicProperties.isDatabaseMySQL()) {
            return new DfUrlAnalyzerMySQL(_url);
        } else if (_basicProperties.isDatabasePostgreSQL()) {
            return new DfUrlAnalyzerPostgreSQL(_url);
        } else if (_basicProperties.isDatabaseOracle()) {
            // Oracle does not support catalog
            return createNullAnalyzer();
        } else if (_basicProperties.isDatabaseDB2()) {
            // DB2 (JDBC driver) does not support catalog
            //return new DfUrlAnalyzerDB2(_url);
            return createNullAnalyzer();
        } else if (_basicProperties.isDatabaseSQLServer()) {
            return new DfUrlAnalyzerSQLServer(_url);
        } else if (_basicProperties.isDatabaseH2()) {
            return new DfUrlAnalyzerH2(_url);
        } else if (_basicProperties.isDatabaseDerby()) {
            return new DfUrlAnalyzerDerby(_url);
        } else if (_basicProperties.isDatabaseSQLite()) {
            return new DfUrlAnalyzerSQLite(_url);
        } else if (_basicProperties.isDatabaseMSAccess()) {
            // MS Access (JDBC driver) does not support catalog
            //return new DfUrlAnalyzerMSAccess(_url);
            return createNullAnalyzer();
        } else if (_basicProperties.isDatabaseSybase()) {
            return new DfUrlAnalyzerSybase(_url);
        }
        return createNullAnalyzer();
    }

    protected DfUrlAnalyzer createNullAnalyzer() {
        return new DfUrlAnalyzer() {
            public String extractCatalog() {
                return null;
            }
        };
    }
}
