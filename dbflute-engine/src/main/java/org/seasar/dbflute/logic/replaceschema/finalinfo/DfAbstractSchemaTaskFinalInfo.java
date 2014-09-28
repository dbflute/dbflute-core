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
package org.seasar.dbflute.logic.replaceschema.finalinfo;

import java.util.ArrayList;
import java.util.List;

import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfAbstractSchemaTaskFinalInfo {

    protected String _resultMessage;
    protected final List<String> _detailMessageList = new ArrayList<String>();
    protected boolean _failure;

    public boolean isValidInfo() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(_resultMessage);
    }

    public String getResultMessage() {
        return _resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this._resultMessage = resultMessage;
    }

    public List<String> getDetailMessageList() {
        return _detailMessageList;
    }

    public void addDetailMessage(String detailMessage) {
        this._detailMessageList.add(detailMessage);
    }

    public boolean isFailure() {
        return _failure;
    }

    public void setFailure(boolean failure) {
        this._failure = failure;
    }
}
