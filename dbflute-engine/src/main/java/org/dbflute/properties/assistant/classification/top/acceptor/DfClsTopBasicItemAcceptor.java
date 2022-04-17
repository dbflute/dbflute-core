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
package org.dbflute.properties.assistant.classification.top.acceptor;

import java.util.Map;

import org.dbflute.exception.DfClassificationRequiredAttributeNotFoundException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.properties.assistant.classification.DfClassificationTop;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationTop (2021/07/04 Sunday at roppongi japanese)
 */
public class DfClsTopBasicItemAcceptor {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _classificationName; // not null
    protected final Map<?, ?> _topElementMap; // not null, topComment key existence already checked here

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsTopBasicItemAcceptor(String classificationName, Map<?, ?> topElementMap) {
        _classificationName = classificationName;
        _topElementMap = topElementMap;
    }

    // ===================================================================================
    //                                                                         Top Comment
    //                                                                         ===========
    public String acceptTopComment() { // not null
        final String topComment = (String) _topElementMap.get(DfClassificationTop.KEY_TOP_COMMENT);

        // always not null because the key existence already checked here (but just in case)
        // so this exception is only for empty value
        if (topComment == null || topComment.trim().isEmpty()) { // e.g. topComment=[empty]
            throwClassificationLiteralCommentNotFoundException();
        }
        return topComment;
    }

    protected void throwClassificationLiteralCommentNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The required topComment value is empty.");
        br.addItem("Advice");
        br.addElement("The classification should have the topComment value.");
        br.addElement("For example:");
        br.addElement("  (x): topComment= // *Bad: empty");
        br.addElement("  (o): topComment=sea is blue... // Good");
        br.addElement("");
        br.addElement("See the document for the DBFlute property.");
        br.addItem("Classification");
        br.addElement(_classificationName);
        br.addItem("Top Element Map");
        br.addElement(_topElementMap);
        final String msg = br.buildExceptionMessage();
        throw new DfClassificationRequiredAttributeNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                           Code Type
    //                                                                           =========
    public String acceptCodeType(String defaultType) { // not null with default
        final String codeType;
        {
            String tmpType = (String) _topElementMap.get(DfClassificationTop.KEY_CODE_TYPE);
            if (Srl.is_Null_or_TrimmedEmpty(tmpType)) {
                tmpType = (String) _topElementMap.get(DfClassificationTop.KEY_DATA_TYPE); // for compatibility
            }
            codeType = tmpType;
        }
        return codeType != null ? codeType : defaultType;
    }
}
