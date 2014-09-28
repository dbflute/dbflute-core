/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.mock;

import java.util.Collections;
import java.util.Set;

import org.seasar.dbflute.Entity;
import org.seasar.dbflute.dbmeta.DBMeta;

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
    public DBMeta getDBMeta() {
        return null;
    }

    public String getTableDbName() {
        return "MEMBER";
    }

    public String getTablePropertyName() {
        return "Member";
    }

    public boolean hasPrimaryKeyValue() {
        return _memberId != null;
    }

    public Set<String> myuniqueDrivenProperties() {
        return Collections.emptySet();
    }

    public Set<String> modifiedProperties() {
        return Collections.emptySet();
    }

    public void clearModifiedInfo() {
    }

    public boolean hasModification() {
        return false;
    }

    public void markAsSelect() {
    }

    public boolean createdBySelect() {
        return false;
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
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public int hashCode() {
        return _memberId;
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
