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
package org.seasar.dbflute.cbean.sqlclause.subquery;

import org.seasar.dbflute.cbean.cipher.GearedCipherManager;
import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;
import org.seasar.dbflute.dbmeta.name.ColumnRealNameProvider;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.dbmeta.name.ColumnSqlNameProvider;

/**
 * @author jflute
 * @since 0.9.7.2 (2010/06/20 Sunday)
 */
public class InScopeRelation extends AbstractSubQuery {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _suppressLocalAliasName;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public InScopeRelation(SubQueryPath subQueryPath, ColumnRealNameProvider localRealNameProvider,
            ColumnSqlNameProvider subQuerySqlNameProvider, int subQueryLevel, SqlClause subQuerySqlClause,
            String subQueryIdentity, DBMeta subQueryDBMeta, GearedCipherManager cipherManager,
            boolean suppressLocalAliasName) {
        super(subQueryPath, localRealNameProvider, subQuerySqlNameProvider, subQueryLevel, subQuerySqlClause,
                subQueryIdentity, subQueryDBMeta, cipherManager);
        _suppressLocalAliasName = suppressLocalAliasName;
    }

    // ===================================================================================
    //                                                                        Build Clause
    //                                                                        ============
    public String buildInScopeRelation(String columnDbName, String relatedColumnDbName,
            String correlatedFixedCondition, String inScopeOption) {
        inScopeOption = inScopeOption != null ? inScopeOption + " " : "";
        final String subQueryClause;
        {
            final ColumnSqlName relatedColumnSqlName = _subQuerySqlNameProvider.provide(relatedColumnDbName);
            subQueryClause = getSubQueryClause(relatedColumnSqlName, correlatedFixedCondition);
        }
        final String beginMark = resolveSubQueryBeginMark(_subQueryIdentity) + ln();
        final String endMark = resolveSubQueryEndMark(_subQueryIdentity);
        final String endIndent = "       ";
        final ColumnRealName columnRealName;
        {
            final ColumnRealName localRealName = _localRealNameProvider.provide(columnDbName);
            if (_suppressLocalAliasName) {
                columnRealName = ColumnRealName.create(null, localRealName.getColumnSqlName());
            } else {
                columnRealName = localRealName;
            }
        }
        return columnRealName + " " + inScopeOption + "in (" + beginMark + subQueryClause + ln() + endIndent + ")"
                + endMark;
    }

    protected String getSubQueryClause(ColumnSqlName relatedColumnSqlName, String correlatedFixedCondition) {
        final String tableAliasName = getSubQueryLocalAliasName();
        final String selectClause;
        {
            final ColumnRealName relatedColumnRealName = ColumnRealName.create(tableAliasName, relatedColumnSqlName);
            selectClause = "select " + relatedColumnRealName;
        }
        final String fromWhereClause = buildPlainFromWhereClause(selectClause, tableAliasName, correlatedFixedCondition);
        final String subQueryClause = selectClause + " " + fromWhereClause;
        return resolveSubQueryLevelVariable(subQueryClause);
    }
}
