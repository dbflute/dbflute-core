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
package org.seasar.dbflute.cbean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.seasar.dbflute.cbean.chelper.HpDerivingSubQueryInfo;
import org.seasar.dbflute.cbean.chelper.HpFixedConditionQueryResolver;
import org.seasar.dbflute.cbean.chelper.HpInvalidQueryInfo;
import org.seasar.dbflute.cbean.chelper.HpManualOrderThemeListHandler;
import org.seasar.dbflute.cbean.chelper.HpQDRFunction;
import org.seasar.dbflute.cbean.chelper.HpQDRParameter;
import org.seasar.dbflute.cbean.chelper.HpQDRSetupper;
import org.seasar.dbflute.cbean.chelper.HpSSQFunction;
import org.seasar.dbflute.cbean.chelper.HpSSQOption;
import org.seasar.dbflute.cbean.chelper.HpSSQSetupper;
import org.seasar.dbflute.cbean.chelper.HpSpecifiedColumn;
import org.seasar.dbflute.cbean.cipher.ColumnFunctionCipher;
import org.seasar.dbflute.cbean.cipher.GearedCipherManager;
import org.seasar.dbflute.cbean.ckey.ConditionKey;
import org.seasar.dbflute.cbean.ckey.ConditionKeyInScope;
import org.seasar.dbflute.cbean.coption.ConditionOption;
import org.seasar.dbflute.cbean.coption.DerivedReferrerOption;
import org.seasar.dbflute.cbean.coption.FromToOption;
import org.seasar.dbflute.cbean.coption.LikeSearchOption;
import org.seasar.dbflute.cbean.coption.ParameterOption;
import org.seasar.dbflute.cbean.coption.RangeOfOption;
import org.seasar.dbflute.cbean.cvalue.ConditionValue;
import org.seasar.dbflute.cbean.cvalue.ConditionValue.QueryModeProvider;
import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.cbean.sqlclause.SqlClauseMySql;
import org.seasar.dbflute.cbean.sqlclause.SqlClauseOracle;
import org.seasar.dbflute.cbean.sqlclause.clause.ClauseLazyReflector;
import org.seasar.dbflute.cbean.sqlclause.join.FixedConditionLazyChecker;
import org.seasar.dbflute.cbean.sqlclause.join.FixedConditionResolver;
import org.seasar.dbflute.cbean.sqlclause.orderby.OrderByElement;
import org.seasar.dbflute.cbean.sqlclause.query.QueryClause;
import org.seasar.dbflute.cbean.sqlclause.query.QueryClauseArranger;
import org.seasar.dbflute.cbean.sqlclause.query.QueryUsedAliasInfo;
import org.seasar.dbflute.cbean.sqlclause.subquery.ExistsReferrer;
import org.seasar.dbflute.cbean.sqlclause.subquery.InScopeRelation;
import org.seasar.dbflute.cbean.sqlclause.subquery.QueryDerivedReferrer;
import org.seasar.dbflute.cbean.sqlclause.subquery.ScalarCondition;
import org.seasar.dbflute.cbean.sqlclause.subquery.ScalarCondition.PartitionByProvider;
import org.seasar.dbflute.cbean.sqlclause.subquery.SpecifyDerivedReferrer;
import org.seasar.dbflute.cbean.sqlclause.subquery.SubQueryPath;
import org.seasar.dbflute.cbean.sqlclause.union.UnionClauseProvider;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.DBMetaProvider;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.info.ForeignInfo;
import org.seasar.dbflute.dbmeta.info.ReferrerInfo;
import org.seasar.dbflute.dbmeta.info.RelationInfo;
import org.seasar.dbflute.dbmeta.info.UniqueInfo;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;
import org.seasar.dbflute.dbmeta.name.ColumnRealNameProvider;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.dbmeta.name.ColumnSqlNameProvider;
import org.seasar.dbflute.dbway.ExtensionOperand;
import org.seasar.dbflute.dbway.WayOfMySQL;
import org.seasar.dbflute.exception.ConditionInvokingFailureException;
import org.seasar.dbflute.exception.IllegalConditionBeanOperationException;
import org.seasar.dbflute.exception.OrScopeQueryAndPartUnsupportedOperationException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.exception.thrower.ConditionBeanExceptionThrower;
import org.seasar.dbflute.helper.beans.DfBeanDesc;
import org.seasar.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.seasar.dbflute.jdbc.Classification;
import org.seasar.dbflute.jdbc.ParameterUtil;
import org.seasar.dbflute.jdbc.ParameterUtil.ShortCharHandlingMode;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfReflectionUtil;
import org.seasar.dbflute.util.DfReflectionUtil.ReflectionFailureException;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * The abstract class of condition-query.
 * @author jflute
 */
public abstract class AbstractConditionQuery implements ConditionQuery {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final ConditionKey CK_EQ = ConditionKey.CK_EQUAL;
    protected static final ConditionKey CK_NES = ConditionKey.CK_NOT_EQUAL_STANDARD;
    protected static final ConditionKey CK_NET = ConditionKey.CK_NOT_EQUAL_TRADITION;
    protected static final ConditionKey CK_GT = ConditionKey.CK_GREATER_THAN;
    protected static final ConditionKey CK_LT = ConditionKey.CK_LESS_THAN;
    protected static final ConditionKey CK_GE = ConditionKey.CK_GREATER_EQUAL;
    protected static final ConditionKey CK_LE = ConditionKey.CK_LESS_EQUAL;
    protected static final ConditionKey CK_INS = ConditionKey.CK_IN_SCOPE;
    protected static final ConditionKey CK_NINS = ConditionKey.CK_NOT_IN_SCOPE;
    protected static final ConditionKey CK_LS = ConditionKey.CK_LIKE_SEARCH;
    protected static final ConditionKey CK_NLS = ConditionKey.CK_NOT_LIKE_SEARCH;
    protected static final ConditionKey CK_ISN = ConditionKey.CK_IS_NULL;
    protected static final ConditionKey CK_ISNOE = ConditionKey.CK_IS_NULL_OR_EMPTY;
    protected static final ConditionKey CK_ISNN = ConditionKey.CK_IS_NOT_NULL;

    /** Object for DUMMY. */
    protected static final Object DOBJ = new Object();

    /** The property of condition-query. */
    protected static final String CQ_PROPERTY = "conditionQuery";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** SQL clause. (NotNull) */
    protected final SqlClause _sqlClause;

    /** My alias name. (NotNull) */
    protected final String _aliasName;

    /** The nest level of relation. */
    protected final int _nestLevel;

    /** The level of subQuery. */
    protected int _subQueryLevel;

    /** The base condition-bean of this query. (NotNull: after setting, basically is set) */
    protected ConditionBean _baseCB;

    // -----------------------------------------------------
    //                                          Foreign Info
    //                                          ------------
    /** The property name of foreign. */
    protected String _foreignPropertyName;

    /** The path of relation. */
    protected String _relationPath;

    /** The referrer query. */
    protected final ConditionQuery _referrerQuery;

    // -----------------------------------------------------
    //                                                Inline
    //                                                ------
    /** Is it the in-line. */
    protected boolean _inline;

    /** Is it on-clause. */
    protected boolean _onClause;

    // -----------------------------------------------------
    //                                      Relation Keeping
    //                                      ----------------
    /** The map of query-relation condition-query to keep parameters for parameter comment. */
    protected Map<String, ConditionQuery> _queryRelationKeepingMap;

    /** The map of sub-query condition-query to keep parameters for parameter comment. */
    protected Map<String, Map<String, ConditionQuery>> _subQueryKeepingMap;

    /** The map of sub-query parameter to keep parameters for parameter comment. */
    protected Map<String, Map<String, Object>> _subQueryParameterKeepingMap;

    /** The map of parameter option for parameter comment. */
    protected Map<String, ParameterOption> _parameterOptionMap;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     * @param referrerQuery The instance of referrer query. (NullAllowed: If null, this is base query)
     * @param sqlClause The instance of SQL clause. (NotNull)
     * @param aliasName The alias name for this query. (NotNull)
     * @param nestLevel The nest level of this query. (If zero, this is base query)
     */
    public AbstractConditionQuery(ConditionQuery referrerQuery, SqlClause sqlClause, String aliasName, int nestLevel) {
        _referrerQuery = referrerQuery;
        _sqlClause = sqlClause;
        _aliasName = aliasName;
        _nestLevel = nestLevel;
    }

    // *constructor can not be changed so set it up later

    public void xsetBaseCB(ConditionBean baseCB) {
        _baseCB = baseCB;
    }

    protected <CQ extends AbstractConditionQuery> CQ xinitRelCQ(CQ cq, ConditionBean baseCB,
            String foreignPropertyName, String nestRelationPath) {
        cq.xsetBaseCB(_baseCB);
        cq.xsetForeignPropertyName(foreignPropertyName);
        cq.xsetRelationPath(nestRelationPath);
        return cq;
    }

    // ===================================================================================
    //                                                                     DBMeta Provider
    //                                                                     ===============
    /**
     * Get the provider of DB meta.
     * @return The provider of DB meta. (NotNull)
     */
    protected abstract DBMetaProvider xgetDBMetaProvider();

    /**
     * Find the DB meta.
     * @param tableFlexibleName The table flexible name. (NotNull)
     * @return The DB meta of the table. (NotNull)
     */
    protected DBMeta findDBMeta(String tableFlexibleName) {
        return xgetDBMetaProvider().provideDBMetaChecked(tableFlexibleName);
    }

    /**
     * Get the local DB meta.
     * @return The instance of local DB meta. (NotNull)
     */
    protected DBMeta xgetLocalDBMeta() {
        return findDBMeta(getTableDbName());
    }

    // ===================================================================================
    //                                                                  Important Accessor
    //                                                                  ==================
    /**
     * {@inheritDoc}
     */
    public ConditionBean xgetBaseCB() {
        return _baseCB;
    }

    /**
     * {@inheritDoc}
     */
    public ConditionQuery xgetBaseQuery() {
        ConditionQuery currentQuery = this;
        while (true) {
            final ConditionQuery referrerQuery = currentQuery.xgetReferrerQuery();
            if (referrerQuery == null) {
                break;
            }
            currentQuery = referrerQuery;
        }
        return currentQuery;
    }

    /**
     * {@inheritDoc}
     */
    public ConditionQuery xgetReferrerQuery() {
        return _referrerQuery;
    }

    /**
     * {@inheritDoc}
     */
    public SqlClause xgetSqlClause() {
        return _sqlClause;
    }

    /**
     * {@inheritDoc}
     */
    public String xgetAliasName() {
        return _aliasName;
    }

    /**
     * {@inheritDoc}
     */
    public int xgetNestLevel() {
        return _nestLevel;
    }

    /**
     * {@inheritDoc}
     */
    public int xgetNextNestLevel() {
        return _nestLevel + 1;
    }

    protected int xgetNNLvl() { // for generated source
        return xgetNextNestLevel();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBaseQuery() {
        return (xgetReferrerQuery() == null);
    }

    // -----------------------------------------------------
    //                                             Real Name
    //                                             ---------
    /**
     * {@inheritDoc}
     */
    public ColumnRealName toColumnRealName(String columnDbName) { // with finding DBMeta
        return ColumnRealName.create(xgetAliasName(), toColumnSqlName(columnDbName));
    }

    /**
     * {@inheritDoc}
     */
    public ColumnRealName toColumnRealName(ColumnInfo columnInfo) { // without finding DBMeta
        return ColumnRealName.create(xgetAliasName(), columnInfo.getColumnSqlName());
    }

    /**
     * {@inheritDoc}
     */
    public ColumnSqlName toColumnSqlName(String columnDbName) { // with finding DBMeta
        return xgetLocalDBMeta().findColumnInfo(columnDbName).getColumnSqlName();
    }

    // -----------------------------------------------------
    //                                          Foreign Info
    //                                          ------------
    /**
     * {@inheritDoc}
     */
    public String xgetForeignPropertyName() {
        return _foreignPropertyName;
    }

    public void xsetForeignPropertyName(String foreignPropertyName) {
        this._foreignPropertyName = foreignPropertyName;
    }

    /**
     * {@inheritDoc}
     */
    public String xgetRelationPath() {
        return _relationPath;
    }

    public void xsetRelationPath(String relationPath) {
        this._relationPath = relationPath;
    }

    // -----------------------------------------------------
    //                                                Inline
    //                                                ------
    public void xsetOnClause(boolean onClause) {
        _onClause = onClause;
    }

    // -----------------------------------------------------
    //                                              Location
    //                                              --------
    /**
     * {@inheritDoc}
     */
    public String xgetLocationBase() {
        final StringBuilder sb = new StringBuilder();
        ConditionQuery query = this;
        while (true) {
            if (query.isBaseQuery()) {
                sb.insert(0, CQ_PROPERTY + ".");
                break;
            } else {
                final String foreignPropertyName = query.xgetForeignPropertyName();
                if (foreignPropertyName == null) {
                    String msg = "The foreignPropertyName of the query should not be null:";
                    msg = msg + " query=" + query;
                    throw new IllegalStateException(msg);
                }
                sb.insert(0, CQ_PROPERTY + initCap(foreignPropertyName) + ".");
            }
            query = query.xgetReferrerQuery();
        }
        return sb.toString();
    }

    /**
     * Get the location of the property.
     * @param propertyName The name of property. (NotNull)
     * @return The location of the property as path. (NotNull)
     */
    protected String xgetLocation(String propertyName) {
        return xgetLocationBase() + propertyName;
    }

    // ===================================================================================
    //                                                                  Nested SetupSelect
    //                                                                  ==================
    public void doNss(NssCall callback) { // very internal
        final String foreignPropertyName = callback.qf().xgetForeignPropertyName();
        final String foreignTableAliasName = callback.qf().xgetAliasName();
        final String localRelationPath = xgetRelationPath();
        final String foreignRelationPath = callback.qf().xgetRelationPath();
        xgetSqlClause().registerSelectedRelation(foreignTableAliasName, getTableDbName(), foreignPropertyName,
                localRelationPath, foreignRelationPath);
    }

    public static interface NssCall { // very internal
        public ConditionQuery qf();
    }

    // ===================================================================================
    //                                                                          Outer Join
    //                                                                          ==========
    /**
     * Register outer-join. <br />
     * Optional info, fixed condition and fixed in-line, are resolved in this method.
     * @param foreignCQ The condition-query for foreign table. (NotNull)
     * @param joinOnResourceMap The resource map of join condition on on-clause. (NotNull)
     * @param foreignPropertyName The property name of foreign relation corresponding to this join. (NotNull)
     */
    protected void registerOuterJoin(ConditionQuery foreignCQ, Map<String, String> joinOnResourceMap,
            String foreignPropertyName) {
        final DBMeta dbmeta = xgetLocalDBMeta();
        final ForeignInfo foreignInfo = dbmeta.findForeignInfo(foreignPropertyName);
        doRegisterOuterJoin(foreignCQ, joinOnResourceMap, foreignPropertyName, foreignInfo);
    }

    protected void doRegisterOuterJoin(ConditionQuery foreignCQ, Map<String, String> joinOnResourceMap,
            final String foreignPropertyName, ForeignInfo foreignInfo) {
        // translate join-on map using column real name
        final Map<ColumnRealName, ColumnRealName> joinOnMap = newLinkedHashMap();
        for (Entry<String, String> entry : joinOnResourceMap.entrySet()) {
            final String local = entry.getKey();
            final String foreign = entry.getValue();
            joinOnMap.put(toColumnRealName(local), foreignCQ.toColumnRealName(foreign));
        }
        final String foreignAlias = foreignCQ.xgetAliasName();
        final String foreignTable = foreignCQ.getTableDbName();
        final String localAlias = xgetAliasName();
        final String localTable = getTableDbName();
        final String fixedCondition = foreignInfo.getFixedCondition();
        final boolean fixedInline = foreignInfo.isFixedInline();
        final FixedConditionResolver resolver = createForeignFixedConditionResolver(foreignCQ);
        final String relationPath = foreignCQ.xgetRelationPath();
        if (fixedInline) {
            xgetSqlClause().registerOuterJoinFixedInline(foreignAlias, foreignTable, localAlias, localTable // basic
                    , joinOnMap, relationPath, foreignInfo // join objects
                    , fixedCondition, resolver); // fixed condition (to in-line view)
        } else { // normally here
            xgetSqlClause().registerOuterJoin(foreignAlias, foreignTable, localAlias, localTable // basic
                    , joinOnMap, relationPath, foreignInfo // join objects
                    , fixedCondition, resolver); // fixed condition (to on-clause)
        }
        xprepareFixedConditionDynamicParameterLazyChecker(foreignPropertyName, foreignInfo);
    }

    protected FixedConditionResolver createForeignFixedConditionResolver(ConditionQuery foreignCQ) {
        return newFixedConditionResolver(this, foreignCQ, xgetDBMetaProvider());
    }

    protected FixedConditionResolver newFixedConditionResolver(ConditionQuery localCQ, ConditionQuery foreignCQ,
            DBMetaProvider dbmetaProvider) {
        return new HpFixedConditionQueryResolver(localCQ, foreignCQ, dbmetaProvider);
    }

    protected void xprepareFixedConditionDynamicParameterLazyChecker(final String foreignPropertyName,
            final ForeignInfo foreignInfo) {
        if (!foreignInfo.hasFixedConditionDynamicParameter()) {
            return;
        }
        // lazy check because the following code is allowed:
        // e.g. ColumnQuery
        //   cb.columnQuery(new ... {
        //   }).lessThan(new ... {
        //       cb.specify().specifyMemberAddressAsValid()... // no dynamic parameter
        //   });
        xgetSqlClause().registerFixedConditionLazyChecker(new FixedConditionLazyChecker() {
            public void check() {
                xcalbackAssertFixedConditionDynamicParameter(foreignPropertyName, foreignInfo);
            }
        });
    }

    protected void xcalbackAssertFixedConditionDynamicParameter(String foreignPropertyName, ForeignInfo foreignInfo) {
        // cannot get dynamic parameter map directly in super classes of runtime
        // and does not want to add inner class in generated classes,
        // so this way...as a last-ditch measure
        final Map<String, Object> parameterMap = xfindFixedConditionDynamicParameterMap(foreignPropertyName);
        xdoAssertFixedConditionDynamicParameter(foreignPropertyName, foreignInfo, parameterMap);
    }

    protected abstract Map<String, Object> xfindFixedConditionDynamicParameterMap(String property);

    protected void xassertFCDP(String property, Map<String, Object> parameterMap) { // assertFixedConditionDynamicParameter()
        final ForeignInfo foreignInfo = xgetLocalDBMeta().findForeignInfo(property);
        xdoAssertFixedConditionDynamicParameter(property, foreignInfo, parameterMap);
    }

    protected void xdoAssertFixedConditionDynamicParameter(String property, ForeignInfo foreignInfo,
            Map<String, Object> parameterMap) {
        if (foreignInfo.isFixedConditionDynamicParameterRequired()) { // required check
            boolean notFound = false;
            if (parameterMap != null) {
                for (Object value : parameterMap.values()) {
                    if (value == null) {
                        notFound = true;
                        break;
                    }
                }
            } else { // null treated as not found
                notFound = true;
            }
            if (notFound) {
                final String tableDbName = getTableDbName();
                final String fixedCondition = foreignInfo.getFixedCondition();
                createCBExThrower().throwFixedConditionParameterNotFoundException(tableDbName, property,
                        fixedCondition, parameterMap);
            }
        }
    }

    // ===================================================================================
    //                                                                         Union Query
    //                                                                         ===========
    /** The map parameter-bean of union query. */
    protected SimpleMapPmb<ConditionQuery> _unionQueryMap;

    /**
     * Get the map parameter-bean of union query. (for parameter comment) {Internal}
     * @return The instance of map parameter-bean. (NotNull)
     */
    public SimpleMapPmb<ConditionQuery> getInternalUnionQueryMap() {
        if (_unionQueryMap == null) {
            _unionQueryMap = xcreateUnionMapPmb();
        }
        return _unionQueryMap;
    }

    /**
     * Set union query. {Internal}
     * @param unionQuery Union query. (NotNull)
     */
    public void xsetUnionQuery(ConditionQuery unionQuery) {
        xsetupUnion(unionQuery, false, getInternalUnionQueryMap());
    }

    /** The map parameter-bean of union all query. */
    protected SimpleMapPmb<ConditionQuery> _unionAllQueryMap;

    /**
     * Get the map parameter-bean of union all query. (for parameter comment) {Internal}
     * @return The instance of map parameter-bean. (NotNull)
     */
    public SimpleMapPmb<ConditionQuery> getInternalUnionAllQueryMap() {
        if (_unionAllQueryMap == null) {
            _unionAllQueryMap = xcreateUnionMapPmb();
        }
        return _unionAllQueryMap;
    }

    protected SimpleMapPmb<ConditionQuery> xcreateUnionMapPmb() {
        return new SimpleMapPmb<ConditionQuery>();
    }

    /**
     * Set union all query. {Internal}
     * @param unionAllQuery Union all query. (NotNull)
     */
    public void xsetUnionAllQuery(ConditionQuery unionAllQuery) {
        xsetupUnion(unionAllQuery, true, getInternalUnionAllQueryMap());
    }

    protected void xsetupUnion(final ConditionQuery unionQuery, boolean unionAll,
            SimpleMapPmb<ConditionQuery> unionQueryMap) {
        if (unionQuery == null) {
            String msg = "The argument 'unionQuery' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        // needs to reflect lazily for:
        // o SetupSelect(Relation) after Union (however, basically they should be called before union)
        // o ManualOrder with Dream Cruise using Specify(Relation) after Union
        final ConditionQuery selfCQ = this;
        xgetSqlClause().registerClauseLazyReflector(new ClauseLazyReflector() {
            public void reflect() {
                reflectRelationOnUnionQuery(selfCQ, unionQuery); // reflect relations
            }
        });
        final String key = (unionAll ? "unionAllQuery" : "unionQuery") + unionQueryMap.size();
        unionQueryMap.addParameter(key, unionQuery);
        final String propName = "internalUnion" + (unionAll ? "All" : "") + "QueryMap." + key;
        registerUnionQuery(unionQuery, unionAll, propName);
    }

    /**
     * Reflect relation on union query.
     * @param baseQueryAsSuper Base query as super. (NotNull)
     * @param unionQueryAsSuper Union query as super. (NotNull)
     */
    protected abstract void reflectRelationOnUnionQuery(ConditionQuery baseQueryAsSuper,
            ConditionQuery unionQueryAsSuper);

    /**
     * Has union query or union all query?
     * @return The determination, true or false.
     */
    public boolean hasUnionQueryOrUnionAllQuery() {
        return (_unionQueryMap != null && !_unionQueryMap.isEmpty())
                || (_unionAllQueryMap != null && !_unionAllQueryMap.isEmpty());
    }

    // ===================================================================================
    //                                                                      Register Query
    //                                                                      ==============
    // -----------------------------------------------------
    //                                          Normal Query
    //                                          ------------
    protected void regQ(ConditionKey key, Object value, ConditionValue cvalue, String columnDbName) {
        if (!prepareQueryChecked(key, value, cvalue, columnDbName)) {
            return;
        }
        setupConditionValueAndRegisterWhereClause(key, value, cvalue, columnDbName);
    }

    protected void regQ(ConditionKey key, Object value, ConditionValue cvalue, String columnDbName,
            ConditionOption option) {
        if (!prepareQueryChecked(key, value, cvalue, columnDbName)) {
            return;
        }
        setupConditionValueAndRegisterWhereClause(key, value, cvalue, columnDbName, option);
    }

    protected boolean prepareQueryChecked(ConditionKey key, Object value, ConditionValue cvalue, String columnDbName) {
        return xdoPrepareQuery(key, value, cvalue, columnDbName, true);
    }

    protected boolean prepareQueryNoCheck(ConditionKey key, Object value, ConditionValue cvalue, String columnDbName) {
        return xdoPrepareQuery(key, value, cvalue, columnDbName, false);
    }

    protected boolean xdoPrepareQuery(ConditionKey key, Object value, ConditionValue cvalue, String columnDbName,
            boolean checked) {
        final ColumnRealName callerName = toColumnRealName(columnDbName); // logging only
        final boolean overrideValue = key.needsOverrideValue(cvalue);
        if (overrideValue) {
            handleOverridingQuery(key, value, cvalue, columnDbName);
        }
        if (key.prepareQuery(xcreateQueryModeProvider(), cvalue, value, callerName)) {
            return true;
        } else {
            if (!overrideValue && checked) {
                handleInvalidQuery(key, value, cvalue, columnDbName);
            }
            return false;
        }
    }

    // -----------------------------------------------------
    //                                      Overriding Query
    //                                      ----------------
    protected void handleOverridingQuery(ConditionKey key, Object value, ConditionValue cvalue, String columnDbName) {
        if (isOverrideQueryAllowed(key, value, cvalue, columnDbName)) {
            return;
        }
        throwQueryAlreadyRegisteredException(key, value, cvalue, columnDbName);
    }

    protected boolean isOverrideQueryAllowed(ConditionKey key, Object value, ConditionValue cvalue, String columnDbName) {
        return xgetSqlClause().isOverridingQueryEnabled();
    }

    // -----------------------------------------------------
    //                                         Invalid Query
    //                                         -------------
    protected void handleInvalidQuery(ConditionKey key, Object value, ConditionValue cvalue, String columnDbName) {
        final HpInvalidQueryInfo invalidQueryInfo = xcreateInvalidQueryInfo(key, value, columnDbName);
        xdoHandleInvalidQuery(columnDbName, invalidQueryInfo);
    }

    protected void handleInvalidQueryList(List<ConditionKey> keyList, List<? extends Object> valueList,
            String columnDbName) {
        if (keyList.size() != valueList.size()) {
            String msg = "The argument 'keyList' should have the same size as 'valueList':";
            msg = msg + " keyList=" + keyList + ", valueList=" + valueList;
            throw new IllegalArgumentException(msg);
        }
        final HpInvalidQueryInfo[] invalidQueryInfoAry = new HpInvalidQueryInfo[keyList.size()];
        int index = 0;
        for (ConditionKey key : keyList) {
            final Object value = valueList.get(index);
            invalidQueryInfoAry[index] = xcreateInvalidQueryInfo(key, value, columnDbName);
            ++index;
        }
        xdoHandleInvalidQuery(columnDbName, invalidQueryInfoAry);
    }

    protected void xdoHandleInvalidQuery(String columnDbName, HpInvalidQueryInfo... invalidQueryInfoAry) {
        if (xgetSqlClause().isNullOrEmptyQueryChecked()) {
            throwInvalidQueryRegisteredException(invalidQueryInfoAry);
        } else {
            for (HpInvalidQueryInfo invalidQueryInfo : invalidQueryInfoAry) {
                xgetSqlClause().saveInvalidQuery(invalidQueryInfo);
            }
        }
    }

    protected HpInvalidQueryInfo xcreateInvalidQueryInfo(ConditionKey key, Object value, String columnDbName) {
        final String locationBase = xgetLocationBase();
        final ColumnInfo targetColumn = xgetLocalDBMeta().findColumnInfo(columnDbName);
        final HpInvalidQueryInfo invalidQueryInfo = new HpInvalidQueryInfo(locationBase, targetColumn, key, value);
        if (_inline) {
            invalidQueryInfo.inlineView();
        } else if (_onClause) {
            invalidQueryInfo.onClause();
        }
        return invalidQueryInfo;
    }

    protected QueryModeProvider xcreateQueryModeProvider() {
        return new QueryModeProvider() {
            public boolean isOrScopeQuery() {
                return xgetSqlClause().isOrScopeQueryEffective();
            }

            public boolean isInline() {
                return _inline;
            }

            public boolean isOnClause() {
                return _onClause;
            }
        };
    }

    protected void throwQueryAlreadyRegisteredException(ConditionKey key, Object value, ConditionValue cvalue,
            String columnDbName) {
        createCBExThrower().throwQueryAlreadyRegisteredException(key, value, cvalue, columnDbName);
    }

    protected void throwInvalidQueryRegisteredException(HpInvalidQueryInfo... invalidQueryInfoAry) {
        createCBExThrower().throwInvalidQueryRegisteredException(invalidQueryInfoAry);
    }

    // -----------------------------------------------------
    //                                      LikeSearch Query
    //                                      ----------------
    protected void regLSQ(ConditionKey key, String value, ConditionValue cvalue, String columnDbName,
            LikeSearchOption option) {
        registerLikeSearchQuery(key, value, cvalue, columnDbName, option);
    }

    protected void registerLikeSearchQuery(ConditionKey key, String value, ConditionValue cvalue, String columnDbName,
            LikeSearchOption option) {
        if (option == null) {
            throwLikeSearchOptionNotFoundException(columnDbName, value);
            return; // unreachable
        }
        if (!prepareQueryChecked(key, value, cvalue, columnDbName)) {
            return;
        }
        if (xsuppressEscape()) {
            option.notEscape();
        }
        // basically for DBMS that has original wild-cards
        xgetSqlClause().adjustLikeSearchDBWay(option);

        if (value == null || !option.isSplit()) {
            if (option.canOptimizeCompoundColumnLikePrefix()) {
                // - - - - - - - - - -
                // optimized compound
                // - - - - - - - - - -
                doRegisterLikeSearchQueryCompoundOptimized(value, cvalue, columnDbName, option);
            } else {
                // - - - - - - - - - - - - -
                // normal or normal compound
                // - - - - - - - - - - - - -
                setupConditionValueAndRegisterWhereClause(key, value, cvalue, columnDbName, option);
            }
            return;
        }
        // - - - - - - -
        // splitByXxx()
        // - - - - - - -
        doRegisterLikeSearchQuerySplitBy(key, value, cvalue, columnDbName, option);
    }

    protected void doRegisterLikeSearchQueryCompoundOptimized(String value, ConditionValue cvalue, String columnDbName,
            LikeSearchOption option) {
        if (!option.isLikePrefix()) {
            String msg = "This optimization is only for LikePrefix: " + option;
            throw new IllegalStateException(msg);
        }
        // *char type only but no checked (cannot check)
        final List<HpSpecifiedColumn> compoundColumnList = option.getCompoundColumnList();
        final List<Integer> sizeList = option.getCompoundColumnSizeList();
        String currentValue = value;
        int currentLength = value.length();
        String currentColumn = columnDbName;
        final boolean needsAndPart = isOrScopeQueryDirectlyUnder();
        if (needsAndPart) {
            xgetSqlClause().beginOrScopeQueryAndPart();
        }
        try {
            boolean shortLengthBreak = false;
            final Iterator<HpSpecifiedColumn> compoundColumnIterator = compoundColumnList.iterator();
            for (Integer columnSize : sizeList) { // should be less or equal column count (checked in option)
                if (currentLength >= columnSize) { // can treat current condition as equal
                    final String equalValue = currentValue.substring(0, columnSize);
                    invokeQueryEqual(currentColumn, equalValue);
                    currentValue = currentValue.substring(columnSize);
                    currentLength = currentValue.length();
                    final HpSpecifiedColumn specifiedColumn;
                    if (compoundColumnIterator.hasNext()) {
                        specifiedColumn = compoundColumnIterator.next();
                        currentColumn = specifiedColumn.getColumnDbName();
                    } else { // means just size
                        currentColumn = null; // means end
                        break; // though basically no need to break because of size loop end
                    }
                } else { // short length condition value
                    shortLengthBreak = true;
                    break;
                }
            }
            if (currentValue.length() > 0 && currentColumn != null) { // double check
                final LikeSearchOption copyOption = option.createDeepCopy();
                copyOption.clearCompoundColumn(); // also fixed sizes cleared
                if (!shortLengthBreak) {
                    while (compoundColumnIterator.hasNext()) {
                        copyOption.addCompoundColumn(compoundColumnIterator.next());
                    }
                }
                invokeQueryLikeSearch(currentColumn, currentValue, copyOption);
            }
        } finally {
            if (needsAndPart) {
                xgetSqlClause().endOrScopeQueryAndPart();
            }
        }
    }

    protected void doRegisterLikeSearchQuerySplitBy(ConditionKey key, String value, ConditionValue cvalue,
            String columnDbName, LikeSearchOption option) {
        assertObjectNotNull("option(LikeSearchOption)", option);
        // these values should be valid only (already filtered before)
        // and invalid values are ignored even at the check mode
        // but if all elements are invalid, it is an exception
        final String[] strArray = option.generateSplitValueArray(value);
        if (strArray.length == 0) {
            handleInvalidQuery(key, value, cvalue, columnDbName);
            return;
        }
        if (!option.isAsOrSplit()) {
            // as 'and' condition
            final boolean needsAndPart = isOrScopeQueryDirectlyUnder();
            if (needsAndPart) {
                xgetSqlClause().beginOrScopeQueryAndPart();
            }
            try {
                for (int i = 0; i < strArray.length; i++) {
                    final String currentValue = strArray[i];
                    setupConditionValueAndRegisterWhereClause(key, currentValue, cvalue, columnDbName, option);
                }
            } finally {
                if (needsAndPart) {
                    xgetSqlClause().endOrScopeQueryAndPart();
                }
            }
        } else {
            // as 'or' condition
            if (isOrScopeQueryAndPartEffective()) {
                // limit because of so complex
                String msg = "The AsOrSplit in and-part is unsupported: " + getTableDbName();
                throw new OrScopeQueryAndPartUnsupportedOperationException(msg);
            }
            final boolean needsNewOrScope = !isOrScopeQueryEffective();
            if (needsNewOrScope) {
                xgetSqlClause().beginOrScopeQuery();
            }
            try {
                for (int i = 0; i < strArray.length; i++) {
                    final String currentValue = strArray[i];
                    if (i == 0) {
                        setupConditionValueAndRegisterWhereClause(key, currentValue, cvalue, columnDbName, option);
                    } else {
                        invokeQueryLikeSearch(columnDbName, currentValue, option);
                    }
                }
            } finally {
                if (needsNewOrScope) {
                    xgetSqlClause().endOrScopeQuery();
                }
            }
        }
    }

    protected void throwLikeSearchOptionNotFoundException(String columnDbName, String value) {
        final DBMeta dbmeta = xgetDBMetaProvider().provideDBMeta(getTableDbName());
        createCBExThrower().throwLikeSearchOptionNotFoundException(columnDbName, value, dbmeta);
    }

    protected boolean xsuppressEscape() { // for override
        return false; // as default
    }

    protected void invokeQueryLikeSearch(String columnFlexibleName, Object value, LikeSearchOption option) {
        invokeQuery(columnFlexibleName, "likeSearch", value, option);
    }

    protected boolean isOrScopeQueryDirectlyUnder() {
        final boolean orScopeQuery = isOrScopeQueryEffective();
        final boolean orScopeQueryAndPart = isOrScopeQueryAndPartEffective();
        return orScopeQuery && !orScopeQueryAndPart;
    }

    protected boolean isOrScopeQueryEffective() {
        return xgetSqlClause().isOrScopeQueryEffective();
    }

    protected boolean isOrScopeQueryAndPartEffective() {
        return xgetSqlClause().isOrScopeQueryAndPartEffective();
    }

    // -----------------------------------------------------
    //                                          FromTo Query
    //                                          ------------
    protected void regFTQ(Date fromDate, Date toDate, ConditionValue cvalue, String columnDbName, FromToOption option) {
        assertObjectNotNull("option(FromToOption)", option);
        filterFromToOption(option); // for fixed option
        final Date filteredFromDate = option.filterFromDate(fromDate);
        final ConditionKey fromKey = option.getFromDateConditionKey();
        final boolean fromValidQuery = prepareQueryNoCheck(fromKey, filteredFromDate, cvalue, columnDbName);

        final Date filteredToDate = option.filterToDate(toDate);
        final ConditionKey toKey = option.getToDateConditionKey();
        final boolean toValidQuery = prepareQueryNoCheck(toKey, filteredToDate, cvalue, columnDbName);

        final boolean needsAndPart = isOrScopeQueryDirectlyUnder() && fromValidQuery && toValidQuery;
        if (needsAndPart) {
            xgetSqlClause().beginOrScopeQueryAndPart();
        }
        try {
            if (fromValidQuery) {
                final Object registered = filterFromToRegisteredDate(filteredFromDate, columnDbName);
                setupConditionValueAndRegisterWhereClause(fromKey, registered, cvalue, columnDbName);
            }
            if (toValidQuery) {
                final Object registered = filterFromToRegisteredDate(filteredToDate, columnDbName);
                setupConditionValueAndRegisterWhereClause(toKey, registered, cvalue, columnDbName);
            }
            if (!fromValidQuery && !toValidQuery) { // both no condition
                final List<ConditionKey> keyList = newArrayList(fromKey, toKey);
                final List<Date> valueList = newArrayList(fromDate, toDate);
                handleInvalidQueryList(keyList, valueList, columnDbName);
            } else if (!fromValidQuery || !toValidQuery) { // either no condition
                if (!option.isOneSideAllowed()) { // not allowed (if both required)
                    final List<ConditionKey> keyList = newArrayList(fromKey, toKey);
                    final List<Date> valueList = newArrayList(fromDate, toDate);
                    handleInvalidQueryList(keyList, valueList, columnDbName);
                }
            }
        } finally {
            if (needsAndPart) {
                xgetSqlClause().endOrScopeQueryAndPart();
            }
        }
    }

    protected void filterFromToOption(FromToOption option) {
        // do nothing as default, basically for option default
    }

    protected Object filterFromToRegisteredDate(Date date, String columnDbName) {
        return date; // do nothing as default, basically for converting to next date
    }

    // -----------------------------------------------------
    //                                         RangeOf Query
    //                                         -------------
    protected void regROO(Number minNumber, Number maxNumber, ConditionValue cvalue, String columnDbName,
            RangeOfOption option) {
        assertObjectNotNull("option(RangeOfOption)", option);
        if (option.hasCalculationRange()) {
            final ConditionBean dreamCruiseCB = xgetBaseCB().xcreateDreamCruiseCB();
            //dreamCruiseCB.x
            //dreamCruiseCB.overTheWaves(xcreateManualOrderSpecifiedColumn(dreamCruiseCB));
            option.xinitCalculationRange(xgetBaseCB(), dreamCruiseCB);
        }
        final ConditionKey minKey = option.getMinNumberConditionKey();
        final boolean minValidQuery = prepareQueryNoCheck(minKey, minNumber, cvalue, columnDbName);

        final ConditionKey maxKey = option.getMaxNumberConditionKey();
        final boolean maxValidQuery = prepareQueryNoCheck(maxKey, maxNumber, cvalue, columnDbName);

        final boolean needsAndPart = isOrScopeQueryDirectlyUnder() && minValidQuery && maxValidQuery;
        if (needsAndPart) {
            xgetSqlClause().beginOrScopeQueryAndPart();
        }
        try {
            if (minValidQuery) {
                setupConditionValueAndRegisterWhereClause(minKey, minNumber, cvalue, columnDbName, option);
            }
            if (maxValidQuery) {
                setupConditionValueAndRegisterWhereClause(maxKey, maxNumber, cvalue, columnDbName, option);
            } else {
                if (!minValidQuery) { // means both queries are invalid
                    final List<ConditionKey> keyList = newArrayList(minKey, maxKey);
                    final List<Number> valueList = newArrayList(minNumber, maxNumber);
                    handleInvalidQueryList(keyList, valueList, columnDbName);
                }
            }
        } finally {
            if (needsAndPart) {
                xgetSqlClause().endOrScopeQueryAndPart();
            }
        }
    }

    // -----------------------------------------------------
    //                                         InScope Query
    //                                         -------------
    protected void regINS(ConditionKey key, List<?> value, ConditionValue cvalue, String columnDbName) {
        if (!prepareQueryChecked(key, value, cvalue, columnDbName)) {
            return;
        }
        final int inScopeLimit = xgetSqlClause().getInScopeLimit();
        if (inScopeLimit > 0 && value.size() > inScopeLimit) {
            // if the key is for inScope, it should be split as 'or'
            // (if the key is for notInScope, it should be split as 'and')
            final boolean orScopeQuery = xgetSqlClause().isOrScopeQueryEffective();
            final boolean orScopeQueryAndPart = xgetSqlClause().isOrScopeQueryAndPartEffective();
            final boolean needsAndPart = orScopeQuery && !orScopeQueryAndPart;
            if (isConditionKeyInScope(key)) {
                // if or-scope query has already been effective, create new or-scope
                xgetSqlClause().beginOrScopeQuery();
            } else {
                if (needsAndPart) {
                    xgetSqlClause().beginOrScopeQueryAndPart();
                }
            }

            try {
                // split the condition
                @SuppressWarnings("unchecked")
                final List<Object> objectList = (List<Object>) value;
                final List<List<Object>> valueList = DfCollectionUtil.splitByLimit(objectList, inScopeLimit);
                for (int i = 0; i < valueList.size(); i++) {
                    final List<Object> currentValue = valueList.get(i);
                    if (i == 0) {
                        setupConditionValueAndRegisterWhereClause(key, currentValue, cvalue, columnDbName);
                    } else {
                        invokeQuery(columnDbName, key.getConditionKey(), currentValue);
                    }
                }
            } finally {
                if (isConditionKeyInScope(key)) {
                    xgetSqlClause().endOrScopeQuery();
                } else {
                    if (needsAndPart) {
                        xgetSqlClause().endOrScopeQueryAndPart();
                    }
                }
            }
        } else {
            setupConditionValueAndRegisterWhereClause(key, value, cvalue, columnDbName);
        }
    }

    static boolean isConditionKeyInScope(ConditionKey key) { // default scope for test 
        return ConditionKeyInScope.class.isAssignableFrom(key.getClass());
    }

    // -----------------------------------------------------
    //                                          Inline Query
    //                                          ------------
    protected void regIQ(ConditionKey key, Object value, ConditionValue cvalue, String columnDbName) {
        doRegIQ(key, value, cvalue, columnDbName, null);
    }

    protected void regIQ(ConditionKey key, Object value, ConditionValue cvalue, String columnDbName,
            ConditionOption option) {
        doRegIQ(key, value, cvalue, columnDbName, option);
    }

    protected void doRegIQ(final ConditionKey key, final Object value, final ConditionValue cvalue,
            final String columnDbName, final ConditionOption option) {
        if (!prepareQueryChecked(key, value, cvalue, columnDbName)) {
            return;
        }
        final DBMeta dbmeta = xgetDBMetaProvider().provideDBMetaChecked(getTableDbName());
        final ColumnInfo columnInfo = dbmeta.findColumnInfo(columnDbName);
        final String propertyName = columnInfo.getPropertyName();
        final String uncapPropName = initUncap(propertyName);
        // If Java, it is necessary to use uncapPropName!
        final String location = xgetLocation(uncapPropName);
        key.setupConditionValue(xcreateQueryModeProvider(), cvalue, value, location, option);
        final ColumnSqlName columnSqlName = columnInfo.getColumnSqlName();
        final ColumnFunctionCipher cipher = xgetSqlClause().findColumnFunctionCipher(columnInfo);
        if (isBaseQuery()) {
            xgetSqlClause().registerBaseTableInlineWhereClause(columnSqlName, key, cvalue, cipher, option);
        } else {
            final String aliasName = xgetAliasName();
            xgetSqlClause().registerOuterJoinInlineWhereClause(aliasName, columnSqlName, key, cvalue, cipher, option,
                    _onClause);
        }
    }

    // -----------------------------------------------------
    //                                        ExistsReferrer
    //                                        --------------
    protected void registerExistsReferrer(ConditionQuery subQuery, String columnDbName, String relatedColumnDbName,
            String propertyName, String referrerPropertyName) {
        registerExistsReferrer(subQuery, columnDbName, relatedColumnDbName, propertyName, referrerPropertyName, false);
    }

    protected void registerNotExistsReferrer(ConditionQuery subQuery, String columnDbName, String relatedColumnDbName,
            String propertyName, String referrerPropertyName) {
        registerExistsReferrer(subQuery, columnDbName, relatedColumnDbName, propertyName, referrerPropertyName, true);
    }

    protected void registerExistsReferrer(final ConditionQuery subQuery, String columnDbName,
            String relatedColumnDbName, String propertyName, String referrerPropertyName, boolean notExists) {
        assertSubQueryNotNull("ExistsReferrer", relatedColumnDbName, subQuery);
        final SubQueryPath subQueryPath = new SubQueryPath(xgetLocation(propertyName));
        final GeneralColumnRealNameProvider localRealNameProvider = new GeneralColumnRealNameProvider();
        final int subQueryLevel = subQuery.xgetSqlClause().getSubQueryLevel();
        final SqlClause subQueryClause = subQuery.xgetSqlClause();
        final String subQueryIdentity = propertyName + "[" + subQueryLevel + "]";
        final ColumnSqlNameProvider subQuerySqlNameProvider = new ColumnSqlNameProvider() {
            public ColumnSqlName provide(String columnDbName) {
                return subQuery.toColumnSqlName(columnDbName);
            }
        };
        final DBMeta subQueryDBMeta = findDBMeta(subQuery.getTableDbName());
        final GearedCipherManager cipherManager = xgetSqlClause().getGearedCipherManager();
        final ExistsReferrer existsReferrer = new ExistsReferrer(subQueryPath, localRealNameProvider,
                subQuerySqlNameProvider, subQueryLevel, subQueryClause, subQueryIdentity, subQueryDBMeta, cipherManager);
        final String correlatedFixedCondition = xbuildReferrerCorrelatedFixedCondition(subQuery, referrerPropertyName);
        final String existsOption = notExists ? "not" : null;
        final String clause = existsReferrer.buildExistsReferrer(columnDbName, relatedColumnDbName,
                correlatedFixedCondition, existsOption);

        // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 
        // Exists -> possible to be inner
        // NotExists -> no way to be inner
        //
        // for example, the following SQL is no way to be inner
        // (suppose if PURCHASE refers WITHDRAWAL)
        // 
        // select mb.MEMBER_ID, mb.MEMBER_NAME
        //      , mb.MEMBER_STATUS_CODE, wd.MEMBER_ID as WD_MEMBER_ID
        //   from MEMBER mb
        //     left outer join MEMBER_WITHDRAWAL wd on mb.MEMBER_ID = wd.MEMBER_ID
        //  where not exists (select pc.PURCHASE_ID
        //                      from PURCHASE pc
        //                     where pc.MEMBER_ID = wd.MEMBER_ID
        //        )
        //  order by mb.MEMBER_ID
        // = = = = = = = = = =/
        final boolean noWayInner = notExists; // but 'exists' allowed
        registerWhereClause(clause, noWayInner);
    }

    protected String xbuildReferrerCorrelatedFixedCondition(ConditionQuery subQuery, String referrerPropertyName) {
        if (referrerPropertyName == null) {
            return null;
        }
        final DBMeta localDBMeta = xgetLocalDBMeta();
        if (!localDBMeta.hasReferrer(referrerPropertyName)) { // one-to-one referrer
            return null;
        }
        final ReferrerInfo referrerInfo = localDBMeta.findReferrerInfo(referrerPropertyName);
        return xdoBuildReferrerCorrelatedFixedCondition(subQuery, referrerInfo);
    }

    protected String xdoBuildReferrerCorrelatedFixedCondition(ConditionQuery subQuery, ReferrerInfo referrerInfo) {
        final RelationInfo reverseRelation = referrerInfo.getReverseRelation();
        if (reverseRelation == null) {
            return null;
        }
        if (!(reverseRelation instanceof ForeignInfo)) {
            String msg = "The reverse relation (referrer's reverse) should be foreign info: " + referrerInfo;
            throw new IllegalStateException(msg);
        }
        final ForeignInfo foreignInfo = (ForeignInfo) reverseRelation;
        final String fixedCondition = foreignInfo.getFixedCondition();
        if (fixedCondition == null || fixedCondition.trim().length() == 0) {
            return null;
        }
        final FixedConditionResolver resolver = createReferrerFixedConditionResolver(subQuery);
        return resolver.resolveVariable(fixedCondition, false);
    }

    protected FixedConditionResolver createReferrerFixedConditionResolver(ConditionQuery referrerCQ) {
        return newFixedConditionResolver(referrerCQ, this, xgetDBMetaProvider());
    }

    // *unsupported ExistsReferrer as in-line because it's (or was) so dangerous

    // -----------------------------------------------------
    //                                       InScopeRelation
    //                                       ---------------
    // {Modified at DBFlute-0.7.5}
    protected void registerInScopeRelation(ConditionQuery subQuery, String columnDbName, String relatedColumnDbName,
            String propertyName, String relationPropertyName) {
        registerInScopeRelation(subQuery, columnDbName, relatedColumnDbName, propertyName, relationPropertyName, null);
    }

    protected void registerNotInScopeRelation(ConditionQuery subQuery, String columnDbName, String relatedColumnDbName,
            String propertyName, String relationPropertyName) {
        registerInScopeRelation(subQuery, columnDbName, relatedColumnDbName, propertyName, relationPropertyName, "not");
    }

    protected void registerInScopeRelation(final ConditionQuery subQuery, String columnDbName,
            String relatedColumnDbName, String propertyName, String relationPropertyName, String inScopeOption) {
        assertSubQueryNotNull("InScopeRelation", columnDbName, subQuery);
        final SubQueryPath subQueryPath = new SubQueryPath(xgetLocation(propertyName));
        final GeneralColumnRealNameProvider localRealNameProvider = new GeneralColumnRealNameProvider();
        final int subQueryLevel = subQuery.xgetSqlClause().getSubQueryLevel();
        final SqlClause subQueryClause = subQuery.xgetSqlClause();
        final String subQueryIdentity = propertyName + "[" + subQueryLevel + "]";
        final ColumnSqlNameProvider subQuerySqlNameProvider = new ColumnSqlNameProvider() {
            public ColumnSqlName provide(String columnDbName) {
                return subQuery.toColumnSqlName(columnDbName);
            }
        };
        final DBMeta subQueryDBMeta = findDBMeta(subQuery.getTableDbName());
        final GearedCipherManager cipherManager = xgetSqlClause().getGearedCipherManager();
        final boolean suppressLocalAliasName = isInScopeRelationSuppressLocalAliasName();
        final InScopeRelation inScopeRelation = new InScopeRelation(subQueryPath, localRealNameProvider,
                subQuerySqlNameProvider, subQueryLevel, subQueryClause, subQueryIdentity, subQueryDBMeta,
                cipherManager, suppressLocalAliasName);
        final String correlatedFixedCondition = xbuildForeignCorrelatedFixedCondition(subQuery, relationPropertyName);
        final String clause = inScopeRelation.buildInScopeRelation(columnDbName, relatedColumnDbName,
                correlatedFixedCondition, inScopeOption);
        registerWhereClause(clause);
    }

    protected boolean isInScopeRelationSuppressLocalAliasName() {
        // no alias name at InlineView
        return false; // as default
    }

    protected String xbuildForeignCorrelatedFixedCondition(ConditionQuery subQuery, String relationPropertyName) {
        if (relationPropertyName == null) {
            return null;
        }
        final DBMeta localDBMeta = xgetLocalDBMeta();
        final RelationInfo relationInfo = localDBMeta.findRelationInfo(relationPropertyName);
        if (!relationInfo.isReferrer()) {
            return null;
        }
        if (!(relationInfo instanceof ReferrerInfo)) {
            return null;
        }
        final ReferrerInfo referrerInfo = (ReferrerInfo) relationInfo;
        return xdoBuildReferrerCorrelatedFixedCondition(subQuery, referrerInfo);
    }

    // [DBFlute-0.7.4]
    // -----------------------------------------------------
    //                              (Specify)DerivedReferrer
    //                              ------------------------
    protected void registerSpecifyDerivedReferrer(String function, ConditionQuery subQuery, String columnDbName,
            String relatedColumnDbName, String propertyName, String referrerPropertyName, String aliasName,
            DerivedReferrerOption option) {
        doRegisterSpecifyDerivedReferrer(function, subQuery, columnDbName, relatedColumnDbName, propertyName,
                referrerPropertyName, aliasName, option != null ? option : new DerivedReferrerOption());
    }

    protected void doRegisterSpecifyDerivedReferrer(String function, final ConditionQuery subQuery,
            String columnDbName, String relatedColumnDbName, String propertyName, String referrerPropertyName,
            String aliasName, DerivedReferrerOption option) {
        assertFunctionNotNull("SpecifyDerivedReferrer", columnDbName, function);
        assertSubQueryNotNull("SpecifyDerivedReferrer", columnDbName, subQuery);
        option.xacceptBaseCB(xgetBaseCB());
        if (isDerivedReferrerSelectAllPossible(subQuery, option)) {
            createCBExThrower().throwSpecifyDerivedReferrerSelectAllPossibleException(function, subQuery, aliasName);
        }
        final SubQueryPath subQueryPath = new SubQueryPath(xgetLocation(propertyName));
        final GeneralColumnRealNameProvider localRealNameProvider = new GeneralColumnRealNameProvider();
        final int subQueryLevel = subQuery.xgetSqlClause().getSubQueryLevel();
        final SqlClause subQueryClause = subQuery.xgetSqlClause();
        final String subQueryIdentity = propertyName + "[" + subQueryLevel + "]";
        final ColumnSqlNameProvider subQuerySqlNameProvider = new ColumnSqlNameProvider() {
            public ColumnSqlName provide(String columnDbName) {
                return subQuery.toColumnSqlName(columnDbName);
            }
        };
        final DBMeta subQueryDBMeta = findDBMeta(subQuery.getTableDbName());
        final GearedCipherManager cipherManager = xgetSqlClause().getGearedCipherManager();
        final String mainSubQueryIdentity = propertyName + "[" + subQueryLevel + ":subquerymain]";
        final SpecifyDerivedReferrer derivedReferrer = option.createSpecifyDerivedReferrer(subQueryPath,
                localRealNameProvider, subQuerySqlNameProvider, subQueryLevel, subQueryClause, subQueryIdentity,
                subQueryDBMeta, cipherManager, mainSubQueryIdentity, aliasName);
        xregisterParameterOption(option);
        final String correlatedFixedCondition = xbuildReferrerCorrelatedFixedCondition(subQuery, referrerPropertyName);
        final String clause = derivedReferrer.buildDerivedReferrer(function, columnDbName, relatedColumnDbName,
                correlatedFixedCondition, option);
        final HpDerivingSubQueryInfo subQueryInfo = xcreateDerivingSubQueryInfo(function, aliasName, clause,
                derivedReferrer);
        xgetSqlClause().specifyDerivingSubQuery(subQueryInfo);
    }

    protected boolean isDerivedReferrerSelectAllPossible(final ConditionQuery subQuery, DerivedReferrerOption option) {
        return option.isSuppressCorrelation() && subQuery.xgetBaseCB().hasSelectAllPossible();
    }

    protected HpDerivingSubQueryInfo xcreateDerivingSubQueryInfo(String function, String aliasName, String clause,
            SpecifyDerivedReferrer derivedReferrer) {
        return new HpDerivingSubQueryInfo(function, aliasName, clause, derivedReferrer);
    }

    protected void registerSpecifyMyselfDerived(String function, ConditionQuery subQuery, String columnDbName,
            String relatedColumnDbName, String propertyName, String referrerPropertyName, String aliasName,
            DerivedReferrerOption option) {
        doRegisterSpecifyDerivedReferrer(function, subQuery, columnDbName, relatedColumnDbName, propertyName,
                referrerPropertyName, aliasName, resolveMyselfDerivedReferrerOption(option));
    }

    protected DerivedReferrerOption resolveMyselfDerivedReferrerOption(DerivedReferrerOption option) {
        final DerivedReferrerOption resolvedOption = option != null ? option : new DerivedReferrerOption();
        resolvedOption.suppressCorrelation();
        return resolvedOption;
    }

    // [DBFlute-0.8.8.1]
    // -----------------------------------------------------
    //                                (Query)DerivedReferrer
    //                                ----------------------
    protected void registerQueryDerivedReferrer(String function, ConditionQuery subQuery, String columnDbName,
            String relatedColumnDbName, String propertyName, String referrerPropertyName, String operand, Object value,
            String parameterPropertyName, DerivedReferrerOption option) {
        doRegisterQueryDerivedReferrer(function, subQuery, columnDbName, relatedColumnDbName, propertyName,
                referrerPropertyName, operand, value, parameterPropertyName, option != null ? option
                        : new DerivedReferrerOption());
    }

    protected void doRegisterQueryDerivedReferrer(String function, final ConditionQuery subQuery, String columnDbName,
            String relatedColumnDbName, String propertyName, String referrerPropertyName, String operand, Object value,
            String parameterPropertyName, DerivedReferrerOption option) {
        assertFunctionNotNull("QueryDerivedReferrer", columnDbName, function);
        assertSubQueryNotNull("QueryDerivedReferrer", columnDbName, subQuery);
        option.xacceptBaseCB(xgetBaseCB());
        if (isDerivedReferrerSelectAllPossible(subQuery, option)) {
            createCBExThrower().throwQueryDerivedReferrerSelectAllPossibleException(function, subQuery);
        }
        final SubQueryPath subQueryPath = new SubQueryPath(xgetLocation(propertyName));
        final GeneralColumnRealNameProvider localRealNameProvider = new GeneralColumnRealNameProvider();
        final int subQueryLevel = subQuery.xgetSqlClause().getSubQueryLevel();
        final SqlClause subQueryClause = subQuery.xgetSqlClause();
        final String subQueryIdentity = propertyName + "[" + subQueryLevel + "]";
        final ColumnSqlNameProvider subQuerySqlNameProvider = new ColumnSqlNameProvider() {
            public ColumnSqlName provide(String columnDbName) {
                return subQuery.toColumnSqlName(columnDbName);
            }
        };
        final DBMeta subQueryDBMeta = findDBMeta(subQuery.getTableDbName());
        final GearedCipherManager cipherManager = xgetSqlClause().getGearedCipherManager();
        final String mainSubQueryIdentity = propertyName + "[" + subQueryLevel + ":subquerymain]";
        final String parameterPath = xgetLocation(parameterPropertyName);
        final QueryDerivedReferrer derivedReferrer = option.createQueryDerivedReferrer(subQueryPath,
                localRealNameProvider, subQuerySqlNameProvider, subQueryLevel, subQueryClause, subQueryIdentity,
                subQueryDBMeta, cipherManager, mainSubQueryIdentity, operand, value, parameterPath);
        xregisterParameterOption(option);
        final String correlatedFixedCondition = xbuildReferrerCorrelatedFixedCondition(subQuery, referrerPropertyName);
        final String clause = derivedReferrer.buildDerivedReferrer(function, columnDbName, relatedColumnDbName,
                correlatedFixedCondition, option);

        // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 
        // is null or null-revived conversion (coalesce) -> no way to be inner
        // 
        // for example, the following SQL is no way to be inner
        // (suppose if PURCHASE refers WITHDRAWAL)
        // 
        // select mb.MEMBER_ID, mb.MEMBER_NAME
        //      , mb.MEMBER_STATUS_CODE, wd.MEMBER_ID as WD_MEMBER_ID
        //   from MEMBER mb
        //     left outer join MEMBER_WITHDRAWAL wd on mb.MEMBER_ID = wd.MEMBER_ID
        //  where (select max(pc.PURCHASE_PRICE)
        //           from PURCHASE pc
        //          where pc.MEMBER_ID = wd.MEMBER_ID -- may null
        //        ) is null
        //  order by mb.MEMBER_ID
        // 
        // and using coalesce means it may select records that have null value
        // so using coalesce is no way in spite of operand
        // = = = = = = = = = =/
        final boolean noWayInner = HpQDRParameter.isOperandIsNull(operand) || option.mayNullRevived();
        registerWhereClause(clause, noWayInner);
    }

    protected void registerQueryMyselfDerived(String function, ConditionQuery subQuery, String columnDbName,
            String relatedColumnDbName, String propertyName, String referrerPropertyName, String operand, Object value,
            String parameterPropertyName, DerivedReferrerOption option) {
        doRegisterQueryDerivedReferrer(function, subQuery, columnDbName, relatedColumnDbName, propertyName,
                referrerPropertyName, operand, value, parameterPropertyName, resolveMyselfDerivedReferrerOption(option));
    }

    // type argument for cast
    protected <CB extends ConditionBean> HpQDRFunction<CB> xcreateQDRFunctionMyselfDerived(Class<CB> cbType) {
        return new HpQDRFunction<CB>(new HpQDRSetupper<CB>() {
            public void setup(String fn, SubQuery<CB> sq, String rd, Object vl, DerivedReferrerOption op) {
                xqderiveMyselfDerived(fn, sq, rd, vl, op);
            }
        });
    }

    protected <CB extends ConditionBean> void xqderiveMyselfDerived(String fn, SubQuery<CB> sq, String rd, Object vl,
            DerivedReferrerOption op) {
        // overridden by sub-class (not abstract for suppressing option)
    }

    // [DBFlute-0.8.8]
    // -----------------------------------------------------
    //                                       ScalarCondition
    //                                       ---------------
    protected <CB extends ConditionBean> void registerScalarCondition(final String function,
            final ConditionQuery subQuery, String propertyName, String operand, final HpSSQOption<CB> option) {
        assertSubQueryNotNull("ScalarCondition", propertyName, subQuery);
        final SubQueryPath subQueryPath = new SubQueryPath(xgetLocation(propertyName));
        final GeneralColumnRealNameProvider localRealNameProvider = new GeneralColumnRealNameProvider();
        final int subQueryLevel = subQuery.xgetSqlClause().getSubQueryLevel();
        final SqlClause subQueryClause = subQuery.xgetSqlClause();
        final String subQueryIdentity = propertyName + "[" + subQueryLevel + "]";
        final ColumnSqlNameProvider subQuerySqlNameProvider = new ColumnSqlNameProvider() {
            public ColumnSqlName provide(String columnDbName) {
                return subQuery.toColumnSqlName(columnDbName);
            }
        };
        final DBMeta subQueryDBMeta = findDBMeta(subQuery.getTableDbName());
        final GearedCipherManager cipherManager = xgetSqlClause().getGearedCipherManager();
        final String mainSubQueryIdentity = propertyName + "[" + subQueryLevel + ":subquerymain]";
        final PartitionByProvider partitionByProvider = new ScalarCondition.PartitionByProvider() {
            public SqlClause provideSqlClause() {
                return option.preparePartitionBySqlClause();
            }
        };
        final ScalarCondition scalarCondition = new ScalarCondition(subQueryPath, localRealNameProvider,
                subQuerySqlNameProvider, subQueryLevel, subQueryClause, subQueryIdentity, subQueryDBMeta,
                cipherManager, mainSubQueryIdentity, operand, partitionByProvider);
        final QueryClause clause = new QueryClause() { // lazy registration to use partition-by
            public String toString() {
                return scalarCondition.buildScalarCondition(function);
            }
        };
        // no speak about inner-join because of no possible of null revival
        final QueryUsedAliasInfo usedAliasInfo = new QueryUsedAliasInfo(xgetAliasName(), null);
        registerWhereClause(clause, usedAliasInfo);
    }

    protected <CB extends ConditionBean> HpSSQFunction<CB> xcreateSSQFunction(ConditionKey ckey, Class<CB> tp) { // type for cast
        return xcreateSSQFunction(ckey.getOperand(), tp); // delegate to string operand, also for compatible
    }

    protected <CB extends ConditionBean> HpSSQFunction<CB> xcreateSSQFunction(final String rd, Class<CB> tp) {
        return new HpSSQFunction<CB>(new HpSSQSetupper<CB>() {
            public void setup(String fn, SubQuery<CB> sq, HpSSQOption<CB> op) {
                xscalarCondition(fn, sq, rd, op);
            }
        });
    }

    protected <CB extends ConditionBean> void xscalarCondition(String fn, SubQuery<CB> sq, String rd, HpSSQOption<CB> op) {
        // overridden by sub-class (not abstract for suppressing option)
    }

    // -----------------------------------------------------
    //                                          MyselfExists
    //                                          ------------
    protected void registerMyselfExists(ConditionQuery subQuery, String subQueryPropertyName) {
        final String relatedColumnDbName;
        {
            subQuery.xgetSqlClause().getSpecifiedColumnInfoAsOne();
            final String specifiedDbName = subQuery.xgetSqlClause().getSpecifiedColumnDbNameAsOne();
            if (specifiedDbName != null) {
                relatedColumnDbName = specifiedDbName;
            } else { // as default
                // this function is only allowed when only-one PK
                final UniqueInfo primaryUniqueInfo = findDBMeta(subQuery.getTableDbName()).getPrimaryUniqueInfo();
                final ColumnInfo primaryColumnInfo = primaryUniqueInfo.getFirstColumn();
                relatedColumnDbName = primaryColumnInfo.getColumnDbName();
            }
        }
        registerExistsReferrer(subQuery, relatedColumnDbName, relatedColumnDbName, subQueryPropertyName, null);
    }

    // -----------------------------------------------------
    //                                         MyselfInScope
    //                                         -------------
    protected void registerMyselfInScope(ConditionQuery subQuery, String subQueryPropertyName) {
        final String relatedColumnDbName;
        {
            final String specifiedDbName = subQuery.xgetSqlClause().getSpecifiedColumnDbNameAsOne();
            if (specifiedDbName != null) {
                relatedColumnDbName = specifiedDbName;
            } else { // as default
                // this function is only allowed when only-one PK
                final UniqueInfo primaryUniqueInfo = findDBMeta(subQuery.getTableDbName()).getPrimaryUniqueInfo();
                final ColumnInfo primaryColumnInfo = primaryUniqueInfo.getFirstColumn();
                relatedColumnDbName = primaryColumnInfo.getColumnDbName();
            }
        }
        registerInScopeRelation(subQuery, relatedColumnDbName, relatedColumnDbName, subQueryPropertyName, null);
    }

    // -----------------------------------------------------
    //                                       SubQuery Common
    //                                       ---------------
    protected class GeneralColumnRealNameProvider implements ColumnRealNameProvider {
        public ColumnRealName provide(String columnDbName) {
            return toColumnRealName(columnDbName);
        }
    }

    // these assertions are basically for internal
    protected void assertSubQueryNotNull(String title, String columnDbName, ConditionQuery subQuery) {
        if (subQuery == null) {
            String msg = "The condition-query for the sub-query should not be null:";
            msg = msg + " " + title + "(" + columnDbName + ")";
            throw new IllegalStateException(msg);
        }
    }

    protected void assertFunctionNotNull(String title, String columnDbName, String function) {
        if (function == null) {
            String msg = "The function for the sub-query should not be null:";
            msg = msg + " " + title + "(" + columnDbName + ")";
            throw new IllegalStateException(msg);
        }
    }

    // -----------------------------------------------------
    //                                          Where Clause
    //                                          ------------
    protected void setupConditionValueAndRegisterWhereClause(ConditionKey key, Object value, ConditionValue cvalue,
            String columnDbName) {
        final ConditionOption embeddedOption = createEmbeddedOption(key, value, cvalue, columnDbName);
        setupConditionValueAndRegisterWhereClause(key, value, cvalue, columnDbName, embeddedOption);
    }

    protected ConditionOption createEmbeddedOption(ConditionKey key, Object value, ConditionValue cvalue,
            String columnDbName) { // to override (closet option)
        return null;
    }

    protected void setupConditionValueAndRegisterWhereClause(ConditionKey key, Object value, ConditionValue cvalue,
            String columnDbName, ConditionOption option) {
        final DBMeta dbmeta = xgetLocalDBMeta();
        final ColumnInfo columnInfo = dbmeta.findColumnInfo(columnDbName);
        final String propertyName = columnInfo.getPropertyName();
        final String uncapPropName = initUncap(propertyName);
        // if Java, it is necessary to use uncapPropName
        final String location = xgetLocation(uncapPropName);
        key.setupConditionValue(xcreateQueryModeProvider(), cvalue, value, location, option);
        final ColumnRealName columnRealName = toColumnRealName(columnDbName);
        final ColumnFunctionCipher cipher = xgetSqlClause().findColumnFunctionCipher(columnInfo);
        final String usedAliasName = xgetAliasName();
        xgetSqlClause().registerWhereClause(columnRealName, key, cvalue, cipher, option, usedAliasName);
    }

    protected void registerWhereClause(String whereClause) {
        registerWhereClause(whereClause, false);
    }

    protected void registerWhereClause(String whereClause, boolean noWayInner) {
        final String usedAliasName = xgetAliasName();
        xgetSqlClause().registerWhereClause(whereClause, usedAliasName, noWayInner);
    }

    protected void registerWhereClause(QueryClause whereClause, QueryUsedAliasInfo... usedAliasInfos) {
        xgetSqlClause().registerWhereClause(whereClause, usedAliasInfos);
    }

    protected void registerInlineWhereClause(String whereClause) {
        if (isBaseQuery()) {
            xgetSqlClause().registerBaseTableInlineWhereClause(whereClause);
        } else {
            xgetSqlClause().registerOuterJoinInlineWhereClause(xgetAliasName(), whereClause, _onClause);
        }
    }

    // -----------------------------------------------------
    //                                           Union Query
    //                                           -----------
    protected void registerUnionQuery(final ConditionQuery unionQuery, boolean unionAll,
            final String unionQueryPropertyName) {
        xgetSqlClause().registerUnionQuery(new UnionClauseProvider() {
            public String provide() {
                return xgetUnionQuerySql(unionQuery, unionQueryPropertyName);
            }
        }, unionAll);
    }

    protected String xgetUnionQuerySql(ConditionQuery unionQuery, String unionQueryPropertyName) {
        final String fromClause = unionQuery.xgetSqlClause().getFromClause();
        final String whereClause = unionQuery.xgetSqlClause().getWhereClause();
        final String unionQueryClause;
        if (whereClause.trim().length() <= 0) {
            unionQueryClause = fromClause + " " + xgetSqlClause().getUnionWhereClauseMark();
        } else {
            final int whereIndex = whereClause.indexOf("where ");
            if (whereIndex < 0) {
                String msg = "The whereClause should have 'where' string: " + whereClause;
                throw new IllegalStateException(msg);
            }
            final int clauseIndex = whereIndex + "where ".length();
            final String front = whereClause.substring(0, clauseIndex);
            final String mark = xgetSqlClause().getUnionWhereFirstConditionMark();
            final String rear = whereClause.substring(clauseIndex);
            final String markedClause = front + mark + rear;
            unionQueryClause = fromClause + " " + markedClause;
        }
        final String oldStr = "/*pmb.conditionQuery.";
        final String newStr = "/*pmb.conditionQuery." + unionQueryPropertyName + ".";
        return replaceString(unionQueryClause, oldStr, newStr);
    }

    // -----------------------------------------------------
    //                                            Inner Join
    //                                            ----------
    /**
     * Change the join type for this relation to inner join. <br />
     * This method is for PERFORMANCE TUNING basically.
     */
    public void innerJoin() {
        if (isBaseQuery()) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("The method 'innerJoin()' should be called for a relation query.");
            br.addItem("Advice");
            br.addElement("Please confirm your program.");
            br.addElement("For example:");
            br.addElement("  (x) - cb.query().innerJoin();");
            br.addElement("  (o) - cb.query().queryMemberStatus().innerJoin();");
            br.addItem("Base Table");
            br.addElement(getTableDbName());
            final String msg = br.buildExceptionMessage();
            throw new IllegalConditionBeanOperationException(msg);
        }
        xgetSqlClause().changeToInnerJoin(xgetAliasName());
    }

    // ===================================================================================
    //                                                                            Order By
    //                                                                            ========
    protected void registerOrderBy(String columnDbName, boolean ascOrDesc) {
        final DBMeta dbmeta = xgetLocalDBMeta();
        final ColumnInfo columnInfo = dbmeta.findColumnInfo(columnDbName);
        final ColumnRealName columnRealName = toColumnRealName(columnInfo);
        xgetSqlClause().registerOrderBy(columnRealName.toString(), ascOrDesc, columnInfo);
    }

    protected void regOBA(String columnDbName) {
        assertOrderByPurpose(columnDbName);
        registerOrderBy(columnDbName, true);
    }

    protected void regOBD(String columnDbName) {
        assertOrderByPurpose(columnDbName);
        registerOrderBy(columnDbName, false);
    }

    protected void assertOrderByPurpose(String columnDbName) {
        if (xgetSqlClause().getPurpose().isNoOrderBy()) {
            throwOrderByIllegalPurposeException(columnDbName);
        }
    }

    protected void throwOrderByIllegalPurposeException(String columnDbName) {
        createCBExThrower().throwOrderByIllegalPurposeException(xgetSqlClause().getPurpose(), xgetBaseCB(),
                getTableDbName(), columnDbName);
    }

    /**
     * Order with the keyword 'nulls first'.
     * <pre>
     * MemberCB cb = new MemberCB();
     * cb.query().addOrderBy_Birthdate_Asc().<span style="color: #DD4747">withNullsFirst()</span>;
     * <span style="color: #3F7E5E">// order by BIRTHDATE asc nulls first</span>
     * </pre>
     */
    public void withNullsFirst() { // is user public!
        xgetSqlClause().addNullsFirstToPreviousOrderBy();
    }

    /**
     * Order with the keyword 'nulls last'.
     * <pre>
     * MemberCB cb = new MemberCB();
     * cb.query().addOrderBy_Birthdate_Asc().<span style="color: #DD4747">withNullsLast()</span>;
     * <span style="color: #3F7E5E">// order by BIRTHDATE asc nulls last</span>
     * </pre>
     */
    public void withNullsLast() { // is user public!
        xgetSqlClause().addNullsLastToPreviousOrderBy();
    }

    protected void xdoWithManualOrder(ManualOrderBean mob) {
        assertObjectNotNull("withManualOrder(mob)", mob);
        final OrderByElement lastElement = xgetSqlClause().getOrderByLastElement();
        xcheckManualOrderState(mob, lastElement);
        xcheckManualOrderUnique(mob, lastElement);
        mob.bind(new HpManualOrderThemeListHandler() {
            public String register(String themeKey, Object orderValue) {
                return xregisterManualOrderParameterToThemeList(themeKey, orderValue);
            }
        });
        if (mob.hasOrderByCalculation()) {
            final ConditionBean dreamCruiseCB = xgetBaseCB().xcreateDreamCruiseCB();
            dreamCruiseCB.overTheWaves(xcreateManualOrderSpecifiedColumn(dreamCruiseCB));
            mob.xinitOrderByCalculation(xgetBaseCB(), dreamCruiseCB);
        }
        mob.validate();
        xgetSqlClause().addManualOrderToPreviousOrderByElement(mob);
    }

    protected void xcheckManualOrderState(ManualOrderBean mob, final OrderByElement lastElement) {
        if (lastElement == null) {
            createCBExThrower().throwManualOrderNotFoundOrderByException(_baseCB, mob);
        }
    }

    protected void xcheckManualOrderUnique(ManualOrderBean mob, final OrderByElement lastElement) {
        final List<OrderByElement> orderByList = xgetSqlClause().getOrderByComponent().getOrderByList();
        for (OrderByElement existingOrder : orderByList) {
            final ManualOrderBean existingMob = existingOrder.getManualOrderBean();
            if (existingMob != null && existingMob.equals(mob)) { // the bean already exists
                createCBExThrower().throwManualOrderSameBeanAlreadyExistsException(_baseCB, existingMob, existingOrder,
                        mob, lastElement);
            }
        }
    }

    protected HpSpecifiedColumn xcreateManualOrderSpecifiedColumn(ConditionBean dreamCruiseCB) {
        final OrderByElement orderByLastElement = xgetSqlClause().getOrderByLastElement();
        final String aliasName = orderByLastElement.getAliasName();
        final String columnName = orderByLastElement.getColumnName();
        final ColumnInfo columnInfo = orderByLastElement.getColumnInfo();
        final boolean derived = orderByLastElement.isDerivedOrderBy();
        return new HpSpecifiedColumn(aliasName, columnInfo, dreamCruiseCB, columnName, derived);
    }

    protected void registerSpecifiedDerivedOrderBy_Asc(String aliasName) {
        if (!xgetSqlClause().hasSpecifiedDerivingSubQuery(aliasName)) {
            throwSpecifiedDerivedOrderByAliasNameNotFoundException(aliasName);
        }
        xgetSqlClause().registerSpecifiedDerivedOrderBy(aliasName, true);
    }

    protected void registerSpecifiedDerivedOrderBy_Desc(String aliasName) {
        if (!xgetSqlClause().hasSpecifiedDerivingSubQuery(aliasName)) {
            throwSpecifiedDerivedOrderByAliasNameNotFoundException(aliasName);
        }
        xgetSqlClause().registerSpecifiedDerivedOrderBy(aliasName, false);
    }

    protected void throwSpecifiedDerivedOrderByAliasNameNotFoundException(String aliasName) {
        createCBExThrower().throwSpecifiedDerivedOrderByAliasNameNotFoundException(aliasName);
    }

    // ===================================================================================
    //                                                                       Name Resolver
    //                                                                       =============
    /**
     * Resolve relation no.
     * @param localTableName The name of local table. (NotNull)
     * @param foreignPropertyName The property name of foreign relation. (NotNull)
     * @return The resolved relation No.
     */
    protected String resolveNextRelationPath(String localTableName, String foreignPropertyName) {
        final int relationNo = xgetSqlClause().resolveRelationNo(localTableName, foreignPropertyName);
        String nextRelationPath = SqlClause.RELATION_PATH_DELIMITER + relationNo;
        final String relationPath = xgetRelationPath();
        if (relationPath != null) {
            nextRelationPath = relationPath + nextRelationPath;
        }
        return nextRelationPath;
    }

    protected String xresolveNRP(String localTableName, String foreignPropertyName) { // for generated source
        return resolveNextRelationPath(localTableName, foreignPropertyName);
    }

    /**
     * Resolve alias name for join table.
     * @param relationPath Relation path. (NotNull)
     * @param nestLevel The nest No of condition query.
     * @return The resolved name. (NotNull)
     */
    protected String resolveJoinAliasName(String relationPath, int nestLevel) {
        return xgetSqlClause().resolveJoinAliasName(relationPath, nestLevel);
    }

    protected String xresolveJAN(String relationPath, int nestLevel) { // for generated source
        return resolveJoinAliasName(relationPath, nestLevel);
    }

    // ===================================================================================
    //                                                                 Reflection Invoking
    //                                                                 ===================
    /**
     * {@inheritDoc}
     */
    public ConditionValue invokeValue(String columnFlexibleName) {
        assertStringNotNullAndNotTrimmedEmpty("columnFlexibleName", columnFlexibleName);
        final DBMeta dbmeta = xgetLocalDBMeta();
        final String columnCapPropName = initCap(dbmeta.findColumnInfo(columnFlexibleName).getPropertyName());
        final String methodName = "get" + columnCapPropName;
        final Method method = xhelpGettingCQMethod(this, methodName, (Class<?>[]) null);
        if (method == null) {
            throwConditionInvokingGetMethodNotFoundException(columnFlexibleName, methodName);
            return null; // unreachable
        }
        try {
            return (ConditionValue) xhelpInvokingCQMethod(this, method, (Object[]) null);
        } catch (ReflectionFailureException e) {
            throwConditionInvokingGetReflectionFailureException(columnFlexibleName, methodName, e);
            return null; // unreachable
        }
    }

    protected void throwConditionInvokingGetMethodNotFoundException(String columnFlexibleName, String methodName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the method for getting the condition.");
        br.addItem("columnFlexibleName");
        br.addElement(columnFlexibleName);
        br.addItem("methodName");
        br.addElement(methodName);
        final String msg = br.buildExceptionMessage();
        throw new ConditionInvokingFailureException(msg);
    }

    protected void throwConditionInvokingGetReflectionFailureException(String columnFlexibleName, String methodName,
            ReflectionFailureException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to invoke the method for getting value.");
        br.addItem("columnFlexibleName");
        br.addElement(columnFlexibleName);
        br.addItem("methodName");
        br.addElement(methodName);
        final String msg = br.buildExceptionMessage();
        throw new ConditionInvokingFailureException(msg, e);
    }

    /**
     * {@inheritDoc}
     */
    public void invokeQuery(String columnFlexibleName, String conditionKeyName, Object conditionValue) {
        doInvokeQuery(columnFlexibleName, conditionKeyName, conditionValue, null);
    }

    /**
     * {@inheritDoc}
     */
    public void invokeQuery(String columnFlexibleName, String conditionKeyName, Object conditionValue,
            ConditionOption conditionOption) {
        assertObjectNotNull("conditionOption", conditionOption);
        doInvokeQuery(columnFlexibleName, conditionKeyName, conditionValue, conditionOption);
    }

    protected void doInvokeQuery(String colName, String ckey, Object value, ConditionOption option) {
        assertStringNotNullAndNotTrimmedEmpty("columnFlexibleName", colName);
        assertStringNotNullAndNotTrimmedEmpty("conditionKeyName", ckey);
        if (value == null) {
            return; // do nothing if the value is null when the key has arguments
        }
        final PropertyNameCQContainer container = xhelpExtractingPropertyNameCQContainer(colName);
        final String flexibleName = container.getFlexibleName();
        final ConditionQuery cq = container.getConditionQuery();
        final DBMeta dbmeta = findDBMeta(cq.getTableDbName());
        final ColumnInfo columnInfo;
        try {
            columnInfo = dbmeta.findColumnInfo(flexibleName);
        } catch (RuntimeException e) {
            throwConditionInvokingColumnFindFailureException(colName, ckey, value, option, e);
            return; // unreachable (to avoid compile error)
        }
        final String columnCapPropName = initCap(columnInfo.getPropertyName());
        final boolean noArg = Srl.equalsIgnoreCase(ckey, "IsNull", "IsNotNull", "IsNullOrEmpty", "EmptyString");
        final boolean rangeOf = Srl.equalsIgnoreCase(ckey, "RangeOf");
        final boolean fromTo = Srl.equalsIgnoreCase(ckey, "FromTo", "DateFromTo");
        if (!noArg) {
            try {
                value = columnInfo.convertToObjectNativeType(value); // convert type
            } catch (RuntimeException e) {
                throwConditionInvokingValueConvertFailureException(colName, ckey, value, option, e);
            }
        }
        final String methodName = "set" + columnCapPropName + "_" + initCap(ckey);
        final List<Class<?>> typeList = newArrayListSized(4);
        if (fromTo) {
            typeList.add(Date.class);
            typeList.add(Date.class);
        } else if (rangeOf) {
            final Class<?> propertyType = columnInfo.getObjectNativeType();
            typeList.add(propertyType);
            typeList.add(propertyType);
        } else {
            if (!noArg) {
                typeList.add(value.getClass());
            }
        }
        if (option != null) {
            typeList.add(option.getClass());
        }
        final List<Class<?>> filteredTypeList = newArrayListSized(typeList.size());
        for (Class<?> parameterType : typeList) {
            filteredTypeList.add(xfilterInvokeQueryParameterType(colName, ckey, parameterType));
        }
        final Class<?>[] parameterTypes = filteredTypeList.toArray(new Class<?>[filteredTypeList.size()]);
        final Method method = xhelpGettingCQMethod(cq, methodName, parameterTypes);
        if (method == null) {
            throwConditionInvokingSetMethodNotFoundException(colName, ckey, value, option, methodName, parameterTypes);
        }
        try {
            final List<Object> argList = newArrayList();
            if (fromTo || rangeOf) {
                if (!(value instanceof List<?>)) { // check type
                    throwConditionInvokingDateFromToValueInvalidException(colName, ckey, value, option, methodName,
                            parameterTypes);
                }
                argList.addAll((List<?>) value);
            } else {
                if (!noArg) {
                    argList.add(value);
                }
            }
            if (option != null) {
                argList.add(option);
            }
            final List<Object> filteredArgList = newArrayListSized(argList.size());
            for (Object arg : argList) {
                filteredArgList.add(xfilterInvokeQueryParameterValue(colName, ckey, arg));
            }
            xhelpInvokingCQMethod(cq, method, filteredArgList.toArray());
        } catch (ReflectionFailureException e) {
            throwConditionInvokingSetReflectionFailureException(colName, ckey, value, option, methodName,
                    parameterTypes, e);
        }
    }

    protected Class<?> xfilterInvokeQueryParameterType(String colName, String ckey, Class<?> parameterType) {
        return parameterType; // no filter as default (e.g. overridden by Scala to convert to immutable list)
    }

    protected Object xfilterInvokeQueryParameterValue(String colName, String ckey, Object parameterValue) {
        return parameterValue; // no filter as default (e.g. overridden by Scala to convert to immutable list)
    }

    protected void throwConditionInvokingColumnFindFailureException(String columnFlexibleName, String conditionKeyName,
            Object conditionValue, ConditionOption conditionOption, RuntimeException cause) {
        final String notice = "Failed to find the column in the table.";
        doThrowConditionInvokingFailureException(notice, columnFlexibleName, conditionKeyName, conditionValue,
                conditionOption, null, null, cause);
    }

    protected void throwConditionInvokingValueConvertFailureException(String columnFlexibleName,
            String conditionKeyName, Object conditionValue, ConditionOption conditionOption, RuntimeException cause) {
        final String notice = "Failed to convert the value to property type.";
        doThrowConditionInvokingFailureException(notice, columnFlexibleName, conditionKeyName, conditionValue,
                conditionOption, null, null, cause);
    }

    protected void throwConditionInvokingSetMethodNotFoundException(String columnFlexibleName, String conditionKeyName,
            Object conditionValue, ConditionOption conditionOption, String methodName, Class<?>[] parameterTypes) {
        final String notice = "Not found the method for setting the condition.";
        doThrowConditionInvokingFailureException(notice, columnFlexibleName, conditionKeyName, conditionValue,
                conditionOption, methodName, parameterTypes, null);
    }

    protected void throwConditionInvokingDateFromToValueInvalidException(String columnFlexibleName,
            String conditionKeyName, Object conditionValue, ConditionOption conditionOption, String methodName,
            Class<?>[] parameterTypes) {
        final String notice = "The conditionValue should be List that has 2 elements, fromDate and toDate.";
        doThrowConditionInvokingFailureException(notice, columnFlexibleName, conditionKeyName, conditionValue,
                conditionOption, methodName, parameterTypes, null);
    }

    protected void throwConditionInvokingSetReflectionFailureException(String columnFlexibleName,
            String conditionKeyName, Object conditionValue, ConditionOption conditionOption, String methodName,
            Class<?>[] parameterTypes, ReflectionFailureException cause) {
        final String notice = "Failed to invoke the method for setting the condition.";
        doThrowConditionInvokingFailureException(notice, columnFlexibleName, conditionKeyName, conditionValue,
                conditionOption, methodName, parameterTypes, cause);
    }

    protected void doThrowConditionInvokingFailureException(String notice, String columnFlexibleName,
            String conditionKeyName, Object conditionValue, ConditionOption conditionOption, String methodName,
            Class<?>[] parameterTypes, RuntimeException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("Table");
        br.addElement(getTableDbName());
        br.addItem("columnFlexibleName");
        br.addElement(columnFlexibleName);
        br.addItem("conditionKeyName");
        br.addElement(conditionKeyName);
        br.addItem("conditionValue");
        br.addElement(conditionValue);
        br.addElement(conditionValue != null ? conditionValue.getClass() : null);
        br.addItem("conditionOption");
        br.addElement(conditionOption);
        if (methodName != null) {
            final StringBuilder sb = new StringBuilder();
            if (parameterTypes != null) {
                int index = 0;
                for (Class<?> parameterType : parameterTypes) {
                    if (index > 0) {
                        sb.append(", ");
                    }
                    sb.append(DfTypeUtil.toClassTitle(parameterType));
                    ++index;
                }
            }
            br.addItem("Method");
            br.addElement(methodName + "(" + sb.toString() + ")");
        }
        final String msg = br.buildExceptionMessage();
        if (cause != null) {
            throw new ConditionInvokingFailureException(msg, cause);
        } else {
            throw new ConditionInvokingFailureException(msg);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void invokeQueryEqual(String columnFlexibleName, Object value) {
        invokeQuery(columnFlexibleName, CK_EQ.getConditionKey(), value);
    }

    /**
     * {@inheritDoc}
     */
    public void invokeOrderBy(String columnFlexibleName, boolean isAsc) {
        assertStringNotNullAndNotTrimmedEmpty("columnFlexibleName", columnFlexibleName);
        final PropertyNameCQContainer container = xhelpExtractingPropertyNameCQContainer(columnFlexibleName);
        final String flexibleName = container.getFlexibleName();
        final ConditionQuery cq = container.getConditionQuery();
        final String ascDesc = isAsc ? "Asc" : "Desc";
        final DBMeta dbmeta = findDBMeta(cq.getTableDbName());
        final String columnCapPropName = initCap(dbmeta.findColumnInfo(flexibleName).getPropertyName());
        final String methodName = "addOrderBy_" + columnCapPropName + "_" + ascDesc;
        final Method method = xhelpGettingCQMethod(cq, methodName, (Class<?>[]) null);
        if (method == null) {
            throwConditionInvokingOrderMethodNotFoundException(columnFlexibleName, isAsc, methodName);
        }
        try {
            xhelpInvokingCQMethod(cq, method, (Object[]) null);
        } catch (ReflectionFailureException e) {
            throwConditionInvokingOrderReflectionFailureException(columnFlexibleName, isAsc, methodName, e);
        }
    }

    protected void throwConditionInvokingOrderMethodNotFoundException(String columnFlexibleName, boolean isAsc,
            String methodName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the method for adding the order-by condition.");
        br.addItem("Table");
        br.addElement(getTableDbName());
        br.addItem("columnFlexibleName");
        br.addElement(columnFlexibleName);
        br.addItem("isAsc");
        br.addElement(isAsc);
        br.addItem("Method");
        br.addElement(methodName);
        final String msg = br.buildExceptionMessage();
        throw new ConditionInvokingFailureException(msg);
    }

    protected void throwConditionInvokingOrderReflectionFailureException(String columnFlexibleName, boolean isAsc,
            String methodName, ReflectionFailureException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to invoke the method for setting the order-by condition.");
        br.addItem("Table");
        br.addElement(getTableDbName());
        br.addItem("columnFlexibleName");
        br.addElement(columnFlexibleName);
        br.addItem("isAsc");
        br.addElement(isAsc);
        br.addItem("Method");
        br.addElement(methodName);
        final String msg = br.buildExceptionMessage();
        throw new ConditionInvokingFailureException(msg, cause);
    }

    /**
     * {@inheritDoc}
     */
    public ConditionQuery invokeForeignCQ(String foreignPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("foreignPropertyName", foreignPropertyName);
        final List<String> splitList = Srl.splitList(foreignPropertyName, ".");
        ConditionQuery foreignCQ = this;
        for (String elementName : splitList) {
            foreignCQ = doInvokeForeignCQ(foreignCQ, elementName);
        }
        return foreignCQ;
    }

    protected ConditionQuery doInvokeForeignCQ(ConditionQuery cq, String foreignPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("foreignPropertyName", foreignPropertyName);
        final String methodName = "query" + initCap(foreignPropertyName);
        final Method method = xhelpGettingCQMethod(cq, methodName, (Class<?>[]) null);
        if (method == null) {
            throwConditionInvokingForeignQueryMethodNotFoundException(cq, foreignPropertyName, methodName);
            return null; // unreachable
        }
        try {
            return (ConditionQuery) xhelpInvokingCQMethod(cq, method, (Object[]) null);
        } catch (ReflectionFailureException e) {
            throwConditionInvokingForeignQueryReflectionFailureException(cq, foreignPropertyName, methodName, e);
            return null; // unreachable
        }
    }

    protected void throwConditionInvokingForeignQueryMethodNotFoundException(ConditionQuery cq,
            String foreignPropertyName, String methodName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the method for getting a foreign condition query.");
        br.addItem("Table");
        br.addElement(getTableDbName());
        br.addItem("foreignPropertyName");
        br.addElement(foreignPropertyName);
        br.addItem("Method");
        br.addElement(methodName);
        final String msg = br.buildExceptionMessage();
        throw new ConditionInvokingFailureException(msg);
    }

    protected void throwConditionInvokingForeignQueryReflectionFailureException(ConditionQuery cq,
            String foreignPropertyName, String methodName, ReflectionFailureException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to invoke the method for setting a condition(query).");
        br.addItem("Table");
        br.addElement(getTableDbName());
        br.addItem("foreignPropertyName");
        br.addElement(foreignPropertyName);
        br.addItem("Method");
        br.addElement(methodName);
        final String msg = br.buildExceptionMessage();
        throw new ConditionInvokingFailureException(msg, cause);
    }

    /**
     * {@inheritDoc}
     */
    public boolean invokeHasForeignCQ(String foreignPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("foreignPropertyName", foreignPropertyName);
        final List<String> splitList = Srl.splitList(foreignPropertyName, ".");
        ConditionQuery foreignCQ = this;
        final int splitLength = splitList.size();
        int index = 0;
        for (String elementName : splitList) {
            if (!doInvokeHasForeignCQ(foreignCQ, elementName)) {
                return false;
            }
            if ((index + 1) < splitLength) { // last loop
                foreignCQ = foreignCQ.invokeForeignCQ(elementName);
            }
            ++index;
        }
        return true;
    }

    protected boolean doInvokeHasForeignCQ(ConditionQuery cq, String foreignPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("foreignPropertyName", foreignPropertyName);
        final String methodName = "hasConditionQuery" + initCap(foreignPropertyName);
        final Method method = xhelpGettingCQMethod(cq, methodName, (Class<?>[]) null);
        if (method == null) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Not found the method for determining a foreign condition query.");
            br.addItem("Table");
            br.addElement(cq.getTableDbName());
            br.addItem("foreignPropertyName");
            br.addElement(foreignPropertyName);
            br.addItem("methodName");
            br.addElement(methodName);
            br.addItem("ConditionQuery");
            br.addElement(DfTypeUtil.toClassTitle(cq));
            final String msg = br.buildExceptionMessage();
            throw new ConditionInvokingFailureException(msg);
        }
        try {
            return (Boolean) xhelpInvokingCQMethod(cq, method, (Object[]) null);
        } catch (ReflectionFailureException e) {
            String msg = "Failed to invoke the method for determining a condition(query):";
            msg = msg + " foreignPropertyName=" + foreignPropertyName;
            msg = msg + " methodName=" + methodName + " table=" + getTableDbName();
            throw new ConditionInvokingFailureException(msg, e);
        }
    }

    protected PropertyNameCQContainer xhelpExtractingPropertyNameCQContainer(String name) {
        final String[] strings = name.split("\\.");
        final int length = strings.length;
        String propertyName = null;
        ConditionQuery cq = this;
        int index = 0;
        for (String element : strings) {
            if (length == (index + 1)) { // at last loop!
                propertyName = element;
                break;
            }
            cq = cq.invokeForeignCQ(element);
            ++index;
        }
        return new PropertyNameCQContainer(propertyName, cq);
    }

    protected static class PropertyNameCQContainer {
        protected String _flexibleName;
        protected ConditionQuery _cq;

        public PropertyNameCQContainer(String flexibleName, ConditionQuery cq) {
            this._flexibleName = flexibleName;
            this._cq = cq;
        }

        public String getFlexibleName() {
            return _flexibleName;
        }

        public ConditionQuery getConditionQuery() {
            return _cq;
        }
    }

    protected Method xhelpGettingCQMethod(ConditionQuery cq, String methodName, Class<?>[] argTypes) {
        final Class<? extends ConditionQuery> cqType = cq.getClass();
        final DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc(cqType);
        final Method found = beanDesc.getMethodNoException(methodName, argTypes);
        if (found != null) {
            return found;
        }
        return DfReflectionUtil.getWholeMethod(cqType, methodName, argTypes); // e.g. native method of classification
    }

    protected Object xhelpInvokingCQMethod(ConditionQuery cq, Method method, Object[] args) {
        return DfReflectionUtil.invokeForcedly(method, cq, args);
    }

    // ===================================================================================
    //                                                                     Condition Value
    //                                                                     ===============
    protected ConditionValue nCV() {
        return newConditionValue();
    }

    protected ConditionValue newConditionValue() {
        return new ConditionValue();
    }

    // ===================================================================================
    //                                                                        Filter Value
    //                                                                        ============
    /**
     * Delegate to filterRemoveEmptyString(). {Internal}
     * @param value The string value for query. (NullAllowed)
     * @return Filtered value. (NullAllowed)
     */
    protected String fRES(String value) {
        return filterRemoveEmptyString(value);
    }

    /**
     * Filter removing an empty string as null. <br />
     * You can extend this to use an empty string value as condition.
     * @param value The string value for query. (NullAllowed)
     * @return Filtered value. (NullAllowed)
     */
    protected String filterRemoveEmptyString(String value) {
        if (isEmptyStringQueryAllowed()) {
            return value;
        }
        return ((value != null && !"".equals(value)) ? value : null);
    }

    /**
     * Does it allowed an empty string to set for query?
     * @return The determination, true or false.
     */
    protected boolean isEmptyStringQueryAllowed() {
        return xgetSqlClause().isEmptyStringQueryEnabled();
    }

    /**
     * Delegate to filterConvertToPureDate().
     * @param date The instance of date for query. (NullAllowed)
     * @return Filtered date. (NullAllowed)
     */
    protected java.util.Date fCTPD(java.util.Date date) {
        return filterConvertToPureDate(date);
    }

    /**
     * Filter converting the date to a pure date.
     * @param date The instance of date for query. (NullAllowed)
     * @return Filtered value. (NullAllowed)
     */
    protected java.util.Date filterConvertToPureDate(java.util.Date date) {
        return DfTypeUtil.toDate(date);
    }

    // ===================================================================================
    //                                                                       Create Option
    //                                                                       =============
    /**
     * create the option of like search as prefix search.
     * @return The option of like search as prefix search. (NotNull)
     */
    protected LikeSearchOption cLSOP() { // createLikeSearchOption
        return new LikeSearchOption().likePrefix();
    }

    // ===================================================================================
    //                                                                       Convert Value
    //                                                                       =============
    /**
     * @param obj The object of the property. (NullAllowed)
     * @param type The type instance of the property. (NullAllowed)
     * @param <PROPERTY> The type of property.
     * @return The number type result of the property. (NullAllowed: if null, returns null)
     */
    @SuppressWarnings("unchecked")
    protected <PROPERTY extends Number> PROPERTY cTNum(Object obj, Class<PROPERTY> type) { // convert to number
        return (PROPERTY) DfTypeUtil.toNumber(obj, type);
    }

    /**
     * @param col The collection of the property. (NullAllowed)
     * @param <PROPERTY> The type of property.
     * @return The list of the property. (NullAllowed: if null, returns null)
     */
    protected <PROPERTY> List<PROPERTY> cTL(Collection<PROPERTY> col) { // convert to list
        // though BindVariableNode (or Embedded) can accept Collection and filter it,
        // this process has been existed for a long time as traditional filter
        return xconvertToList(col);
    }

    protected List<String> cTStrL(Collection<? extends Classification> col) { // convert to string list
        if (col == null) {
            return null;
        }
        final List<String> list = new ArrayList<String>();
        for (Classification cls : col) {
            if (cls != null) {
                list.add(cls.code());
            }
        }
        return list;
    }

    protected <PROPERTY extends Number> List<PROPERTY> cTNumL(Collection<? extends Classification> col,
            Class<PROPERTY> type) { // convert to number list
        if (col == null) {
            return null;
        }
        final List<PROPERTY> list = new ArrayList<PROPERTY>();
        for (Classification cls : col) {
            if (cls != null) {
                @SuppressWarnings("unchecked")
                final PROPERTY value = (PROPERTY) DfTypeUtil.toNumber(cls.code(), type);
                list.add(value);
            }
        }
        return list;
    }

    /**
     * @param col The collection of property. (NullAllowed)
     * @param <PROPERTY> The type of property.
     * @return The list of the property. (NullAllowed: if null, returns null)
     */
    protected <PROPERTY> List<PROPERTY> xconvertToList(Collection<PROPERTY> col) {
        if (col == null) {
            return null;
        }
        if (col instanceof List<?>) {
            return xfilterRemoveNullOrEmptyValueFromList((List<PROPERTY>) col);
        }
        return xfilterRemoveNullOrEmptyValueFromList(new ArrayList<PROPERTY>(col));
    }

    protected <PROPERTY_TYPE> List<PROPERTY_TYPE> xfilterRemoveNullOrEmptyValueFromList(List<PROPERTY_TYPE> ls) {
        if (ls == null) {
            return null;
        }
        final List<PROPERTY_TYPE> newList = new ArrayList<PROPERTY_TYPE>();
        for (Iterator<PROPERTY_TYPE> ite = ls.iterator(); ite.hasNext();) {
            final PROPERTY_TYPE element = ite.next();
            if (element == null) {
                continue;
            }
            if (element instanceof String) {
                if (((String) element).length() == 0) {
                    continue;
                }
            }
            newList.add(element);
        }
        return newList;
    }

    // ===================================================================================
    //                                                                     Short Character
    //                                                                     ===============
    // handleShortChar()
    protected String hSC(String columnName, String value, Integer size, String modeCode) {
        final ShortCharHandlingMode mode = ShortCharHandlingMode.codeOf(modeCode);
        if (mode == null) {
            String msg = "The mode was not found by the code: ";
            msg = msg + " columnName=" + columnName + " modeCode=" + modeCode;
            throw new IllegalStateException(msg);
        }
        return ParameterUtil.handleShortChar(columnName, value, size, mode);
    }

    // ===================================================================================
    //                                                                    Full Text Search
    //                                                                    ================
    // -----------------------------------------------------
    //                                                 MySQL
    //                                                 -----
    protected void xdoMatchForMySQL(List<ColumnInfo> textColumnList, String conditionValue,
            WayOfMySQL.FullTextSearchModifier modifier) {
        if (conditionValue == null || conditionValue.length() == 0) {
            return; // ignored according to condition-bean rule
        }
        final String clause = ((SqlClauseMySql) xgetSqlClause()).buildMatchCondition(textColumnList, conditionValue,
                modifier, getTableDbName(), xgetAliasName());
        registerWhereClause(clause);
    }

    // -----------------------------------------------------
    //                                     PostgreSQL/Oracle
    //                                     -----------------
    protected void xdoMatchByLikeSearch(List<ColumnInfo> textColumnList, String conditionValue) {
        if (conditionValue == null || conditionValue.length() == 0) {
            return;
        }
        assertObjectNotNull("textColumnList", textColumnList);
        if (textColumnList.isEmpty()) {
            String msg = "The argument 'textColumnList' should not be empty list.";
            throw new IllegalArgumentException(msg);
        }
        conditionValue = xescapeFullTextSearchValue(conditionValue);
        xgetSqlClause().beginOrScopeQuery();
        try {
            for (ColumnInfo columnInfo : textColumnList) {
                if (columnInfo == null) {
                    continue;
                }
                final String tableOfColumn = columnInfo.getDBMeta().getTableDbName();
                if (!tableOfColumn.equalsIgnoreCase(getTableDbName())) {
                    String msg = "The table of the text column should be '" + getTableDbName() + "'";
                    msg = msg + " but the table is '" + tableOfColumn + "': column=" + columnInfo;
                    throw new IllegalArgumentException(msg);
                }
                if (!columnInfo.isObjectNativeTypeString()) {
                    String msg = "The text column should be String type:";
                    msg = msg + " column=" + columnInfo;
                    throw new IllegalArgumentException(msg);
                }
                invokeQueryLikeSearch(columnInfo.getColumnDbName(), conditionValue, xcreateMatchLikeSearch());
            }
        } finally {
            xgetSqlClause().endOrScopeQuery();
        }
    }

    protected String xescapeFullTextSearchValue(String conditionValue) {
        String msg = "You should override this method.";
        throw new UnsupportedOperationException(msg);
    }

    protected String xescapeOracleFullTextSearchValue(String conditionValue) {
        return ((SqlClauseOracle) xgetSqlClause()).escapeFullTextSearchValue(conditionValue);
    }

    protected LikeSearchOption xcreateMatchLikeSearch() {
        String msg = "You should override this method.";
        throw new UnsupportedOperationException(msg);
    }

    protected LikeSearchOption xcreatePostgreSQLMatchLikeSearch() {
        return new PostgreSQLMatchLikeSearch();
    }

    public class PostgreSQLMatchLikeSearch extends LikeSearchOption {
        @Override
        public ExtensionOperand getExtensionOperand() {
            return xgetPostgreSQLMatchOperand();
        }
    }

    protected ExtensionOperand xgetPostgreSQLMatchOperand() {
        String msg = "You should override this method.";
        throw new UnsupportedOperationException(msg);
    }

    protected LikeSearchOption xcreateOracleMatchLikeSearch() {
        return new OracleMatchLikeSearch();
    }

    public class OracleMatchLikeSearch extends LikeSearchOption {
        @Override
        public QueryClauseArranger getWhereClauseArranger() {
            return ((SqlClauseOracle) xgetSqlClause()).createFullTextSearchClauseArranger();
        }
    }

    // ===================================================================================
    //                                                                  ColumnQuery Object
    //                                                                  ==================
    /**
     * Get the condition-bean map of ColumnQuery for parameter comment. {Internal}. <br />
     * This is basically for (Specify)DerivedReferrer's bind conditions in ColumnQuery. <br />
     * The value is treated as Object type because this will be only called from parameter comment.
     * @return The instance of the map. (NullAllowed)
     */
    public Map<String, Object> getColQyCBMap() {
        return xgetSqlClause().getColumnQueryObjectMap();
    }

    protected String xregisterColumyQueryObjectToThemeList(String themeKey, Object addedValue) {
        return xgetSqlClause().registerColumnQueryObjectToThemeList(themeKey, addedValue);
    }

    // ===================================================================================
    //                                                               ManualOrder Parameter
    //                                                               =====================
    /**
     * Get the parameter map of ManualOrder for parameter comment. {Internal}.
     * @return The instance of the map. (NullAllowed)
     */
    public Map<String, Object> getMnuOdrPrmMap() {
        return xgetSqlClause().getManualOrderParameterMap();
    }

    protected String xregisterManualOrderParameterToThemeList(String themeKey, Object addedValue) {
        return xgetSqlClause().registerManualOrderParameterToThemeList(themeKey, addedValue);
    }

    // ===================================================================================
    //                                                                      Free Parameter
    //                                                                      ==============
    /**
     * Get the map of free parameter for parameter comment. {Internal}.
     * @return The instance of the map. (NullAllowed)
     */
    public Map<String, Object> getFreePrmMap() {
        return xgetSqlClause().getFreeParameterMap();
    }

    // 'public' modifier for versatility
    //  e.g. called by compound PK's LoadReferrer
    public String xregisterFreeParameterToThemeList(String themeKey, Object addedValue) {
        return xgetSqlClause().registerFreeParameterToThemeList(themeKey, addedValue);
    }

    // ===================================================================================
    //                                                                    Relation Keeping
    //                                                                    ================
    protected boolean xhasQueRlMap(String prop) {
        return _queryRelationKeepingMap != null && _queryRelationKeepingMap.containsKey(prop);
    }

    @SuppressWarnings("unchecked")
    protected <CQ extends ConditionQuery> CQ xgetQueRlMap(String prop) {
        return _queryRelationKeepingMap != null ? (CQ) _queryRelationKeepingMap.get(prop) : null;
    }

    protected void xregQueRl(String prop, ConditionQuery cq) {
        if (_queryRelationKeepingMap == null) {
            _queryRelationKeepingMap = newLinkedHashMapSized(4);
        }
        _queryRelationKeepingMap.put(prop, cq);
    }

    protected void xregOutJo(String prop) { // should be called after registration
        if (_queryRelationKeepingMap == null) { // no way, just in case
            _queryRelationKeepingMap = newLinkedHashMapSized(4);
        }
        ConditionQuery cq = _queryRelationKeepingMap.get(prop);
        if (cq == null) { // no way, just in case
            String msg = "Not found the condition-query for (Query)Relation: " + prop;
            throw new IllegalStateException(msg);
        }
        final ForeignInfo foreignInfo = xgetLocalDBMeta().findForeignInfo(prop);
        final Map<ColumnInfo, ColumnInfo> localForeignColMap = foreignInfo.getLocalForeignColumnInfoMap();
        final Map<String, String> joinOnMap = newLinkedHashMapSized(localForeignColMap.size());
        for (Entry<ColumnInfo, ColumnInfo> entry : localForeignColMap.entrySet()) {
            final ColumnInfo localCol = entry.getKey();
            final ColumnInfo foreignCol = entry.getValue();
            joinOnMap.put(localCol.getColumnDbName(), foreignCol.getColumnDbName());
        }
        registerOuterJoin(cq, joinOnMap, prop);
    }

    @SuppressWarnings("unchecked")
    protected <CQ extends ConditionQuery> Map<String, CQ> xgetSQueMap(String identity) {
        return _subQueryKeepingMap != null ? (Map<String, CQ>) _subQueryKeepingMap.get(identity) : null;
    }

    protected String xkeepSQue(String identity, ConditionQuery subQuery) {
        if (_subQueryKeepingMap == null) {
            _subQueryKeepingMap = newLinkedHashMapSized(4);
        }
        Map<String, ConditionQuery> sqMap = _subQueryKeepingMap.get(identity);
        if (sqMap == null) {
            sqMap = newLinkedHashMapSized(4);
            _subQueryKeepingMap.put(identity, sqMap);
        }
        final String key = "subQueryKey" + (sqMap.size() + 1);
        sqMap.put(key, subQuery);
        return identity + "." + key; // [property-name] + [.] + [key]
    }

    protected Map<String, Object> xgetSQuePmMap(String identity) {
        return _subQueryParameterKeepingMap != null ? _subQueryParameterKeepingMap.get(identity) : null;
    }

    protected String xkeepSQuePm(String identity, Object paramValue) {
        // identity same as condition-query keeping
        if (_subQueryParameterKeepingMap == null) {
            _subQueryParameterKeepingMap = newLinkedHashMapSized(4);
        }
        Map<String, Object> paramMap = _subQueryParameterKeepingMap.get(identity);
        if (paramMap == null) {
            paramMap = newLinkedHashMapSized(4);
            _subQueryParameterKeepingMap.put(identity, paramMap);
        }
        final String key = "subQueryParameterKey" + (paramMap.size() + 1);
        paramMap.put(key, paramValue);
        return identity + "Parameter." + key; // [property-name (needs suffix 'Parameter')] + [.] + [key]
    }

    // ===================================================================================
    //                                                                    Option Parameter
    //                                                                    ================
    public void xregisterParameterOption(ParameterOption option) {
        if (option == null) {
            return;
        }
        if (_parameterOptionMap == null) {
            _parameterOptionMap = newHashMap();
        }
        final String parameterKey = "option" + _parameterOptionMap.size();
        _parameterOptionMap.put(parameterKey, option);
        final String parameterMapPath = xgetLocationBase() + "optionParameterMap";
        option.acceptParameterKey(parameterKey, parameterMapPath);
    }

    public Map<String, ParameterOption> getOptionParameterMap() { // for parameter comment
        return _parameterOptionMap;
    }

    // ===================================================================================
    //                                                                        Purpose Type
    //                                                                        ============
    protected void lock() {
        xgetSqlClause().lock();
    }

    protected void unlock() {
        xgetSqlClause().unlock();
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
    // -----------------------------------------------------
    //                                                String
    //                                                ------
    protected final String replaceString(String text, String fromText, String toText) {
        return Srl.replace(text, fromText, toText);
    }

    protected String initCap(String str) {
        return Srl.initCap(str);
    }

    protected String initUncap(String str) {
        return Srl.initUncap(str);
    }

    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }

    // -----------------------------------------------------
    //                                            Collection
    //                                            ----------
    protected <KEY, VALUE> HashMap<KEY, VALUE> newHashMap() {
        return DfCollectionUtil.newHashMap();
    }

    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap() {
        return DfCollectionUtil.newLinkedHashMap();
    }

    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMapSized(int size) {
        return DfCollectionUtil.newLinkedHashMapSized(size);
    }

    protected <ELEMENT> ArrayList<ELEMENT> newArrayList() {
        return DfCollectionUtil.newArrayList();
    }

    protected <ELEMENT> ArrayList<ELEMENT> newArrayListSized(int size) {
        return DfCollectionUtil.newArrayListSized(size);
    }

    protected <ELEMENT> List<ELEMENT> newArrayList(ELEMENT... elements) {
        return DfCollectionUtil.newArrayList(elements);
    }

    protected <ELEMENT> ArrayList<ELEMENT> newArrayList(Collection<ELEMENT> collection) {
        return DfCollectionUtil.newArrayList(collection);
    }

    // -----------------------------------------------------
    //                                         Assert Object
    //                                         -------------
    /**
     * Assert that the object is not null.
     * @param variableName Variable name. (NotNull)
     * @param value Value. (NotNull)
     * @exception IllegalArgumentException
     */
    protected void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Assert that the column-name is not null and is not empty and does not contain comma.
     * @param columnName Column-name. (NotNull)
     * @exception IllegalArgumentException
     */
    protected void assertColumnName(String columnName) {
        if (columnName == null) {
            String msg = "The columnName should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (columnName.trim().length() == 0) {
            String msg = "The columnName should not be empty-string.";
            throw new IllegalArgumentException(msg);
        }
        if (columnName.indexOf(",") >= 0) {
            String msg = "The columnName should not contain comma ',': " + columnName;
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Assert that the alias-name is not null and is not empty and does not contain comma.
     * @param aliasName Alias-name. (NotNull)
     * @exception IllegalArgumentException
     */
    protected void assertAliasName(String aliasName) {
        if (aliasName == null) {
            String msg = "The aliasName should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (aliasName.trim().length() == 0) {
            String msg = "The aliasName should not be empty-string.";
            throw new IllegalArgumentException(msg);
        }
        if (aliasName.indexOf(",") >= 0) {
            String msg = "The aliasName should not contain comma ',': " + aliasName;
            throw new IllegalArgumentException(msg);
        }
    }

    // -----------------------------------------------------
    //                                         Assert String
    //                                         -------------
    /**
     * Assert that the string is not null and not trimmed empty.
     * @param variableName Variable name. (NotNull)
     * @param value Value. (NotNull)
     */
    protected void assertStringNotNullAndNotTrimmedEmpty(String variableName, String value) {
        assertObjectNotNull("variableName", variableName);
        assertObjectNotNull("value", value);
        if (value.trim().length() == 0) {
            String msg = "The value should not be empty: variableName=" + variableName + " value=" + value;
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final String titleName = DfTypeUtil.toClassTitle(this);
        return titleName + ":{aliasName=" + _aliasName + ", nestLevel=" + _nestLevel + ", subQueryLevel="
                + _subQueryLevel + ", foreignPropertyName=" + _foreignPropertyName + ", relationPath=" + _relationPath
                + ", onClauseInline=" + _onClause + "}";
    }
}
