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
import org.seasar.dbflute.logic.generate.language.DfLanguageSmallHelper;

/**
 * @author jflute
 */
public class DfLanguageGrammarScala implements DfLanguageGrammar {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfLanguageGrammarJava _grammarJava = new DfLanguageGrammarJava();
    protected final DfLanguageSmallHelper _simpleHelper = new DfLanguageSmallHelper();

    // ===================================================================================
    //                                                                       Basic Keyword
    //                                                                       =============
    public String getClassFileExtension() {
        return "scala";
    }

    public String getExtendsMark() {
        return "extends";
    }

    public String getImplementsMark() {
        return "extends";
    }

    public String getImplementsDelimiter() {
        return " with ";
    }

    public boolean isSameAreaExtendsImplements() {
        return true;
    }

    public String getPublicModifier() {
        return "";
    }

    public String getProtectedModifier() {
        return "protected";
    }

    public String getPublicFinal() {
        return "val"; // it has no public
    }

    public String getPublicStaticFinal() {
        return "val"; // it has no static
    }

    public String getGenericBeginMark() {
        return "[";
    }

    public String getGenericEndMark() {
        return "]";
    }

    // ===================================================================================
    //                                                              Programming Expression
    //                                                              ======================
    public String buildVariableSimpleDefinition(String type, String variable) {
        return variable + ": " + type;
    }

    public String adjustMethodInitialChar(String methodName) {
        return _grammarJava.adjustMethodInitialChar(methodName);
    }

    public String adjustPropertyInitialChar(String propertyName) {
        return _grammarJava.adjustPropertyInitialChar(propertyName);
    }

    public String buildPropertyGetterCall(String propertyName) {
        return _grammarJava.buildPropertyGetterCall(propertyName);
    }

    public String buildClassTypeLiteral(String className) {
        String exp = className;
        if (className.equals("Option") || className.contains("Optional")) { // patch
            if (!className.contains("[")) {
                exp = className + "[_]";
            }
        }
        return "classOf[" + exp + "]";
    }

    public String buildGenericListClassName(String element) {
        return "List" + buildGenericOneClassHint(element);
    }

    public String buildGenericMapListClassName(String key, String value) {
        return buildGenericListClassName("Map" + buildGenericTwoClassHint(key, value));
    }

    public String buildGenericOneClassHint(String first) {
        return "[" + first + "]";
    }

    public String buildGenericTwoClassHint(String first, String second) {
        return "[" + first + ", " + second + "]";
    }

    public String buildGenericThreeClassHint(String first, String second, String third) {
        return "[" + first + ", " + second + ", " + third + "]";
    }

    public boolean hasGenericClassElement(String className, String genericExp) {
        return _simpleHelper.hasGenericClassElement(className, genericExp, "[", "]");
    }

    public String extractGenericClassElement(String className, String genericExp) {
        return _simpleHelper.extractGenericClassElement(className, genericExp, "[", "]");
    }

    public String buildEntityPropertyGetSet(Column fromCol, Column toCol) {
        return _grammarJava.buildEntityPropertyGetSet(fromCol, toCol);
    }

    public String buildEntityPropertyName(Column col) {
        return _grammarJava.buildEntityPropertyName(col);
    }

    public String buildCDefElementValue(String cdefBase, String propertyName, String valueType, boolean toNumber,
            boolean toBoolean) {
        final String cdefCode = cdefBase + ".code";
        if (toNumber) {
            return "toNumber(" + cdefCode + ", classOf[" + valueType + "])";
        } else if (toBoolean) {
            return "toBoolean(" + cdefCode + ")";
        } else {
            return cdefCode;
        }
    }

    public String buildOneLinerListNewBackStage(List<String> elementList) {
        return _grammarJava.buildOneLinerListNewBackStage(elementList);
    }

    public String buildJavaNativeDefaultValue(String javaNative) { // not immutable type
        return "Boolean".equals(javaNative) ? "false" : "null";
    }

    // ===================================================================================
    //                                                                    Small Adjustment 
    //                                                                    ================
    public boolean isPgReservColumn(String columnName) {
        return _grammarJava.isPgReservColumn(columnName);
    }

    public String escapeJavaDocString(String comment) {
        return _grammarJava.escapeJavaDocString(comment);
    }

    public String adjustClassElementIndent(String baseIndent) {
        return _grammarJava.adjustClassElementIndent(baseIndent);
    }

    public String buildJavaDocCommentWithTitleIndentDirectly(String resolvedTitle, String adjustedIndent) {
        return _grammarJava.buildJavaDocCommentWithTitleIndentDirectly(resolvedTitle, adjustedIndent);
    }

    public String buildJavaDocLineAndIndent(String sourceCodeLineSeparator, String baseIndent) {
        return _grammarJava.buildJavaDocLineAndIndent(sourceCodeLineSeparator, baseIndent);
    }

    public String buildJavaDocLineAndIndentDirectly(String sourceCodeLineSeparator, String adjustedIndent) {
        return _grammarJava.buildJavaDocLineAndIndentDirectly(sourceCodeLineSeparator, adjustedIndent);
    }
}