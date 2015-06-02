/*
 * Copyright 2014-2015 the original author or authors.
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.twowaysql.node.IfCommentEvaluator;
import org.dbflute.twowaysql.node.IfNode;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfParameterAutoDetectIfNode {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfParameterAutoDetectAssist _assist;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfParameterAutoDetectIfNode(DfParameterAutoDetectAssist assist) {
        _assist = assist;
    }

    // ===================================================================================
    //                                                                  Process AutoDetect
    //                                                                  ==================
    public void processAutoDetectIfNode(String sql, Map<String, String> propertyNameTypeMap, Map<String, String> propertyNameOptionMap,
            IfNode ifNode) {
        final String ifCommentBooleanType = switchPlainTypeName("boolean");
        final String expression = ifNode.getExpression().trim(); // trim it just in case
        final List<String> elementList = Srl.splitList(expression, " ");
        for (int i = 0; i < elementList.size(); i++) {
            // boolean-not mark unused here so remove it at first
            final String element = substringBooleanNotRear(elementList.get(i));
            if (!isPmCommentStartsWithPmb(element)) {
                continue;
            }
            if (isPmCommentNestedProperty(element) || isPmCommentMethodCall(element)) {
                continue;
            }
            final String propertyName = substringPmCommentPmbRear(element);
            if (propertyNameTypeMap.containsKey(propertyName)) {
                // because of priority low (bind variable is given priority over if-comment)
                continue;
            }
            if (isRevervedProperty(propertyName)) {
                continue;
            }
            final int nextIndex = i + 1;
            if (elementList.size() <= nextIndex) { // last now
                propertyNameTypeMap.put(propertyName, ifCommentBooleanType);
                continue;
            }
            // next exists here
            final String nextElement = elementList.get(nextIndex);
            if (isIfCommentStatementConnector(nextElement)) { // e.g. '&&' or '||'
                propertyNameTypeMap.put(propertyName, ifCommentBooleanType);
                continue;
            }
            if (!isIfCommentStatementOperand(nextElement)) { // no way (wrong syntax)
                continue;
            }
            final int nextNextIndex = i + 2;
            if (elementList.size() <= nextNextIndex) { // no way (wrong syntax)
                continue;
            }
            // next next exists
            final String nextNextElement = elementList.get(nextNextIndex);
            if (isPmCommentStartsWithPmb(nextNextElement)) { // e.g. pmb.foo == pmb.bar
                continue;
            }
            // using-value statement here e.g. pmb.foo == 'foo'
            // condition value is treated as testValue to derive
            final String propertyType = derivePropertyTypeFromTestValue(nextNextElement);
            propertyNameTypeMap.put(propertyName, propertyType);
        }
    }

    public void processAlternateBooleanMethodIfNode(String sql, Set<String> alternateBooleanMethodNameSet, IfNode ifNode) {
        final String expression = ifNode.getExpression().trim(); // trim it just in case
        if (Srl.containsAny(expression, getIfCommentConnectors()) || Srl.containsAny(expression, getIfCommentOperands())) {
            return; // unknown (type)
        }
        if (isPmCommentNestedProperty(expression) || !isPmCommentMethodCall(expression)) {
            return; // e.g. pmb.foo.bar
        }
        if (!isPmCommentStartsWithPmb(substringBooleanNotRear(expression))) {
            return; // e.g. #current.isFoo()
        }
        // pmb.foo() or !pmb.foo() here
        String methodName = substringPmCommentPmbRear(expression); // -> foo()
        methodName = Srl.substringLastFront(methodName, "()"); // -> foo
        alternateBooleanMethodNameSet.add(methodName); // filter later
    }

    // ===================================================================================
    //                                                                    IfComment Helper
    //                                                                    ================
    protected String[] getIfCommentConnectors() {
        return IfCommentEvaluator.getConnectors();
    }

    protected String[] getIfCommentOperands() {
        return IfCommentEvaluator.getOperands();
    }

    protected boolean isIfCommentStatementConnector(String target) {
        return IfCommentEvaluator.isConnector(target);
    }

    protected boolean isIfCommentStatementOperand(String target) {
        return IfCommentEvaluator.isOperand(target);
    }

    protected String substringBooleanNotRear(String expression) {
        return IfCommentEvaluator.substringBooleanNotRear(expression);
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected String derivePropertyTypeFromTestValue(String textValue) {
        return _assist.derivePropertyTypeFromTestValue(textValue);
    }

    protected String switchPlainTypeName(String plainTypeName) {
        return _assist.switchPlainTypeName(plainTypeName);
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
}
