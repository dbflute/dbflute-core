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
package org.seasar.dbflute.logic.jdbc.metadata;

import java.util.LinkedHashMap;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public abstract class DfAbstractMetaDataExtractor {

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected String filterSchemaName(String schemaName) {
        // a driver may throw the exception if the value is empty string
        // (For example, MS Access)
        if (Srl.isTrimmedEmpty(schemaName)) {
            return null;
        }
        return schemaName;
    }

    protected UnifiedSchema createAsDynamicSchema(String catalog, String schema) {
        return UnifiedSchema.createAsDynamicSchema(catalog, schema);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    // -----------------------------------------------------
    //                                      Basic Properties
    //                                      ----------------
    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfDatabaseTypeFacadeProp getDatabaseTypeFacadeProp() {
        return getBasicProperties().getDatabaseTypeFacadeProp();
    }

    protected boolean isDatabaseMySQL() {
        return getDatabaseTypeFacadeProp().isDatabaseMySQL();
    }

    protected boolean isDatabasePostgreSQL() {
        return getDatabaseTypeFacadeProp().isDatabasePostgreSQL();
    }

    protected boolean isDatabaseOracle() {
        return getDatabaseTypeFacadeProp().isDatabaseOracle();
    }

    protected boolean isDatabaseDB2() {
        return getDatabaseTypeFacadeProp().isDatabaseDB2();
    }

    protected boolean isDatabaseSQLServer() {
        return getDatabaseTypeFacadeProp().isDatabaseSQLServer();
    }

    protected boolean isDatabaseH2() {
        return getDatabaseTypeFacadeProp().isDatabaseH2();
    }

    protected boolean isDatabaseDerby() {
        return getDatabaseTypeFacadeProp().isDatabaseDerby();
    }

    protected boolean isDatabaseSQLite() {
        return getDatabaseTypeFacadeProp().isDatabaseSQLite();
    }

    protected boolean isDatabaseMsAccess() {
        return getDatabaseTypeFacadeProp().isDatabaseMSAccess();
    }

    protected boolean isDatabaseFirebird() {
        return getDatabaseTypeFacadeProp().isDatabaseFirebird();
    }

    protected boolean isDatabaseSybase() {
        return getDatabaseTypeFacadeProp().isDatabaseSybase();
    }

    protected boolean isPrimaryKeyExtractingUnsupported() {
        return getDatabaseTypeFacadeProp().isDatabaseAsPrimaryKeyExtractingUnsupported();
    }

    protected boolean isForeignKeyExtractingUnsupported() {
        return getDatabaseTypeFacadeProp().isDatabaseAsForeignKeyExtractingUnsupported();
    }

    protected boolean checkMetaTableDiffIfNeeds(String tableName, String metaTableName) {
        if (!needsToCheckMetaTable()) {
            return false;
        }
        return !Srl.equalsFlexibleTrimmed(tableName, metaTableName);
    }

    protected boolean needsToCheckMetaTable() {
        // Firebird treats the argument "tableName" as PrefixSearch
        // (otherwise, Oracle does not need to check because of synonym handling)
        return isDatabaseFirebird();
    }

    // -----------------------------------------------------
    //                                   Database Properties
    //                                   -------------------
    protected DfDatabaseProperties getDatabaseProperties() {
        return getProperties().getDatabaseProperties();
    }

    protected boolean isRetryCaseInsensitiveColumn() {
        return getDatabaseProperties().isRetryCaseInsensitiveColumn();
    }

    protected boolean isRetryCaseInsensitivePrimaryKey() {
        return getDatabaseProperties().isRetryCaseInsensitivePrimaryKey();
    }

    protected boolean isRetryCaseInsensitiveForeignKey() {
        return getDatabaseProperties().isRetryCaseInsensitiveForeignKey();
    }

    protected boolean isRetryCaseInsensitiveUniqueKey() {
        return getDatabaseProperties().isRetryCaseInsensitiveUniqueKey();
    }

    protected boolean isRetryCaseInsensitiveIndex() {
        return getDatabaseProperties().isRetryCaseInsensitiveIndex();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return "\n";
    }

    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap() {
        return new LinkedHashMap<KEY, VALUE>();
    }
}
