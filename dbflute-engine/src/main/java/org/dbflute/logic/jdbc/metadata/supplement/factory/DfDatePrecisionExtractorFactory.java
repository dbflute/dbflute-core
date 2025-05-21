/*
 * Copyright 2014-2025 the original author or authors.
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

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.logic.jdbc.metadata.supplement.DfDatetimePrecisionExtractor;
import org.dbflute.logic.jdbc.metadata.supplement.DfDatetimePrecisionExtractorMySQL;
import org.dbflute.properties.facade.DfDatabaseTypeFacadeProp;

/**
 * @author jflute
 * @since 1.1.4 (2017/08/12 Saturday at ikspiari)
 */
public class DfDatePrecisionExtractorFactory {

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
     * @param dataSource The data source to extract. (NotNull)
     * @param unifiedSchema The unified schema to extract. (NullAllowed)
     * @param databaseTypeFacadeProp The facade properties for database type. (NotNull)
     */
    public DfDatePrecisionExtractorFactory(DataSource dataSource, UnifiedSchema unifiedSchema,
            DfDatabaseTypeFacadeProp databaseTypeFacadeProp) {
        _dataSource = dataSource;
        _unifiedSchema = unifiedSchema;
        _databaseTypeFacadeProp = databaseTypeFacadeProp;
    }

    // ===================================================================================
    //                                                                              Create
    //                                                                              ======
    /**
     * @return The extractor of date-time precision. (NullAllowed)
     */
    public DfDatetimePrecisionExtractor createDatetimePrecisionExtractor() {
        if (_databaseTypeFacadeProp.isDatabaseMySQL()) {
            final DfDatetimePrecisionExtractorMySQL extractor = new DfDatetimePrecisionExtractorMySQL();
            extractor.setDataSource(_dataSource);
            extractor.setUnifiedSchema(_unifiedSchema);
            return extractor;
        }
        return null;
    }
}
