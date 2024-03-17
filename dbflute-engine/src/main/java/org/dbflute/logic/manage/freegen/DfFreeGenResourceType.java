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
package org.dbflute.logic.manage.freegen;

import java.util.LinkedHashMap;
import java.util.Map;

import org.dbflute.logic.manage.freegen.table.appcls.DfAppClsTableLoader;
import org.dbflute.logic.manage.freegen.table.appcls.DfWebClsTableLoader;
import org.dbflute.logic.manage.freegen.table.elasticsearch.DfElasticsearchTableLoader;
import org.dbflute.logic.manage.freegen.table.filepath.DfFilePathTableLoader;
import org.dbflute.logic.manage.freegen.table.json.DfJsonGeneralTableLoader;
import org.dbflute.logic.manage.freegen.table.json.DfJsonKeyTableLoader;
import org.dbflute.logic.manage.freegen.table.json.DfJsonSchemaTableLoader;
import org.dbflute.logic.manage.freegen.table.lastaflute.DfLastaDocTableLoader;
import org.dbflute.logic.manage.freegen.table.mailflute.DfMailFluteTableLoader;
import org.dbflute.logic.manage.freegen.table.pmfile.DfPmFileTableLoader;
import org.dbflute.logic.manage.freegen.table.prop.DfPropTableLoader;
import org.dbflute.logic.manage.freegen.table.solr.DfSolrXmlTableLoader;
import org.dbflute.logic.manage.freegen.table.swagger.DfSwaggerTableLoader;
import org.dbflute.logic.manage.freegen.table.xls.DfXlsTableLoader;

/**
 * @author jflute
 */
public enum DfFreeGenResourceType {

    PROP, XLS, FILE_PATH, JSON_GENERAL, JSON_KEY, JSON_SCHEMA // general
    , SOLR, ELASTICSEARCH, SWAGGER // product
    , MAIL_FLUTE, PM_FILE, LASTA_DOC, APP_CLS, WEB_CLS // lastaflute
    ;

    public static final Map<DfFreeGenResourceType, DfFreeGenTableLoader> tableLoaderMap;
    static {
        tableLoaderMap = new LinkedHashMap<DfFreeGenResourceType, DfFreeGenTableLoader>();
        tableLoaderMap.put(DfFreeGenResourceType.PROP, new DfPropTableLoader());
        tableLoaderMap.put(DfFreeGenResourceType.XLS, new DfXlsTableLoader());
        tableLoaderMap.put(DfFreeGenResourceType.FILE_PATH, new DfFilePathTableLoader());
        tableLoaderMap.put(DfFreeGenResourceType.JSON_GENERAL, new DfJsonGeneralTableLoader());
        tableLoaderMap.put(DfFreeGenResourceType.JSON_KEY, new DfJsonKeyTableLoader());
        tableLoaderMap.put(DfFreeGenResourceType.JSON_SCHEMA, new DfJsonSchemaTableLoader());
        tableLoaderMap.put(DfFreeGenResourceType.SOLR, new DfSolrXmlTableLoader());
        tableLoaderMap.put(DfFreeGenResourceType.ELASTICSEARCH, new DfElasticsearchTableLoader());
        tableLoaderMap.put(DfFreeGenResourceType.SWAGGER, new DfSwaggerTableLoader());
        tableLoaderMap.put(DfFreeGenResourceType.MAIL_FLUTE, new DfMailFluteTableLoader(false));
        tableLoaderMap.put(DfFreeGenResourceType.PM_FILE, new DfPmFileTableLoader(false));
        tableLoaderMap.put(DfFreeGenResourceType.LASTA_DOC, new DfLastaDocTableLoader());
        tableLoaderMap.put(DfFreeGenResourceType.APP_CLS, new DfAppClsTableLoader());
        tableLoaderMap.put(DfFreeGenResourceType.WEB_CLS, new DfWebClsTableLoader());
    }
}
