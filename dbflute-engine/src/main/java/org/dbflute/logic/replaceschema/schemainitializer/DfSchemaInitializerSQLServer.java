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
package org.dbflute.logic.replaceschema.schemainitializer;

import org.dbflute.logic.jdbc.metadata.info.DfProcedureMeta;
import org.dbflute.properties.facade.DfDatabaseTypeFacadeProp;

/**
 * The schema initializer for SqlServer.
 * @author jflute
 */
public class DfSchemaInitializerSQLServer extends DfSchemaInitializerJdbc {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfDatabaseTypeFacadeProp _databaseTypeFacadeProp;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSchemaInitializerSQLServer(DfDatabaseTypeFacadeProp databaseTypeFacadeProp) {
        _databaseTypeFacadeProp = databaseTypeFacadeProp;
    }

    // ===================================================================================
    //                                                                      Drop Procedure
    //                                                                      ==============
    @Override
    protected String buildProcedureSqlName(DfProcedureMeta metaInfo) {
        final String sqlName = removeSemicolonSuffixIfExists(super.buildProcedureSqlName(metaInfo));
        if (_databaseTypeFacadeProp.isSubTypeOnDatabaseSQLServerLocalDB()) {
            return filterLocalDBProcedureSqlName(metaInfo, sqlName);
        } else { // mainly here
            return sqlName;
        }
    }

    protected String removeSemicolonSuffixIfExists(String procedureSqlName) {
        final int semicolonIndex = procedureSqlName.indexOf(";");
        if (semicolonIndex >= 0) {
            procedureSqlName = procedureSqlName.substring(0, semicolonIndex);
        }
        return procedureSqlName;
    }

    // -----------------------------------------------------
    //                                               LocalDB
    //                                               -------
    protected String filterLocalDBProcedureSqlName(DfProcedureMeta metaInfo, String sqlName) {
        final String catalog = metaInfo.getProcedureSchema().getPureCatalog();
        if (catalog == null) {
            return sqlName;
        }
        // "drop procedure xxx.dbo.yyy" occurred error at SqlLocalDB.
        // but "drop procedure dbo.yyy" is no error.
        return sqlName.replace(catalog + ".", ""); // thanks, udagawa-san
    }
}