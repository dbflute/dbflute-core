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
package org.seasar.dbflute.cbean.chelper;

import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.cbean.sqlclause.subquery.DerivedReferrer;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;

/**
 * @author jflute
 */
public class HpDerivingSubQueryInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _function;
    protected final String _aliasName;
    protected final String _derivingSubQuery;
    protected final DerivedReferrer _derivedReferrer;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpDerivingSubQueryInfo(String function, String aliasName, String derivingSubQuery,
            DerivedReferrer derivedReferrer) {
        _function = function;
        _aliasName = aliasName;
        _derivingSubQuery = derivingSubQuery;
        _derivedReferrer = derivedReferrer;
    }

    // ===================================================================================
    //                                                                       Meta Provider
    //                                                                       =============
    public HpSpecifiedColumn extractDerivingColumn() {
        final SqlClause subQuerySqlClause = _derivedReferrer.getSubQuerySqlClause();
        final HpSpecifiedColumn specifiedColumn = subQuerySqlClause.getSpecifiedColumnAsOne();
        if (specifiedColumn != null) {
            return specifiedColumn;
        }
        return subQuerySqlClause.getSpecifiedDerivingColumnAsOne(); // nested
    }

    public ColumnInfo extractDerivingColumnInfo() {
        final SqlClause subQuerySqlClause = _derivedReferrer.getSubQuerySqlClause();
        final ColumnInfo columnInfo = subQuerySqlClause.getSpecifiedColumnInfoAsOne();
        if (columnInfo != null) {
            return columnInfo;
        }
        return subQuerySqlClause.getSpecifiedDerivingColumnInfoAsOne(); // nested
    }

    public boolean isFunctionCountFamily() {
        return _function.toLowerCase().startsWith("count"); // count() or count(distinct)
    }

    public boolean isFunctionMax() {
        return _function.equalsIgnoreCase("max");
    }

    public boolean isFunctionMin() {
        return _function.equalsIgnoreCase("min");
    }

    public boolean isFunctionSum() {
        return _function.equalsIgnoreCase("sum");
    }

    public boolean isFunctionAvg() {
        return _function.equalsIgnoreCase("avg");
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getFunction() {
        return _function;
    }

    public String getAliasName() {
        return _aliasName;
    }

    public String getDerivingSubQuery() {
        return _derivingSubQuery;
    }

    public DerivedReferrer getDerivedReferrer() {
        return _derivedReferrer;
    }
}
