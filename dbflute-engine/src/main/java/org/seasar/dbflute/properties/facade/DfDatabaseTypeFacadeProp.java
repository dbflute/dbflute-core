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
package org.seasar.dbflute.properties.facade;

import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.properties.DfBasicProperties;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/29 Friday)
 */
public class DfDatabaseTypeFacadeProp {

    protected final DfBasicProperties _basicProp;

    public DfDatabaseTypeFacadeProp(DfBasicProperties basicProp) {
        _basicProp = basicProp;
    }

    public String getTargetDatabase() {
        return _basicProp.getTargetDatabase();
    }

    public DBDef getCurrentDBDef() {
        return _basicProp.getCurrentDBDef();
    }

    public boolean isDatabaseMySQL() {
        return _basicProp.isDatabaseMySQL();
    }

    public boolean isDatabasePostgreSQL() {
        return _basicProp.isDatabasePostgreSQL();
    }

    public boolean isDatabaseOracle() {
        return _basicProp.isDatabaseOracle();
    }

    public boolean isDatabaseDB2() {
        return _basicProp.isDatabaseDB2();
    }

    public boolean isDatabaseSQLServer() {
        return _basicProp.isDatabaseSQLServer();
    }

    public boolean isDatabaseH2() {
        return _basicProp.isDatabaseH2();
    }

    public boolean isDatabaseDerby() {
        return _basicProp.isDatabaseDerby();
    }

    public boolean isDatabaseSQLite() { // sub supported
        return _basicProp.isDatabaseSQLite();
    }

    public boolean isDatabaseMSAccess() { // sub supported
        return _basicProp.isDatabaseMSAccess();
    }

    public boolean isDatabaseFirebird() { // a-little-bit supported
        return _basicProp.isDatabaseFirebird();
    }

    public boolean isDatabaseSybase() { // a-little-bit supported
        return _basicProp.isDatabaseSybase();
    }

    public boolean isDatabase_Supported() {
        return _basicProp.isDatabase_Supported();
    }

    public boolean isDatabaseAsMainSupported() {
        return _basicProp.isDatabaseAsMainSupported();
    }

    public boolean isDatabaseAsSubSupported() {
        return _basicProp.isDatabaseAsSubSupported();
    }

    // /- - - - - - - - - - - - - - - - - - - -
    // simple DBMS definition about generating
    // - - - - - - - - - -/
    public boolean isDatabaseAsSchemaSpecificationOmittable() {
        return _basicProp.isDatabaseAsSchemaSpecificationOmittable();
    }

    public boolean isDatabaseAsUnifiedSchemaUnsupported() {
        return _basicProp.isDatabaseAsUnifiedSchemaUnsupported();
    }

    public boolean isDatabaseAsPrimaryKeyExtractingUnsupported() {
        return _basicProp.isDatabaseAsPrimaryKeyExtractingUnsupported();
    }

    public boolean isDatabaseAsForeignKeyExtractingUnsupported() {
        return _basicProp.isDatabaseAsForeignKeyExtractingUnsupported();
    }
}
