/*
 * Copyright 2014-2018 the original author or authors.
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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.dbflute.twowaysql.SqlAnalyzer;
import org.dbflute.twowaysql.node.BindVariableNode;
import org.dbflute.twowaysql.node.ForNode;
import org.dbflute.twowaysql.node.IfNode;
import org.dbflute.twowaysql.node.Node;

/**
 * @author jflute
 */
public class DfParameterAutoDetectProcess {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfParameterAutoDetectAssist _assist = newParameterAutoDetectAssist();
    protected final Set<String> _alternateBooleanMethodNameSet = new LinkedHashSet<String>();

    protected DfParameterAutoDetectAssist newParameterAutoDetectAssist() {
        return new DfParameterAutoDetectAssist();
    }

    // ===================================================================================
    //                                                                         Auto Detect
    //                                                                         ===========
    public void processAutoDetect(String sql, Map<String, String> propertyNameTypeMap, Map<String, String> propertyNameOptionMap,
            Set<String> autoDetectedPropertyNameSet) {
        final SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);
        final Node rootNode = analyzer.analyze();
        doProcessAutoDetect(sql, propertyNameTypeMap, propertyNameOptionMap, autoDetectedPropertyNameSet, rootNode);
    }

    protected void doProcessAutoDetect(String sql, Map<String, String> propertyNameTypeMap, Map<String, String> propertyNameOptionMap,
            Set<String> autoDetectedPropertyNameSet, Node node) {
        // only bind variable comment is supported
        // because simple specification is very important here
        if (node instanceof BindVariableNode) {
            final BindVariableNode bindNode = (BindVariableNode) node;
            processAutoDetectBindNode(sql, propertyNameTypeMap, propertyNameOptionMap, autoDetectedPropertyNameSet, bindNode);
        } else if (node instanceof IfNode) {
            final IfNode ifNode = (IfNode) node;
            processAutoDetectIfNode(sql, propertyNameTypeMap, propertyNameOptionMap, ifNode);
            processAlternateBooleanMethodIfNode(sql, ifNode); // process alternate boolean methods, supported with auto-detect
        } else if (node instanceof ForNode) {
            processAutoDetectForNode(sql, propertyNameTypeMap, propertyNameOptionMap, (ForNode) node);
        }
        for (int i = 0; i < node.getChildSize(); i++) {
            final Node childNode = node.getChild(i);
            doProcessAutoDetect(sql, propertyNameTypeMap, propertyNameOptionMap, autoDetectedPropertyNameSet, childNode); // recursive call
        }
    }

    protected void processAutoDetectBindNode(String sql, Map<String, String> propertyNameTypeMap,
            Map<String, String> propertyNameOptionMap, Set<String> autoDetectedPropertyNameSet, BindVariableNode variableNode) {
        final DfParameterAutoDetectBindNode detect = newParameterAutoDetectBindNode(_assist);
        detect.processAutoDetectBindNode(sql, propertyNameTypeMap, propertyNameOptionMap, autoDetectedPropertyNameSet, variableNode);
    }

    protected DfParameterAutoDetectBindNode newParameterAutoDetectBindNode(DfParameterAutoDetectAssist assist) {
        return new DfParameterAutoDetectBindNode(assist);
    }

    protected void processAutoDetectIfNode(String sql, Map<String, String> propertyNameTypeMap, Map<String, String> propertyNameOptionMap,
            IfNode ifNode) {
        final DfParameterAutoDetectIfNode detect = newParameterAutoDetectIfNode(_assist);
        detect.processAutoDetectIfNode(sql, propertyNameTypeMap, propertyNameOptionMap, ifNode);
    }

    protected void processAlternateBooleanMethodIfNode(String sql, IfNode ifNode) {
        final DfParameterAutoDetectIfNode detect = newParameterAutoDetectIfNode(_assist);
        detect.processAlternateBooleanMethodIfNode(sql, _alternateBooleanMethodNameSet, ifNode);
    }

    protected DfParameterAutoDetectIfNode newParameterAutoDetectIfNode(DfParameterAutoDetectAssist assist) {
        return new DfParameterAutoDetectIfNode(assist);
    }

    protected void processAutoDetectForNode(String sql, Map<String, String> propertyNameTypeMap, Map<String, String> propertyNameOptionMap,
            ForNode forNode) {
        final DfParameterAutoDetectForNode detect = newParameterAutoDetectForNode(_assist);
        detect.processAutoDetectForNode(sql, propertyNameTypeMap, propertyNameOptionMap, forNode);
    }

    protected DfParameterAutoDetectForNode newParameterAutoDetectForNode(DfParameterAutoDetectAssist assist) {
        return new DfParameterAutoDetectForNode(assist);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Set<String> getAlternateBooleanMethodNameSet() {
        return _alternateBooleanMethodNameSet;
    }
}
