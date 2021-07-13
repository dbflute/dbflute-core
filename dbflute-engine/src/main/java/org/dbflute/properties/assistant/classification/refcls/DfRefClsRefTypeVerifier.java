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

import java.util.List;
import java.util.stream.Collectors;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.properties.assistant.classification.DfClassificationElement;
import org.dbflute.properties.assistant.classification.DfClassificationTop;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 split from DfRefClsElement (2021/07/03 Saturday at roppongi japanese)
 */
public class DfRefClsRefTypeVerifier {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfRefClsRefType _refType;
    protected final DfClassificationTop _referredClsTop;
    protected final String _resourceFile;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfRefClsRefTypeVerifier(DfRefClsRefType refType, DfClassificationTop referredClsTop, String resourceFile) {
        _refType = refType;
        _referredClsTop = referredClsTop;
        _resourceFile = resourceFile;
    }

    // ===================================================================================
    //                                                               Verify Formal RefType
    //                                                               =====================
    public void verifyFormalRefType(DfClassificationTop classificationTop) {
        if (_refType.isFormalRefType()) {
            return;
        }
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown refType to referred classification in the app classification.");
        br.addItem("Advice");
        br.addElement("refType can be set: 'included', 'exists', 'matches'");
        br.addElement("  included : referred classification codes are included as app classification");
        br.addElement("  exists   : app classification codes should exist in referred classification");
        br.addElement("  matches  : app classification codes matches with referred classification");
        br.addItem("dfprop File");
        br.addElement(_resourceFile);
        br.addItem("AppCls");
        br.addElement(classificationTop.getClassificationName());
        br.addItem("Unknown refType");
        br.addElement(_refType);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    // ===================================================================================
    //                                                                 Verify Relationship
    //                                                                 ===================
    public void verifyRelationshipByRefTypeIfNeeds(DfClassificationTop classificationTop) {
        if (_refType.isRefTypeExists()) {
            doVerifyRefExists(classificationTop);
        } else if (_refType.isRefTypeMatches()) {
            doVerifyRefMatches(classificationTop);
        }
    }

    protected void doVerifyRefExists(DfClassificationTop classificationTop) {
        final List<DfClassificationElement> appElementList = classificationTop.getClassificationElementList();
        final List<DfClassificationElement> referredElementList = _referredClsTop.getClassificationElementList();
        final List<DfClassificationElement> nonExistingList = appElementList.stream().filter(appElement -> {
            return !referredElementList.stream().anyMatch(referredElement -> {
                return appElement.getCode().equals(referredElement.getCode());
            });
        }).collect(Collectors.toList());
        if (!nonExistingList.isEmpty()) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Not found the app classification code in the referred classification");
            br.addItem("Advice");
            br.addElement("The codes of the app classification should be included");
            br.addElement("in the referred classification because of refType='exists'.");
            br.addElement("For example:");
            br.addElement("  (x): app(A), referred(B, C)");
            br.addElement("  (x): app(A, B), referred(A, C)");
            br.addElement("  (x): app(A, B, C), referred(A, B)");
            br.addElement("  (o): app(A), referred(A, B)");
            br.addElement("  (o): app(A, B), referred(A, B, C)");
            br.addElement("  (o): app(A, B, C), referred(A, B, C)");
            br.addItem("dfprop File");
            br.addElement(_resourceFile);
            br.addItem("AppCls");
            br.addElement(classificationTop.getClassificationName() + ": " + buildClsCodesExp(classificationTop));
            br.addItem("ReferredCls");
            br.addElement(_referredClsTop.getClassificationName() + ": " + buildClsCodesExp(_referredClsTop));
            br.addItem("Ref Type");
            br.addElement(_refType);
            br.addItem("Non-Existing Code");
            br.addElement(nonExistingList.stream().map(element -> {
                return element.getCode();
            }).collect(Collectors.joining(", ")));
            final String msg = br.buildExceptionMessage();
            throw new DfIllegalPropertySettingException(msg);
        }
    }

    protected void doVerifyRefMatches(DfClassificationTop classificationTop) {
        final List<DfClassificationElement> webElementList = classificationTop.getClassificationElementList();
        final List<DfClassificationElement> dbElementList = _referredClsTop.getClassificationElementList();
        final boolean hasNonExisting = webElementList.stream().anyMatch(webElement -> {
            return !dbElementList.stream().anyMatch(dbElement -> {
                return webElement.getCode().equals(dbElement.getCode());
            });
        });
        if (webElementList.size() != dbElementList.size() || hasNonExisting) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Unmatched the web classification code with the referred classification");
            br.addItem("Advice");
            br.addElement("The codes of the web classification should match");
            br.addElement("with referred classification because of refType='matches'.");
            br.addElement("For example:");
            br.addElement("  (x): web(A), referred(A, C)");
            br.addElement("  (x): web(A, B), referred(A, C)");
            br.addElement("  (o): web(A, B), referred(A, B)");
            br.addElement("  (o): web(A, B, C), referred(A, B, C)");
            br.addItem("dfprop File");
            br.addElement(_resourceFile);
            br.addItem("AppCls");
            br.addElement(classificationTop.getClassificationName() + ": " + buildClsCodesExp(classificationTop));
            br.addItem("DBCls");
            br.addElement(_referredClsTop.getClassificationName() + ": " + buildClsCodesExp(_referredClsTop));
            br.addItem("Ref Type");
            br.addElement(_refType);
            br.addItem("Code Count");
            br.addElement("app=" + webElementList.size() + " / referred=" + dbElementList.size());
            final String msg = br.buildExceptionMessage();
            throw new DfIllegalPropertySettingException(msg);
        }
    }

    protected String buildClsCodesExp(DfClassificationTop top) {
        final String dbCodes = top.getClassificationElementList().stream().map(element -> {
            return element.getCode();
        }).collect(Collectors.joining(", "));
        return Srl.is_NotNull_and_NotTrimmedEmpty(dbCodes) ? dbCodes : "(no elements)";
    }
}
