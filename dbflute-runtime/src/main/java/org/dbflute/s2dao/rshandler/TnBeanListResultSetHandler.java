/*
 * Copyright 2014-2015 the original author or authors.
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
package org.dbflute.s2dao.rshandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dbflute.bhv.core.context.ConditionBeanContext;
import org.dbflute.bhv.core.context.ResourceContext;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.sqlclause.SqlClause;
import org.dbflute.dbmeta.accessory.DomainEntity;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.outsidesql.OutsideSqlContext;
import org.dbflute.s2dao.extension.TnRelationRowCreatorExtension;
import org.dbflute.s2dao.metadata.TnBeanMetaData;
import org.dbflute.s2dao.metadata.TnPropertyMapping;
import org.dbflute.s2dao.metadata.TnRelationPropertyType;
import org.dbflute.s2dao.rowcreator.TnRelationKey;
import org.dbflute.s2dao.rowcreator.TnRelationRowCache;
import org.dbflute.s2dao.rowcreator.TnRelationRowCreator;
import org.dbflute.s2dao.rowcreator.TnRelationSelector;
import org.dbflute.s2dao.rowcreator.TnRowCreator;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnBeanListResultSetHandler extends TnAbstractBeanResultSetHandler {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param beanMetaData Bean meta data. (NotNull)
     * @param rowCreator Row creator. (NotNull)
     * @param relationRowCreator Relation row creator. (NotNul)
     */
    public TnBeanListResultSetHandler(TnBeanMetaData beanMetaData, TnRowCreator rowCreator, TnRelationRowCreator relationRowCreator) {
        super(beanMetaData, rowCreator, relationRowCreator);
    }

    // ===================================================================================
    //                                                                              Handle
    //                                                                              ======
    public Object handle(ResultSet rs) throws SQLException {
        final List<Object> list = new ArrayList<Object>();
        mappingBean(rs, new BeanRowHandler() {
            public boolean handle(Object row) throws SQLException {
                list.add(row);
                return true; // continue to next records
            }
        });
        return list;
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    protected static interface BeanRowHandler {

        /**
         * @param row The row instance as entity. (NotNull)
         * @return Does it continue to the next row?
         * @throws SQLException When the SQL fails.
         */
        boolean handle(Object row) throws SQLException;
    }

    protected void mappingBean(ResultSet rs, BeanRowHandler handler) throws SQLException {
        // lazy initialization because if the result is zero, the resources are unused
        Map<String, String> selectColumnMap = null;
        Map<String, TnPropertyMapping> propertyCache = null;
        Map<String, Map<String, TnPropertyMapping>> relPropCache = null; // key is relationNoSuffix, columnName
        TnRelationRowCache relRowCache = null;
        TnRelationSelector relSelector = null;

        final TnBeanMetaData basePointBmd = getBeanMetaData();

        // condition-bean info (variable for minimum thread local access)
        final boolean hasCB = hasConditionBean();
        final ConditionBean cb = hasCB ? getConditionBean() : null;

        // outsideSql info (also variable for minimum thread local access)
        final boolean hasOql = hasOutsideSqlContext();
        final OutsideSqlContext oqlCtx = OutsideSqlContext.getOutsideSqlContextOnThread();

        final boolean checkNonSp = checkNonSpecifiedColumnAccess(hasCB, cb, hasOql, oqlCtx);
        final boolean colNullObj = isUseColumnNullObjectHandling(hasCB, cb);
        final boolean skipRelationLoop;
        {
            final boolean emptyRelationCB = hasCB && isSelectedRelationEmpty(cb);
            final boolean specifiedOutsideSql = hasOql && isSpecifiedOutsideSql(oqlCtx);

            // if it has condition-bean that has no relation to get
            // or it has outside SQL context that is specified outside-SQL,
            // they are unnecessary to do relation loop
            skipRelationLoop = emptyRelationCB || specifiedOutsideSql;
        }
        final Map<String, Map<String, Integer>> selectIndexMap = ResourceContext.getSelectIndexMap(); // null allowed

        while (rs.next()) {
            if (selectColumnMap == null) {
                selectColumnMap = createSelectColumnMap(rs);
            }
            if (propertyCache == null) {
                propertyCache = createPropertyCache(selectColumnMap, selectIndexMap);
            }

            // create row instance of base table by row property cache
            final Object row = createRow(rs, selectIndexMap, propertyCache);

            if (skipRelationLoop) {
                adjustCreatedRow(row, checkNonSp, colNullObj, basePointBmd, cb);
                final boolean continueToNext = handler.handle(row);
                if (!continueToNext) {
                    break; // skip rear records (basically for cursor select)
                }
                continue;
            }

            if (relSelector == null) {
                relSelector = createRelationSelector(hasCB, cb);
            }
            if (relPropCache == null) {
                relPropCache = createRelationPropertyCache(selectColumnMap, selectIndexMap, relSelector);
            }
            if (relRowCache == null) {
                relRowCache = createRelationRowCache(hasCB, cb);
            }
            final List<TnRelationPropertyType> rptList = basePointBmd.getRelationPropertyTypeList();
            for (TnRelationPropertyType rpt : rptList) {
                if (relSelector.isNonSelectedRelation(rpt.getRelationNoSuffixPart())) {
                    continue;
                }
                mappingFirstRelation(rs, row, rpt, selectColumnMap, selectIndexMap, relPropCache, relRowCache, relSelector);
            }
            adjustCreatedRow(row, checkNonSp, colNullObj, basePointBmd, cb);
            final boolean continueToNext = handler.handle(row);
            if (!continueToNext) {
                break; // skip rear records (basically for cursor select)
            }
        }
    }

    /**
     * Create the selector of relation.
     * @param hasCB Does the select use condition-bean? 
     * @param cb The condition-bean for the select. (NullAllowed: not condition-bean select)
     * @return The created selector instance. (NotNull)
     */
    protected TnRelationSelector createRelationSelector(final boolean hasCB, ConditionBean cb) {
        final boolean colNullObj = isUseColumnNullObjectHandling(hasCB, cb);
        return new TnRelationSelector() {
            public boolean isNonLimitMapping() {
                return hasCB;
            }

            public boolean isNonSelectedRelation(String relationNoSuffix) {
                return hasCB && !cb.getSqlClause().hasSelectedRelation(relationNoSuffix);
            }

            public boolean isNonSelectedNextConnectingRelation(String relationNoSuffix) {
                return hasCB && !cb.getSqlClause().isSelectedNextConnectingRelation(relationNoSuffix);
            }

            public boolean canUseRelationCache(String relationNoSuffix) {
                return hasCB && cb.getSqlClause().canUseRelationCache(relationNoSuffix);
            }

            public boolean isNonSpecifiedColumnAccessAllowed(String relationNoSuffix) {
                return hasCB && cb.isNonSpecifiedColumnAccessAllowed();
            }

            public boolean isUsingSpecifyColumnInRelation(String relationNoSuffix) {
                if (!hasCB) {
                    return false;
                }
                final SqlClause sqlClause = cb.getSqlClause();
                final String tableAlias = sqlClause.translateSelectedRelationPathToTableAlias(relationNoSuffix);
                if (tableAlias == null) { // no way but just in case
                    return false;
                }
                return sqlClause.hasSpecifiedSelectColumn(tableAlias);
            }

            public Set<ColumnInfo> getRelationSpecifiedNullObjectColumnSet(String relationNoSuffix) {
                if (!hasCB) {
                    return DfCollectionUtil.emptySet();
                }
                return cb.getSqlClause().getRelationSpecifiedNullObjectColumnSet(relationNoSuffix);
            }

            public boolean isColumnNullObjectEnabled(String relationNoSuffix) {
                return colNullObj;
            }
        };
    }

    /**
     * Create the cache of relation row.
     * @param hasCB Does the select use condition-bean?
     * @param cb The condition-bean for the select. (NullAllowed: not condition-bean select)
     * @return The cache of relation row. (NotNull)
     */
    protected TnRelationRowCache createRelationRowCache(boolean hasCB, ConditionBean cb) {
        final int relSize;
        {
            final int defaultRelSize = 4; // as default
            if (hasCB) { // mainly here
                final int selectedRelationCount = getSelectedRelationCount(cb);
                if (selectedRelationCount > 0) {
                    relSize = selectedRelationCount;
                } else { // basically no way (if no count, cache is not created)
                    relSize = defaultRelSize;
                }
            } else { // basically no way (only relation of DBFlute entity is supported)
                relSize = defaultRelSize;
            }
        }
        final boolean canRowCache;
        if (hasCB) { // mainly here
            canRowCache = canRelationMappingCache(cb);
        } else { // basically no way (only relation of DBFlute entity is supported)
            canRowCache = true;
        }
        return new TnRelationRowCache(relSize, canRowCache);
    }

    /**
     * Do mapping first relation row. <br>
     * This logic is similar to next relation mapping in {@link TnRelationRowCreatorExtension}. <br>
     * So you should check it when this logic has modification.
     * @param rs The result set of JDBC, connecting to database here. (NotNull)
     * @param row The base point row. (NotNull)
     * @param rpt The property type of the relation. (NotNull)
     * @param selectColumnMap The map of select column. (NotNull)
     * @param selectIndexMap The map of select index. map:{entityNo(e.g. loc00 or _0_3) = map:{selectColumnKeyName = selectIndex}} (NullAllowed)
     * @param relPropCache The map of relation property cache. (NotNull) 
     * @param relRowCache The cache of relation row. (NotNull)
     * @param relSelector The selector of relation, which can determines e.g. is it not-selected relation?. (NotNull)
     * @throws SQLException When it fails to handle the SQL.
     */
    protected void mappingFirstRelation(ResultSet rs, Object row, TnRelationPropertyType rpt, Map<String, String> selectColumnMap,
            Map<String, Map<String, Integer>> selectIndexMap, Map<String, Map<String, TnPropertyMapping>> relPropCache,
            TnRelationRowCache relRowCache, TnRelationSelector relSelector) throws SQLException {
        final String relationNoSuffix = getFirstLevelRelationPath(rpt);
        final TnRelationKey relKey = relRowCache.createRelationKey(rs, rpt // basic resource
                , selectColumnMap, selectIndexMap // select resource
                , relationNoSuffix); // indicates relation location
        Object relationRow = null;
        if (relKey != null) {
            final boolean canUseRelationCache = relSelector.canUseRelationCache(relationNoSuffix);
            if (canUseRelationCache) {
                relationRow = relRowCache.getRelationRow(relationNoSuffix, relKey);
            }
            if (relationRow == null) { // when no cache
                relationRow = createRelationRow(rs, rpt // basic resource
                        , selectColumnMap, selectIndexMap // select resource
                        , relKey, relPropCache, relRowCache, relSelector); // relation resource
                if (relationRow != null) { // is new created relation row
                    adjustCreatedRelationRow(relationRow, relationNoSuffix, relSelector, rpt);
                    if (canUseRelationCache) {
                        relRowCache.addRelationRow(relationNoSuffix, relKey, relationRow);
                    }
                }
            }
        }
        // if exists, optional or plain value
        // if null, empty optional or nothing
        relationRow = filterOptionalRelationRowIfNeeds(row, rpt, relationRow);
        if (relationRow != null) { // exists or empty optional
            rpt.getPropertyAccessor().setValue(row, relationRow);
        }
    }

    protected String getFirstLevelRelationPath(TnRelationPropertyType rpt) {
        // here is on base so this suffix becomes relation path directly
        return rpt.getRelationNoSuffixPart();
    }

    protected Object filterOptionalRelationRowIfNeeds(Object row, TnRelationPropertyType rpt, Object relationRow) {
        return _relationRowCreator.filterOptionalRelationRowIfNeeds(row, rpt, relationRow);
    }

    // -----------------------------------------------------
    //                                         Non Specified
    //                                         -------------
    /**
     * Does it check access to non-specified column in base-point table?
     * @param hasCB Does it have condition-bean context in thread local?
     * @param cb The condition-bean for the select. (NullAllowed: when not condition-bean select)
     * @param hasOql Does it have outsideSql context in thread local?
     * @param oqlCtx The context of outsideSql. (NullAllowed: when not outsideSql select)
     * @return The determination, true or false.
     */
    protected boolean checkNonSpecifiedColumnAccess(boolean hasCB, ConditionBean cb, boolean hasOql, OutsideSqlContext oqlCtx) {
        if (hasCB) {
            if (cb.isNonSpecifiedColumnAccessAllowed()) {
                return false;
            }
            final String aliasName = cb.getSqlClause().getBasePointAliasName();
            return cb.getSqlClause().hasSpecifiedSelectColumn(aliasName);
        } else if (hasOql) {
            final Class<?> resultType = oqlCtx.getResultType();
            if (resultType == null) { // basically no way, just in case
                return false;
            }
            return isOutsideSqlNonSpecifiedColumnAccessChecked(oqlCtx, resultType);
        }
        return false;
    }

    protected boolean isOutsideSqlNonSpecifiedColumnAccessChecked(OutsideSqlContext context, Class<?> resultType) {
        // the check is no risk so checkable so basically not allowed
        // but context has determination for compatible option
        return isOutsideSqlNonSpecifiedColumnAccessCheckTarget(resultType) && !context.isNonSpecifiedColumnAccessAllowed();
    }

    protected boolean isOutsideSqlNonSpecifiedColumnAccessCheckTarget(Class<?> resultType) {
        // customize-entity does not need to check because it is generated by select clause
        // and using other customize-entity is rare case so no check
        return DomainEntity.class.isAssignableFrom(resultType);
    }

    // -----------------------------------------------------
    //                                    Column Null Object
    //                                    ------------------
    protected boolean isUseColumnNullObjectHandling(boolean hasCB, ConditionBean cb) {
        return hasCB && cb.getSqlClause().isColumnNullObjectAllowed(); // condition-bean only
    }

    // ===================================================================================
    //                                                                       ConditionBean
    //                                                                       =============
    /**
     * Does the select have the condition-bean?
     * @return The determination, true or false.
     */
    protected boolean hasConditionBean() {
        return ConditionBeanContext.isExistConditionBeanOnThread();
    }

    /**
     * Get the condition-bean on the thread if it exists.
     * @return The condition-bean for the select. (NullAllowed)
     */
    protected ConditionBean getConditionBean() {
        return ConditionBeanContext.getConditionBeanOnThread();
    }

    /**
     * Is the selected relation empty? <br>
     * You should call {@link #hasConditionBean()} before calling this.
     * @param cb The condition-bean for the select. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean isSelectedRelationEmpty(ConditionBean cb) {
        return cb.getSqlClause().isSelectedRelationEmpty();
    }

    /**
     * Get the count of selected relation. <br>
     * You should call {@link #hasConditionBean()} before calling this.
     * @param cb The condition-bean for the select. (NotNull)
     * @return The integer of the count. (NotMinus)
     */
    protected int getSelectedRelationCount(ConditionBean cb) {
        return cb.getSqlClause().getSelectedRelationCount();
    }

    /**
     * Can the relation mapping (entity instance) cache? <br>
     * You should call {@link #hasConditionBean()} before calling this.
     * @param cb The condition-bean for the select. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean canRelationMappingCache(ConditionBean cb) {
        return cb.canRelationMappingCache();
    }

    // ===================================================================================
    //                                                                          OutsideSql
    //                                                                          ==========
    protected boolean hasOutsideSqlContext() {
        return OutsideSqlContext.isExistOutsideSqlContextOnThread();
    }

    protected boolean isSpecifiedOutsideSql(OutsideSqlContext context) {
        return context.isSpecifiedOutsideSql();
    }
}
