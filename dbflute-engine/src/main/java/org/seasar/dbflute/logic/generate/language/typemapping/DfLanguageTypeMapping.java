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

/**
 * @author jflute
 */
public interface DfLanguageTypeMapping {

    // ===================================================================================
    //                                                                        Type Mapping
    //                                                                        ============
    /**
     * @return The map of 'JDBC to Java Native'. (NotNull)
     */
    Map<String, String> getJdbcToJavaNativeMap();

    // ===================================================================================
    //                                                                  Native Suffix List
    //                                                                  ==================
    /**
     * @return The list of suffix for string native type. (NotNull)
     */
    List<String> getStringList();

    /**
     * @return The list of suffix for number native type. (NotNull)
     */
    List<String> getNumberList();

    /**
     * @return The list of suffix for date native type. (NotNull)
     */
    List<String> getDateList();

    /**
     * @return The list of suffix for boolean native type. (NotNull)
     */
    List<String> getBooleanList();

    /**
     * @return The list of suffix for binary native type. (NotNull)
     */
    List<String> getBinaryList();

    // ===================================================================================
    //                                                                    Small Adjustment
    //                                                                    ================
    /**
     * @return The java native type as sequence value. (NotNull)
     */
    String getSequenceJavaNativeType();

    /**
     * @return The java native type as default numeric. (NotNull)
     */
    String getDefaultNumericJavaNativeType();

    /**
     * @return The java native type as default decimal. (NotNull)
     */
    String getDefaultDecimalJavaNativeType();

    /**
     * @return The JDBC type of UUID. (NullAllowed: null means unsupported)
     */
    String getJdbcTypeOfUUID();

    /**
     * @param plainTypeName The plain native type derived from test value, based on Java. (NotNull)
     * @return The switched or non-switched type. No switch for Java. (NotNull)
     */
    String switchParameterBeanTestValueType(String plainTypeName);

    /**
     * @param javaNative The java native type for mutable (normal) entity. (NotNull)
     * @return The java native type for immutable entity. (NotNull)
     */
    String convertToImmutableJavaNativeType(String javaNative);

    /**
     * @param immutableJavaNative The java native type for immutable entity. (NotNull)
     * @return The default value of java native type for immutable entity. (NotNull)
     */
    String convertToImmutableJavaNativeDefaultValue(String immutableJavaNative);

    /**
     * @param immutableJavaNative The java native type for immutable entity. (NotNull)
     * @param javaNative The java native type for mutable (normal) entity. (NotNull)
     * @param variable The expression of variable for the column. (NotNull)
     * @return The converted value to java native. (NotNull)
     */
    String convertToJavaNativeValueFromImmutable(String immutableJavaNative, String javaNative, String variable);
}
