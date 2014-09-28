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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.seasar.dbflute.exception.DfTakeFinallyAssertionFailureException;
import org.seasar.dbflute.exception.SQLFailureException;

/**
 * @author jflute
 */
public class DfTakeFinallyFinalInfo extends DfAbstractSchemaTaskFinalInfo {

    protected final List<File> _takeFinallySqlFileList = new ArrayList<File>();
    protected SQLFailureException _breakCause;
    protected DfTakeFinallyAssertionFailureException _assertionEx;

    public List<File> getTakeFinallySqlFileList() {
        return _takeFinallySqlFileList;
    }

    public void addTakeFinallySqlFileAll(List<File> takeFinallySqlFileList) {
        this._takeFinallySqlFileList.addAll(takeFinallySqlFileList);
    }

    public SQLFailureException getBreakCause() {
        return _breakCause;
    }

    public void setBreakCause(SQLFailureException breakCause) {
        this._breakCause = breakCause;
    }

    public DfTakeFinallyAssertionFailureException getAssertionEx() {
        return _assertionEx;
    }

    public void setAssertionEx(DfTakeFinallyAssertionFailureException assertionEx) {
        this._assertionEx = assertionEx;
    }
}
