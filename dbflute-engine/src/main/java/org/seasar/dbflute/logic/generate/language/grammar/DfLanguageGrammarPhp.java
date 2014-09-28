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
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfLanguageGrammarPhp implements DfLanguageGrammar {

    // ===================================================================================
    //                                                                       Basic Keyword
    //                                                                       =============
    public String getClassFileExtension() {
        return "php";
    }

    public String getExtendsMark() {
        return "extends";
    }

    public String getImplementsMark() {
        return "implements";
    }

    public String getImplementsDelimiter() {
        return ", ";
    }

    public boolean isSameAreaExtendsImplements() {
        return false;
    }

    public String getPublicModifier() {
        return "public";
    }

    public String getProtectedModifier() {
        return "protected";
    }

    public String getPublicFinal() {
        return "const";
    }

    public String getPublicStaticFinal() {
        return "const";
    }

    public String getGenericBeginMark() {
        return "";
    }

    public String getGenericEndMark() {
        return "";
    }

    // ===================================================================================
    //                                                              Programming Expression
    //                                                              ======================
    public String buildVariableSimpleDefinition(String type, String variable) {
        return type + " " + variable;
    }

    public String adjustMethodInitialChar(String methodNameResource) {
        return Srl.initUncap(methodNameResource);
    }

    public String adjustPropertyInitialChar(String propertyName) {
        return Srl.initUncap(propertyName);
    }

    public String buildPropertyGetterCall(String propertyName) {
        throw new UnsupportedOperationException("Unsupported at Php");
    }

    public String buildClassTypeLiteral(String className) {
        throw new UnsupportedOperationException("Unsupported at Php");
    }

    public String buildGenericListClassName(String element) {
        throw new UnsupportedOperationException("Unsupported at Php");
    }

    public String buildGenericMapListClassName(String key, String value) {
        throw new UnsupportedOperationException("Unsupported at Php");
    }

    public String buildGenericOneClassHint(String first) {
        return "";
    }

    public String buildGenericTwoClassHint(String first, String second) {
        return "";
    }

    public String buildGenericThreeClassHint(String first, String second, String third) {
        return "";
    }

    public boolean hasGenericClassElement(String className, String genericExp) {
        return false;
    }

    public String extractGenericClassElement(String className, String genericExp) {
        throw new UnsupportedOperationException("Unsupported at Php");
    }

    public String buildEntityPropertyGetSet(Column fromCol, Column toCol) {
        throw new UnsupportedOperationException("Unsupported at Php");
    }

    public String buildEntityPropertyName(Column col) {
        return col.getUncapitalisedJavaName();
    }

    public String buildCDefElementValue(String cdefBase, String propertyName, String valueType, boolean toNumber,
            boolean toBoolean) {
        throw new UnsupportedOperationException("Unsupported at Php");
    }

    public String buildOneLinerListNewBackStage(List<String> elementList) {
        throw new UnsupportedOperationException("Unsupported at Php");
    }

    public String buildJavaNativeDefaultValue(String javaNative) {
        return "null";
    }

    // ===================================================================================
    //                                                                    Small Adjustment 
    //                                                                    ================
    public boolean isPgReservColumn(String columnName) {
        return false; // unknown
    }

    public String escapeJavaDocString(String comment) {
        return comment; // unknown
    }

    public String adjustClassElementIndent(String baseIndent) {
        return baseIndent; // no adjustment, same as Java
    }

    public String buildJavaDocCommentWithTitleIndentDirectly(String resolvedTitle, String adjustedIndent) {
        return adjustedIndent + "/** " + resolvedTitle + " */";
    }

    public String buildJavaDocLineAndIndent(String sourceCodeLineSeparator, String baseIndent) {
        return doBuildJavaDocLineAndIndent(sourceCodeLineSeparator, adjustClassElementIndent(baseIndent));
    }

    public String buildJavaDocLineAndIndentDirectly(String sourceCodeLineSeparator, String adjustedIndent) {
        return doBuildJavaDocLineAndIndent(sourceCodeLineSeparator, adjustedIndent);
    }

    protected String doBuildJavaDocLineAndIndent(String sourceCodeLineSeparator, String adjustedIndent) {
        return "<br />" + sourceCodeLineSeparator + adjustedIndent + " * ";
    }
}