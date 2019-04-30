/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.logic.jdbc.metadata.supplement.factory;

import java.sql.Connection;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.logic.jdbc.metadata.supplement.DfUniqueKeyFkExtractor;
import org.dbflute.logic.jdbc.metadata.supplement.DfUniqueKeyFkExtractorOracle;
import org.dbflute.properties.facade.DfDatabaseTypeFacadeProp;

/**
 * @author jflute
 * @since 1.0.5A (2013/11/04 Monday)
 */
public class DfUniqueKeyFkExtractorFactory {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Connection _conn;
    protected final UnifiedSchema _unifiedSchema;
    protected final DfDatabaseTypeFacadeProp _databaseTypeFacadeProp;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param conn The connection to extract. (NotNull)
     * @param unifiedSchema The unified schema to extract. (NullAllowed)
     * @param databaseTypeFacadeProp The facade properties for database type. (NotNull)
     */
    public DfUniqueKeyFkExtractorFactory(Connection conn, UnifiedSchema unifiedSchema, DfDatabaseTypeFacadeProp databaseTypeFacadeProp) {
        _conn = conn;
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
            extractor.setConnection(_conn);
            extractor.setUnifiedSchema(_unifiedSchema);
            return extractor;
        }
        return null;
    }
}
