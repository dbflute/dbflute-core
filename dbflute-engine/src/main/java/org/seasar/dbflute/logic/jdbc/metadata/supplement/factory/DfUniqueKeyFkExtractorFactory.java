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
package org.seasar.dbflute.logic.jdbc.metadata.supplement.factory;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.logic.jdbc.metadata.supplement.DfUniqueKeyFkExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.supplement.DfUniqueKeyFkExtractorOracle;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;

/**
 * @author jflute
 * @since 1.0.5A (2013/11/04 Monday)
 */
public class DfUniqueKeyFkExtractorFactory {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DataSource _dataSource;
    protected final UnifiedSchema _unifiedSchema;
    protected final DfDatabaseTypeFacadeProp _databaseTypeFacadeProp;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param dataSource The data source. (NotNull)
     * @param unifiedSchema The unified schema to extract. (NullAllowed)
     * @param databaseTypeFacadeProp The facade properties for database type. (NotNull)
     */
    public DfUniqueKeyFkExtractorFactory(DataSource dataSource, UnifiedSchema unifiedSchema,
            DfDatabaseTypeFacadeProp databaseTypeFacadeProp) {
        _dataSource = dataSource;
        _unifiedSchema = unifiedSchema;
        _databaseTypeFacadeProp = databaseTypeFacadeProp;
    }

    // ===================================================================================
    //                                                                              Create
    //                                                                              ======
    /**
     * @return The extractor of unique-key FK. (NullAllowed)
     */
    public DfUniqueKeyFkExtractor createUniqueKeyFkExtractor() {
        if (_databaseTypeFacadeProp.isDatabaseOracle()) {
            // Oracle JDBC driver does not return unique-key FK so it needs to select data dictionary
            final DfUniqueKeyFkExtractorOracle extractor = new DfUniqueKeyFkExtractorOracle();
            extractor.setDataSource(_dataSource);
            extractor.setUnifiedSchema(_unifiedSchema);
            return extractor;
        }
        return null;
    }
}
