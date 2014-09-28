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
package org.seasar.dbflute.logic.generate.language.typemapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.torque.engine.database.model.TypeMap;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 */
public class DfLanguageTypeMappingJava implements DfLanguageTypeMapping {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final Map<String, String> DEFAULT_EMPTY_MAP = DfCollectionUtil.newLinkedHashMap();
    protected static final String JAVA_NATIVE_BIGDECIMAL = "java.math.BigDecimal";

    protected static final List<String> _stringList = newArrayList("String");
    protected static final List<String> _numberList;
    static {
        _numberList = newArrayList("Byte", "Short", "Integer", "Long", "Float", "Double", "BigDecimal", "BigInteger");
    }
    protected static final List<String> _dateList = newArrayList("Date", "Time", "Timestamp");
    protected static final List<String> _booleanList = newArrayList("Boolean");
    protected static final List<String> _binaryList = newArrayList("byte[]");

    protected static <ELEMENT> ArrayList<ELEMENT> newArrayList(ELEMENT... elements) {
        return DfCollectionUtil.newArrayList(elements);
    }

    // ===================================================================================
    //                                                                        Type Mapping
    //                                                                        ============
    public Map<String, String> getJdbcToJavaNativeMap() {
        // Java native types are defined in TypeMap as default type
        // so this returns empty (this is special handling for Java)
        return DEFAULT_EMPTY_MAP;
    }

    // ===================================================================================
    //                                                                  Native Suffix List
    //                                                                  ==================
    public List<String> getStringList() {
        return _stringList;
    }

    public List<String> getNumberList() {
        return _numberList;
    }

    public List<String> getDateList() {
        return _dateList;
    }

    public List<String> getBooleanList() {
        return _booleanList;
    }

    public List<String> getBinaryList() {
        return _binaryList;
    }

    // ===================================================================================
    //                                                                    Small Adjustment
    //                                                                    ================
    public String getSequenceJavaNativeType() {
        return JAVA_NATIVE_BIGDECIMAL;
    }

    public String getDefaultNumericJavaNativeType() {
        return JAVA_NATIVE_BIGDECIMAL;
    }

    public String getDefaultDecimalJavaNativeType() {
        return JAVA_NATIVE_BIGDECIMAL;
    }

    public String getJdbcTypeOfUUID() {
        return TypeMap.UUID; // [UUID Headache]: The reason why UUID type has not been supported yet on JDBC.
    }

    public String switchParameterBeanTestValueType(String plainTypeName) {
        return plainTypeName;
    }

    public String convertToImmutableJavaNativeType(String javaNative) {
        return javaNative;
    }

    public String convertToImmutableJavaNativeDefaultValue(String immutableJavaNative) {
        return "null";
    }

    public String convertToJavaNativeValueFromImmutable(String immutableJavaNative, String javaNative, String variable) {
        return variable;
    }
}
