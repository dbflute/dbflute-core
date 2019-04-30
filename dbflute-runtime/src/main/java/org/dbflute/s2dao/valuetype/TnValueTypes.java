/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.s2dao.valuetype;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.dbflute.bhv.core.context.ResourceContext;
import org.dbflute.dbway.DBDef;
import org.dbflute.jdbc.ValueType;
import org.dbflute.s2dao.valuetype.basic.BigDecimalType;
import org.dbflute.s2dao.valuetype.basic.BigIntegerType;
import org.dbflute.s2dao.valuetype.basic.BinaryStreamType;
import org.dbflute.s2dao.valuetype.basic.BinaryType;
import org.dbflute.s2dao.valuetype.basic.BooleanType;
import org.dbflute.s2dao.valuetype.basic.ByteType;
import org.dbflute.s2dao.valuetype.basic.CharacterType;
import org.dbflute.s2dao.valuetype.basic.ClassificationType;
import org.dbflute.s2dao.valuetype.basic.DoubleType;
import org.dbflute.s2dao.valuetype.basic.FloatType;
import org.dbflute.s2dao.valuetype.basic.IntegerType;
import org.dbflute.s2dao.valuetype.basic.LocalDateAsSqlDateType;
import org.dbflute.s2dao.valuetype.basic.LocalDateAsTimestampType;
import org.dbflute.s2dao.valuetype.basic.LocalDateTimeAsTimestampType;
import org.dbflute.s2dao.valuetype.basic.LocalTimeAsTimeType;
import org.dbflute.s2dao.valuetype.basic.LongType;
import org.dbflute.s2dao.valuetype.basic.ObjectType;
import org.dbflute.s2dao.valuetype.basic.ShortType;
import org.dbflute.s2dao.valuetype.basic.SqlDateType;
import org.dbflute.s2dao.valuetype.basic.StringType;
import org.dbflute.s2dao.valuetype.basic.TimeType;
import org.dbflute.s2dao.valuetype.basic.TimestampType;
import org.dbflute.s2dao.valuetype.basic.UUIDAsDirectType;
import org.dbflute.s2dao.valuetype.basic.UUIDAsStringType;
import org.dbflute.s2dao.valuetype.basic.UtilDateAsSqlDateType;
import org.dbflute.s2dao.valuetype.basic.UtilDateAsTimestampType;
import org.dbflute.s2dao.valuetype.plugin.BytesType;
import org.dbflute.s2dao.valuetype.plugin.FixedLengthStringType;
import org.dbflute.s2dao.valuetype.plugin.ObjectBindingBigDecimalType;
import org.dbflute.s2dao.valuetype.plugin.OracleResultSetType;
import org.dbflute.s2dao.valuetype.plugin.PostgreSQLByteaType;
import org.dbflute.s2dao.valuetype.plugin.PostgreSQLOidType;
import org.dbflute.s2dao.valuetype.plugin.PostgreSQLResultSetType;
import org.dbflute.s2dao.valuetype.plugin.SerializableType;
import org.dbflute.s2dao.valuetype.plugin.StringClobType;

/**
 * @author modified by jflute (originated in Seasar2)
 */
public class TnValueTypes {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // basic (object)
    public static final ValueType STRING = new StringType();
    public static final ValueType CHARACTER = new CharacterType();
    public static final ValueType BYTE = new ByteType();
    public static final ValueType SHORT = new ShortType();
    public static final ValueType INTEGER = new IntegerType();
    public static final ValueType LONG = new LongType();
    public static final ValueType FLOAT = new FloatType();
    public static final ValueType DOUBLE = new DoubleType();
    public static final ValueType BIGDECIMAL = new BigDecimalType();
    public static final ValueType BIGINTEGER = new BigIntegerType();
    public static final ValueType TIME = new TimeType();
    public static final ValueType SQLDATE = new SqlDateType();
    public static final ValueType LOCALDATE_AS_SQLDATE = new LocalDateAsSqlDateType();
    public static final ValueType LOCALDATE_AS_TIMESTAMP = new LocalDateAsTimestampType();
    public static final ValueType LOCALDATETIME_AS_TIMESTAMP = new LocalDateTimeAsTimestampType();
    public static final ValueType LOCALTIME_AS_TIME = new LocalTimeAsTimeType();
    public static final ValueType UTILDATE_AS_SQLDATE = new UtilDateAsSqlDateType();
    public static final ValueType UTILDATE_AS_TIMESTAMP = new UtilDateAsTimestampType();
    public static final ValueType TIMESTAMP = new TimestampType();
    public static final ValueType BINARY = new BinaryType();
    public static final ValueType BINARY_STREAM = new BinaryStreamType();
    public static final ValueType BOOLEAN = new BooleanType();
    public static final ValueType UUID_AS_DIRECT = new UUIDAsDirectType();
    public static final ValueType UUID_AS_STRING = new UUIDAsStringType();

    // basic (interface)
    public static final ValueType CLASSIFICATION = new ClassificationType(); // DBFlute original class

    // basic (default)
    public static final ValueType DEFAULT_OBJECT = new ObjectType();

    // plug-in
    public static final ValueType STRING_CLOB = new StringClobType();
    public static final ValueType BYTES_BLOB = new BytesType(BytesType.BLOB_TRAIT);
    public static final ValueType POSTGRESQL_BYTEA = new PostgreSQLByteaType();
    public static final ValueType POSTGRESQL_OID = new PostgreSQLOidType();
    public static final ValueType FIXED_LENGTH_STRING = new FixedLengthStringType();
    public static final ValueType OBJECT_BINDING_BIGDECIMAL = new ObjectBindingBigDecimalType();

    // cursor
    public static final ValueType POSTGRESQL_RESULT_SET = new PostgreSQLResultSetType();
    public static final ValueType ORACLE_RESULT_SET = new OracleResultSetType();
    public static final ValueType SERIALIZABLE_BYTE_ARRAY = new SerializableType(BytesType.BYTES_TRAIT);

    // class type
    protected static final Class<?> BYTE_ARRAY_CLASS = new byte[0].getClass();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The map of plain value type keyed by DB definition object. (synchronized manually as transaction) */
    protected static final Map<DBDef, TnPlainValueTypes> _valueTypesMap = new ConcurrentHashMap<DBDef, TnPlainValueTypes>();

    /** The lock object to synchronize this static world. (NotNull) */
    protected static final Object _staticWorldLock = new Object();

    static {
        initialize();
    }

    protected static void initialize() {
        _valueTypesMap.put(DBDef.MySQL, createValueTypes());
        _valueTypesMap.put(DBDef.PostgreSQL, createValueTypes());
        {
            final TnPlainValueTypes valueTypes = createValueTypes();
            valueTypes.registerBasicValueType(java.util.Date.class, UTILDATE_AS_TIMESTAMP);
            valueTypes.registerBasicValueType(java.time.LocalDate.class, LOCALDATE_AS_TIMESTAMP);
            valueTypes.registerBasicValueType(java.time.LocalDateTime.class, LOCALDATETIME_AS_TIMESTAMP);
            _valueTypesMap.put(DBDef.Oracle, valueTypes);
        }
        _valueTypesMap.put(DBDef.DB2, createValueTypes());
        {
            final TnPlainValueTypes valueTypes = createValueTypes();
            valueTypes.registerBasicValueType(UUID.class, UUID_AS_STRING);
            _valueTypesMap.put(DBDef.SQLServer, valueTypes);
        }
        _valueTypesMap.put(DBDef.H2, createValueTypes());
        _valueTypesMap.put(DBDef.Derby, createValueTypes());
        _valueTypesMap.put(DBDef.SQLite, createValueTypes());
        _valueTypesMap.put(DBDef.Firebird, createValueTypes());
        _valueTypesMap.put(DBDef.MSAccess, createValueTypes());
        _valueTypesMap.put(DBDef.Unknown, createValueTypes());
    }

    protected static TnPlainValueTypes createValueTypes() {
        return new TnPlainValueTypes();
    }

    protected static TnPlainValueTypes getValueTypes() {
        final DBDef currentDBDef = ResourceContext.currentDBDef();
        return findValueTypes(currentDBDef);
    }

    protected static TnPlainValueTypes findValueTypes(DBDef dbdef) {
        assertObjectNotNull("dbdef", dbdef);
        TnPlainValueTypes valueTypes = _valueTypesMap.get(dbdef);
        if (valueTypes != null) {
            return valueTypes;
        }
        synchronized (_staticWorldLock) {
            valueTypes = _valueTypesMap.get(dbdef);
            if (valueTypes != null) {
                // previous thread might have initialized
                // or reading might failed by same-time writing
                return valueTypes;
            }
            valueTypes = new TnPlainValueTypes();
            _valueTypesMap.put(dbdef, valueTypes);
            return _valueTypesMap.get(dbdef);
        }
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    private TnValueTypes() {
    }

    // ===================================================================================
    //                                                                                Find
    //                                                                                ====
    // -----------------------------------------------------
    //                                                  Find
    //                                                  ----
    /**
     * Find a value type by a class type or an object instance. <br>
     * A class type is a prior searching key.
     * @param type The type of class. (NullAllowed: if null, searching by instance)
     * @param value The object value. (NullAllowed: if null, returns default object type)
     * @return The value type. (NotNull: if not found, returns object type)
     */
    public static ValueType findByTypeOrValue(Class<?> type, Object value) {
        return getValueTypes().findByTypeOrValue(type, value);
    }

    /**
     * Find a value type by an object instance or a definition type of JDBC. <br>
     * An object instance is a prior searching key.
     * @param value The object value. (NullAllowed: if null, returns dynamic object type)
     * @param jdbcDefType The definition type of JDBC. (NullAllowed: if null, searching by instance)
     * @return The value type. (NotNull: if not found, returns object type)
     */
    public static ValueType findByValueOrJdbcDefType(Object value, int jdbcDefType) {
        return getValueTypes().findByValueOrJdbcDefType(value, jdbcDefType);
    }

    // ===================================================================================
    //                                                                                 Get
    //                                                                                 ===
    // basically for Framework
    // -----------------------------------------------------
    //                                               byValue
    //                                               -------
    /**
     * Get the value type by object instance.
     * @param value The object value. (NullAllowed: if null, returns object type)
     * @return The value type. (NotNull: if not found, returns object type)
     */
    public static ValueType getValueType(Object value) {
        return getValueTypes().getValueType(value);
    }

    // -----------------------------------------------------
    //                                                byType
    //                                                ------
    /**
     * Get the value type by class type. <br>
     * The basic objects are prior to the basic interfaces basically,
     * but only when the ENUM is assignable from the class type, interfaces are prior.
     * Because frequently the ENUM has application own interfaces.
     * Actually Classification of DBFlute matches the pattern.
     * @param type The type of class. (NullAllowed: if null, returns object type)
     * @return The value type. (NotNull: if not found, returns object type)
     */
    public static ValueType getValueType(Class<?> type) {
        return getValueTypes().getValueType(type);
    }

    // -----------------------------------------------------
    //                                         byJdbcDefType
    //                                         -------------
    /**
     * @param jdbcDefType The definition type of JDBC.
     * @return The value type. (NotNull)
     */
    public static ValueType getValueType(int jdbcDefType) { // for no entity and so on
        return getValueTypes().getValueType(jdbcDefType);
    }

    // -----------------------------------------------------
    //                                      byName (Plug-in)
    //                                      ----------------
    /**
     * @param valueTypeName The name of value type. (NotNull)
     * @return The value type. (NullAllowed)
     */
    public static ValueType getPluginValueType(String valueTypeName) {
        return getValueTypes().getPluginValueType(valueTypeName);
    }

    // -----------------------------------------------------
    //                                               Default
    //                                               -------
    public static boolean isDefaultObject(ValueType valueType) {
        return getValueTypes().isDefaultObject(valueType);
    }

    public static boolean isDynamicObject(ValueType valueType) {
        return getValueTypes().isDynamicObject(valueType);
    }

    // ===================================================================================
    //                                                                            Register
    //                                                                            ========
    // basically for Application initializer (and Framework)
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    /**
     * Register the basic value type (managed per DBMS).
     * @param dbdef The definition of database. (NotNull)
     * @param keyType The key as type. (NotNull)
     * @param valueType The value type. (NotNull)
     */
    public static synchronized void registerBasicValueType(DBDef dbdef, Class<?> keyType, ValueType valueType) {
        findValueTypes(dbdef).registerBasicValueType(keyType, valueType);
    }

    /**
     * Remove the basic value type (managed per DBMS).
     * @param dbdef The definition of database. (NotNull)
     * @param keyType The key as type. (NotNull)
     */
    public static synchronized void removeBasicValueType(DBDef dbdef, Class<?> keyType) {
        findValueTypes(dbdef).removeBasicValueType(keyType);
    }

    // -----------------------------------------------------
    //                                               Plug-in
    //                                               -------
    /**
     * Register the plug-in value type.
     * @param dbdef The definition of database. (NotNull)
     * @param keyName The key as name. (NotNull)
     * @param valueType The value type. (NotNull)
     */
    public static synchronized void registerPluginValueType(DBDef dbdef, String keyName, ValueType valueType) {
        findValueTypes(dbdef).registerPluginValueType(keyName, valueType);
    }

    /**
     * Remove the plug-in value type.
     * @param dbdef The definition of database. (NotNull)
     * @param keyName The key as name. (NotNull)
     */
    public static synchronized void removePluginValueType(DBDef dbdef, String keyName) {
        findValueTypes(dbdef).removePluginValueType(keyName);
    }

    // ===================================================================================
    //                                                                             Restore
    //                                                                             =======
    protected static synchronized void restoreDefault(DBDef dbdef) { // as unit test utility
        findValueTypes(dbdef).restoreDefault();
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    /**
     * Assert that the object is not null.
     * @param variableName The check name of variable for message. (NotNull)
     * @param value The checked value. (NotNull)
     * @throws IllegalArgumentException When the argument is null.
     */
    protected static void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }
}