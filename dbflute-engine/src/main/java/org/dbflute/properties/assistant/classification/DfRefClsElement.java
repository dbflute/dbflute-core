/*
 * Copyright 2014-2016 the original author or authors.
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
package org.dbflute.properties.assistant.classification;

import java.util.List;
import java.util.stream.Collectors;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;

/**
 * @author jflute
 * @since 1.1.1 (2016/01/11 Monday)
 */
public class DfRefClsElement { // for webCls

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String KEY_REFCLS = "refCls";
    public static final String KEY_REFTYPE = "refType";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _projectName;
    protected final String _classificationName;
    protected final String _classificationType;
    protected final String _refType;
    protected final DfClassificationTop _dbClsTop;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfRefClsElement(String projectName, String classificationName, String classificationType, String refType,
            DfClassificationTop dbClsTop) {
        _projectName = projectName;
        _classificationName = classificationName;
        _classificationType = classificationType;
        _refType = refType;
        _dbClsTop = dbClsTop;
    }

    // ===================================================================================
    //                                                                    Check by RefType
    //                                                                    ================
    public void checkFormalRefType(DfClassificationTop classificationTop) {
        if (isRefTypeIncluded() || isRefTypeExists() || isRefTypeMatches()) {
            return;
        }
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown refType to DB classification in the web classification.");
        br.addItem("Advice");
        br.addElement("refType can be set: 'included', 'exists', 'matches'");
        br.addElement("  included : DB classification codes are included as web classification");
        br.addElement("  exists   : web classification codes should exist in DB classification");
        br.addElement("  matches  : web classification codes matches with DB classification");
        br.addItem("WebCls");
        br.addElement(classificationTop.getClassificationName());
        br.addItem("Unknown refType");
        br.addElement(_refType);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    public void checkRelationshipByRefTypeIfNeeds(DfClassificationTop classificationTop) {
        if (isRefTypeExists()) {
            checkRefExists(classificationTop);
        } else if (isRefTypeMatches()) {
            checkRefMatches(classificationTop);
        }
    }

    protected void checkRefExists(DfClassificationTop classificationTop) {
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

    protected void checkRefMatches(DfClassificationTop classificationTop) {
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

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final String projectExp = _projectName != null ? "(" + _projectName + ")" : "";
        return "{" + projectExp + _classificationName + ", " + _refType + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getProjectName() {
        return _projectName;
    }

    public String getClassificationName() {
        return _classificationName;
    }

    public String getClassificationType() {
        return _classificationType;
    }

    public boolean isRefTypeExists() {
        return _refType.equals("exists");
    }

    public boolean isRefTypeMatches() {
        return _refType.equals("matches");
    }

    public boolean isRefTypeIncluded() { // as default
        return _refType.equals("included");
    }

    public String getRefType() {
        return _refType;
    }

    public DfClassificationTop getDBClsTop() {
        return _dbClsTop;
    }
}
