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

/**
 * @author jflute
 * @since 1.2.5 split from DfRefClsElement (2021/07/03 Saturday at roppongi japanese)
 */
public class DfRefClsRefTypeVerifier {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfRefClsRefType _refType;
    protected final DfClassificationTop _dbClsTop;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfRefClsRefTypeVerifier(DfRefClsRefType refType, DfClassificationTop dbClsTop) {
        _refType = refType;
        _dbClsTop = dbClsTop;
    }

    // ===================================================================================
    //                                                               Verify Formal RefType
    //                                                               =====================
    public void verifyFormalRefType(DfClassificationTop classificationTop) {
        if (_refType.isFormalRefType()) {
            return;
        }
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown refType to DB classification in the app classification.");
        br.addItem("Advice");
        br.addElement("refType can be set: 'included', 'exists', 'matches'");
        br.addElement("  included : DB classification codes are included as app classification");
        br.addElement("  exists   : app classification codes should exist in DB classification");
        br.addElement("  matches  : app classification codes matches with DB classification");
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
        final List<DfClassificationElement> webElementList = classificationTop.getClassificationElementList();
        final List<DfClassificationElement> dbElementList = _dbClsTop.getClassificationElementList();
        final List<DfClassificationElement> nonExistingList = webElementList.stream().filter(webElement -> {
            return !dbElementList.stream().anyMatch(dbElement -> {
                return webElement.getCode().equals(dbElement.getCode());
            });
        }).collect(Collectors.toList());
        if (!nonExistingList.isEmpty()) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Not found the web classification code in the DB classification");
            br.addItem("Advice");
            br.addElement("The codes of the web classification should be included");
            br.addElement("in the DB classification because of refType='exists'.");
            br.addElement("For example:");
            br.addElement("  (x): web(A), db(B, C)");
            br.addElement("  (x): web(A, B), db(A, C)");
            br.addElement("  (x): web(A, B, C), db(A, B)");
            br.addElement("  (o): web(A), db(A, B)");
            br.addElement("  (o): web(A, B), db(A, B, C)");
            br.addElement("  (o): web(A, B, C), db(A, B, C)");
            br.addItem("WebCls");
            final String webCodes = classificationTop.getClassificationElementList().stream().map(element -> {
                return element.getCode();
            }).collect(Collectors.joining(", "));
            br.addElement(classificationTop.getClassificationName() + ": " + webCodes);
            br.addItem("DBCls");
            final String dbCodes = _dbClsTop.getClassificationElementList().stream().map(element -> {
                return element.getCode();
            }).collect(Collectors.joining(", "));
            br.addElement(_dbClsTop.getClassificationName() + ": " + dbCodes);
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
        final List<DfClassificationElement> dbElementList = _dbClsTop.getClassificationElementList();
        final boolean hasNonExisting = webElementList.stream().anyMatch(webElement -> {
            return !dbElementList.stream().anyMatch(dbElement -> {
                return webElement.getCode().equals(dbElement.getCode());
            });
        });
        if (webElementList.size() != dbElementList.size() || hasNonExisting) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Unmatched the web classification code with the DB classification");
            br.addItem("Advice");
            br.addElement("The codes of the web classification should match");
            br.addElement("with DB classification because of refType='matches'.");
            br.addElement("For example:");
            br.addElement("  (x): web(A), db(A, C)");
            br.addElement("  (x): web(A, B), db(A, C)");
            br.addElement("  (o): web(A, B), db(A, B)");
            br.addElement("  (o): web(A, B, C), db(A, B, C)");
            br.addItem("WebCls");
            final String webCodes = classificationTop.getClassificationElementList().stream().map(element -> {
                return element.getCode();
            }).collect(Collectors.joining(", "));
            br.addElement(classificationTop.getClassificationName() + ": " + webCodes);
            br.addItem("DBCls");
            final String dbCodes = _dbClsTop.getClassificationElementList().stream().map(element -> {
                return element.getCode();
            }).collect(Collectors.joining(", "));
            br.addElement(_dbClsTop.getClassificationName() + ": " + dbCodes);
            br.addItem("Ref Type");
            br.addElement(_refType);
            br.addItem("Code Count");
            br.addElement("web=" + webElementList.size() + " / db=" + dbElementList.size());
            final String msg = br.buildExceptionMessage();
            throw new DfIllegalPropertySettingException(msg);
        }
    }
}
