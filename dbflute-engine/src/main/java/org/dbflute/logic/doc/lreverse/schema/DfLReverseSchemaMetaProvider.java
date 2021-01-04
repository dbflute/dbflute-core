/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.logic.doc.lreverse.schema;

import org.apache.torque.engine.database.model.AppData;
import org.apache.torque.engine.database.model.Database;
import org.dbflute.DfBuildProperties;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.logic.jdbc.schemaxml.DfSchemaXmlReader;
import org.dbflute.logic.jdbc.schemaxml.DfSchemaXmlSerializer;
import org.dbflute.properties.DfDocumentProperties;

/**
 * @author jflute
 * @since 1.2.5 as split (2021/01/04 Monday at roppongi japanese)
 */
public class DfLReverseSchemaMetaProvider {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaSource _dataSource;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLReverseSchemaMetaProvider(DfSchemaSource dataSource) {
        _dataSource = dataSource;
    }

    // ===================================================================================
    //                                                                             Prepare
    //                                                                             =======
    public Database prepareDatabase() {
        final String schemaXml = getReverseSchemaXml();
        final DfSchemaXmlSerializer serializer = createSchemaXmlSerializer(schemaXml);
        serializer.serialize();
        final DfSchemaXmlReader reader = createSchemaXmlReader(schemaXml);
        final AppData appData = reader.read();
        return appData.getDatabase();
    }

    protected DfSchemaXmlSerializer createSchemaXmlSerializer(String schemaXml) {
        return DfSchemaXmlSerializer.createAsManage(_dataSource, schemaXml, /*historyFile*/null);
    }

    protected DfSchemaXmlReader createSchemaXmlReader(String schemaXml) {
        return DfSchemaXmlReader.createAsFlexibleToManage(schemaXml);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    protected String getReverseSchemaXml() {
        return getDocumentProperties().getLoadDataReverseSchemaXml();
    }
}
