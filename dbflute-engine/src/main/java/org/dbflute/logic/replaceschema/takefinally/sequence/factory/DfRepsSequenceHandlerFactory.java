/*
 * Copyright 2014-2020 the original author or authors.
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
package org.dbflute.logic.replaceschema.takefinally.sequence.factory;

import java.util.List;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.logic.replaceschema.takefinally.sequence.DfRepsSequenceHandler;
import org.dbflute.logic.replaceschema.takefinally.sequence.DfRepsSequenceHandlerDB2;
import org.dbflute.logic.replaceschema.takefinally.sequence.DfRepsSequenceHandlerH2;
import org.dbflute.logic.replaceschema.takefinally.sequence.DfRepsSequenceHandlerOracle;
import org.dbflute.logic.replaceschema.takefinally.sequence.DfRepsSequenceHandlerPostgreSQL;
import org.dbflute.properties.DfDatabaseProperties;
import org.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 */
public class DfRepsSequenceHandlerFactory {

    protected DfSchemaSource _dataSource;
    protected DfDatabaseTypeFacadeProp _databaseTypeFacadeProp;
    protected DfDatabaseProperties _databaseProperties;

    public DfRepsSequenceHandlerFactory(DfSchemaSource dataSource, DfDatabaseTypeFacadeProp databaseTypeFacadeProp,
            DfDatabaseProperties databaseProperties) {
        _dataSource = dataSource;
        _databaseTypeFacadeProp = databaseTypeFacadeProp;
        _databaseProperties = databaseProperties;
    }

    public DfRepsSequenceHandler createSequenceHandler() {
        final List<UnifiedSchema> targetSchemaList = createTargetSchemaList();
        if (_databaseTypeFacadeProp.isDatabasePostgreSQL()) {
            return new DfRepsSequenceHandlerPostgreSQL(_dataSource, targetSchemaList);
        } else if (_databaseTypeFacadeProp.isDatabaseOracle()) {
            return new DfRepsSequenceHandlerOracle(_dataSource, targetSchemaList);
        } else if (_databaseTypeFacadeProp.isDatabaseDB2()) {
            return new DfRepsSequenceHandlerDB2(_dataSource, targetSchemaList);
        } else if (_databaseTypeFacadeProp.isDatabaseH2()) {
            return new DfRepsSequenceHandlerH2(_dataSource, targetSchemaList);
        }
        return null;
    }

    protected List<UnifiedSchema> createTargetSchemaList() { // not only main schema but also additional schemas
        final List<UnifiedSchema> schemaList = DfCollectionUtil.newArrayList(_dataSource.getSchema());
        schemaList.addAll(_databaseProperties.getAdditionalSchemaList());
        return schemaList;
    }
}
