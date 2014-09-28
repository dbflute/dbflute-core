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

import java.util.List;
import java.util.Map;

import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfLanguageTypeMappingCSharp implements DfLanguageTypeMapping {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final Map<String, String> _jdbcToJavaNativeMap;
    static {
        final Map<String, String> map = DfCollectionUtil.newLinkedHashMap();
        map.put("CHAR", "String");
        map.put("VARCHAR", "String");
        map.put("LONGVARCHAR", "String");
        map.put("NUMERIC", "decimal?");
        map.put("DECIMAL", "decimal?");
        map.put("BIT", "bool?");
        map.put("TINYINT", "int?");
        map.put("SMALLINT", "int?");
        map.put("INTEGER", "int?");
        map.put("BIGINT", "long?");
        map.put("REAL", "decimal?");
        map.put("FLOAT", "decimal?");
        map.put("DOUBLE", "decimal?");
        map.put("DATE", "DateTime?");
        map.put("TIME", "DateTime?");
        map.put("TIMESTAMP", "DateTime?");
        _jdbcToJavaNativeMap = map;
    }
    protected static final List<String> _stringList = DfCollectionUtil.newArrayList("String");
    protected static final List<String> _numberList = DfCollectionUtil.newArrayList("decimal?", "int?", "long?");
    protected static final List<String> _dateList = DfCollectionUtil.newArrayList("DateTime?");
    protected static final List<String> _booleanList = DfCollectionUtil.newArrayList("bool?");
    protected static final List<String> _binaryList = DfCollectionUtil.newArrayList("byte[]");

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    public Map<String, String> getJdbcToJavaNativeMap() {
        return _jdbcToJavaNativeMap;
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
    //                                                                JDBC Type Adjustment
    //                                                                ====================
    public String getSequenceJavaNativeType() {
        return "int?"; // #future jflute long?
    }

    public String getDefaultNumericJavaNativeType() {
        return "decimal?";
    }

    public String getDefaultDecimalJavaNativeType() {
        return "decimal?";
    }

    public String getJdbcTypeOfUUID() {
        return null; // does CSharp support it?
    }

    public String switchParameterBeanTestValueType(String plainTypeName) {
        if (Srl.equalsPlain(plainTypeName, "BigDecimal")) {
            return "decimal?";
        } else if (Srl.equalsPlain(plainTypeName, "Long")) {
            return "long?";
        } else if (Srl.equalsPlain(plainTypeName, "Integer")) {
            return "int?";
        } else if (Srl.equalsPlain(plainTypeName, "Date", "Timestamp", "Time")) {
            return "DateTime?";
        } else if (Srl.equalsPlain(plainTypeName, "Boolean")) {
            return "bool?";
        } else if (Srl.equalsPlain(plainTypeName, "boolean")) {
            return "bool";
        } else if (Srl.isQuotedAnything(plainTypeName, "List<", ">")) {
            final String elementType = Srl.unquoteAnything(plainTypeName, "List<", ">");
            return "IList<" + switchParameterBeanTestValueType(elementType) + ">";
        } else {
            return plainTypeName;
        }
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
