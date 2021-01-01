/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.logic.sql2entity.analyzer;

import java.util.Map;

import org.dbflute.DfBuildProperties;
import org.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.twowaysql.node.BindVariableNode;
import org.dbflute.twowaysql.node.ForNode;
import org.dbflute.twowaysql.node.Node;
import org.dbflute.twowaysql.node.ScopeNode;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfParameterAutoDetectForNode {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfParameterAutoDetectAssist _assist;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfParameterAutoDetectForNode(DfParameterAutoDetectAssist assist) {
        _assist = assist;
    }

    // ===================================================================================
    //                                                                  Process AutoDetect
    //                                                                  ==================
    public void processAutoDetectForNode(String sql, Map<String, String> propertyNameTypeMap, Map<String, String> propertyNameOptionMap,
            ForNode forNode) {
        final String expression = forNode.getExpression();
        if (!isPmCommentStartsWithPmb(expression)) {
            return;
        }
        final String propertyName = substringPmCommentPmbRear(expression);
        if (propertyNameTypeMap.containsKey(propertyName)) {
            // because of priority low (bind variable is given priority over for-comment)
            return;
        }
        if (isRevervedProperty(propertyName)) {
            return;
        }
        final DfForNodeDetectedPropertyInfo detected = analyzeForNodeElementType(forNode, propertyName);
        if (detected != null) {
            final String propertyType = switchPlainTypeName(detected.getPropertyType());
            propertyNameTypeMap.put(propertyName, propertyType);
            final String propertyOption = detected.getPropertyOption();
            if (Srl.is_NotNull_and_NotTrimmedEmpty(propertyOption)) {
                propertyNameOptionMap.put(propertyName, propertyOption);
            }
        }
    }

    protected DfForNodeDetectedPropertyInfo analyzeForNodeElementType(Node node, String propertyName) {
        if (isPmCommentNestedProperty(propertyName) || isPmCommentMethodCall(propertyName)) {
            return null;
        }
        final DfLanguageGrammar grammar = getLanguageGrammar();
        DfForNodeDetectedPropertyInfo detected = null;
        for (int i = 0; i < node.getChildSize(); i++) {
            final Node childNode = node.getChild(i);
            if (childNode instanceof BindVariableNode) {
                final BindVariableNode bindNode = (BindVariableNode) childNode;
                final String expression = bindNode.getExpression();
                if (!isPmCommentEqualsCurrent(expression)) {
                    continue;
                }
                if (isPmCommentNestedProperty(expression) || isPmCommentMethodCall(expression)) {
                    continue;
                }
                // /*#current*/ here
                final String testValue = bindNode.getTestValue();
                if (testValue == null) {
                    continue;
                }
                final String propertyType = derivePropertyTypeFromTestValue(testValue);
                final String propertyOption = derivePropertyOptionFromTestValue(testValue);
                if (Srl.is_NotNull_and_NotTrimmedEmpty(propertyType)) {
                    detected = new DfForNodeDetectedPropertyInfo();
                    final String generic = grammar.buildGenericOneClassHint(propertyType);
                    detected.setPropertyType("List" + generic);
                    detected.setPropertyOption(propertyOption);
                }
            } else if (childNode instanceof ForNode) {
                final ForNode nestedNode = (ForNode) childNode;
                final String expression = nestedNode.getExpression();
                if (!isPmCommentStartsWithCurrent(expression)) {
                    continue;
                }
                // /*FOR #current.xxx*/ here
                final String nestedForPropName = substringPmCommentCurrentRear(expression);
                detected = analyzeForNodeElementType(nestedNode, nestedForPropName); // recursive call
                if (detected != null) {
                    final String generic = grammar.buildGenericOneClassHint(detected.getPropertyType());
                    detected.setPropertyType("List" + generic);
                }
            } else if (childNode instanceof ScopeNode) { // IF, Begin, First, ...
                detected = analyzeForNodeElementType(childNode, propertyName); // recursive call
            }
            if (detected != null) {
                break;
            }
        }
        if (detected == null) {
            return null;
        }
        return detected;
    }

    protected static class DfForNodeDetectedPropertyInfo {
        protected String _propertyType;
        protected String _propertyOption;

        public String getPropertyType() {
            return _propertyType;
        }

        public void setPropertyType(String propertyType) {
            this._propertyType = propertyType;
        }

        public String getPropertyOption() {
            return _propertyOption;
        }

        public void setPropertyOption(String propertyOption) {
            this._propertyOption = propertyOption;
        }
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected String switchPlainTypeName(String plainTypeName) {
        return _assist.switchPlainTypeName(plainTypeName);
    }

    protected String derivePropertyTypeFromTestValue(String textValue) {
        return _assist.derivePropertyTypeFromTestValue(textValue);
    }

    protected String derivePropertyOptionFromTestValue(final String testValue) {
        return _assist.derivePropertyOptionFromTestValue(testValue);
    }

    protected boolean isRevervedProperty(String expression) {
        return _assist.isRevervedProperty(expression);
    }

    protected boolean isPmCommentStartsWithPmb(String expression) {
        return _assist.isPmCommentStartsWithPmb(expression);
    }

    protected String substringPmCommentPmbRear(String expression) {
        return _assist.substringPmCommentPmbRear(expression);
    }

    protected boolean isPmCommentEqualsCurrent(String expression) {
        return _assist.isPmCommentEqualsCurrent(expression);
    }

    protected boolean isPmCommentStartsWithCurrent(String expression) {
        return _assist.isPmCommentStartsWithCurrent(expression);
    }

    protected String substringPmCommentCurrentRear(String expression) {
        return _assist.substringPmCommentCurrentRear(expression);
    }

    protected boolean isPmCommentNestedProperty(String expression) {
        return _assist.isPmCommentNestedProperty(expression);
    }

    protected boolean isPmCommentMethodCall(String expression) {
        return _assist.isPmCommentMethodCall(expression);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfLanguageGrammar getLanguageGrammar() {
        return getBasicProperties().getLanguageDependency().getLanguageGrammar();
    }
}
