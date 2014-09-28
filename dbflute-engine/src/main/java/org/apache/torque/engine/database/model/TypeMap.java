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
 *
 * And the following license definition is for Apache Torque.
 * DBFlute modified this source code and redistribute as same license 'Apache'.
 * /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * - - - - - - - - - -/
 */
package org.apache.torque.engine.database.model;

/* ====================================================================
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.sql.Types;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.logic.generate.language.typemapping.DfLanguageTypeMapping;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * A class that maps JDBC types to their corresponding
 * Java object types, and Java native types. Used
 * by Column.java to perform object/native mappings.
 *
 * These are the official SQL type to Java type mappings.
 * These don't quite correspond to the way the peer
 * system works so we'll have to make some adjustments.
 * <pre>
 * ------------------------------------------------------
 * JDBC Type     | Java Native          | CSharp Native |
 * ------------------------------------------------------
 * CHAR          | java.lang.String     | String        |
 * VARCHAR       | java.lang.String     | String        |
 * LONGVARCHAR   | java.lang.String     | String        |
 * NUMERIC       | java.math.BigDecimal | decimal?      |
 * DECIMAL       | java.math.BigDecimal | decimal?      |
 * TINYINT       | java.lang.Integer    | int?          |
 * SMALLINT      | java.lang.Integer    | int?          |
 * INTEGER       | java.lang.Integer    | int?          |
 * BIGINT        | java.lang.Long       | long?         |
 * REAL          | java.math.BigDecimal | decimal?      |
 * FLOAT         | java.math.BigDecimal | decimal?      |
 * DOUBLE        | java.math.BigDecimal | decimal?      |
 * DATE          | java.util.Date       | DateTime?     |
 * TIME          | java.sql.Time        | DateTime?     |
 * TIMESTAMP     | java.sql.Timestamp   | DateTime?     |
 * BIT           | java.lang.Boolean    | bool?         |
 * BOOLEAN       | java.lang.Boolean    | bool?         |
 * BINARY        | byte[]               | byte[]        |
 * VARBINARY     | byte[]               | byte[]        |
 * LONGVARBINARY | byte[]               | byte[]        |
 * BLOB          | byte[]               | byte[]        |
 * ARRAY         | *Unsupported         | *Unsupported  |
 * UUID          | java.util.UUID       | *Unsupported  |
 * ------------------------------------------------------
 * </pre>
 * ARRAY type is basically unsupported but it's defined for user's extension.
 * UUID type that does not exist in JDBC is supported as special handling.
 * @author modified by jflute (originated in Apache Torque)
 */
public class TypeMap {

    // ===================================================================================
    //                                                                                 Log
    //                                                                                 ===
    /** Log instance. */
    public static final Log _log = LogFactory.getLog(TypeMap.class);

    // ===================================================================================
    //                                                                           JDBC Type
    //                                                                           =========
    // -----------------------------------------------------
    //                                              Embedded
    //                                              --------
    public static final String CHAR = "CHAR";
    public static final String VARCHAR = "VARCHAR";
    public static final String LONGVARCHAR = "LONGVARCHAR";
    public static final String CLOB = "CLOB";
    public static final String NUMERIC = "NUMERIC";
    public static final String DECIMAL = "DECIMAL";
    public static final String TINYINT = "TINYINT";
    public static final String SMALLINT = "SMALLINT";
    public static final String INTEGER = "INTEGER";
    public static final String BIGINT = "BIGINT";
    public static final String REAL = "REAL";
    public static final String FLOAT = "FLOAT";
    public static final String DOUBLE = "DOUBLE";
    public static final String DATE = "DATE";
    public static final String TIME = "TIME";
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String BIT = "BIT";
    public static final String BOOLEAN = "BOOLEAN";
    public static final String BINARY = "BINARY";
    public static final String VARBINARY = "VARBINARY";
    public static final String LONGVARBINARY = "LONGVARBINARY";
    public static final String BLOB = "BLOB";
    public static final String ARRAY = "ARRAY";
    public static final String OTHER = "OTHER";

    // -----------------------------------------------------
    //                                              Original
    //                                              --------
    public static final String UUID = "UUID";

    // ===================================================================================
    //                                                                         Java Native
    //                                                                         ===========
    // This is default native type(for Java).
    public static final String CHAR_NATIVE_TYPE = "String";
    public static final String VARCHAR_NATIVE_TYPE = "String";
    public static final String LONGVARCHAR_NATIVE_TYPE = "String";
    public static final String CLOB_NATIVE_TYPE = "String";
    public static final String NUMERIC_NATIVE_TYPE = "java.math.BigDecimal";
    public static final String DECIMAL_NATIVE_TYPE = "java.math.BigDecimal";
    public static final String TINYINT_NATIVE_TYPE = "Integer";
    public static final String SMALLINT_NATIVE_TYPE = "Integer";
    public static final String INTEGER_NATIVE_TYPE = "Integer";
    public static final String BIGINT_NATIVE_TYPE = "Long";
    public static final String REAL_NATIVE_TYPE = "java.math.BigDecimal";
    public static final String FLOAT_NATIVE_TYPE = "java.math.BigDecimal";
    public static final String DOUBLE_NATIVE_TYPE = "java.math.BigDecimal";
    public static final String DATE_NATIVE_TYPE = "java.util.Date";
    public static final String TIME_NATIVE_TYPE = "java.sql.Time";
    public static final String TIMESTAMP_NATIVE_TYPE = "java.sql.Timestamp";
    public static final String BIT_NATIVE_TYPE = "Boolean";
    public static final String BOOLEAN_NATIVE_TYPE = "Boolean";
    public static final String BINARY_NATIVE_TYPE = "byte[]";
    public static final String VARBINARY_NATIVE_TYPE = "byte[]";
    public static final String LONGVARBINARY_NATIVE_TYPE = "byte[]";
    public static final String BLOB_NATIVE_TYPE = "byte[]";
    public static final String ARRAY_NATIVE_TYPE = "String";
    public static final String UUID_NATIVE_TYPE = "java.util.UUID";
    public static final String OTHER_NATIVE_TYPE = "Object";

    // ===================================================================================
    //                                                                            Type Map
    //                                                                            ========
    private static final Map<String, String> _jdbcTypeToJavaNativeMap = DfCollectionUtil.newHashMap();
    private static final Map<Integer, String> _jdbcDefValueToJdbcTypeMap = DfCollectionUtil.newHashMap();
    private static final Map<String, Integer> _jdbcTypeToJdbcDefValueMap = DfCollectionUtil.newHashMap();
    private static final Map<String, String> _javaNativeToFlexNativeMap = DfCollectionUtil.newHashMap();

    // ===================================================================================
    //                                                        Property jdbcToJavaNativeMap
    //                                                        ============================
    protected static Map<String, String> _propertyJdbcTypeToJavaNativeMap;
    protected static Map<String, String> _propertyJavaNativeToFlexNativeMap;
    static {
        setupPropertyNativeMap();
    }

    protected static void setupPropertyNativeMap() {
        final DfBuildProperties prop = getProperties();
        _propertyJdbcTypeToJavaNativeMap = prop.getTypeMappingProperties().getJdbcToJavaNativeMap();
        _propertyJavaNativeToFlexNativeMap = prop.getFlexDtoProperties().getJavaToFlexNativeMap();
    }

    // ===================================================================================
    //                                                                    Initialized Mark
    //                                                                    ================
    private static boolean _initialized = false;

    // ===================================================================================
    //                                                                              Reload
    //                                                                              ======
    public static void reload() { // for test
        setupPropertyNativeMap();
        _initialized = false;
        initialize();
    }

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initializes the SQL to Java map so that it
     * can be used by client code.
     */
    public synchronized static void initialize() {
        if (_initialized) {
            return;
        }

        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // The map of JDBC Type to Java Native
        // _/_/_/_/_/_/_/_/_/_/
        // initialize specified type in property or default type
        // (default types are for Java so no definition in language dependencies of Java)
        _jdbcTypeToJavaNativeMap.put(CHAR, initializeJavaNative(CHAR, CHAR_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(VARCHAR, initializeJavaNative(VARCHAR, VARCHAR_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(LONGVARCHAR, initializeJavaNative(LONGVARCHAR, LONGVARCHAR_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(CLOB, initializeJavaNative(CLOB, CLOB_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(NUMERIC, initializeJavaNative(NUMERIC, getDefaultNumericJavaNativeType()));
        _jdbcTypeToJavaNativeMap.put(DECIMAL, initializeJavaNative(DECIMAL, getDefaultDecimalJavaNativeType()));
        _jdbcTypeToJavaNativeMap.put(TINYINT, initializeJavaNative(TINYINT, TINYINT_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(SMALLINT, initializeJavaNative(SMALLINT, SMALLINT_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(INTEGER, initializeJavaNative(INTEGER, INTEGER_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(BIGINT, initializeJavaNative(BIGINT, BIGINT_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(REAL, initializeJavaNative(REAL, REAL_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(FLOAT, initializeJavaNative(FLOAT, FLOAT_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(DOUBLE, initializeJavaNative(DOUBLE, DOUBLE_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(DATE, initializeJavaNative(DATE, DATE_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(TIME, initializeJavaNative(TIME, TIME_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(TIMESTAMP, initializeJavaNative(TIMESTAMP, TIMESTAMP_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(BIT, initializeJavaNative(BIT, BIT_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(BOOLEAN, initializeJavaNative(BOOLEAN, BOOLEAN_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(BINARY, initializeJavaNative(BINARY, BINARY_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(VARBINARY, initializeJavaNative(VARBINARY, VARBINARY_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(LONGVARBINARY, initializeJavaNative(LONGVARBINARY, LONGVARBINARY_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(BLOB, initializeJavaNative(BLOB, BLOB_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(ARRAY, initializeJavaNative(ARRAY, ARRAY_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(UUID, initializeJavaNative(UUID, UUID_NATIVE_TYPE));
        _jdbcTypeToJavaNativeMap.put(OTHER, initializeJavaNative(OTHER, OTHER_NATIVE_TYPE));

        // register new JDBC type from property
        for (Entry<String, String> entry : _propertyJdbcTypeToJavaNativeMap.entrySet()) {
            final String propertyJdbcType = entry.getKey();
            if (_jdbcTypeToJavaNativeMap.containsKey(propertyJdbcType)) {
                continue; // because only new type needs to be registered here
            }
            _jdbcTypeToJavaNativeMap.put(propertyJdbcType, entry.getValue());
        }

        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // The map of JDBC Definition-Value to JDBC Type
        // _/_/_/_/_/_/_/_/_/_/
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.CHAR), CHAR);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.VARCHAR), VARCHAR);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.LONGVARCHAR), LONGVARCHAR);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.CLOB), CLOB);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.NUMERIC), NUMERIC);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.DECIMAL), DECIMAL);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.TINYINT), TINYINT);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.SMALLINT), SMALLINT);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.INTEGER), INTEGER);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.BIGINT), BIGINT);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.REAL), REAL);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.FLOAT), FLOAT);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.DOUBLE), DOUBLE);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.BIT), BIT);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.BOOLEAN), BOOLEAN);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.DATE), DATE);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.TIME), TIME);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.TIMESTAMP), TIMESTAMP);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.BINARY), BINARY);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.VARBINARY), VARBINARY);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.LONGVARBINARY), LONGVARBINARY);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.BLOB), BLOB);
        // [UUID Headache]: UUID has not been supported yet on JDBC.
        //_jdbcDefValueToJdbcTypeMap.put(new Integer(Types.UUID), UUID);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.ARRAY), ARRAY);
        _jdbcDefValueToJdbcTypeMap.put(new Integer(Types.OTHER), OTHER);

        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // The map of JDBC Type to JDBC Definition-Value
        // _/_/_/_/_/_/_/_/_/_/
        for (Entry<Integer, String> entry : _jdbcDefValueToJdbcTypeMap.entrySet()) {
            final Integer jdbcDefValue = entry.getKey();
            final String jdbcType = entry.getValue();
            _jdbcTypeToJdbcDefValueMap.put(jdbcType, jdbcDefValue);
        }

        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // The map of Java Native to Flex Native
        // _/_/_/_/_/_/_/_/_/_/
        _javaNativeToFlexNativeMap.put("String", initializeFlexNative("String", "String"));
        _javaNativeToFlexNativeMap.put("Short", initializeFlexNative("Short", "int"));
        _javaNativeToFlexNativeMap.put("Integer", initializeFlexNative("Integer", "int"));
        _javaNativeToFlexNativeMap.put("Long", initializeFlexNative("Long", "Number"));
        _javaNativeToFlexNativeMap.put("Float", initializeFlexNative("Float", "Number"));
        _javaNativeToFlexNativeMap.put("Double", initializeFlexNative("Double", "Number"));
        _javaNativeToFlexNativeMap.put("Number", initializeFlexNative("Number", "Number"));
        _javaNativeToFlexNativeMap.put("Boolean", initializeFlexNative("Boolean", "Boolean"));
        _javaNativeToFlexNativeMap.put("java.math.BigDecimal", initializeFlexNative("java.math.BigDecimal", "Number"));
        _javaNativeToFlexNativeMap.put("java.util.Date", initializeFlexNative("java.util.Date", "Date"));
        _javaNativeToFlexNativeMap.put("java.sql.Time", initializeFlexNative("java.sql.Time", "Date"));
        _javaNativeToFlexNativeMap.put("java.sql.Timestamp", initializeFlexNative("java.sql.Timestamp", "Date"));
        _javaNativeToFlexNativeMap.put("byte[]", initializeFlexNative("byte[]", "Object"));
        _javaNativeToFlexNativeMap.put("Object", initializeFlexNative("Object", "Object"));

        // This class has initialized now!
        _initialized = true;
    }

    /**
     * @param jdbcType The type as string for JDBC. (NotNull)
     * @param defaultJavaNative The default type as string for java native. (NotNull)
     * @return Java-native. (NotNull: If the key does not have an element, it returns default type.)
     */
    protected static String initializeJavaNative(String jdbcType, String defaultJavaNative) {
        final String javaNative = _propertyJdbcTypeToJavaNativeMap.get(jdbcType);
        return javaNative != null ? javaNative : defaultJavaNative;
    }

    /**
     * @param javaNative The type as string for java native. (NotNull)
     * @param defaultFlexNative The default type as string for flex native. (NotNull)
     * @return Java-native. (NotNull: If the key does not have an element, it returns default type.)
     */
    protected static String initializeFlexNative(String javaNative, String defaultFlexNative) {
        final String flexNative = _propertyJavaNativeToFlexNativeMap.get(javaNative);
        return flexNative != null ? flexNative : defaultFlexNative;
    }

    protected static void initializeIfNeeds() {
        if (!_initialized) {
            initialize();
        }
    }

    // ===================================================================================
    //                                                                           JDBC Type
    //                                                                           =========
    /**
     * @param jdbcDefValue The JDBC definition value to find. (NotNull)
     * @return The type as JDBC. (NullAllowed: when not found)
     */
    public static String findJdbcTypeByJdbcDefValue(Integer jdbcDefValue) {
        initializeIfNeeds();
        final String jdbcType = _jdbcDefValueToJdbcTypeMap.get(jdbcDefValue);
        if (Srl.is_Null_or_TrimmedEmpty(jdbcType)) {
            return null;
        }
        return jdbcType;
    }

    /**
     * @param jdbcType The JDBC type to find. (NotNull)
     * @return The found JDBC definition value. (NotNull: if not found, returns OTHER)
     */
    public static Integer getJdbcDefValueByJdbcType(String jdbcType) {
        initializeIfNeeds();
        final Integer defValue = _jdbcTypeToJdbcDefValueMap.get(jdbcType);
        if (defValue == null) {
            return Types.OTHER;
        }
        return defValue;
    }

    public static boolean isJdbcTypeChar(String jdbcType) {
        return CHAR.equals(jdbcType);
    }

    public static boolean isJdbcTypeClob(String jdbcType) {
        return CLOB.equals(jdbcType);
    }

    public static boolean isJdbcTypeDate(String jdbcType) {
        return DATE.equals(jdbcType);
    }

    public static boolean isJdbcTypeTimestamp(String jdbcType) {
        return TIMESTAMP.equals(jdbcType);
    }

    public static boolean isJdbcTypeTime(String jdbcType) {
        return TIME.equals(jdbcType);
    }

    public static boolean isJdbcTypeBlob(String jdbcType) {
        return BLOB.equals(jdbcType);
    }

    // ===================================================================================
    //                                                                         Native Type
    //                                                                         ===========
    // -----------------------------------------------------
    //                                           Java Native
    //                                           -----------
    // *Java Native is NOT always FQCN (For example, String and CSharp's type)
    /**
     * Find java native type by JDBC type.
     * @param jdbcType The type of JDBC. (NotNull)
     * @param columnSize The size of column. (NullAllowed: if null, returns numeric or decimal)
     * @param decimalDigits The decimal digits. (NullAllowed: if null, returns numeric or decimal)
     * @return The string expression of java native type. (NotNull)
     */
    public static String findJavaNativeByJdbcType(String jdbcType, Integer columnSize, Integer decimalDigits) {
        initializeIfNeeds();
        if (Srl.is_Null_or_TrimmedEmpty(jdbcType)) {
            throw new IllegalArgumentException("The argument 'jdbcType' should not be null.");
        }
        final String javaType = doFindJavaNativeByJdbcType(jdbcType);
        final String autoMappingNumber = processAutoMappingNumber(jdbcType, columnSize, decimalDigits, javaType);
        if (autoMappingNumber != null) {
            return autoMappingNumber;
        }
        return javaType;
    }

    protected static String processAutoMappingNumber(String jdbcType, Integer columnSize, Integer decimalDigits,
            String javaType) {
        if (needsAutoMappingNumber(jdbcType, javaType)) {
            final String defaultJavaNativeType;
            if (NUMERIC.equalsIgnoreCase(jdbcType)) {
                defaultJavaNativeType = getDefaultNumericJavaNativeType();
            } else { // DECIMAL
                defaultJavaNativeType = getDefaultDecimalJavaNativeType();
            }
            if (columnSize == null || columnSize == 0) { // cannot judge about auto-mapping
                return defaultJavaNativeType;
            }
            if (decimalDigits != null && decimalDigits > 0) { // has decimal digits
                return defaultJavaNativeType;
            }
            // columnSize > 0 && (decimalDigits == null || decimalDigits == 0) here
            if (columnSize > 9) {
                if (columnSize > 18) {
                    final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
                    if (prop.isCompatibleAutoMappingOldStyle()) {
                        return doFindJavaNativeByJdbcType(BIGINT); // old style
                    } else {
                        return defaultJavaNativeType;
                    }
                } else {
                    return doFindJavaNativeByJdbcType(BIGINT);
                }
            } else {
                return doFindJavaNativeByJdbcType(INTEGER);
            }
        }
        return null;
    }

    protected static String doFindJavaNativeByJdbcType(String jdbcType) {
        initializeIfNeeds();
        if (!_jdbcTypeToJavaNativeMap.containsKey(jdbcType)) {
            String msg = "_jdbcTypeToJavaNativeMap doesn't contain the type as key: ";
            msg = msg + "key=" + jdbcType + " map=" + _jdbcTypeToJavaNativeMap;
            _log.warn(msg);
            throw new IllegalStateException(msg);
        }
        return _jdbcTypeToJavaNativeMap.get(jdbcType);
    }

    protected static boolean needsAutoMappingNumber(String jdbcType, final String javaType) {
        return isAutoMappingTargetNumberType(jdbcType) && javaType.equalsIgnoreCase("$$AutoMapping$$");
    }

    protected static boolean isAutoMappingTargetNumberType(String torqueType) {
        return NUMERIC.equals(torqueType) || DECIMAL.equals(torqueType);
    }

    // -----------------------------------------------------
    //                                           FLEX Native
    //                                           -----------
    public static String findFlexNativeByJavaNative(String javaNative) {
        return doFindFlexNativeByJavaNative(javaNative);
    }

    protected static String doFindFlexNativeByJavaNative(String javaNative) {
        initializeIfNeeds();
        if (!_javaNativeToFlexNativeMap.containsKey(javaNative)) {
            String msg = "_javaNativeToFlexNativeMap doesn't contain the type as key: ";
            msg = msg + "key=" + javaNative + " map=" + _javaNativeToFlexNativeMap;
            _log.warn(msg);
            throw new IllegalStateException(msg);
        }
        return _javaNativeToFlexNativeMap.get(javaNative);
    }

    // -----------------------------------------------------
    //                                        Default Native
    //                                        --------------
    public static String getDefaultNumericJavaNativeType() {
        final DfLanguageTypeMapping mapping = getBasicProperties().getLanguageDependency().getLanguageTypeMapping();
        return mapping.getDefaultNumericJavaNativeType();
    }

    public static String getDefaultDecimalJavaNativeType() {
        final DfLanguageTypeMapping mapping = getBasicProperties().getLanguageDependency().getLanguageTypeMapping();
        return mapping.getDefaultDecimalJavaNativeType();
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected static DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected static DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected static DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return getProperties().getLittleAdjustmentProperties();
    }
}
