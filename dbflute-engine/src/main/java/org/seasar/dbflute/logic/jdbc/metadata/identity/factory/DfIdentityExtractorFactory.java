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
package org.seasar.dbflute.logic.jdbc.metadata.identity.factory;

import javax.sql.DataSource;

import org.seasar.dbflute.logic.jdbc.metadata.identity.DfIdentityExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.identity.DfIdentityExtractorDB2;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;

/**
 * @author jflute
 * @since 0.8.1 (2008/10/10 Friday)
 */
public class DfIdentityExtractorFactory {

    protected final DataSource _dataSource;
    protected final DfDatabaseTypeFacadeProp _databaseTypeFacadeProp;

    /**
     * @param dataSource The data source. (NotNull)
     * @param databaseTypeFacadeProp The facade properties for database type. (NotNull)
     */
    public DfIdentityExtractorFactory(DataSource dataSource, DfDatabaseTypeFacadeProp databaseTypeFacadeProp) {
        _dataSource = dataSource;
        _databaseTypeFacadeProp = databaseTypeFacadeProp;
    }

    /**
     * @return The extractor of DB comments. (NullAllowed)
     */
    public DfIdentityExtractor createIdentityExtractor() {
        if (_databaseTypeFacadeProp.isDatabaseDB2()) {
            final DfIdentityExtractorDB2 extractor = new DfIdentityExtractorDB2();
            extractor.setDataSource(_dataSource);
            return extractor;
        }
        return null;
    }
}
