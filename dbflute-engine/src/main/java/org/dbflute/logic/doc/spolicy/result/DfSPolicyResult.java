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
package org.dbflute.logic.doc.spolicy.result;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jflute
 * @since 1.1.2 (2016/12/31 Saturday)
 */
public class DfSPolicyResult {

    protected final Map<String, List<DfSPolicyViolation>> _violationMap = new LinkedHashMap<String, List<DfSPolicyViolation>>();

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

    public boolean isEmpty() {
        return _violationMap.isEmpty();
    }

    public void violate(String policy, String message) {
        List<DfSPolicyViolation> violationList = _violationMap.get(policy);
        if (violationList == null) {
            violationList = new ArrayList<DfSPolicyViolation>();
            _violationMap.put(policy, violationList);
        }
        violationList.add(new DfSPolicyViolation(policy, message));
    }

    public Map<String, List<DfSPolicyViolation>> getViolationMap() {
        return _violationMap;
    }
}
