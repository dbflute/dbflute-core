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
package org.seasar.dbflute.logic.jdbc.metadata.sequence.factory;

import java.util.List;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.helper.jdbc.context.DfSchemaSource;
import org.seasar.dbflute.logic.jdbc.metadata.sequence.DfSequenceExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.sequence.DfSequenceExtractorDB2;
import org.seasar.dbflute.logic.jdbc.metadata.sequence.DfSequenceExtractorH2;
import org.seasar.dbflute.logic.jdbc.metadata.sequence.DfSequenceExtractorOracle;
import org.seasar.dbflute.logic.jdbc.metadata.sequence.DfSequenceExtractorPostgreSQL;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 */
public class DfSequenceExtractorFactory {

    protected final DfSchemaSource _dataSource;
    protected final DfDatabaseTypeFacadeProp _databaseTypeFacadeProp;
    protected final DfDatabaseProperties _databaseProperties;
    protected boolean _suppressAdditionalSchema;

    public DfSequenceExtractorFactory(DfSchemaSource dataSource, DfDatabaseTypeFacadeProp databaseTypeFacadeProp,
            DfDatabaseProperties databaseProperties) {
        _dataSource = dataSource;
        _databaseTypeFacadeProp = databaseTypeFacadeProp;
        _databaseProperties = databaseProperties;
    }

    public DfSequenceExtractor createSequenceExtractor() {
        final List<UnifiedSchema> targetSchemaList = createTargetSchemaList();
        if (_databaseTypeFacadeProp.isDatabasePostgreSQL()) {
            return new DfSequenceExtractorPostgreSQL(_dataSource.getDataSource(), targetSchemaList);
        } else if (_databaseTypeFacadeProp.isDatabaseOracle()) {
            return new DfSequenceExtractorOracle(_dataSource.getDataSource(), targetSchemaList);
        } else if (_databaseTypeFacadeProp.isDatabaseDB2()) {
            return new DfSequenceExtractorDB2(_dataSource.getDataSource(), targetSchemaList);
        } else if (_databaseTypeFacadeProp.isDatabaseH2()) {
            return new DfSequenceExtractorH2(_dataSource.getDataSource(), targetSchemaList);
        }
        return null;
    }

    protected List<UnifiedSchema> createTargetSchemaList() { // not only main schema but also additional schemas
        final List<UnifiedSchema> schemaList = DfCollectionUtil.newArrayList(_dataSource.getSchema());
        if (!_suppressAdditionalSchema) {
            schemaList.addAll(_databaseProperties.getAdditionalSchemaList());
        }
        return schemaList;
    }

    public void suppressAdditionalSchema() {
        _suppressAdditionalSchema = true;
    }
}
