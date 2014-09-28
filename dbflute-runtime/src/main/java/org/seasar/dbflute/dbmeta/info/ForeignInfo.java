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
package org.seasar.dbflute.dbmeta.info;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.seasar.dbflute.Entity;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.PropertyGateway;
import org.seasar.dbflute.dbmeta.PropertyMethodFinder;
import org.seasar.dbflute.util.DfReflectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * The information of foreign relation.
 * @author jflute
 */
public class ForeignInfo implements RelationInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _constraintName;
    protected final String _foreignPropertyName;
    protected final DBMeta _localDBMeta;
    protected final DBMeta _foreignDBMeta;
    protected final Map<ColumnInfo, ColumnInfo> _localForeignColumnInfoMap;
    protected final Map<ColumnInfo, ColumnInfo> _foreignLocalColumnInfoMap;
    protected final int _relationNo;
    protected final Class<?> _objectNativeType; // always relation entity type, provided by DB meta 
    protected final Class<?> _propertyAccessType; // same as entity type or might be optional
    protected final boolean _oneToOne;
    protected final boolean _bizOneToOne;
    protected final boolean _referrerAsOne;
    protected final boolean _additionalFK;
    protected final String _fixedCondition;
    protected final List<String> _dynamicParameterList;
    protected final boolean _fixedInline;
    protected final String _reversePropertyName;
    protected final PropertyGateway _propertyGateway;
    protected final PropertyMethodFinder _propertyMethodFinder;
    protected final Method _readMethod;
    protected final Method _writeMethod;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ForeignInfo(String constraintName, String foreignPropertyName // name
            , DBMeta localDBMeta, DBMeta foreignDBMeta // DB meta
            , Map<ColumnInfo, ColumnInfo> localForeignColumnInfoMap, int relationNo // relation attribute
            , Class<?> propertyAccessType // property info (object native type is provided by DB meta)
            , boolean oneToOne, boolean bizOneToOne, boolean referrerAsOne, boolean additionalFK // relation type
            , String fixedCondition, List<String> dynamicParameterList, boolean fixedInline // fixed condition
            , String reversePropertyName, PropertyMethodFinder propertyMethodFinder // various info
    ) { // big constructor
        assertObjectNotNull("constraintName", constraintName);
        assertObjectNotNull("foreignPropertyName", foreignPropertyName);
        assertObjectNotNull("localDBMeta", localDBMeta);
        assertObjectNotNull("foreignDBMeta", foreignDBMeta);
        assertObjectNotNull("localForeignColumnInfoMap", localForeignColumnInfoMap);
        assertObjectNotNull("propertyAccessType", propertyAccessType);
        assertObjectNotNull("propertyMethodFinder", propertyMethodFinder);
        _constraintName = constraintName;
        _foreignPropertyName = foreignPropertyName;
        _localDBMeta = localDBMeta;
        _foreignDBMeta = foreignDBMeta;
        _localForeignColumnInfoMap = Collections.unmodifiableMap(localForeignColumnInfoMap);
        final Map<ColumnInfo, ColumnInfo> foreignLocalColumnInfoMap = new LinkedHashMap<ColumnInfo, ColumnInfo>(4);
        for (Entry<ColumnInfo, ColumnInfo> entry : localForeignColumnInfoMap.entrySet()) {
            foreignLocalColumnInfoMap.put(entry.getValue(), entry.getKey());
        }
        _foreignLocalColumnInfoMap = Collections.unmodifiableMap(foreignLocalColumnInfoMap);
        _relationNo = relationNo;
        _objectNativeType = foreignDBMeta.getEntityType();
        _propertyAccessType = propertyAccessType;
        _oneToOne = oneToOne;
        _bizOneToOne = bizOneToOne;
        _referrerAsOne = referrerAsOne;
        _additionalFK = additionalFK;
        _fixedCondition = fixedCondition;
        _fixedInline = fixedInline;
        if (dynamicParameterList != null) {
            _dynamicParameterList = Collections.unmodifiableList(dynamicParameterList);
        } else {
            _dynamicParameterList = Collections.emptyList();
        }
        _reversePropertyName = reversePropertyName;
        _propertyGateway = findPropertyGateway();
        _propertyMethodFinder = propertyMethodFinder;
        _readMethod = findReadMethod();
        _writeMethod = findWriteMethod();
    }

    // ===================================================================================
    //                                                                    Column Existence
    //                                                                    ================
    public boolean containsLocalColumn(ColumnInfo localColumn) {
        return doContainsLocalColumn(localColumn.getColumnDbName());
    }

    protected boolean doContainsLocalColumn(String columnName) {
        for (ColumnInfo columnInfo : _localForeignColumnInfoMap.keySet()) {
            if (columnInfo.getColumnDbName().equals(columnName)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsForeignColumn(ColumnInfo foreignColumn) {
        return doContainsForeignColumn(foreignColumn.getColumnDbName());
    }

    protected boolean doContainsForeignColumn(String columnName) {
        for (ColumnInfo columnInfo : _foreignLocalColumnInfoMap.keySet()) {
            if (columnInfo.getColumnDbName().equals(columnName)) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                      Column Mapping
    //                                                                      ==============
    public ColumnInfo findLocalByForeign(String foreignColumnDbName) {
        final ColumnInfo keyColumnInfo = _foreignDBMeta.findColumnInfo(foreignColumnDbName);
        final ColumnInfo resultColumnInfo = (ColumnInfo) _foreignLocalColumnInfoMap.get(keyColumnInfo);
        if (resultColumnInfo == null) {
            String msg = "Not found by foreignColumnDbName in foreignLocalColumnInfoMap:";
            msg = msg + " foreignColumnDbName=" + foreignColumnDbName;
            msg = msg + " foreignLocalColumnInfoMap=" + _foreignLocalColumnInfoMap;
            throw new IllegalArgumentException(msg);
        }
        return resultColumnInfo;
    }

    public ColumnInfo findForeignByLocal(String localColumnDbName) {
        final ColumnInfo keyColumnInfo = _localDBMeta.findColumnInfo(localColumnDbName);
        final ColumnInfo resultColumnInfo = (ColumnInfo) _localForeignColumnInfoMap.get(keyColumnInfo);
        if (resultColumnInfo == null) {
            String msg = "Not found by localColumnDbName in localForeignColumnInfoMap:";
            msg = msg + " localColumnDbName=" + localColumnDbName;
            msg = msg + " localForeignColumnInfoMap=" + _localForeignColumnInfoMap;
            throw new IllegalArgumentException(msg);
        }
        return resultColumnInfo;
    }

    // ===================================================================================
    //                                                                          Reflection
    //                                                                          ==========
    // -----------------------------------------------------
    //                                                  Read
    //                                                  ----
    /**
     * Read the value to the entity by its gateway (means no reflection). <br />
     * It returns plain value in entity as property access type.
     * @param <PROPERTY> The type of property, might be optional.
     * @param localEntity The local entity of this column to read. (NotNull)
     * @return The read instance of foreign entity, might be optional. (NotNull: when optional, NullAllowed: when native type)
     */
    @SuppressWarnings("unchecked")
    public <PROPERTY> PROPERTY read(Entity localEntity) {
        return (PROPERTY) _propertyGateway.read(localEntity);
    }

    /**
     * Get the read method for entity reflection.
     * @return The read method, cached in this instance. (NotNull)
     */
    public Method getReadMethod() { // basically unused in DBFlute, use gateway instead
        return _readMethod;
    }

    // -----------------------------------------------------
    //                                                 Write
    //                                                 -----
    /**
     * Write the value to the entity by its gateway (means no reflection). <br />
     * No converting to optional so check the property access type.
     * @param localEntity The local entity of this column to write. (NotNull)
     * @param foreignEntity The written instance of foreign entity, might be optional. (NullAllowed: if null, null written)
     */
    public void write(Entity localEntity, Object foreignEntity) {
        _propertyGateway.write(localEntity, foreignEntity);
    }

    /**
     * Get the write method for entity reflection.
     * @return The writer method, cached in this instance. (NotNull)
     */
    public Method getWriteMethod() { // basically unused in DBFlute, use gateway instead
        return _writeMethod;
    }

    // -----------------------------------------------------
    //                                                Finder
    //                                                ------
    protected PropertyGateway findPropertyGateway() {
        final PropertyGateway gateway = _localDBMeta.findForeignPropertyGateway(_foreignPropertyName);
        if (gateway == null) { // no way
            String msg = "Not found the foreign property gateway by the name: " + _foreignPropertyName;
            throw new IllegalStateException(msg);
        }
        return gateway;
    }

    protected Method findReadMethod() {
        final Class<? extends Entity> localType = _localDBMeta.getEntityType();
        return _propertyMethodFinder.findReadMethod(localType, _foreignPropertyName, _propertyAccessType);
    }

    protected Method findWriteMethod() {
        final Class<? extends Entity> localType = _localDBMeta.getEntityType();
        return _propertyMethodFinder.findWriteMethod(localType, _foreignPropertyName, _propertyAccessType);
    }

    // -----------------------------------------------------
    //                                               Invoker
    //                                               -------
    protected Object invokeMethod(Method method, Object target, Object[] args) {
        return DfReflectionUtil.invoke(method, target, args);
    }

    // ===================================================================================
    //                                                             Relation Implementation
    //                                                             =======================
    public String getRelationPropertyName() {
        return getForeignPropertyName();
    }

    public DBMeta getTargetDBMeta() {
        return getForeignDBMeta();
    }

    public Map<ColumnInfo, ColumnInfo> getLocalTargetColumnInfoMap() {
        return getLocalForeignColumnInfoMap();
    }

    public boolean isReferrer() {
        return false;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String initCap(final String name) {
        return Srl.initCap(name);
    }

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

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    public int hashCode() {
        return _foreignPropertyName.hashCode() + _localDBMeta.hashCode() + _foreignDBMeta.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ForeignInfo)) {
            return false;
        }
        final ForeignInfo target = (ForeignInfo) obj;
        if (!this._foreignPropertyName.equals(target.getForeignPropertyName())) {
            return false;
        }
        if (!this._localDBMeta.equals(target.getLocalDBMeta())) {
            return false;
        }
        if (!this._foreignDBMeta.equals(target.getForeignDBMeta())) {
            return false;
        }
        return true;
    }

    public String toString() {
        return _localDBMeta.getTableDbName() + "." + _foreignPropertyName + "->" + _foreignDBMeta.getTableDbName();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * {@inheritDoc}
     */
    public String getConstraintName() {
        return _constraintName;
    }

    /**
     * Get the property name of the foreign relation. <br />
     * This is unique name in the table. <br />
     * For example, if the member entity has getMemberStatus(), this returns 'memberStatus'.
     * @return The string for property name. (NotNull)
     */
    public String getForeignPropertyName() {
        return _foreignPropertyName;
    }

    /**
     * {@inheritDoc}
     */
    public DBMeta getLocalDBMeta() {
        return _localDBMeta;
    }

    /**
     * Get the DB meta of the foreign table. <br />
     * For example, if the relation MEMBER and MEMBER_STATUS, this returns MEMBER_STATUS's one.
     * @return The DB meta singleton instance. (NotNull)
     */
    public DBMeta getForeignDBMeta() {
        return _foreignDBMeta;
    }

    /**
     * Get the read-only map, key is a local column info, value is a foreign column info.
     * @return The read-only map. (NotNull)
     */
    public Map<ColumnInfo, ColumnInfo> getLocalForeignColumnInfoMap() {
        return _localForeignColumnInfoMap;
    }

    /**
     * Get the read-only map, key is a foreign column info, value is a local column info.
     * @return The read-only map. (NotNull)
     */
    public Map<ColumnInfo, ColumnInfo> getForeignLocalColumnInfoMap() {
        return _foreignLocalColumnInfoMap;
    }

    /**
     * Get the number of a relation. (internal property)
     * @return The number of a relation. (NotNull, NotMinus)
     */
    public int getRelationNo() {
        return _relationNo;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getObjectNativeType() {
        return _objectNativeType;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getPropertyAccessType() {
        return _propertyAccessType;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOneToOne() {
        return _oneToOne;
    }

    /**
     * Does the relation is biz-one-to-one?
     * @return The determination, true or false.
     */
    public boolean isBizOneToOne() {
        return _bizOneToOne;
    }

    /**
     * Does the relation is referrer-as-one?
     * @return The determination, true or false.
     */
    public boolean isReferrerAsOne() {
        return _referrerAsOne;
    }

    /**
     * Does the relation is from additional foreign key?
     * @return The determination, true or false.
     */
    public boolean isAdditionalFK() {
        return _additionalFK;
    }

    /**
     * Get the fixed-condition if it's additional foreign key.
     * @return The string of fixed-condition. (NullAllowed)
     */
    public String getFixedCondition() {
        return _fixedCondition;
    }

    /**
     * Get the read-only list of dynamic parameter name for fixed-condition if it's additional foreign key.
     * @return The read-only list. (NotNull: if no fixed-condition, returns empty list)
     */
    public List<String> getDynamicParameterList() {
        return _dynamicParameterList;
    }

    /**
     * Does the fixed condition is for in-line view?
     * @return The determination, true or false.
     */
    public boolean isFixedInline() {
        return _fixedInline;
    }

    /**
     * {@inheritDoc}
     */
    public RelationInfo getReverseRelation() {
        return _reversePropertyName != null ? _foreignDBMeta.findRelationInfo(_reversePropertyName) : null;
    }

    // -----------------------------------------------------
    //                                               Derived
    //                                               -------
    /**
     * Does the relation is from pure foreign key?
     * @return The determination, true or false.
     */
    public boolean isPureFK() { // derived property
        return !_additionalFK && !_referrerAsOne;
    }

    /**
     * Do the FK columns have not null constraint?
     * @return The determination, true or false.
     */
    public boolean isNotNullFKColumn() {
        for (Entry<ColumnInfo, ColumnInfo> entry : getLocalForeignColumnInfoMap().entrySet()) {
            final ColumnInfo localColumnInfo = entry.getKey();
            if (!localColumnInfo.isNotNull()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Does it have fixed-condition for this relation?
     * @return The determination, true or false.
     */
    public boolean hasFixedCondition() {
        return _fixedCondition != null && _fixedCondition.trim().length() > 0;
    }

    /**
     * Does it have dynamic parameter of fixed-condition?
     * @return The determination, true or false.
     */
    public boolean hasFixedConditionDynamicParameter() {
        return hasFixedCondition() && !_dynamicParameterList.isEmpty();
    }

    /**
     * Is the dynamic parameters of fixed condition required?
     * @return The determination, true or false. (true if NOT contains IF comment)
     */
    public boolean isFixedConditionDynamicParameterRequired() {
        return hasFixedConditionDynamicParameter() && !getFixedCondition().contains("/*IF ");
    }
}
