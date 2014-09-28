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
package org.seasar.dbflute.s2dao.rowcreator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnPropertyMapping;
import org.seasar.dbflute.s2dao.metadata.TnRelationPropertyType;

/**
 * The resource for relation row creation. <br />
 * S2Dao logics are modified for DBFlute.
 * @author modified by jflute (originated in S2Dao)
 */
public class TnRelationRowCreationResource {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    /** Result set. (NotNull) */
    protected ResultSet _resultSet;

    /** Relation row. (TemporaryUsed) */
    protected Object _row;

    /** Relation property type. (NotNull) */
    protected TnRelationPropertyType _relationPropertyType;

    /** The name map of select column. (NotNull) */
    protected Map<String, String> _selectColumnMap;

    /** The map of select index. map:{ localAlias or relationNoSuffix = map:{ selectColumnKeyName = selectIndex } } (NullAllowed) */
    protected Map<String, Map<String, Integer>> _selectIndexMap;

    /** The relation key, which has key values, of the relation. (NotNull) */
    protected TnRelationKey _relationKey;

    /** The map of relation property cache. Keys are relationNoSuffix, columnName. (NotNull) */
    protected Map<String, Map<String, TnPropertyMapping>> _relPropCache;

    /** The cache of relation row. (NotNull) */
    protected TnRelationRowCache _relRowCache;

    /** The selector of relation. (NotNull) */
    protected TnRelationSelector _relSelector;

    /** The suffix of base object. (NotNull, EmptyAllowed: empty means base relation is base point) */
    protected String _baseSuffix;

    /** The suffix of relation no. (NotNull) */
    protected String _relationNoSuffix;

    /** The limit of relation nest level. */
    protected int _limitRelationNestLevel;

    /** The current relation nest level. Default is one. */
    protected int _currentRelationNestLevel;

    /** Current property mapping. (TemporaryUsed) */
    protected TnPropertyMapping _currentPropertyMapping;

    /** The count of valid value. */
    protected int _validValueCount;

    /** Does it create dead link? */
    protected boolean _createDeadLink;

    // -----------------------------------------------------
    //                                                Backup
    //                                                ------
    // these are laze-loaded
    /** The backup of relation property type. The element type is {@link TnRelationPropertyType}. */
    protected Stack<TnRelationPropertyType> _relationPropertyTypeBackup;

    /** The backup of relation key. The element type is {@link TnRelationKey}. */
    protected Stack<TnRelationKey> _relationKeyBackup;

    /** The backup of base suffix. The element type is String. */
    protected Stack<String> _baseSuffixBackup;

    /** The backup of base suffix. The element type is String. */
    protected Stack<String> _relationSuffixBackup;

    // ===================================================================================
    //                                                                        Row Instance
    //                                                                        ============
    public boolean hasRowInstance() {
        return _row != null;
    }

    public void clearRowInstance() {
        _row = null;
    }

    // ===================================================================================
    //                                                              Relation Property Type
    //                                                              ======================
    public TnBeanMetaData getRelationBeanMetaData() {
        return _relationPropertyType.getYourBeanMetaData();
    }

    protected boolean hasNextRelationProperty() {
        return getRelationBeanMetaData().getRelationPropertyTypeSize() > 0;
    }

    protected void backupRelationPropertyType() {
        getRelationPropertyTypeBackup().push(getRelationPropertyType());
    }

    protected void restoreRelationPropertyType() {
        setRelationPropertyType(getRelationPropertyTypeBackup().pop());
    }

    protected Stack<TnRelationPropertyType> getRelationPropertyTypeBackup() {
        if (_relationPropertyTypeBackup == null) {
            _relationPropertyTypeBackup = new Stack<TnRelationPropertyType>();
        }
        return _relationPropertyTypeBackup;
    }

    // ===================================================================================
    //                                                                       Select Column
    //                                                                       =============
    /**
     * Does the column name contain in selected columns?
     * @param columnKeyName The key name of column. e.g. FOO, FOO_0_2 (NotNull)
     * @return The determination, true or false.
     */
    public boolean containsSelectColumn(String columnKeyName) {
        return _selectColumnMap.containsKey(columnKeyName);
    }

    // ===================================================================================
    //                                                                        Relation Key
    //                                                                        ============
    public boolean containsRelationKeyColumn(String columnName) {
        return _relationKey.containsColumn(columnName);
    }

    public Object extractRelationKeyValue(String columnName) {
        return _relationKey.extractKeyValue(columnName);
    }

    protected void backupRelationKey() {
        getRelationKeyBackup().push(getRelationKey());
    }

    protected void restoreRelationKey() {
        setRelationKey(getRelationKeyBackup().pop());
    }

    protected Stack<TnRelationKey> getRelationKeyBackup() {
        if (_relationKeyBackup == null) {
            _relationKeyBackup = new Stack<TnRelationKey>();
        }
        return _relationKeyBackup;
    }

    // ===================================================================================
    //                                                             Relation Property Cache
    //                                                             =======================
    // The type of relationPropertyCache is Map<String(relationNoSuffix), Map<String(columnName), PropertyType>>.
    public void initializePropertyCacheElement() {
        _relPropCache.put(_relationNoSuffix, new HashMap<String, TnPropertyMapping>());
    }

    public boolean hasPropertyCacheElement() {
        final Map<String, TnPropertyMapping> propertyCacheElement = extractPropertyCacheElement();
        return propertyCacheElement != null && !propertyCacheElement.isEmpty();
    }

    public Map<String, TnPropertyMapping> extractPropertyCacheElement() {
        return _relPropCache.get(_relationNoSuffix);
    }

    public void savePropertyCacheElement() {
        if (!hasPropertyCacheElement()) {
            initializePropertyCacheElement();
        }
        final Map<String, TnPropertyMapping> propertyCacheElement = extractPropertyCacheElement();
        final String columnName = buildRelationColumnName();
        if (propertyCacheElement.containsKey(columnName)) {
            return;
        }
        propertyCacheElement.put(columnName, _currentPropertyMapping);
    }

    // ===================================================================================
    //                                                                  Relation Row Cache
    //                                                                  ==================
    /**
     * Prepare the relation key of the current relation. <br />
     * The created relation key is returned and saved in this resource if the key is created.
     * @return The created relation key. (NullAllowed: null means the relation has no data)
     * @throws SQLException
     */
    public TnRelationKey prepareRelationKey() throws SQLException {
        final TnRelationKey relKey = doCreateRelationKey();
        if (relKey != null) {
            setRelationKey(relKey);
        }
        return relKey;
    }

    protected TnRelationKey doCreateRelationKey() throws SQLException {
        return _relRowCache.createRelationKey(_resultSet, _relationPropertyType // basic resource
                , _selectColumnMap, _selectIndexMap // select resource
                , _relationNoSuffix); // relation resource
    }

    // ===================================================================================
    //                                                                     Relation Suffix
    //                                                                     ===============
    public String buildRelationColumnName() {
        return _currentPropertyMapping.getColumnDbName() + _relationNoSuffix;
    }

    protected void addRelationNoSuffix(String additionalRelationNoSuffix) {
        _relationNoSuffix = _relationNoSuffix + additionalRelationNoSuffix;
    }

    protected void backupBaseSuffix() {
        getBaseSuffixBackup().push(getBaseSuffix());
    }

    protected void restoreBaseSuffix() {
        setBaseSuffix(getBaseSuffixBackup().pop());
    }

    protected Stack<String> getBaseSuffixBackup() {
        if (_baseSuffixBackup == null) {
            _baseSuffixBackup = new Stack<String>();
        }
        return _baseSuffixBackup;
    }

    protected void backupRelationNoSuffix() {
        getRelationNoSuffixBackup().push(getRelationNoSuffix());
    }

    protected void restoreRelationNoSuffix() {
        setRelationNoSuffix(getRelationNoSuffixBackup().pop());
    }

    protected Stack<String> getRelationNoSuffixBackup() {
        if (_relationSuffixBackup == null) {
            _relationSuffixBackup = new Stack<String>();
        }
        return _relationSuffixBackup;
    }

    // ===================================================================================
    //                                                                 Relation Nest Level
    //                                                                 ===================
    protected boolean isNextRelationUnderLimit() {
        return _currentRelationNestLevel < _limitRelationNestLevel;
    }

    protected void incrementCurrentRelationNestLevel() {
        ++_currentRelationNestLevel;
    }

    protected void decrementCurrentRelationNestLevel() {
        --_currentRelationNestLevel;
    }

    // ===================================================================================
    //                                                                   Valid Value Count
    //                                                                   =================
    public void incrementValidValueCount() {
        ++_validValueCount;
    }

    public void clearValidValueCount() {
        _validValueCount = 0;
    }

    public boolean hasValidValueCount() {
        return _validValueCount > 0;
    }

    // ===================================================================================
    //                                                                     Backup Resource
    //                                                                     ===============
    public void prepareNextLevelMapping() {
        backupRelationPropertyType();
        backupRelationKey();
        incrementCurrentRelationNestLevel();
    }

    public void closeNextLevelMapping() {
        restoreRelationPropertyType();
        restoreRelationKey();
        decrementCurrentRelationNestLevel();
    }

    public void prepareNextRelationProperty(TnRelationPropertyType nextRpt) {
        backupBaseSuffix();
        backupRelationNoSuffix();
        clearRowInstance();
        setRelationPropertyType(nextRpt);
        setBaseSuffix(_relationNoSuffix); // current relation to base
        addRelationNoSuffix(nextRpt.getRelationNoSuffixPart());
    }

    public void closeNextRelationProperty() {
        restoreBaseSuffix();
        restoreRelationNoSuffix();
    }

    // ===================================================================================
    //                                                                   Relation Selector
    //                                                                   =================
    /**
     * Does it stop the mapping of the next level relation? <br />
     * This contains various determinations for performance.
     * @return The determination, true or false.
     */
    public boolean isStopNextRelationMapping() {
        if (!hasNextRelationProperty()) {
            return true;
        }
        if (!isNonLimitMapping() && !isNextRelationUnderLimit()) {
            return true;
        }
        if (isNonSelectedNextConnectingRelation()) {
            return true;
        }
        return false;
    }

    /**
     * Does it stop the mapping of the current relation? <br />
     * This contains various determinations for performance.
     * @return The determination, true or false.
     */
    public boolean isStopCurrentRelationMapping() {
        return isNonSelectedRelation();
    }

    /**
     * Does the mapping has non-limit of relation nest level?
     * @return The determination, true or false.
     */
    protected boolean isNonLimitMapping() {
        return _relSelector.isNonLimitMapping();
    }

    /**
     * Is the relation (of current relation No suffix) non-selected?
     * @return The determination, true or false.
     */
    protected boolean isNonSelectedRelation() {
        return _relSelector.isNonSelectedRelation(_relationNoSuffix);
    }

    /**
     * Does the relation (of current relation No suffix) non-connect to selected next relation?
     * @return The determination, true or false.
     */
    protected boolean isNonSelectedNextConnectingRelation() {
        return _relSelector.isNonSelectedNextConnectingRelation(_relationNoSuffix);
    }

    /**
     * Can it use the relation cache for entity mapping?
     * @return The determination, true or false.
     */
    public boolean canUseRelationCache() {
        return _relSelector.canUseRelationCache(_relationNoSuffix);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public ResultSet getResultSet() {
        return _resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        _resultSet = resultSet;
    }

    public Object getRow() {
        return _row;
    }

    public void setRow(Object row) {
        _row = row;
    }

    public TnRelationPropertyType getRelationPropertyType() {
        return _relationPropertyType;
    }

    public void setRelationPropertyType(TnRelationPropertyType rpt) {
        _relationPropertyType = rpt;
    }

    public Map<String, String> getSelectColumnMap() {
        return _selectColumnMap;
    }

    public void setSelectColumnMap(Map<String, String> selectColumnMap) {
        _selectColumnMap = selectColumnMap;
    }

    public Map<String, Map<String, Integer>> getSelectIndexMap() {
        return _selectIndexMap;
    }

    public void setSelectIndexMap(Map<String, Map<String, Integer>> selectIndexMap) {
        _selectIndexMap = selectIndexMap;
    }

    public TnRelationKey getRelationKey() {
        return _relationKey;
    }

    public void setRelationKey(TnRelationKey relationKey) {
        _relationKey = relationKey;
    }

    public Map<String, Map<String, TnPropertyMapping>> getRelPropCache() {
        return _relPropCache;
    }

    public void setRelPropCache(Map<String, Map<String, TnPropertyMapping>> relPropCache) {
        this._relPropCache = relPropCache;
    }

    public TnRelationRowCache getRelRowCache() {
        return _relRowCache;
    }

    public void setRelRowCache(TnRelationRowCache relRowCache) {
        this._relRowCache = relRowCache;
    }

    public String getBaseSuffix() {
        return _baseSuffix;
    }

    public void setBaseSuffix(String baseSuffix) {
        this._baseSuffix = baseSuffix;
    }

    public String getRelationNoSuffix() {
        return _relationNoSuffix;
    }

    public void setRelationNoSuffix(String relationNoSuffix) {
        this._relationNoSuffix = relationNoSuffix;
    }

    public int getLimitRelationNestLevel() {
        return _limitRelationNestLevel;
    }

    public void setLimitRelationNestLevel(int limitRelationNestLevel) {
        this._limitRelationNestLevel = limitRelationNestLevel;
    }

    public int getCurrentRelationNestLevel() {
        return _currentRelationNestLevel;
    }

    public void setCurrentRelationNestLevel(int currentRelationNestLevel) {
        this._currentRelationNestLevel = currentRelationNestLevel;
    }

    public TnPropertyMapping getCurrentPropertyMapping() {
        return _currentPropertyMapping;
    }

    public void setCurrentPropertyType(TnPropertyMapping propertyType) {
        this._currentPropertyMapping = propertyType;
    }

    public boolean isCreateDeadLink() {
        return _createDeadLink;
    }

    public void setCreateDeadLink(boolean createDeadLink) {
        this._createDeadLink = createDeadLink;
    }

    public TnRelationSelector getRelationSelector() {
        return _relSelector;
    }

    public void setRelationSelector(TnRelationSelector relSelector) {
        _relSelector = relSelector;
    }
}
