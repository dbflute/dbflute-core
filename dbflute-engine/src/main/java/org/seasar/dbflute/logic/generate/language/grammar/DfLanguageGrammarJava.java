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
import java.util.Set;

import org.apache.torque.engine.database.model.Column;
import org.seasar.dbflute.helper.StringSet;
import org.seasar.dbflute.logic.generate.language.DfLanguageSmallHelper;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfLanguageGrammarJava implements DfLanguageGrammar {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final Set<String> _pgReservColumnSet;
    static {
        // likely words only (and only can be checked at examples)
        final StringSet stringSet = StringSet.createAsCaseInsensitive();
        final List<String> list = DfCollectionUtil.newArrayList("class", "case", "package", "default", "new", "native",
                "void", "public", "protected", "private", "interface", "abstract", "final", "finally", "return",
                "double", "float", "short");
        stringSet.addAll(list);
        _pgReservColumnSet = stringSet;
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfLanguageSmallHelper _simpleHelper = new DfLanguageSmallHelper();

    // ===================================================================================
    //                                                                       Basic Keyword
    //                                                                       =============
    public String getClassFileExtension() {
        return "java";
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
        return "public final";
    }

    public String getPublicStaticFinal() {
        return "public static final";
    }

    public String getGenericBeginMark() {
        return "<";
    }

    public String getGenericEndMark() {
        return ">";
    }

    // ===================================================================================
    //                                                              Programming Expression
    //                                                              ======================
    public String buildVariableSimpleDefinition(String type, String variable) {
        return type + " " + variable;
    }

    public String adjustMethodInitialChar(String methodName) {
        return Srl.initUncap(methodName);
    }

    public String adjustPropertyInitialChar(String propertyName) {
        return Srl.initBeansProp(propertyName);
    }

    public String buildPropertyGetterCall(String propertyName) {
        return "get" + Srl.initCap(propertyName) + "()";
    }

    public String buildClassTypeLiteral(String className) {
        return className + ".class";
    }

    public String buildGenericListClassName(String element) {
        return "List" + buildGenericOneClassHint(element);
    }

    public String buildGenericMapListClassName(String key, String value) {
        return buildGenericListClassName("Map" + buildGenericTwoClassHint(key, value));
    }

    public String buildGenericOneClassHint(String first) {
        return "<" + first + ">";
    }

    public String buildGenericTwoClassHint(String first, String second) {
        return "<" + first + ", " + second + ">";
    }

    public String buildGenericThreeClassHint(String first, String second, String third) {
        return "<" + first + ", " + second + ", " + third + ">";
    }

    public boolean hasGenericClassElement(String className, String genericExp) {
        return _simpleHelper.hasGenericClassElement(className, genericExp, "<", ">");
    }

    public String extractGenericClassElement(String className, String genericExp) {
        return _simpleHelper.extractGenericClassElement(className, genericExp, "<", ">");
    }

    public String buildEntityPropertyGetSet(Column fromCol, Column toCol) {
        final String fromPropName = fromCol.getJavaBeansRulePropertyNameInitCap();
        final String toPropName = toCol.getJavaBeansRulePropertyNameInitCap();
        return "set" + toPropName + "(get" + fromPropName + "())";
    }

    public String buildEntityPropertyName(Column col) {
        return col.getJavaBeansRulePropertyName();
    }

    public String buildCDefElementValue(String cdefBase, String propertyName, String valueType, boolean toNumber,
            boolean toBoolean) {
        final String cdefCode = cdefBase + ".code()";
        if (toNumber) {
            return "toNumber(" + cdefCode + ", " + valueType + ".class)";
        } else if (toBoolean) {
            return "toBoolean(" + cdefCode + ")";
        } else {
            return cdefCode;
        }
    }

    public String buildOneLinerListNewBackStage(List<String> elementList) {
        final StringBuilder sb = new StringBuilder();
        for (String element : elementList) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(element);
        }
        return "newArrayList(" + sb.toString() + ")";
    }

    public String buildJavaNativeDefaultValue(String javaNative) {
        return "null";
    }

    // ===================================================================================
    //                                                                    Small Adjustment 
    //                                                                    ================
    public boolean isPgReservColumn(String columnName) {
        return _pgReservColumnSet.contains(columnName);
    }

    public String adjustClassElementIndent(String baseIndent) {
        return baseIndent; // no adjustment because Java is standard language 
    }

    public String escapeJavaDocString(String comment) {
        String work = comment;
        work = Srl.replace(work, "<", "&lt;");
        work = Srl.replace(work, ">", "&gt;");
        work = Srl.replace(work, "*/", "&#42;/"); // avoid JavaDoc breaker
        return work;
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