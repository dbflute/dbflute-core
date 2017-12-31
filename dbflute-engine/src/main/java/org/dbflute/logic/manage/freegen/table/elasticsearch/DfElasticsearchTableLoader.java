/*
 * Copyright 2014-2017 the original author or authors.
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
package org.dbflute.logic.manage.freegen.table.elasticsearch;

import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenMetaData;
import org.dbflute.logic.manage.freegen.DfFreeGenTableLoader;

/**
 * @author jflute
 */
public class DfElasticsearchTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    @Override
    public DfFreeGenMetaData loadTable(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        return new DfElasticsearchLoadingAgent(requestName, resource, mapProp).loadTable();
    }
}
