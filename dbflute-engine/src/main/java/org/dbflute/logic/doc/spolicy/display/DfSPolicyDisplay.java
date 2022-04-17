/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.logic.doc.spolicy.display;

import org.dbflute.logic.doc.spolicy.result.DfSPolicyResult;
import org.dbflute.properties.assistant.document.textresolver.DfDocumentTextResolver;

/**
 * @author jflute
 * @since 1.2.0 (2019/04/21 Sunday at sheraton)
 */
public class DfSPolicyDisplay { // used by Velocity template

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSPolicyResult _policyResult; // not null
    protected final DfDocumentTextResolver _documentTextResolver = new DfDocumentTextResolver();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyDisplay(DfSPolicyResult policyResult) {
        _policyResult = policyResult;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasViolation() {
        return _policyResult.hasViolation();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getPolicyMessage() {
        return _policyResult.getPolicyMessage();
    }

    public String getPolicyMessageHtmlPreText() {
        return _documentTextResolver.resolveSchemaHtmlPreText(_policyResult.getPolicyMessage());
    }

    public String getViolationMessage() {
        return _policyResult.getViolationMessage();
    }

    public String getViolationMessageHtmlPreText() {
        return _documentTextResolver.resolveSchemaHtmlPreText(_policyResult.getViolationMessage());
    }
}
