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
package org.dbflute.logic.doc.schemahtml;

import java.util.List;
import java.util.Map;

import org.apache.torque.engine.database.model.Table;
import org.dbflute.logic.doc.arrqy.DfArrangeQueryDocSetupper;
import org.dbflute.logic.doc.arrqy.DfArrangeQueryTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.9 (2018/12/31 Monday)
 */
public class DfSchemaHtmlDataArrangeQuery {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfSchemaHtmlDataArrangeQuery.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Map<String, DfArrangeQueryTable> _arrangeQueryTableMap;

    // ===================================================================================
    //                                                                 Available Procedure
    //                                                                 ===================
    public Map<String, DfArrangeQueryTable> getArrangeQueryTableMap(List<Table> tableList) {
        if (_arrangeQueryTableMap != null) {
            return _arrangeQueryTableMap;
        }
        _log.info(" ");
        _log.info("...Setting up arrange queries for documents (ArrangeQueryDoc)");
        _arrangeQueryTableMap = new DfArrangeQueryDocSetupper().extractArrangeQuery(tableList);
        return _arrangeQueryTableMap;
    }
}
