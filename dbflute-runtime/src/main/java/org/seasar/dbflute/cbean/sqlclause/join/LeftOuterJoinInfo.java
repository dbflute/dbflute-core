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
package org.seasar.dbflute.cbean.sqlclause.join;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.cbean.sqlclause.query.QueryClause;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;

/**
 * @author jflute
 */
public class LeftOuterJoinInfo implements Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Serial version UID. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _foreignAliasName; // unique key for this info
    protected String _foreignTableDbName;
    protected String _localAliasName;
    protected String _localTableDbName;

    // join condition and info
    protected Map<ColumnRealName, ColumnRealName> _joinOnMap;
    protected LeftOuterJoinInfo _localJoinInfo; // to be able to trace back toward base point
    protected String _relationPath; // also unique

    // foreign key info
    protected boolean _pureFK; // has foreign key constraint (not additional, not referrer)
    protected boolean _notNullFKColumn; // local column for foreign key has not null constraint

    // query for join
    protected final List<QueryClause> _inlineWhereClauseList = new ArrayList<QueryClause>();
    protected final List<QueryClause> _additionalOnClauseList = new ArrayList<QueryClause>();

    // fixed condition
    protected String _fixedCondition;
    protected FixedConditionResolver _fixedConditionResolver;
    protected boolean _fixedConditionOverRelation; // derived by resolving

    // additional join attribute
    protected boolean _innerJoin; // option (true if inner-join forced or auto-detected)
    protected boolean _underInnerJoin; // option (true if the join has foreign's inner-join)
    protected boolean _whereUsedJoin; // option (true if used on where clause or foreign's use)
    protected boolean _underOverRelation;

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    // -----------------------------------------------------
    //                                      In-line/OrClause
    //                                      ----------------
    public boolean hasInlineOrOnClause() {
        return !_inlineWhereClauseList.isEmpty() || !_additionalOnClauseList.isEmpty();
    }

    // -----------------------------------------------------
    //                                        FixedCondition
    //                                        --------------
    public boolean hasFixedCondition() {
        return _fixedCondition != null && _fixedCondition.trim().length() > 0;
    }

    public void resolveFixedCondition() { // required before using fixed-condition
        if (hasFixedCondition() && _fixedConditionResolver != null) {
            // over-relation should be determined before resolving
            _fixedConditionOverRelation = _fixedConditionResolver.hasOverRelation(_fixedCondition);
            _fixedCondition = _fixedConditionResolver.resolveVariable(_fixedCondition, false);
            determineUnderOverRelation();
        }
    }

    public boolean hasFixedConditionOverRelation() { // should be called after resolving fixed-condition
        return _fixedConditionOverRelation;
    }

    public String resolveFixedInlineView(String foreignTableSqlName, boolean canBeInnerJoin) {
        if (hasFixedCondition() && _fixedConditionResolver != null) {
            return _fixedConditionResolver.resolveFixedInlineView(foreignTableSqlName, canBeInnerJoin);
        }
        return foreignTableSqlName.toString();
    }

    protected void determineUnderOverRelation() {
        if (hasFixedConditionOverRelation()) {
            LeftOuterJoinInfo current = _localJoinInfo;
            while (true) {
                if (current == null) { // means first level (not nested) join
                    break;
                }
                // means nested join here
                current.setUnderOverRelation(true); // tell her about it
                current = current.getLocalJoinInfo();
            }
        }
    }

    // -----------------------------------------------------
    //                                             Countable
    //                                             ---------
    public boolean isCountableJoin() { // called when building clause
        return isInnerJoin() || isUnderInnerJoin() || isWhereUsedJoin();
    }

    // -----------------------------------------------------
    //                                             InnerJoin
    //                                             ---------
    public boolean isStructuralPossibleInnerJoin() { // called when building clause
        if (!isPureStructuralPossibleInnerJoin()) {
            return false;
        }
        // pure structural-possible inner-join here
        // and check all relations from base point are inner-join or not
        // (separated structural-possible should not be inner-join)
        LeftOuterJoinInfo current = _localJoinInfo;
        while (true) {
            if (current == null) { // means first level (not nested) join
                break;
            }
            // means nested join here
            // (e.g. SERVICE_RANK if MEMBER is base point)
            if (!current.isTraceStructuralPossibleInnerJoin()) {
                return false;
            }
            current = current.getLocalJoinInfo();
        }
        return true;
    }

    protected boolean isPureStructuralPossibleInnerJoin() {
        return !hasInlineOrOnClause() && _pureFK && _notNullFKColumn;
    }

    protected boolean isTraceStructuralPossibleInnerJoin() {
        // more pattern may exist but it keeps logic simple for safety
        return isInnerJoin() || isPureStructuralPossibleInnerJoin();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getForeignAliasName() {
        return _foreignAliasName;
    }

    public void setForeignAliasName(String foreignAliasName) {
        _foreignAliasName = foreignAliasName;
    }

    public String getForeignTableDbName() {
        return _foreignTableDbName;
    }

    public void setForeignTableDbName(String foreignTableDbName) {
        _foreignTableDbName = foreignTableDbName;
    }

    public String getLocalAliasName() {
        return _localAliasName;
    }

    public void setLocalAliasName(String localAliasName) {
        _localAliasName = localAliasName;
    }

    public String getLocalTableDbName() {
        return _localTableDbName;
    }

    public void setLocalTableDbName(String localTableDbName) {
        _localTableDbName = localTableDbName;
    }

    public Map<ColumnRealName, ColumnRealName> getJoinOnMap() {
        return _joinOnMap;
    }

    public void setJoinOnMap(Map<ColumnRealName, ColumnRealName> joinOnMap) {
        _joinOnMap = joinOnMap;
    }

    public LeftOuterJoinInfo getLocalJoinInfo() {
        return _localJoinInfo;
    }

    public void setLocalJoinInfo(LeftOuterJoinInfo localJoinInfo) {
        _localJoinInfo = localJoinInfo;
    }

    public String getRelationPath() {
        return _relationPath;
    }

    public void setRelationPath(String relationPath) {
        _relationPath = relationPath;
    }

    public boolean getPureFK() {
        return _pureFK;
    }

    public void setPureFK(boolean pureFK) {
        _pureFK = pureFK;
    }

    public boolean getNotNullFKColumn() {
        return _notNullFKColumn;
    }

    public void setNotNullFKColumn(boolean notNullFKColumn) {
        _notNullFKColumn = notNullFKColumn;
    }

    public List<QueryClause> getInlineWhereClauseList() {
        return _inlineWhereClauseList;
    }

    public void addInlineWhereClause(QueryClause inlineWhereClause) {
        _inlineWhereClauseList.add(inlineWhereClause);
    }

    public List<QueryClause> getAdditionalOnClauseList() {
        return _additionalOnClauseList;
    }

    public void addAdditionalOnClause(QueryClause additionalOnClause) {
        _additionalOnClauseList.add(additionalOnClause);
    }

    public String getFixedCondition() {
        return _fixedCondition;
    }

    public void setFixedCondition(String fixedCondition) {
        _fixedCondition = fixedCondition;
    }

    public FixedConditionResolver getFixedConditionResolver() {
        return _fixedConditionResolver;
    }

    public void setFixedConditionResolver(FixedConditionResolver fixedConditionResolver) {
        _fixedConditionResolver = fixedConditionResolver;
    }

    public boolean isInnerJoin() {
        return _innerJoin;
    }

    public void setInnerJoin(boolean innerJoin) {
        _innerJoin = innerJoin;
    }

    public boolean isUnderInnerJoin() {
        return _underInnerJoin;
    }

    public void setUnderInnerJoin(boolean underInnerJoin) {
        _underInnerJoin = underInnerJoin;
    }

    public boolean isWhereUsedJoin() {
        return _whereUsedJoin;
    }

    public void setWhereUsedJoin(boolean whereUsedJoin) {
        _whereUsedJoin = whereUsedJoin;
    }

    public boolean isUnderOverRelation() {
        return _underOverRelation;
    }

    public void setUnderOverRelation(boolean underOverRelation) {
        _underOverRelation = underOverRelation;
    }
}
