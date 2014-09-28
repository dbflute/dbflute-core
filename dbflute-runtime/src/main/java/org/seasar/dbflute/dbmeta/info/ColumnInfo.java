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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.seasar.dbflute.Entity;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.DBMeta.OptimisticLockType;
import org.seasar.dbflute.dbmeta.PropertyGateway;
import org.seasar.dbflute.dbmeta.PropertyMethodFinder;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.jdbc.Classification;
import org.seasar.dbflute.jdbc.ClassificationMeta;
import org.seasar.dbflute.util.DfReflectionUtil;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * The information of column.
 * @author jflute
 */
public class ColumnInfo {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The empty read-only list for empty property. */
    protected static final List<String> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<String>());

    /** The type name of JodaTime's just date. */
    protected static final String JODA_JUST_DATE_TYPE = "org.joda.time.LocalDate";

    /** The set of type name for JodaTime judgment. */
    protected static final Set<String> JODA_DATE_TYPE_SET;
    static {
        final Set<String> tmpSet = new HashSet<String>();
        tmpSet.add(JODA_JUST_DATE_TYPE);
        tmpSet.add("org.joda.time.LocalDateTime");
        tmpSet.add("org.joda.time.LocalTime");
        JODA_DATE_TYPE_SET = Collections.unmodifiableSet(tmpSet);
    }

    /** The type name of Java8Time's just date. */
    protected static final String JAVA8_JUST_DATE_TYPE = "java.time.LocalDate";

    /** The set of type name for Java8Time judgment. */
    protected static final Set<String> JAVA8_DATE_TYPE_SET;
    static {
        final Set<String> tmpSet = new HashSet<String>();
        tmpSet.add(JAVA8_JUST_DATE_TYPE);
        tmpSet.add("java.time.LocalDateTime");
        tmpSet.add("java.time.LocalTime");
        JAVA8_DATE_TYPE_SET = Collections.unmodifiableSet(tmpSet);
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DBMeta _dbmeta;
    protected final String _columnDbName;
    protected final ColumnSqlName _columnSqlName;
    protected final String _columnSynonym;
    protected final String _columnAlias;
    protected final String _propertyName;
    protected final Class<?> _objectNativeType;
    protected final Class<?> _propertyAccessType;
    protected final boolean _primary;
    protected final boolean _autoIncrement;
    protected final boolean _notNull;
    protected final String _columnDbType;
    protected final Integer _columnSize;
    protected final Integer _decimalDigits;
    protected final String _defaultValue;
    protected final boolean _commonColumn;
    protected final OptimisticLockType _optimisticLockType;
    protected final String _columnComment;
    protected final List<String> _foreignPropList;
    protected final List<String> _referrerPropList;
    protected final boolean _foreignKey;
    protected final ClassificationMeta _classificationMeta;
    protected final PropertyGateway _propertyGateway;
    protected final PropertyMethodFinder _propertyMethodFinder;
    protected final Method _readMethod;
    protected final Method _writeMethod;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ColumnInfo(DBMeta dbmeta // DB meta
            , String columnDbName, String columnSqlName, String columnSynonym, String columnAlias // column name
            , Class<?> objectNativeType, String propertyName, Class<?> propertyAccessType // property info
            , boolean primary, boolean autoIncrement, boolean notNull // column basic check
            , String columnDbType, Integer columnSize, Integer decimalDigits, String defaultValue // column type
            , boolean commonColumn, OptimisticLockType optimisticLockType, String columnComment // column others
            , List<String> foreignPropList, List<String> referrerPropList // relation property
            , ClassificationMeta classificationMeta, PropertyMethodFinder propertyMethodFinder // various info
    ) { // big constructor
        assertObjectNotNull("dbmeta", dbmeta);
        assertObjectNotNull("columnDbName", columnDbName);
        assertObjectNotNull("columnSqlName", columnSqlName);
        assertObjectNotNull("objectNativeType", objectNativeType);
        assertObjectNotNull("propertyName", propertyName);
        assertObjectNotNull("propertyAccessType", propertyAccessType);
        assertObjectNotNull("propertyMethodFinder", propertyMethodFinder);
        _dbmeta = dbmeta;
        _columnDbName = columnDbName;
        _columnSqlName = new ColumnSqlName(columnSqlName);
        _columnSynonym = columnSynonym;
        _columnAlias = columnAlias;
        _objectNativeType = objectNativeType;
        _propertyName = propertyName;
        _propertyAccessType = propertyAccessType;
        _primary = primary;
        _autoIncrement = autoIncrement;
        _notNull = notNull;
        _columnSize = columnSize;
        _columnDbType = columnDbType;
        _decimalDigits = decimalDigits;
        _defaultValue = defaultValue;
        _commonColumn = commonColumn;
        _optimisticLockType = optimisticLockType != null ? optimisticLockType : OptimisticLockType.NONE;
        _columnComment = columnComment;
        _foreignPropList = foreignPropList != null ? foreignPropList : EMPTY_LIST;
        _referrerPropList = referrerPropList != null ? referrerPropList : EMPTY_LIST;
        _foreignKey = foreignPropList != null && !foreignPropList.isEmpty();
        _classificationMeta = classificationMeta;
        _propertyGateway = findPropertyGateway();
        _propertyMethodFinder = propertyMethodFinder; // before finding
        _readMethod = findReadMethod();
        _writeMethod = findWriteMethod();
    }

    // ===================================================================================
    //                                                                          Reflection
    //                                                                          ==========
    // -----------------------------------------------------
    //                                               Gateway
    //                                               -------
    /**
     * Get the property gateway for the column.
     * @return The property gateway for the column, cached in this instance. (NotNull)
     */
    public PropertyGateway getPropertyGateway() {
        return _propertyGateway;
    }

    // -----------------------------------------------------
    //                                                  Read
    //                                                  ----
    /**
     * Read the value from the entity by its gateway (means no reflection). <br />
     * It returns plain value in entity as property access type.
     * @param <PROPERTY> The type of the property.
     * @param entity The target entity of this column to read. (NotNull)
     * @return The read value. (NullAllowed)
     */
    @SuppressWarnings("unchecked")
    public <PROPERTY> PROPERTY read(Entity entity) {
        return (PROPERTY) _propertyGateway.read(entity);
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
     * It contains the basic conversion, but no converting to optional so check the property access type.
     * @param entity The target entity of this column to write. (NotNull)
     * @param value The written value. (NullAllowed: if null, null value is written)
     */
    public void write(Entity entity, Object value) {
        _propertyGateway.write(entity, value);
    }

    /**
     * Get the write method for entity reflection.
     * @return The writer method, cached in this instance. (NotNull)
     */
    public Method getWriteMethod() { // basically unused in DBFlute, use gateway instead
        return _writeMethod;
    }

    // -----------------------------------------------------
    //                                               Generic
    //                                               -------
    /**
     * Get the generic type of property type for list property.
     * @return The type instance. (NullAllowed: when not list type)
     */
    public Class<?> getGenericType() {
        return DfReflectionUtil.getGenericType(getReadMethod().getGenericReturnType());
    }

    // -----------------------------------------------------
    //                                                Finder
    //                                                ------
    protected PropertyGateway findPropertyGateway() {
        final PropertyGateway gateway = _dbmeta.findPropertyGateway(_propertyName);
        if (gateway == null) { // no way
            String msg = "Not found the property gateway by the name: " + _propertyName;
            throw new IllegalStateException(msg);
        }
        return gateway;
    }

    protected Method findReadMethod() {
        final Class<? extends Entity> entityType = _dbmeta.getEntityType();
        return _propertyMethodFinder.findReadMethod(entityType, _propertyName, _propertyAccessType);
    }

    protected Method findWriteMethod() {
        final Class<? extends Entity> entityType = _dbmeta.getEntityType();
        return _propertyMethodFinder.findWriteMethod(entityType, _propertyName, _propertyAccessType);
    }

    // ===================================================================================
    //                                                                        Convert Type
    //                                                                        ============
    /**
     * Convert the value to object native type. <br />
     * @param <VALUE> The type of column value.
     * @param value The conversion target value. (NullAllowed: if null, returns null)
     * @return The converted value as object native type. (NullAllowed: when the value is null)
     */
    @SuppressWarnings("unchecked")
    public <VALUE> VALUE convertToObjectNativeType(Object value) {
        final VALUE result;
        if (value instanceof Collection<?>) {
            final Collection<?> valueList = (Collection<?>) value;
            final List<Object> resultList = new ArrayList<Object>(valueList.size());
            for (Object obj : valueList) {
                resultList.add(doConvertToObjectNativeType(obj));
            }
            result = (VALUE) resultList;
        } else {
            result = (VALUE) doConvertToObjectNativeType(value);
        }
        return result;
    }

    protected <VALUE> VALUE doConvertToObjectNativeType(Object value) {
        if (value != null && value instanceof Classification) {
            value = ((Classification) value).code();
        }
        if (value == null) {
            return null;
        }
        final Class<?> nativeType = _objectNativeType;
        final Object converted;
        if (Number.class.isAssignableFrom(nativeType)) {
            converted = DfTypeUtil.toNumber(value, nativeType);
        } else if (Timestamp.class.isAssignableFrom(nativeType)) {
            converted = DfTypeUtil.toTimestamp(value);
        } else if (Time.class.isAssignableFrom(nativeType)) {
            converted = DfTypeUtil.toTime(value);
        } else if (Date.class.isAssignableFrom(nativeType)) {
            converted = DfTypeUtil.toDate(value);
        } else if (Boolean.class.isAssignableFrom(nativeType)) {
            converted = DfTypeUtil.toBoolean(value);
        } else if (byte[].class.isAssignableFrom(nativeType)) {
            if (value instanceof Serializable) {
                converted = DfTypeUtil.toBinary((Serializable) value);
            } else {
                converted = value; // no change
            }
        } else if (UUID.class.isAssignableFrom(nativeType)) {
            converted = DfTypeUtil.toUUID(value);
        } else {
            converted = value;
        }
        @SuppressWarnings("unchecked")
        final VALUE result = (VALUE) converted;
        return result;
    }

    /**
     * @param <VALUE> The type of property value.
     * @param value The conversion target value. (NullAllowed)
     * @return The converted value as property type. (NullAllowed)
     * @deprecated use convertToObjectNativeType()
     */
    public <VALUE> VALUE toPropretyType(Object value) {
        return convertToObjectNativeType(value);
    }

    // ===================================================================================
    //                                                                         FlexibleKey
    //                                                                         ===========
    /**
     * Dive into flexible map by flexible keys.
     * @param flexibleMap The flexible map for column. (NotNull)
     */
    public void diveIntoFlexibleMap(Map<String, ColumnInfo> flexibleMap) {
        final List<String> flexibleKeyList = getFlexibleKeyList();
        for (String flexibleKey : flexibleKeyList) {
            flexibleMap.put(flexibleKey, this);
        }
    }

    /**
     * Get the list of flexible keys for this column.
     * @return The list of flexible keys. (NotNull)
     */
    protected List<String> getFlexibleKeyList() {
        return generateFlexibleKeyList(_columnDbName, _columnSynonym, _propertyName);
    }

    /**
     * Generate the list of flexible keys. (static utility)
     * @param columnDbName The DB name of column. (NotNull)
     * @param columnSynonym The DB synonym name of column. (NotNull)
     * @param propertyName The property name of column. (NotNull)
     * @return The list of flexible keys. (NotNull)
     */
    public static List<String> generateFlexibleKeyList(String columnDbName, String columnSynonym, String propertyName) {
        final List<String> keyList = new ArrayList<String>();
        keyList.add(columnDbName);
        if (columnSynonym != null) {
            keyList.add(columnSynonym);
        }
        keyList.add(propertyName);
        return keyList;
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
    @Override
    public int hashCode() {
        return _dbmeta.hashCode() + _columnDbName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ColumnInfo)) {
            return false;
        }
        final ColumnInfo target = (ColumnInfo) obj;
        if (!this._dbmeta.equals(target.getDBMeta())) {
            return false;
        }
        if (!this._columnDbName.equals(target.getColumnDbName())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(_dbmeta.getTableDbName());
        sb.append(".").append(_columnDbName);
        sb.append(":{");
        sb.append(_columnDbType);
        if (_columnSize != null) {
            sb.append("(").append(_columnSize);
            if (_decimalDigits != null) {
                sb.append(", ").append(_decimalDigits);
            }
            sb.append(")");
        }
        sb.append(", ").append(_objectNativeType.getName());
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the DB meta of the column's table.
     * @return The DB meta singleton instance. (NotNull)
     */
    public DBMeta getDBMeta() {
        return _dbmeta;
    }

    /**
     * Get the DB name of the column. <br />
     * This is for identity of column. (NOT for SQL)
     * @return The DB name of the column. (NotNull)
     */
    public String getColumnDbName() {
        return _columnDbName;
    }

    /**
     * Get the SQL name of the column. <br />
     * This is for SQL, which is resolved about schema prefix and quoted and so on...  
     * @return The SQL-name object of the column. (NotNull)
     */
    public ColumnSqlName getColumnSqlName() {
        return _columnSqlName;
    }

    /**
     * Get the synonym of the column. <br />
     * This is for the synonym of DBFlute. (for example, PgReservColumn handling)
     * @return The synonym of the column. (NullAllowed: when the column does not have its synonym)
     */
    public String getColumnSynonym() {
        return _columnSynonym;
    }

    /**
     * Get the alias of the column.
     * @return The alias of the column. (NullAllowed: when it cannot get an alias from meta)
     */
    public String getColumnAlias() {
        return _columnAlias;
    }

    /**
     * Get the native type mapped to object for the column. (NOT property access type) <br />
     * e.g. String even if optional is defined at getter/setter in entity. <br />
     * Also there is the other method that returns property access type.
     * @return The class type of property for the column. (NotNull)
     */
    public Class<?> getObjectNativeType() {
        return _objectNativeType;
    }

    /**
     * Is the object native type String? (assignable from)
     * @return The determination, true or false.
     */
    public boolean isObjectNativeTypeString() {
        return String.class.isAssignableFrom(_objectNativeType);
    }

    /**
     * Is the object native type Number? (assignable from)
     * @return The determination, true or false.
     */
    public boolean isObjectNativeTypeNumber() {
        return Number.class.isAssignableFrom(_objectNativeType);
    }

    /**
     * Is the object native type Date? (assignable from)
     * @return The determination, true or false.
     */
    public boolean isObjectNativeTypeDate() {
        return assignableObjectNativeTypeUtilDate() // traditional date
                || assignableObjectNativeTypeJodaDate() // JodaDate
                || assignableObjectNativeTypeJodaDate(); // Java8Date
    }

    protected boolean assignableObjectNativeTypeUtilDate() {
        return Date.class.isAssignableFrom(_objectNativeType);
    }

    protected boolean assignableObjectNativeTypeJodaDate() {
        return JODA_DATE_TYPE_SET.contains(_objectNativeType.getName());
    }

    protected boolean assignableObjectNativeTypeJava8Date() {
        return JAVA8_DATE_TYPE_SET.contains(_objectNativeType.getName());
    }

    /**
     * Is the object native type just (equals) Date? (assignable from)
     * @return The determination, true or false.
     */
    public boolean isObjectNativeTypeJustDate() {
        return justObjectNativeTypeUtilDate() // traditional date
                || justObjectNativeJodaDate() // JodaDate
                || justObjectNativeJava8Date(); // Java8Date
    }

    protected boolean justObjectNativeTypeUtilDate() {
        return Date.class.equals(_objectNativeType);
    }

    protected boolean justObjectNativeJodaDate() {
        return _objectNativeType.getName().equals(JODA_JUST_DATE_TYPE);
    }

    protected boolean justObjectNativeJava8Date() {
        return _objectNativeType.getName().equals(JAVA8_JUST_DATE_TYPE);
    }

    /**
     * Get the name of property for the column. (JavaBeansRule)
     * @return The name of property for the column. (NotNull)
     */
    public String getPropertyName() {
        return _propertyName;
    }

    /**
     * Get the type of property access for the column. <br />
     * It is defined at getter/setter in entity. (e.g. String or Optional) <br />
     * Also there is the other method that always returns object native type.
     * @return The class type to access the property. (NotNull)
     */
    public Class<?> getPropertyAccessType() {
        return _propertyAccessType;
    }

    /**
     * Get the type of property for the column. (NOT property access type) <br />
     * e.g. String even if optional is defined at getter/setter in entity.
     * @return The class type of property for the column. (NotNull)
     * @deprecated Use object native type
     */
    public Class<?> getPropertyType() {
        return getObjectNativeType();
    }

    /**
     * Is the property type String? (assignable from)
     * @return The determination, true or false.
     * @deprecated Use object native type
     */
    public boolean isPropertyTypeString() {
        return isObjectNativeTypeString();
    }

    /**
     * Is the property type Number? (assignable from)
     * @return The determination, true or false.
     * @deprecated Use object native type
     */
    public boolean isPropertyTypeNumber() {
        return isObjectNativeTypeNumber();
    }

    /**
     * Is the property type Date? (assignable from)
     * @return The determination, true or false.
     * @deprecated Use object native type
     */
    public boolean isPropertyTypeDate() {
        return isObjectNativeTypeDate();
    }

    /**
     * Is the column a part of primary keys?
     * @return The determination, true or false.
     */
    public boolean isPrimary() {
        return _primary;
    }

    /**
     * Is the column auto increment?
     * @return The determination, true or false.
     */
    public boolean isAutoIncrement() {
        return _autoIncrement;
    }

    /**
     * Is the column not null?
     * @return The determination, true or false.
     */
    public boolean isNotNull() {
        return _notNull;
    }

    /**
     * Get the DB type of the column.
     * @return The DB type of the column. (NotNull: If the type is unknown, it returns 'UnknownType'.)
     */
    public String getColumnDbType() {
        return _columnDbType;
    }

    /**
     * Get the size of the column.
     * @return The size of the column. (NullAllowed: If the type does not have size, it returns null.)
     */
    public Integer getColumnSize() {
        return _columnSize;
    }

    /**
     * Get the decimal digits of the column.
     * @return The decimal digits of the column. (NullAllowed: If the type does not have digits, it returns null.)
     */
    public Integer getDecimalDigits() {
        return _decimalDigits;
    }

    /**
     * Get the default value of the column. (as string)
     * @return The default value of the column. (NullAllowed)
     */
    public String getDefaultValue() {
        return _defaultValue;
    }

    /**
     * Is the column a part of common columns?
     * @return The determination, true or false.
     */
    public boolean isCommonColumn() {
        return _commonColumn;
    }

    /**
     * Is the column for optimistic lock?
     * @return The determination, true or false.
     */
    public boolean isOptimisticLock() {
        return isVersionNo() || isUpdateDate();
    }

    /**
     * Is the column version-no for optimistic lock?
     * @return The determination, true or false.
     */
    public boolean isVersionNo() {
        return OptimisticLockType.VERSION_NO == _optimisticLockType;
    }

    /**
     * Is the column update-date for optimistic lock?
     * @return The determination, true or false.
     */
    public boolean isUpdateDate() {
        return OptimisticLockType.UPDATE_DATE == _optimisticLockType;
    }

    /**
     * Get the comment of the column. <br />
     * If the real comment contains the alias,
     * this result does NOT contain it and its delimiter.  
     * @return The comment of the column. (NullAllowed: when it cannot get an alias from meta)
     */
    public String getColumnComment() {
        return _columnComment;
    }

    // these methods, get foreign/referrer info list, are not called
    // in core logic (e.g. mapping) so initialization is allowed to be here
    /**
     * Get the read-only list of the foreign info related to this column. <br />
     * It contains one-to-one relations.
     * @return The read-only list. (NotNull: when no FK, returns empty list)
     */
    public List<ForeignInfo> getForeignInfoList() {
        // find in this timing because initialization timing of column info is before FK's one.
        final List<ForeignInfo> foreignInfoList = new ArrayList<ForeignInfo>();
        for (String foreignProp : _foreignPropList) {
            foreignInfoList.add(getDBMeta().findForeignInfo(foreignProp));
        }
        return Collections.unmodifiableList(foreignInfoList); // as read-only
    }

    /**
     * Get the read-only list of the referrer info related to this column.
     * @return The read-only list. (NotNull: when no reference, returns empty list)
     */
    public List<ReferrerInfo> getReferrerInfoList() {
        // find in this timing because initialization timing of column info is before FK's one.
        final List<ReferrerInfo> referrerInfoList = new ArrayList<ReferrerInfo>();
        for (String fkProp : _referrerPropList) {
            referrerInfoList.add(getDBMeta().findReferrerInfo(fkProp));
        }
        return Collections.unmodifiableList(referrerInfoList); // as read-only
    }

    /**
     * Is the column a part of any foreign key?
     * @return The determination, true or false.
     */
    public boolean isForeignKey() {
        return _foreignKey;
    }

    /**
     * Get the meta of classification related to the column.
     * @return The instance of classification meta. (NullAllowed)
     */
    public ClassificationMeta getClassificationMeta() {
        return _classificationMeta;
    }
}
