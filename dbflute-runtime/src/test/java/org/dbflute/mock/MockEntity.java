/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.mock;

import java.util.Collections;
import java.util.Set;

import org.dbflute.Entity;
import org.dbflute.dbmeta.DBMeta;

/**
 * @author jflute
 * @since 1.0.5F (2014/05/05 Monday)
 */
public class MockEntity implements Entity {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Integer _memberId;
    protected String _memberName;

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    public DBMeta asDBMeta() {
        return new MockDBMeta();
    }

    public String asTableDbName() {
        return "MEMBER";
    }

    public String getTablePropertyName() {
        return "Member";
    }

    // ===================================================================================
    //                                                                 Modified Properties
    //                                                                 ===================
    public Set<String> mymodifiedProperties() {
        return Collections.emptySet();
    }

    public void mymodifyProperty(String propertyName) {
    }

    public void mymodifyPropertyCancel(String propertyName) {
    }

    public void clearModifiedInfo() {
    }

    public boolean hasModification() {
        return false;
    }

    public void modifiedToSpecified() {
    }

    public Set<String> myspecifiedProperties() {
        return null;
    }

    public void myspecifyProperty(String propertyName) {
    }

    public void myspecifyPropertyCancel(String propertyName) {
    }

    public void clearSpecifiedInfo() {
    }

    // ===================================================================================
    //                                                                          Unique Key
    //                                                                          ==========
    public boolean hasPrimaryKeyValue() {
        return _memberId != null;
    }

    public Set<String> myuniqueDrivenProperties() {
        return Collections.emptySet();
    }

    public void myuniqueByProperty(String propertyName) {
    }

    public void myuniqueByPropertyCancel(String propertyName) {
    }

    public void clearUniqueDrivenInfo() {
    }

    // ===================================================================================
    //                                                                      Classification
    //                                                                      ==============
    public void myunlockUndefinedClassificationAccess() {
    }

    public boolean myundefinedClassificationAccessAllowed() {
        return false;
    }

    // ===================================================================================
    //                                                                     Birthplace Mark
    //                                                                     ===============
    public void markAsSelect() {
    }

    public boolean createdBySelect() {
        return false;
    }

    @Override
    public void clearMarkAsSelect() {
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public int hashCode() {
        return _memberId != null ? _memberId : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MockEntity) {
            final MockEntity other = (MockEntity) obj;
            if (_memberId != null) {
                return _memberId.equals(other._memberId);
            } else {
                return other._memberId == null;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "{" + _memberId + ", " + _memberName + "}";
    }

    public int instanceHash() {
        return super.hashCode();
    }

    public String toStringWithRelation() {
        return null;
    }

    public String buildDisplayString(String name, boolean column, boolean relation) {
        return null;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Integer getMemberId() {
        return _memberId;
    }

    public void setMemberId(Integer memberId) {
        this._memberId = memberId;
    }

    public String getMemberName() {
        return _memberName;
    }

    public void setMemberName(String memberName) {
        this._memberName = memberName;
    }
}
