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
package org.seasar.dbflute.s2dao.extension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.seasar.dbflute.Entity;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.jdbc.ValueType;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnPropertyMapping;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;
import org.seasar.dbflute.s2dao.metadata.TnRelationPropertyType;
import org.seasar.dbflute.s2dao.rowcreator.TnRelationKey;
import org.seasar.dbflute.s2dao.rowcreator.TnRelationRowCache;
import org.seasar.dbflute.s2dao.rowcreator.TnRelationRowCreationResource;
import org.seasar.dbflute.s2dao.rowcreator.impl.TnRelationRowCreatorImpl;
import org.seasar.dbflute.s2dao.rshandler.TnBeanListResultSetHandler;
import org.seasar.dbflute.util.DfReflectionUtil;

/**
 * The DBFlute extension of relation row creator.
 * @author jflute
 */
public class TnRelationRowCreatorExtension extends TnRelationRowCreatorImpl {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final TnRelationRowOptionalHandler _optionalFactory;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnRelationRowCreatorExtension(TnRelationRowOptionalHandler optionalFactory) {
        _optionalFactory = optionalFactory;
    }

    public static TnRelationRowCreatorExtension createRelationRowCreator(TnRelationRowOptionalHandler optionalFactory) {
        return new TnRelationRowCreatorExtension(optionalFactory);
    }

    // ===================================================================================
    //                                                             Relation KeyValue Setup
    //                                                             =======================
    @Override
    protected void setupRelationKeyValue(TnRelationRowCreationResource res) {
        // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        // setup of relation key is handled at all-value setup marked as '#RELKEY'
        // so only entity instance creation exists in this method
        // = = = = = = = = = =/
        final TnRelationPropertyType rpt = res.getRelationPropertyType();
        final TnBeanMetaData yourBmd = rpt.getYourBeanMetaData();
        final DBMeta dbmeta = yourBmd.getDBMeta();
        if (!res.hasRowInstance()) { // always no instance here (check just in case)
            final Object row;
            if (dbmeta != null) {
                row = dbmeta.newEntity();
            } else { // no way (relation of DBFlute entity is only supported)
                row = newRelationRow(rpt);
            }
            res.setRow(row);
        }
    }

    protected Object newRelationRow(TnRelationPropertyType rpt) { // for non DBFlute entity
        return DfReflectionUtil.newInstance(rpt.getPropertyDesc().getPropertyType());
    }

    // ===================================================================================
    //                                                             Relation AllValue Setup
    //                                                             =======================
    @Override
    protected void setupRelationAllValue(TnRelationRowCreationResource res) throws SQLException {
        final Map<String, TnPropertyMapping> propertyCacheElement = res.extractPropertyCacheElement();
        for (Entry<String, TnPropertyMapping> entry : propertyCacheElement.entrySet()) {
            final TnPropertyMapping pt = entry.getValue();
            res.setCurrentPropertyType(pt);
            if (!isValidRelationPerPropertyLoop(res)) { // no way unless the method is overridden
                res.clearRowInstance();
                return;
            }
            setupRelationProperty(res);
        }
        if (!isValidRelationAfterPropertyLoop(res)) { // e.g. when all values are null
            res.clearRowInstance();
            return;
        }
        res.clearValidValueCount();
        if (res.isStopNextRelationMapping()) {
            return;
        }
        setupNextRelationRow(res);
    }

    protected void setupRelationProperty(TnRelationRowCreationResource res) throws SQLException {
        final String columnName = res.buildRelationColumnName();
        // already created here, this is old S2Dao logic 
        //if (!res.hasRowInstance()) {
        //    res.setRow(newRelationRow(res));
        //}
        registerRelationValue(res, columnName);
    }

    protected void registerRelationValue(TnRelationRowCreationResource res, String columnName) throws SQLException {
        final TnPropertyMapping mapping = res.getCurrentPropertyMapping();
        Object value = null;
        if (res.containsRelationKeyColumn(columnName)) { // #RELKEY
            // if this column is relation key, it gets the value from relation key values
            // for performance and avoiding twice getting same column value
            value = res.extractRelationKeyValue(columnName);
        } else {
            final ValueType valueType = mapping.getValueType();
            final Map<String, Map<String, Integer>> selectIndexMap = res.getSelectIndexMap();
            final ResultSet rs = res.getResultSet();
            if (selectIndexMap != null) {
                final String relationNoSuffix = res.getRelationNoSuffix();
                value = ResourceContext.getRelationValue(rs, relationNoSuffix, columnName, valueType, selectIndexMap);
            } else {
                value = valueType.getValue(rs, columnName);
            }
        }
        if (value != null) {
            res.incrementValidValueCount();
            doRegisterRelationValue(res, mapping, value);
        }
    }

    protected void doRegisterRelationValue(TnRelationRowCreationResource res, TnPropertyMapping mapping, Object value) {
        final ColumnInfo columnInfo = mapping.getEntityColumnInfo();
        if (columnInfo != null) {
            columnInfo.write((Entity) res.getRow(), value);
        } else {
            mapping.getPropertyAccessor().setValue(res.getRow(), value);
        }
    }

    // -----------------------------------------------------
    //                                         Next Relation
    //                                         -------------
    protected void setupNextRelationRow(TnRelationRowCreationResource res) throws SQLException {
        final TnBeanMetaData nextBmd = res.getRelationBeanMetaData();
        final Object row = res.getRow();
        res.prepareNextLevelMapping();
        try {
            final List<TnRelationPropertyType> nextRptList = nextBmd.getRelationPropertyTypeList();
            for (TnRelationPropertyType nextRpt : nextRptList) {
                setupNextRelationRowElement(res, row, nextRpt);
            }
        } finally {
            res.setRow(row);
            res.closeNextLevelMapping();
        }
    }

    protected void setupNextRelationRowElement(TnRelationRowCreationResource res, Object row,
            TnRelationPropertyType nextRpt) throws SQLException {
        res.prepareNextRelationProperty(nextRpt);
        try {
            mappingNextRelation(res, row);
        } finally {
            res.closeNextRelationProperty();
        }
    }

    /**
     * Do mapping next relation row. <br />
     * This logic is similar to first relation mapping in {@link TnBeanListResultSetHandler}. <br />
     * So you should check it when this logic has modification.
     * @param res The resource of relation row creation. (NotNull)
     * @param row The base point row, which is previous relation row. (NotNull)
     * @throws SQLException
     */
    protected void mappingNextRelation(TnRelationRowCreationResource res, Object row) throws SQLException {
        if (res.isStopCurrentRelationMapping()) {
            return;
        }
        final TnRelationKey relKey = res.prepareRelationKey(); // also saves it in resource
        final TnRelationPropertyType rpt = res.getRelationPropertyType();
        Object relationRow = null;
        if (relKey != null) {
            final String relationNoSuffix = res.getRelationNoSuffix();
            final boolean canUseRelationCache = res.canUseRelationCache();
            TnRelationRowCache relRowCache = null;
            if (canUseRelationCache) {
                relRowCache = res.getRelRowCache();
                relationRow = relRowCache.getRelationRow(relationNoSuffix, relKey);
            }
            if (relationRow == null) { // when no cache
                relationRow = createRelationRow(res);
                if (relationRow != null) { // is new created relation row
                    adjustCreatedRow(relationRow, rpt);
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

    protected void adjustCreatedRow(Object relationRow, TnRelationPropertyType rpt) {
        TnRowCreatorExtension.adjustCreatedRow(relationRow, rpt.getYourBeanMetaData());
    }

    // ===================================================================================
    //                                                                Property Cache Setup
    //                                                                ====================
    @Override
    protected void setupPropertyCache(TnRelationRowCreationResource res) throws SQLException {
        // - - - - - - - - - - - 
        // Recursive Call Point!
        // - - - - - - - - - - -
        if (res.isStopCurrentRelationMapping()) {
            return;
        }

        // set up property cache about current bean meta data
        final TnBeanMetaData nextBmd = res.getRelationBeanMetaData();
        final List<TnPropertyType> ptList = nextBmd.getPropertyTypeList();
        for (TnPropertyType pt : ptList) { // already been filtered as target only
            res.setCurrentPropertyType(pt);
            setupPropertyCacheElement(res);
        }

        // set up next level relation's property cache
        if (res.isStopNextRelationMapping()) {
            return;
        }
        res.prepareNextLevelMapping();
        try {
            setupNextPropertyCache(res, nextBmd);
        } finally {
            res.closeNextLevelMapping();
        }
    }

    // -----------------------------------------------------
    //                                         Next Relation
    //                                         -------------
    protected void setupNextPropertyCache(TnRelationRowCreationResource res, TnBeanMetaData nextBmd)
            throws SQLException {
        final List<TnRelationPropertyType> nextRptList = nextBmd.getRelationPropertyTypeList();
        for (TnRelationPropertyType nextRpt : nextRptList) {
            setupNextPropertyCacheElement(res, nextRpt);
        }
    }

    protected void setupNextPropertyCacheElement(TnRelationRowCreationResource res, TnRelationPropertyType nextRpt)
            throws SQLException {
        res.prepareNextRelationProperty(nextRpt);
        try {
            setupPropertyCache(res); // recursive call
        } finally {
            res.closeNextRelationProperty();
        }
    }

    // ===================================================================================
    //                                                                     Option Override
    //                                                                     ===============
    @Override
    protected boolean isCreateDeadLink() {
        return false;
    }

    @Override
    protected int getLimitRelationNestLevel() {
        // basically unused on DBFlute because only ConditionBean uses relation row,
        // and ConditionBean supports unlimited relation nest level
        // so this limit size is always used after hasConditionBean()
        return 2; // for Compatible (old parameter)
    }

    // ===================================================================================
    //                                                                   Optional Handling
    //                                                                   =================
    public Object filterOptionalRelationRowIfNeeds(Object row, TnRelationPropertyType rpt, Object relationRow) {
        return _optionalFactory.filterOptionalRelationRowIfNeeds(row, rpt, relationRow);
    }
}
