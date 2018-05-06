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
package org.dbflute.logic.doc.spolicy.reps;

import java.io.File;

import org.apache.torque.engine.database.model.AppData;
import org.apache.torque.engine.database.model.Database;
import org.dbflute.DfBuildProperties;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.logic.doc.spolicy.DfSPolicyChecker;
import org.dbflute.logic.jdbc.schemaxml.DfSchemaXmlReader;
import org.dbflute.logic.jdbc.schemaxml.DfSchemaXmlSerializer;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfClassificationProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.properties.DfReplaceSchemaProperties;
import org.dbflute.properties.DfSchemaPolicyProperties;
import org.dbflute.task.bs.assistant.DfDocumentSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.2 (2017/1/4 Wednesday)
 */
public class DfSPolicyInRepsChecker {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfSPolicyInRepsChecker.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaSource _dataSource;
    protected final DfDocumentSelector _documentSelector;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyInRepsChecker(DfSchemaSource dataSource, DfDocumentSelector documentSelector) {
        _dataSource = dataSource;
        _documentSelector = documentSelector;
    }

    // ===================================================================================
    //                                                                               Check
    //                                                                               =====
    public void checkSchemaPolicyInRepsIfNeeds() {
        final DfReplaceSchemaProperties repsProp = getReplaceSchemaProperties();
        if (!repsProp.isCheckSchemaPolicyInReps()) {
            return;
        }
        final DfSchemaPolicyProperties policyProp = getSchemaPolicyProperties();
        if (!policyProp.hasPolicy()) {
            return;
        }
        _log.info("...Beginning schema policy check in replace-schema");
        final String schemaXml = repsProp.getSchemaPolicyInRepsSchemaXml();
        deleteTemporarySchemaXmlIfExists(schemaXml);
        final DfSchemaXmlSerializer serializer = createSchemaXmlSerializer(schemaXml);
        serializer.serialize();
        try {
            final DfSchemaXmlReader reader = createSchemaXmlReader(schemaXml);
            final AppData appData = reader.read();
            final Database database = appData.getDatabase();
            initializeClassificationDeployment(database); // for "then classification"
            final DfSPolicyChecker checker = createChecker(policyProp, database);
            checker.checkPolicyIfNeeds();
        } finally {
            deleteTemporarySchemaXmlIfExists(schemaXml);
        }
    }

    protected DfSchemaXmlSerializer createSchemaXmlSerializer(final String schemaXml) {
        return DfSchemaXmlSerializer.createAsManage(_dataSource, schemaXml, /*historyFile*/null); // history unneeded
    }

    protected DfSchemaXmlReader createSchemaXmlReader(String schemaXml) {
        final String databaseType = getBasicProperties().getDatabaseTypeFacadeProp().getTargetDatabase();
        return DfSchemaXmlReader.createAsPlain(schemaXml, databaseType, /*readingFilter*/null); // filter unneeded
    }

    protected void initializeClassificationDeployment(final Database database) {
        final DfClassificationProperties clsProp = getClassificationProperties();
        clsProp.initializeClassificationDeployment(database);
    }

    protected DfSPolicyChecker createChecker(DfSchemaPolicyProperties policyProp, Database database) {
        return policyProp.createChecker(database, () -> {
            return _documentSelector.lazyLoadIfNeedsCoreSchemaDiffList(); // for table/column first date
        });
    }

    protected void deleteTemporarySchemaXmlIfExists(String schemaXml) {
        final File schemaXmlFile = new File(schemaXml);
        if (schemaXmlFile.exists()) {
            schemaXmlFile.delete(); // just in case
        }
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfClassificationProperties getClassificationProperties() {
        return getProperties().getClassificationProperties();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    protected DfSchemaPolicyProperties getSchemaPolicyProperties() {
        return getProperties().getSchemaPolicyProperties();
    }

    protected DfReplaceSchemaProperties getReplaceSchemaProperties() {
        return getProperties().getReplaceSchemaProperties();
    }
}
