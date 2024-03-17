/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.logic.doc.spolicy.result;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dbflute.helper.function.IndependentProcessor;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyParsedPolicy;

/**
 * @author jflute
 * @since 1.1.2 (2016/12/31 Saturday)
 */
public class DfSPolicyResult {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSPolicyParsedPolicy _schemaPolicy; // not null

    protected String _policyMessage; // not null after logging
    protected final Map<String, List<DfSPolicyViolation>> _violationMap = new LinkedHashMap<String, List<DfSPolicyViolation>>();
    protected String _violationMessage; // not null after checked and violated

    protected IndependentProcessor _endingHandler; // not null after checked

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyResult(DfSPolicyParsedPolicy schemaPolicy) {
        _schemaPolicy = schemaPolicy;
    }

    // ===================================================================================
    //                                                                       Schema Policy
    //                                                                       =============
    public void acceptPolicyMessage(String policyMessage) {
        if (policyMessage == null) {
            throw new IllegalArgumentException("The argument 'policyMessage' should not be null.");
        }
        _policyMessage = policyMessage;
    }

    // ===================================================================================
    //                                                                           Violation
    //                                                                           =========
    public static class DfSPolicyViolation {

        protected final String _policy;
        protected final String _message;

        public DfSPolicyViolation(String policy, String message) {
            _policy = policy;
            _message = message;
        }

        public String getPolicy() {
            return _policy;
        }

        public String getMessage() {
            return _message;
        }
    }

    public boolean hasViolation() {
        return !_violationMap.isEmpty();
    }

    public void violate(String policy, String message) {
        List<DfSPolicyViolation> violationList = _violationMap.get(policy);
        if (violationList == null) {
            violationList = new ArrayList<DfSPolicyViolation>();
            _violationMap.put(policy, violationList);
        }
        violationList.add(new DfSPolicyViolation(policy, message));
    }

    public void acceptViolationMessage(String violationMessage) {
        if (violationMessage == null) {
            throw new IllegalArgumentException("The argument 'violationMessage' should not be null.");
        }
        _violationMessage = violationMessage;
    }

    // ===================================================================================
    //                                                                              Ending
    //                                                                              ======
    public void acceptEndingHandler(IndependentProcessor endingHandler) {
        if (endingHandler == null) {
            throw new IllegalArgumentException("The argument 'endingHandler' should not be null.");
        }
        _endingHandler = endingHandler;
    }

    public void ending() { // may throw
        if (_endingHandler == null) {
            throw new IllegalStateException("Not found the ending handler at that time.");
        }
        _endingHandler.process();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "result:{violations=" + _violationMap.size() + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public DfSPolicyParsedPolicy getSchemaPolicy() {
        return _schemaPolicy;
    }

    public String getPolicyMessage() {
        return _policyMessage;
    }

    public Map<String, List<DfSPolicyViolation>> getViolationMap() {
        return _violationMap;
    }

    public String getViolationMessage() {
        return _violationMessage;
    }
}
