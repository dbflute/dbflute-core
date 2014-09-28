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

import org.seasar.dbflute.cbean.cipher.ColumnFunctionCipher;
import org.seasar.dbflute.cbean.cipher.GearedCipherManager;
import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;
import org.seasar.dbflute.dbmeta.name.ColumnRealNameProvider;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.dbmeta.name.ColumnSqlNameProvider;
import org.seasar.dbflute.exception.thrower.ConditionBeanExceptionThrower;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.7.2 (2010/06/20 Sunday)
 */
public abstract class AbstractSubQuery {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SubQueryPath _subQueryPath;
    protected final ColumnRealNameProvider _localRealNameProvider;
    protected final ColumnSqlNameProvider _subQuerySqlNameProvider;
    protected final int _subQueryLevel;
    protected final SqlClause _subQuerySqlClause;
    protected final String _subQueryIdentity;
    protected final DBMeta _subQueryDBMeta;
    protected final GearedCipherManager _cipherManager;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param subQueryPath The property path of sub-query. (NotNull)
     * @param localRealNameProvider The provider of column real name for local table. (NotNull)
     * @param subQuerySqlNameProvider The provider of column real name for sub-query. (NotNull)
     * @param subQueryLevel The sub-query level for sub-query.
     * @param subQuerySqlClause The SQL clause for sub-query. (NotNull)
     * @param subQueryIdentity The identity string for sub-query. (NotNull)
     * @param subQueryDBMeta The DB meta for sub-query. (NotNull)
     * @param cipherManager The manager of geared cipher. (NullAllowed)
     */
    public AbstractSubQuery(SubQueryPath subQueryPath, ColumnRealNameProvider localRealNameProvider,
            ColumnSqlNameProvider subQuerySqlNameProvider, int subQueryLevel, SqlClause subQuerySqlClause,
            String subQueryIdentity, DBMeta subQueryDBMeta, GearedCipherManager cipherManager) {
        _subQueryPath = subQueryPath;
        _localRealNameProvider = localRealNameProvider;
        _subQuerySqlNameProvider = subQuerySqlNameProvider;
        _subQueryLevel = subQueryLevel;
        _subQuerySqlClause = subQuerySqlClause;
        _subQueryIdentity = subQueryIdentity;
        _subQueryDBMeta = subQueryDBMeta;
        _cipherManager = cipherManager;
    }

    // ===================================================================================
    //                                                                          Alias Name
    //                                                                          ==========
    protected String getSubQueryLocalAliasName() {
        return _subQuerySqlClause.getBasePointAliasName();
    }

    protected String buildSubQueryMainAliasName() {
        return "sub" + _subQueryLevel + "main";
    }

    protected String resolveSubQueryLevelVariable(String subQueryClause) {
        return replace(subQueryClause, "${subQueryLevel}", String.valueOf(_subQueryLevel));
    }

    // ===================================================================================
    //                                                                        Build Clause
    //                                                                        ============
    /**
     * Build the clause of plain sub-query from from-where clause.
     * @param selectClause The clause of select for sub-query. (NotNull)
     * @param localAliasName The alias name of sub-query local table. (NotNull)
     * @param correlatedFixedCondition The fixed condition as correlated condition. (NullAllowed)
     * @return The clause string of plain sub-query. (NotNull)
     */
    protected String buildPlainFromWhereClause(String selectClause, String localAliasName,
            String correlatedFixedCondition) {
        final SubQueryClause clause = createSubQueryClause(selectClause, localAliasName);
        return clause.buildPlainSubQueryFromWhereClause(correlatedFixedCondition);
    }

    /**
     * Build the clause of correlation sub-query from from-where clause.
     * @param selectClause The clause of select for sub-query. (NotNull)
     * @param localAliasName The alias name of sub-query local table. (NotNull)
     * @param correlatedColumnRealName The real name of correlated column that is main-query table's column. (NotNull)
     * @param relatedColumnSqlName The real name of related column that is sub-query table's column. (NotNull)
     * @param correlatedFixedCondition The fixed condition as correlated condition. (NullAllowed)
     * @return The clause string of correlation sub-query. (NotNull)
     */
    protected String buildCorrelationFromWhereClause(String selectClause, String localAliasName,
            ColumnRealName correlatedColumnRealName, ColumnSqlName relatedColumnSqlName, String correlatedFixedCondition) {
        final SubQueryClause clause = createSubQueryClause(selectClause, localAliasName);
        return clause.buildCorrelationSubQueryFromWhereClause(correlatedColumnRealName, relatedColumnSqlName,
                correlatedFixedCondition);
    }

    /**
     * Build the clause of correlation sub-query from from-where clause.
     * @param selectClause The clause of select for sub-query. (NotNull)
     * @param localAliasName The alias name of sub-query local table. (NotNull)
     * @param correlatedColumnRealNames The real names of correlated column that is main-query table's column. (NotNull)
     * @param relatedColumnSqlNames The real names of related column that is sub-query table's column. (NotNull)
     * @param correlatedFixedCondition The fixed condition as correlated condition. (NullAllowed)
     * @return The clause string of correlation sub-query. (NotNull)
     */
    protected String buildCorrelationFromWhereClause(String selectClause, String localAliasName,
            ColumnRealName[] correlatedColumnRealNames, ColumnSqlName[] relatedColumnSqlNames,
            String correlatedFixedCondition) {
        final SubQueryClause clause = createSubQueryClause(selectClause, localAliasName);
        return clause.buildCorrelationSubQueryFromWhereClause(correlatedColumnRealNames, relatedColumnSqlNames,
                correlatedFixedCondition);
    }

    protected SubQueryClause createSubQueryClause(String selectClause, String localAliasName) {
        return new SubQueryClause(_subQueryPath, selectClause, _subQuerySqlClause, localAliasName);
    }

    protected boolean isSinglePrimaryKey(String correlatedColumnDbName, String relatedColumnDbName) {
        return !isCompoundPrimaryKey(correlatedColumnDbName, relatedColumnDbName);
    }

    protected boolean isCompoundPrimaryKey(String correlatedColumnDbName, String relatedColumnDbName) {
        return correlatedColumnDbName.contains(",") && relatedColumnDbName.contains(",");
    }

    // ===================================================================================
    //                                                                     Indent Resolver
    //                                                                     ===============
    protected String resolveSubQueryBeginMark(String subQueryIdentity) {
        return _subQuerySqlClause.resolveSubQueryBeginMark(subQueryIdentity);
    }

    protected String resolveSubQueryEndMark(String subQueryIdentity) {
        return _subQuerySqlClause.resolveSubQueryEndMark(subQueryIdentity);
    }

    // ===================================================================================
    //                                                                       Geared Cipher
    //                                                                       =============
    protected ColumnFunctionCipher findColumnFunctionCipher(ColumnInfo columnInfo) {
        return _cipherManager != null ? _cipherManager.findColumnFunctionCipher(columnInfo) : null;
    }

    protected String decrypt(ColumnInfo columnInfo, String valueExp) {
        final ColumnFunctionCipher cipher = findColumnFunctionCipher(columnInfo);
        return cipher != null ? cipher.decrypt(valueExp) : valueExp;
    }

    // ===================================================================================
    //                                                                    Exception Helper
    //                                                                    ================
    protected ConditionBeanExceptionThrower createCBExThrower() {
        return new ConditionBeanExceptionThrower();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected final String replace(String text, String fromText, String toText) {
        return Srl.replace(text, fromText, toText);
    }

    protected final String initCap(String str) {
        return Srl.initCap(str);
    }

    protected final String initUncap(String str) {
        return Srl.initUncap(str);
    }

    protected final String ln() {
        return DBFluteSystem.getBasicLn();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public SubQueryPath getSubQueryPath() {
        return _subQueryPath;
    }

    public int getSubQueryLevel() {
        return _subQueryLevel;
    }

    public SqlClause getSubQuerySqlClause() {
        return _subQuerySqlClause;
    }
}
