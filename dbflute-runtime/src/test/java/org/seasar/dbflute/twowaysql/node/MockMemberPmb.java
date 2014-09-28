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
package org.seasar.dbflute.twowaysql.node;

import java.util.List;

/**
 * @author jflute
 */
public class MockMemberPmb {

    protected Integer _memberId;
    protected String _memberName;
    protected List<Integer> _memberIdList;
    protected List<String> _memberNameList;
    protected String[] _memberNames;

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

    public List<Integer> getMemberIdList() {
        return _memberIdList;
    }

    public void setMemberIdList(List<Integer> memberIdList) {
        this._memberIdList = memberIdList;
    }

    public List<String> getMemberNameList() {
        return _memberNameList;
    }

    public void setMemberNameList(List<String> memberNameList) {
        this._memberNameList = memberNameList;
    }

    public String[] getMemberNames() {
        return _memberNames;
    }

    public void setMemberNames(String[] emberNames) {
        this._memberNames = emberNames;
    }
}
