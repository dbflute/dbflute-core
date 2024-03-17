/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.cbean.sqlclause.subquery;

import java.util.List;

import org.dbflute.cbean.cipher.GearedCipherManager;
import org.dbflute.cbean.sqlclause.SqlClause;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.name.ColumnRealName;
import org.dbflute.dbmeta.name.ColumnRealNameProvider;
import org.dbflute.dbmeta.name.ColumnSqlName;
import org.dbflute.dbmeta.name.ColumnSqlNameProvider;
import org.dbflute.util.Srl;

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
            ColumnSqlNameProvider subQuerySqlNameProvider, int subQueryLevel, SqlClause subQuerySqlClause, String subQueryIdentity,
            DBMeta subQueryDBMeta, GearedCipherManager cipherManager, boolean suppressLocalAliasName) {
        super(subQueryPath, localRealNameProvider, subQuerySqlNameProvider, subQueryLevel, subQuerySqlClause, subQueryIdentity,
                subQueryDBMeta, cipherManager);
        _suppressLocalAliasName = suppressLocalAliasName;
    }

    // ===================================================================================
    //                                                                        Build Clause
    //                                                                        ============
    public String buildInScopeRelation(String localColumnDbName, String relatedColumnDbName, String correlatedFixedCondition,
            String inScopeOption) {
        inScopeOption = inScopeOption != null ? inScopeOption + " " : ""; // e.g. not
        final String localQueryColumnExp = prepareLocalQueryColumnExp(localColumnDbName, relatedColumnDbName);
        final String subQueryClause = prepareSubQueryClause(localColumnDbName, relatedColumnDbName, correlatedFixedCondition);
        final String beginMark = resolveSubQueryBeginMark(_subQueryIdentity) + ln();
        final String endMark = resolveSubQueryEndMark(_subQueryIdentity);
        final String endIndent = "       ";
        return localQueryColumnExp + " " + inScopeOption + "in (" + beginMark + subQueryClause + ln() + endIndent + ")" + endMark;
    }

    // -----------------------------------------------------
    //                                     LocalQuery Column
    //                                     -----------------
    protected String prepareLocalQueryColumnExp(String localColumnDbName, String relatedColumnDbName) {
        final String localQueryColumnExp;
        if (isSinglePrimaryKey(localColumnDbName, relatedColumnDbName)) {
            localQueryColumnExp = deriveLocalQueryColumnExp(localColumnDbName);
        } else { // compound primary keys
            final List<String> localColumnSplit = Srl.splitListTrimmed(localColumnDbName, ",");
            localQueryColumnExp = deriveLocalQueryColumnExp(localColumnSplit);
        }
        return localQueryColumnExp;
    }

    protected String deriveLocalQueryColumnExp(String localColumnDbName) {
        final ColumnRealName columnRealName = deriveLocalColumnRealName(localColumnDbName);
        return columnRealName.toString(); // e.g. dfloc.MEMBER_ID
    }

    protected String deriveLocalQueryColumnExp(List<String> localColumnDbNameList) {
        final ColumnRealName[] localColumnRealNames = new ColumnRealName[localColumnDbNameList.size()];
        for (int i = 0; i < localColumnDbNameList.size(); i++) {
            final String localColumnDbName = localColumnDbNameList.get(i);
            localColumnRealNames[i] = deriveLocalColumnRealName(localColumnDbName);
        }
        return generateCompoundColumnInExp(localColumnRealNames, /*useParentheses*/true);
    }

    protected ColumnRealName deriveLocalColumnRealName(String localColumnDbName) {
        final ColumnRealName derivedRealName;
        {
            final ColumnRealName localRealName = _localRealNameProvider.provide(localColumnDbName);
            if (_suppressLocalAliasName) {
                derivedRealName = ColumnRealName.create(null, localRealName.getColumnSqlName()); // e.g. MEMBER_ID
            } else {
                derivedRealName = localRealName; // e.g. dfloc.MEMBER_ID
            }
        }
        return derivedRealName;
    }

    // -----------------------------------------------------
    //                                       SubQuery Clause
    //                                       ---------------
    protected String prepareSubQueryClause(String localColumnDbName, String relatedColumnDbName, String correlatedFixedCondition) {
        final String subQueryClause;
        if (isSinglePrimaryKey(localColumnDbName, relatedColumnDbName)) {
            final ColumnSqlName relatedColumnSqlName = _subQuerySqlNameProvider.provide(relatedColumnDbName);
            subQueryClause = deriveSubQueryClause(relatedColumnSqlName, correlatedFixedCondition);
        } else { // compound primary key (since 1.2.8)
            // InScopeSubQuery of Compound FK can be supported because it's SQL standard (2024/03/17)
            // https://github.com/dbflute/dbflute-core/issues/204
            final List<String> relatedColumnSplit = Srl.splitListTrimmed(relatedColumnDbName, ",");
            final ColumnSqlName[] relatedColumnSqlNames = new ColumnSqlName[relatedColumnSplit.size()];
            for (int i = 0; i < relatedColumnSplit.size(); i++) {
                relatedColumnSqlNames[i] = _subQuerySqlNameProvider.provide(relatedColumnSplit.get(i));
            }
            subQueryClause = deriveSubQueryClause(relatedColumnSqlNames, correlatedFixedCondition);
        }
        return subQueryClause;
    }

    protected String deriveSubQueryClause(ColumnSqlName relatedColumnSqlName, String correlatedFixedCondition) {
        final String tableAliasName = getSubQueryLocalAliasName();
        final String selectClause = "select " + ColumnRealName.create(tableAliasName, relatedColumnSqlName);
        final String fromWhereClause = buildPlainFromWhereClause(selectClause, tableAliasName, correlatedFixedCondition);
        final String subQueryClause = selectClause + " " + fromWhereClause;
        return resolveSubQueryLevelVariable(subQueryClause);
    }

    protected String deriveSubQueryClause(ColumnSqlName[] relatedColumnSqlNames, String correlatedFixedCondition) {
        final String tableAliasName = getSubQueryLocalAliasName();
        final String selectClause; // e.g. select sub1loc.REF_FIRST_ID, sub1loc.REF_SECOND_ID
        {
            final ColumnRealName[] relatedColumnRealNames = new ColumnRealName[relatedColumnSqlNames.length];
            for (int i = 0; i < relatedColumnSqlNames.length; i++) {
                relatedColumnRealNames[i] = ColumnRealName.create(tableAliasName, relatedColumnSqlNames[i]);
            }
            selectClause = "select " + generateCompoundColumnInExp(relatedColumnRealNames, /*useParentheses*/false);
        }
        final String fromWhereClause = buildPlainFromWhereClause(selectClause, tableAliasName, correlatedFixedCondition);
        final String subQueryClause = selectClause + " " + fromWhereClause;
        return resolveSubQueryLevelVariable(subQueryClause);
    }

    // -----------------------------------------------------
    //                            Compound Column Expression
    //                            --------------------------
    protected String generateCompoundColumnInExp(ColumnRealName[] columnRealNames, boolean useParentheses) {
        final StringBuilder sb = new StringBuilder();
        if (useParentheses) {
            sb.append("(");
        }
        int index = 0;
        for (ColumnRealName columnRealName : columnRealNames) {
            if (index >= 1) {
                sb.append(", ");
            }
            sb.append(columnRealName);
            ++index;
        }
        if (useParentheses) {
            sb.append(")");
        }
        return sb.toString(); // e.g. (dfloc.PK_FIRST_ID, dfloc.PK_SECOND_ID)
    }
}
