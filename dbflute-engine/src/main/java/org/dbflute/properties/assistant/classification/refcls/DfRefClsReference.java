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

import java.util.Collections;
import java.util.Map;

import org.dbflute.properties.assistant.classification.DfClassificationTop;

/**
 * @author jflute
 * @since 1.2.5 (2021/07/08 Thursday at roppongi japanese)
 */
public class DfRefClsReference {

    protected final String _refClsTheme; // not null
    protected final Map<String, DfClassificationTop> _referredClsTopMap; // key is classification name, not null
    protected final DfRefClsReferredCDef _referredCDef; // not null

    public DfRefClsReference(String refClsTheme, Map<String, DfClassificationTop> referredClsTopMap, DfRefClsReferredCDef referredCDef) {
        _refClsTheme = refClsTheme;
        _referredClsTopMap = referredClsTopMap;
        _referredCDef = referredCDef;
    }

    public String getRefClsTheme() {
        return _refClsTheme;
    }

    public Map<String, DfClassificationTop> getReferredClsTopMap() {
        return Collections.unmodifiableMap(_referredClsTopMap);
    }

    public DfRefClsReferredCDef getReferredCDef() {
        return _referredCDef;
    }
}
