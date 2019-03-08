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
package org.dbflute.logic.sql2entity.analyzer;

import java.util.Map;
import java.util.Set;

import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.twowaysql.node.BindVariableNode;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfParameterAutoDetectBindNode {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfParameterAutoDetectAssist _assist;
    protected boolean _requiredTestValue = true;
    protected String _propertyTypeNoTestValue;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfParameterAutoDetectBindNode(DfParameterAutoDetectAssist assist) {
        _assist = assist;
    }

    public DfParameterAutoDetectBindNode unuseTestValue(String propertyTypeNoTestValue) {
        _requiredTestValue = false;
        _propertyTypeNoTestValue = propertyTypeNoTestValue;
        return this;
    }

    // ===================================================================================
    //                                                                  Process AutoDetect
    //                                                                  ==================
    public void processAutoDetectBindNode(String sql, Map<String, String> propertyNameTypeMap, Map<String, String> propertyNameOptionMap,
            Set<String> autoDetectedPropertyNameSet, BindVariableNode variableNode) {
        final String expression = variableNode.getExpression();
        final String testValue = variableNode.getTestValue();
        if (_requiredTestValue && testValue == null) {
            return;
        }
        if (!isPmCommentStartsWithPmb(expression)) {
            return;
        }
        if (isPmCommentNestedProperty(expression) || isPmCommentMethodCall(expression)) {
            return;
        }
        final String propertyName = substringPmCommentPmbRear(expression);
        if (isRevervedProperty(propertyName)) {
            return;
        }
        final String typeName = derivePropertyTypeFromTestValue(testValue); // not null
        propertyNameTypeMap.put(propertyName, typeName); // override if same one exists
        autoDetectedPropertyNameSet.add(propertyName);
        final String option = variableNode.getOptionDef();
        // add option if it exists
        // so it is enough to set an option to only one bind variable comment
        // if several bind variable comments for the same property exist
        final String derivedOption = derivePropertyOptionFromTestValue(testValue); // null allowed
        if (Srl.is_NotNull_and_NotTrimmedEmpty(option)) {
            final String resolvedOption;
            if (Srl.is_NotNull_and_NotTrimmedEmpty(derivedOption)) {
                resolvedOption = option + "|" + derivedOption; // merged
            } else {
                resolvedOption = option;
            }
            propertyNameOptionMap.put(propertyName, resolvedOption);
        } else {
            if (Srl.is_NotNull_and_NotTrimmedEmpty(derivedOption)) {
                propertyNameOptionMap.put(propertyName, derivedOption);
            }
        }
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected String derivePropertyTypeFromTestValue(String textValue) {
        return _requiredTestValue ? _assist.derivePropertyTypeFromTestValue(textValue) : _propertyTypeNoTestValue;
    }

    protected String derivePropertyOptionFromTestValue(String testValue) {
        return _requiredTestValue ? _assist.derivePropertyOptionFromTestValue(testValue) : null;
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
}
