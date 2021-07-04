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
package org.dbflute.properties.assistant.classification.refcls;

/**
 * @author jflute
 * @since 1.2.5 split from DfRefClsElement (2021/07/03 Saturday at roppongi japanese)
 */
public class DfRefClsRefType { // for e.g. appcls, webcls, namedcls

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _refTypeValue;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfRefClsRefType(String refTypeValue) {
        _refTypeValue = refTypeValue;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean isFormalRefType() {
        return isRefTypeIncluded() || isRefTypeExists() || isRefTypeMatches();
    }

    public boolean isRefTypeExists() {
        return _refTypeValue.equals("exists");
    }

    public boolean isRefTypeMatches() {
        return _refTypeValue.equals("matches");
    }

    public boolean isRefTypeIncluded() {
        return _refTypeValue.equals("included");
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _refTypeValue + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getRefTypeValue() {
        return _refTypeValue;
    }
}
