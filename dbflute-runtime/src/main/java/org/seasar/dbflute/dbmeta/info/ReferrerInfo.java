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
import java.util.Map;
import java.util.Map.Entry;

import org.seasar.dbflute.Entity;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.PropertyMethodFinder;
import org.seasar.dbflute.util.DfReflectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * The information of referrer relation.
 * @author jflute
 */
public class ReferrerInfo implements RelationInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _constraintName;
    protected final String _referrerPropertyName;
    protected final DBMeta _localDBMeta;
    protected final DBMeta _referrerDBMeta;
    protected final Map<ColumnInfo, ColumnInfo> _localReferrerColumnInfoMap;
    protected final Map<ColumnInfo, ColumnInfo> _referrerLocalColumnInfoMap;
    protected final Class<?> _objectNativeType; // always entity type, provided by DB meta
    protected final Class<?> _propertyAccessType; // basically list type
    protected final boolean _oneToOne;
    protected final String _reversePropertyName;
    protected final PropertyMethodFinder _propertyMethodFinder;
    protected final Method _readMethod;
    protected final Method _writeMethod;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ReferrerInfo(String constraintName, String referrerPropertyName // name
            , DBMeta localDBMeta, DBMeta referrerDBMeta // DB meta
            , Map<ColumnInfo, ColumnInfo> localReferrerColumnInfoMap // relation attribute
            , Class<?> propertyAccessType // property info (object native type is provided by DB meta)
            , boolean oneToOne // relation type
            , String reversePropertyName, PropertyMethodFinder propertyMethodFinder // various info
    ) { // big constructor
        assertObjectNotNull("constraintName", constraintName);
        assertObjectNotNull("referrerPropertyName", referrerPropertyName);
        assertObjectNotNull("localDBMeta", localDBMeta);
        assertObjectNotNull("referrerDBMeta", referrerDBMeta);
        assertObjectNotNull("localReferrerColumnInfoMap", localReferrerColumnInfoMap);
        assertObjectNotNull("propertyAccessType", propertyAccessType);
        assertObjectNotNull("propertyMethodFinder", propertyMethodFinder);
        _constraintName = constraintName;
        _referrerPropertyName = referrerPropertyName;
        _localDBMeta = localDBMeta;
        _referrerDBMeta = referrerDBMeta;
        _localReferrerColumnInfoMap = Collections.unmodifiableMap(localReferrerColumnInfoMap);
        final Map<ColumnInfo, ColumnInfo> referrerLocalColumnInfoMap = new LinkedHashMap<ColumnInfo, ColumnInfo>(4);
        for (Entry<ColumnInfo, ColumnInfo> entry : localReferrerColumnInfoMap.entrySet()) {
            referrerLocalColumnInfoMap.put(entry.getValue(), entry.getKey());
        }
        _referrerLocalColumnInfoMap = Collections.unmodifiableMap(referrerLocalColumnInfoMap);
        _objectNativeType = referrerDBMeta.getEntityType();
        _propertyAccessType = propertyAccessType;
        _oneToOne = oneToOne;
        _reversePropertyName = reversePropertyName;
        // referrer property is not accessed in runtime so it doesn't need
        // (and don't want DB meta to be fat)
        //_propertyGateway = findPropertyGateway();
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
        for (ColumnInfo columnInfo : _localReferrerColumnInfoMap.keySet()) {
            if (columnInfo.getColumnDbName().equals(columnName)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsReferrerColumn(ColumnInfo referrerColumn) {
        return doContainsReferrerColumn(referrerColumn.getColumnDbName());
    }

    protected boolean doContainsReferrerColumn(String columnName) {
        for (ColumnInfo columnInfo : _referrerLocalColumnInfoMap.keySet()) {
            if (columnInfo.getColumnDbName().equals(columnName)) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                      Column Mapping
    //                                                                      ==============
    public ColumnInfo findLocalByReferrer(String referrerColumnDbName) {
        final ColumnInfo keyColumnInfo = _referrerDBMeta.findColumnInfo(referrerColumnDbName);
        final ColumnInfo resultColumnInfo = (ColumnInfo) _referrerLocalColumnInfoMap.get(keyColumnInfo);
        if (resultColumnInfo == null) {
            String msg = "Not found by referrerColumnDbName in referrerLocalColumnInfoMap:";
            msg = msg + " referrerColumnDbName=" + referrerColumnDbName;
            msg = msg + " referrerLocalColumnInfoMap=" + _referrerLocalColumnInfoMap;
            throw new IllegalArgumentException(msg);
        }
        return resultColumnInfo;
    }

    public ColumnInfo findReferrerByLocal(String localColumnDbName) {
        final ColumnInfo keyColumnInfo = _localDBMeta.findColumnInfo(localColumnDbName);
        final ColumnInfo resultColumnInfo = (ColumnInfo) _localReferrerColumnInfoMap.get(keyColumnInfo);
        if (resultColumnInfo == null) {
            String msg = "Not found by localColumnDbName in localReferrerColumnInfoMap:";
            msg = msg + " localColumnDbName=" + localColumnDbName;
            msg = msg + " localReferrerColumnInfoMap=" + _localReferrerColumnInfoMap;
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
     * Read the value to the entity. <br />
     * It returns plain value in entity as property access type.
     * @param <PROPERTY> The type of property, basically entity list.
     * @param localEntity The local entity of this column to read. (NotNull)
     * @return The read instance of referrer entity. (NullAllowed)
     */
    @SuppressWarnings("unchecked")
    public <PROPERTY> PROPERTY read(Entity localEntity) {
        return (PROPERTY) invokeMethod(getReadMethod(), localEntity, new Object[] {});
    }

    /**
     * Get the read method for entity reflection.
     * @return The read method, cached in this instance. (NotNull)
     */
    public Method getReadMethod() {
        return _readMethod;
    }

    // -----------------------------------------------------
    //                                                 Write
    //                                                 -----
    /**
     * Write the value to the entity. <br />
     * No converting to anything so check the property access type.
     * @param localEntity The local entity of this column to write. (NotNull)
     * @param referrerEntityList The written list of referrer entity. (NullAllowed: if null, null written)
     */
    public void write(Entity localEntity, Object referrerEntityList) {
        invokeMethod(getWriteMethod(), localEntity, new Object[] { referrerEntityList });
    }

    /**
     * Get the write method for entity reflection.
     * @return The writer method, cached in this instance. (NotNull)
     */
    public Method getWriteMethod() {
        return _writeMethod;
    }

    // -----------------------------------------------------
    //                                                Finder
    //                                                ------
    protected Method findReadMethod() {
        final Class<? extends Entity> localType = _localDBMeta.getEntityType();
        return _propertyMethodFinder.findReadMethod(localType, _referrerPropertyName, _propertyAccessType);
    }

    protected Method findWriteMethod() {
        final Class<? extends Entity> localType = _localDBMeta.getEntityType();
        return _propertyMethodFinder.findWriteMethod(localType, _referrerPropertyName, _propertyAccessType);
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
        return getReferrerPropertyName();
    }

    public DBMeta getTargetDBMeta() {
        return getReferrerDBMeta();
    }

    public Map<ColumnInfo, ColumnInfo> getLocalTargetColumnInfoMap() {
        return getLocalReferrerColumnInfoMap();
    }

    public boolean isReferrer() {
        return true;
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
        return _referrerPropertyName.hashCode() + _localDBMeta.hashCode() + _referrerDBMeta.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ReferrerInfo)) {
            return false;
        }
        final ReferrerInfo target = (ReferrerInfo) obj;
        if (!this._referrerPropertyName.equals(target.getReferrerPropertyName())) {
            return false;
        }
        if (!this._localDBMeta.equals(target.getLocalDBMeta())) {
            return false;
        }
        if (!this._referrerDBMeta.equals(target.getReferrerDBMeta())) {
            return false;
        }
        return true;
    }

    public String toString() {
        return _localDBMeta.getTableDbName() + "." + _referrerPropertyName + "<-" + _referrerDBMeta.getTableDbName();
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
     * For example, if the relation MEMBER and PURCHASE, this returns 'purchaseList'.
     * @return The string for property name. (NotNull)
     */
    public String getReferrerPropertyName() {
        return _referrerPropertyName;
    }

    /**
     * {@inheritDoc}
     */
    public DBMeta getLocalDBMeta() {
        return _localDBMeta;
    }

    /**
     * Get the DB meta of the referrer table. <br />
     * For example, if the relation MEMBER and PURCHASE, this returns PURCHASE's one.
     * @return The DB meta singleton instance. (NotNull)
     */
    public DBMeta getReferrerDBMeta() {
        return _referrerDBMeta;
    }

    /**
     * Get the read-only map, key is a local column info, value is a referrer column info.
     * @return The read-only map. (NotNull)
     */
    public Map<ColumnInfo, ColumnInfo> getLocalReferrerColumnInfoMap() {
        return _localReferrerColumnInfoMap;
    }

    /**
     * Get the read-only map, key is a referrer column info, value is a column column info.
     * @return The read-only map. (NotNull)
     */
    public Map<ColumnInfo, ColumnInfo> getReferrerLocalColumnInfoMap() {
        return _referrerLocalColumnInfoMap;
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
     * {@inheritDoc} <br />
     * But basically this returns false because DBFlute treats one-to-one relations as a foreign relation.  
     */
    public boolean isOneToOne() {
        return _oneToOne;
    }

    /**
     * {@inheritDoc}
     */
    public RelationInfo getReverseRelation() {
        return _reversePropertyName != null ? _referrerDBMeta.findRelationInfo(_reversePropertyName) : null;
    }
}
