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
import org.seasar.dbflute.dbmeta.DerivedMappable;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;
import org.seasar.dbflute.dbmeta.name.ColumnRealNameProvider;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.dbmeta.name.ColumnSqlNameProvider;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.7.2 (2010/06/20 Sunday)
 */
public class SpecifyDerivedReferrer extends DerivedReferrer {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The prefix mark for derived mapping alias. */
    protected static final String DERIVED_MAPPABLE_ALIAS_PREFIX = DerivedMappable.MAPPING_ALIAS_PREFIX;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The alias name for derived column. (NullAllowed: if null, means no alias expression) */
    protected final String _aliasName;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SpecifyDerivedReferrer(SubQueryPath subQueryPath, ColumnRealNameProvider localRealNameProvider,
            ColumnSqlNameProvider subQuerySqlNameProvider, int subQueryLevel, SqlClause subQuerySqlClause,
            String subQueryIdentity, DBMeta subQueryDBMeta, GearedCipherManager cipherManager,
            String mainSubQueryIdentity, String aliasName) {
        super(subQueryPath, localRealNameProvider, subQuerySqlNameProvider, subQueryLevel, subQuerySqlClause,
                subQueryIdentity, subQueryDBMeta, cipherManager, mainSubQueryIdentity);
        _aliasName = aliasName;
    }

    // ===================================================================================
    //                                                                        Build Clause
    //                                                                        ============
    @Override
    protected String doBuildDerivedReferrer(String function, ColumnRealName correlatedColumnRealName,
            ColumnSqlName relatedColumnSqlName, String subQueryClause, String beginMark, String endMark,
            String endIndent) {
        return buildCompleteClause(subQueryClause, beginMark, endMark, endIndent);
    }

    @Override
    protected String doBuildDerivedReferrer(String function, ColumnRealName[] correlatedColumnRealNames,
            ColumnSqlName[] relatedColumnSqlNames, String subQueryClause, String beginMark, String endMark,
            String endIndent) {
        return buildCompleteClause(subQueryClause, beginMark, endMark, endIndent);
    }

    protected String buildCompleteClause(String subQueryClause, String beginMark, String endMark, String endIndent) {
        final String aliasExp;
        if (_aliasName != null) {
            final String realAlias;
            if (_aliasName.startsWith(DERIVED_MAPPABLE_ALIAS_PREFIX)) {
                realAlias = Srl.substringFirstRear(_aliasName, DERIVED_MAPPABLE_ALIAS_PREFIX);
            } else {
                realAlias = _aliasName;
            }
            aliasExp = " as " + realAlias;
        } else {
            aliasExp = "";
        }
        return "(" + beginMark + subQueryClause + ln() + endIndent + ")" + aliasExp + endMark;
    }

    @Override
    protected void throwDerivedReferrerInvalidColumnSpecificationException(String function) {
        createCBExThrower().throwSpecifyDerivedReferrerInvalidColumnSpecificationException(function, _aliasName);
    }

    @Override
    protected void doAssertDerivedReferrerColumnType(String function, String derivedColumnDbName,
            Class<?> derivedColumnType) {
        if ("sum".equalsIgnoreCase(function) || "avg".equalsIgnoreCase(function)) {
            if (!Number.class.isAssignableFrom(derivedColumnType)) {
                throwSpecifyDerivedReferrerUnmatchedColumnTypeException(function, derivedColumnDbName,
                        derivedColumnType);
            }
        }
    }

    protected void throwSpecifyDerivedReferrerUnmatchedColumnTypeException(String function, String derivedColumnDbName,
            Class<?> derivedColumnType) {
        createCBExThrower().throwSpecifyDerivedReferrerUnmatchedColumnTypeException(function, derivedColumnDbName,
                derivedColumnType);
    }
}
