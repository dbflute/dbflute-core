/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.properties.assistant.classification.top.deprecated;

import java.util.List;
import java.util.Map;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.properties.assistant.classification.DfClassificationElement;
import org.dbflute.properties.assistant.classification.DfClassificationTop;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationTop (2021/07/03 Saturday at roppongi japanese)
 */
public class DfClsTopDeprecatedExistenceVerifier {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfClassificationTop _classificationTop; // not null, mutable
    protected final Map<String, String> _deprecatedMap; // not null, mutable

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsTopDeprecatedExistenceVerifier(DfClassificationTop classificationTop, Map<String, String> deprecatedMap) {
        _classificationTop = classificationTop;
        _deprecatedMap = deprecatedMap;
    }

    // ===================================================================================
    //                                                                    Verify Existence
    //                                                                    ================
    public void verifyDeprecatedElementExistence() {
        final List<DfClassificationElement> elementList = _classificationTop.getClassificationElementList();
        for (String deprecated : _deprecatedMap.keySet()) {
            boolean found = false;
            for (DfClassificationElement element : elementList) {
                final String name = element.getName();
                if (name.equals(deprecated)) {
                    found = true;
                }
            }
            if (!found) {
                throwDeprecatedClassificationElementNotFoundException(deprecated);
            }
        }
    }

    protected void throwDeprecatedClassificationElementNotFoundException(String deprecated) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the specified element in deprecated list.");
        br.addItem("Classification");
        br.addElement(_classificationTop.getClassificationName());
        br.addItem("Existing Element");
        final StringBuilder sb = new StringBuilder();
        for (DfClassificationElement element : _classificationTop.getClassificationElementList()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(element.getName());
        }
        br.addElement(sb.toString());
        br.addItem("NotFound Element");
        br.addElement(deprecated);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }
}
