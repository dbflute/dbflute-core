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
    protected final String _refClsTheme; // e.g. maihamadb, not null
    protected final String _classificationName; // e.g. MemberStatus, not null
    protected final String _groupName; // null allowed
    protected final DfRefClsRefType _refType; // not null
    protected final DfClassificationTop _referredClsTop; // not null
    protected final DfRefClsReferredCDef _referredCDef; // not null
    protected final String _resourceFile; // for e.g. exception message, not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfRefClsElement(String refClsTheme, String classificationName, String groupName, String refType,
            DfClassificationTop referredClsTop, DfRefClsReferredCDef referredCDef, String resourceFile) {
        _refClsTheme = refClsTheme;
        _classificationName = classificationName;
        _groupName = groupName;
        _refType = new DfRefClsRefType(refType);
        _referredClsTop = referredClsTop;
        _referredCDef = referredCDef;
        _resourceFile = resourceFile;
    }

    // ===================================================================================
    //                                                               Determine refClsTheme
    //                                                               =====================
    public boolean isRefClsThemeAppCls() {
        return _refClsTheme.equals("appcls");
    }

    public boolean isRefClsThemeWebCls() {
        return _refClsTheme.equals("webcls");
    }

    public boolean isRefClsThemeNamedCls() {
        return _refClsTheme.endsWith("_cls");
    }

    // ===================================================================================
    //                                                                    Determine refTyp
    //                                                                    ================
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
    //                                                                      Verify refType
    //                                                                      ==============
    public void verifyFormalRefType(DfClassificationTop classificationTop) {
        createRefClsRefTypeVerifier().verifyFormalRefType(classificationTop);
    }

    public void verifyRelationshipByRefTypeIfNeeds(DfClassificationTop classificationTop) {
        createRefClsRefTypeVerifier().verifyRelationshipByRefTypeIfNeeds(classificationTop);
    }

    protected DfRefClsRefTypeVerifier createRefClsRefTypeVerifier() {
        return new DfRefClsRefTypeVerifier(_refType, _referredClsTop, _resourceFile);
    }

    // ===================================================================================
    //                                                                       Referred CDef
    //                                                                       =============
    public String getReferredCDefPackage() {
        return _referredCDef.getCDefPackage();
    }

    public String getReferredCDefClassName() {
        return _referredCDef.getCDefClassName();
    }

    public String getReferredCDefType() {
        return _referredCDef.getCDefClassName() + "." + _classificationName;
    }

    // ===================================================================================
    //                                                                       Collaboration
    //                                                                       =============
    public String getCollaborationAdjective() { // in e.g. text
        if (isUseRefExp()) {
            return "referred";
        } else { // as default
            return "DB"; // to keep compatible
        }
    }

    public String getCollaborationWord() { // in e.g. method
        if (isUseRefExp()) {
            return "Ref";
        } else { // as default
            return "DB"; // to keep compatible
        }
    }

    protected boolean isUseRefExp() {
        return isRefClsThemeAppCls() || isRefClsThemeWebCls() || isRefClsThemeNamedCls();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final String projectExp = _refClsTheme != null ? "(" + _refClsTheme + ")" : "";
        return "{" + projectExp + _classificationName + ", " + _refType + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getRefClsTheme() {
        return _refClsTheme;
    }

    public String getClassificationName() {
        return _classificationName;
    }

    public String getClassificationType() { // compatible for plain freegen, just in case
        return getReferredCDefType();
    }

    public String getGroupName() {
        return _groupName; // null allowed
    }

    public String getRefType() { // may be used in template
        return _refType.getRefTypeValue();
    }

    public DfClassificationTop getReferredClsTop() {
        return _referredClsTop;
    }
}
