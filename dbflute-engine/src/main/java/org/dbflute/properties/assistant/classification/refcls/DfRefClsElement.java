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

import org.dbflute.properties.assistant.classification.DfClassificationTop;

/**
 * @author jflute
 * @since 1.1.1 (2016/01/11 Monday)
 */
public class DfRefClsElement { // directly used in template

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String KEY_REFCLS = "refCls";
    public static final String KEY_REFTYPE = "refType";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _projectName; // not null
    protected final String _classificationName; // not null
    protected final String _classificationType; // not null
    protected final String _groupName; // null allowed
    protected final DfRefClsRefType _refType; // not null
    protected final DfClassificationTop _dbClsTop; // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfRefClsElement(String projectName, String classificationName, String classificationType, String groupName, String refType,
            DfClassificationTop dbClsTop) {
        _projectName = projectName;
        _classificationName = classificationName;
        _classificationType = classificationType;
        _groupName = groupName;
        _refType = new DfRefClsRefType(refType);
        _dbClsTop = dbClsTop;
    }

    // ===================================================================================
    //                                                                   Determine RefType
    //                                                                   =================
    public boolean isRefTypeIncluded() {
        return _refType.isRefTypeIncluded();
    }

    public boolean isRefTypeExists() {
        return _refType.isRefTypeExists();
    }

    public boolean isRefTypeMatches() {
        return _refType.isRefTypeMatches();
    }

    // ===================================================================================
    //                                                                      Verify RefType
    //                                                                      ==============
    public void verifyFormalRefType(DfClassificationTop classificationTop) {
        createRefClsRefTypeVerifier().verifyFormalRefType(classificationTop);
    }

    public void verifyRelationshipByRefTypeIfNeeds(DfClassificationTop classificationTop) {
        createRefClsRefTypeVerifier().verifyRelationshipByRefTypeIfNeeds(classificationTop);
    }

    protected DfRefClsRefTypeVerifier createRefClsRefTypeVerifier() {
        return new DfRefClsRefTypeVerifier(_refType, _dbClsTop);
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

    public String getGroupName() {
        return _groupName; // null allowed
    }

    public String getRefType() { // may be used in template
        return _refType.getRefTypeValue();
    }

    public DfClassificationTop getDBClsTop() {
        return _dbClsTop;
    }
}
