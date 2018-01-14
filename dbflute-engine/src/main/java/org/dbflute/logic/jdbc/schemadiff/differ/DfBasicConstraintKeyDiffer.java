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
package org.dbflute.logic.jdbc.schemadiff.differ;

import java.util.Collection;

import org.dbflute.DfBuildProperties;
import org.dbflute.logic.jdbc.schemadiff.DfConstraintDiff;
import org.dbflute.logic.jdbc.schemadiff.DfDiffAssist;
import org.dbflute.logic.jdbc.schemadiff.DfNextPreviousDiff;
import org.dbflute.logic.jdbc.schemadiff.DfTableDiff;
import org.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @param <KEY> The type of constraint key.
 * @param <DIFF> The type of constraint diff.
 */
public abstract class DfBasicConstraintKeyDiffer<KEY, DIFF extends DfConstraintDiff> implements DfConstraintKeyDiffer<KEY, DIFF> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DfTableDiff _tableDiff;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfBasicConstraintKeyDiffer(DfTableDiff tableDiff) {
        _tableDiff = tableDiff;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean isSameConstraintName(String next, String previous) {
        final boolean sameName = isSame(next, previous);
        if (sameName && isAutoGeneratedName(next)) {
            return false;
        }
        return sameName;
    }

    // it may need to extend this logic at the future
    public boolean isAutoGeneratedName(String name) {
        if (isDatabaseOracle()) { // e.g. SYS_1232...
            if (name != null && Srl.startsWithIgnoreCase(name, "sys_")) {
                return true;
            }
        } else if (isDatabaseDB2()) { // e.g. SQL11221...
            if (name != null && Srl.startsWithIgnoreCase(name, "sql")) {
                return true;
            }
        } else if (isDatabaseDerby()) { // e.g. SQL11221...
            if (name != null && Srl.startsWithIgnoreCase(name, "sql")) {
                return true;
            }
        }
        return false;
    }

    public boolean isSameStructure(KEY next, KEY previous) {
        return isSame(column(next), column(previous));
    }

    // ===================================================================================
    //                                                                         Same Helper
    //                                                                         ===========
    protected boolean isSame(Object next, Object previous) {
        return DfDiffAssist.isSame(next, previous);
    }

    // ===================================================================================
    //                                                                  Next Previous Diff
    //                                                                  ==================
    protected DfNextPreviousDiff createNextPreviousDiff(String next, String previous) {
        return DfNextPreviousDiff.create(next, previous);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected String buildCommaString(Collection<String> values) {
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String value : values) {
            if (index > 0) {
                sb.append(", ");
            }
            sb.append(value);
            ++index;
        }
        return sb.toString();
    }

    protected String extractConstraintName(KEY nextKey, KEY previousKey) {
        // either should be not null
        return nextKey != null ? constraintName(nextKey) : constraintName(previousKey);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected static DfDatabaseTypeFacadeProp getDatabaseTypeFacadeProp() {
        return DfBuildProperties.getInstance().getBasicProperties().getDatabaseTypeFacadeProp();
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
}
