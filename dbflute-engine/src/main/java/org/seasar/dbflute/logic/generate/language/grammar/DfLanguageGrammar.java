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
package org.seasar.dbflute.logic.generate.language.grammar;

import java.util.List;

import org.apache.torque.engine.database.model.Column;

/**
 * @author jflute
 */
public interface DfLanguageGrammar {

    // ===================================================================================
    //                                                                       Basic Keyword
    //                                                                       =============
    /**
     * @return The file extension of class. (NotNull)
     */
    String getClassFileExtension();

    /**
     * @return The mark of 'extends'. (NotNull)
     */
    String getExtendsMark();

    /**
     * @return The mark of 'implements'. (NotNull)
     */
    String getImplementsMark();

    /**
     * @return The delimiter of 'implements'. (NotNull)
     */
    String getImplementsDelimiter();

    /**
     * Is the 'extends' area same as 'implements' area?
     * @return The determination, true or false.
     */
    boolean isSameAreaExtendsImplements();

    /**
     * @return The modifier of 'public'. e.g. 'public' (NotNull)
     */
    String getPublicModifier();

    /**
     * @return The modifier of 'protected'. e.g. 'protected' (NotNull)
     */
    String getProtectedModifier();

    /**
     * @return The definition of 'public'. e.g. 'public final' (NotNull)
     */
    String getPublicFinal();

    /**
     * @return The definition of 'public static'. e.g. 'public static final' (NotNull)
     */
    String getPublicStaticFinal();

    /**
     * @return The begin mark of generic. e.g. '<' (NotNull)
     */
    String getGenericBeginMark();

    /**
     * @return The end mark of generic. e.g. '>' (NotNull)
     */
    String getGenericEndMark();

    // ===================================================================================
    //                                                              Programming Expression
    //                                                              ======================
    /**
     * @param type The type name of program data. (NotNull)
     * @param variable The variable name. (NotNull)
     * @return The simple definition expression of the variable. e.g. 'String name' (NotNull)
     */
    String buildVariableSimpleDefinition(String type, String variable);

    /**
     * @param methodName The method name that might not be adjusted. (NotNull)
     * @return The initial-character-adjusted name for the method. (NotNull)
     */
    String adjustMethodInitialChar(String methodName);

    /**
     * @param propertyName The property name that might not be adjusted. (NotNull)
     * @return The initial-character-adjusted name for the method. (NotNull)
     */
    String adjustPropertyInitialChar(String propertyName);

    /**
     * @param propertyName The pure (no call expression) property name. (NotNull)
     * @return The property-suffix-adjusted name for the method. (NotNull)
     */
    String buildPropertyGetterCall(String propertyName);

    /**
     * @param className The name of class. (NotNull)
     * @return The type literal of the class. (NotNull)
     */
    String buildClassTypeLiteral(String className);

    /**
     * @param element The element type for list generic. (NotNull)
     * @return The definition of 'List&lt;element&gt;'. (NotNull)
     */
    String buildGenericListClassName(String element);

    /**
     * @param key The key type for map generic. (NotNull)
     * @param value The value type for map generic. (NotNull)
     * @return The definition of 'List&lt;Map&lt;key, value&gt;&gt;'. (NotNull)
     */
    String buildGenericMapListClassName(String key, String value);

    /**
     * @param first The only-one type name for the generic. (NotNull)
     * @return The definition of '&lt;first&gt;'. (NotNull)
     */
    String buildGenericOneClassHint(String first);

    /**
     * @param first The first type name for the generic. (NotNull)
     * @param second The second type name for the generic. (NotNull)
     * @return The definition of '&lt;first, second&gt;'. (NotNull)
     */
    String buildGenericTwoClassHint(String first, String second);

    /**
     * @param first The first type name for the generic. (NotNull)
     * @param second The second type name for the generic. (NotNull)
     * @param third The third type name for the generic. (NotNull)
     * @return The definition of '&lt;first, second, third&gt;'. (NotNull)
     */
    String buildGenericThreeClassHint(String first, String second, String third);

    /**
     * @param className The class name that has generic element. (NotNull)
     * @param genericExp The expression of generic to be split. (NotNull)
     * @return The determination, true or false.
     */
    boolean hasGenericClassElement(String className, String genericExp);

    /**
     * @param className The class name that has generic element. (NotNull)
     * @param genericExp The expression of generic to be split. (NotNull)
     * @return The string of extracted generic element. (NullAllowed: when not found)
     */
    String extractGenericClassElement(String className, String genericExp);

    /**
     * @param fromCol The column object to get. (NotNull)
     * @param toCol The column object to set. (NotNull)
     * @return The string expression of mapping logic by getter and setter. (NotNull)
     */
    String buildEntityPropertyGetSet(Column fromCol, Column toCol);

    /**
     * @param col The column object to build. (NotNull)
     * @return The string expression of entity property name. (NotNUll)
     */
    String buildEntityPropertyName(Column col);

    /**
     * @param cdefBase The CDef expression, e.g. CDef.MemberStatus.Formalized. (NotNull)
     * @param propertyName The property name for the CDef. (NotNull)
     * @param valueType The property type of the value. (NotNull)
     * @param toNumber Does it convert to number?
     * @param toBoolean Does it convert to boolean?
     * @return The CDef element value. e.g. CDef.MemberStatus.Formalized.code() (NotNull)
     */
    String buildCDefElementValue(String cdefBase, String propertyName, String valueType, boolean toNumber,
            boolean toBoolean);

    /**
     * @return The new expression of list with elements as one liner. (NotNull)
     */
    String buildOneLinerListNewBackStage(List<String> elementList);

    /**
     * @param javaNative The type of java native. (NotNull)
     * @return The expression of default value. (NotNull)
     */
    String buildJavaNativeDefaultValue(String javaNative);

    // ===================================================================================
    //                                                                    Small Adjustment 
    //                                                                    ================
    /**
     * @return Is the column name match with program reserved name?
     */
    boolean isPgReservColumn(String columnName);

    /**
     * @param baseIndent The base indent, based on Java indent. (NotNull)
     * @return The adjusted or non-adjusted indent. (NotNull)
     */
    String adjustClassElementIndent(String baseIndent);

    /**
     * @param comment The comment text for JavaDoc. (NotNull)
     * @return The escaped text. (NotNull)
     */
    String escapeJavaDocString(String comment);

    /**
     * @param resolvedTitle The title already resolved for JavaDoc. (NotNull)
     * @param adjustedIndent The indent already adjusted, means no adjustment is needed. (NotNull)
     * @return The string expression of whole JavaDoc comment with title, without rear line separator. (NotNull)
     */
    String buildJavaDocCommentWithTitleIndentDirectly(String resolvedTitle, String adjustedIndent);

    /**
     * @param sourceCodeLineSeparator The line separator for source code. (NotNull)
     * @param baseIndent The base indent, based on Java indent. (NotNull)
     * @return The string expression of JavaDoc line and indent, e.g. "&lt;br /&gt;(ln)     * ". (NotNull)
     */
    String buildJavaDocLineAndIndent(String sourceCodeLineSeparator, String baseIndent);

    /**
     * @param sourceCodeLineSeparator The line separator for source code. (NotNull)
     * @param adjustedIndent The indent already adjusted, means no adjustment is needed. (NotNull)
     * @return The string expression of JavaDoc line and indent, e.g. "&lt;br /&gt;(ln)     * ". (NotNull)
     */
    String buildJavaDocLineAndIndentDirectly(String sourceCodeLineSeparator, String adjustedIndent);
}