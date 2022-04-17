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
package org.dbflute.properties.assistant.classification.top.elementoption.sistercode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dbflute.properties.assistant.classification.DfClassificationElement;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationTop (2021/07/04 Sunday at roppongi japanese)
 */
public class DfClsTopSisterCodeHandler {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<DfClassificationElement> _elementList;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsTopSisterCodeHandler(List<DfClassificationElement> elementList) {
        _elementList = elementList;
    }

    // ===================================================================================
    //                                                                    Top Determinaton
    //                                                                    ================
    public boolean hasSisterCode() {
        for (DfClassificationElement element : _elementList) {
            if (element.hasSiserCode()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSisterEmpty() {
        if (hasSisterCode()) {
            for (DfClassificationElement element : _elementList) {
                if (!element.hasSiserCode()) {
                    return true;
                }
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                    Boolean Handling
    //                                                                    ================
    public boolean isSisterBooleanHandling() { // for e.g. Flg classification
        if (_elementList.size() != 2) {
            return false;
        }
        final Set<String> firstSet = new HashSet<String>();
        {
            final String[] firstSisters = _elementList.get(0).getSisters();
            for (String sister : firstSisters) {
                firstSet.add(sister.toLowerCase());
            }
        }
        final Set<String> secondSet = new HashSet<String>();
        {
            final String[] secondSisters = _elementList.get(1).getSisters();
            for (String sister : secondSisters) {
                secondSet.add(sister.toLowerCase());
            }
        }
        return (firstSet.contains("true") && secondSet.contains("false") // first true
                || firstSet.contains("false") && secondSet.contains("true")); // first false
    }
}
