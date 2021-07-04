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
package org.dbflute.properties.assistant.classification.top.grouping;

import java.util.List;

import org.dbflute.properties.assistant.classification.DfClassificationTop;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationGroup (2021/07/03 Saturday at roppongi japanese)
 */
public class DfClsTopGroupCodingExpression {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfClassificationTop _classificationTop; // null allowed, empty allowed
    protected final List<String> _elementNameList; // null allowed, empty allowed

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsTopGroupCodingExpression(DfClassificationTop classificationTop, List<String> elementNameList) {
        _classificationTop = classificationTop;
        _elementNameList = elementNameList;
    }

    // ===================================================================================
    //                                                                   Return Expression
    //                                                                   =================
    public String buildReturnExpThis() { // for inGroup()
        return doBuildReturnExp("this"); // e.g. "GROUP1.equals(this) || GRUOP2.equals(this)"
    }

    protected String doBuildReturnExp(String target) {
        if (_elementNameList == null) { // just in case
            throw new IllegalStateException("Not found the elementNameList.");
        }
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String elementName : _elementNameList) {
            if (index > 0) {
                sb.append(" || ");
            }
            sb.append(elementName).append(".equals(").append(target).append(")");
            ++index;
        }
        return sb.toString();
    }

    // ===================================================================================
    //                                                            CDef Argument Expression
    //                                                            ========================
    public String buildCDefArgExp() {
        return buildCDefArgExp(null); // e.g. "GROUP1, GROUP2" or "CDef.SeaStatus.GROUP1, ..."
    }

    public String buildCDefArgExp(String cdefClassName) {
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String elementName : _elementNameList) {
            if (index > 0) {
                sb.append(", ");
            }
            if (cdefClassName != null) {
                sb.append(cdefClassName).append(".");
                sb.append(_classificationTop.getClassificationName()).append(".");
            }
            sb.append(elementName);
            ++index;
        }
        return sb.toString();
    }
}
