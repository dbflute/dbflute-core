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

import org.seasar.dbflute.exception.DfAlterCheckAlterSqlFailureException;
import org.seasar.dbflute.exception.DfAlterCheckDifferenceFoundException;
import org.seasar.dbflute.exception.DfAlterCheckReplaceSchemaFailureException;
import org.seasar.dbflute.exception.DfAlterCheckSavePreviousFailureException;
import org.seasar.dbflute.exception.DfTakeFinallyAssertionFailureException;
import org.seasar.dbflute.exception.SQLFailureException;

/**
 * @author jflute
 */
public class DfAlterCheckFinalInfo extends DfAbstractSchemaTaskFinalInfo {

    // one exists, the others always does not exist
    protected final List<File> _alterSqlFileList = new ArrayList<File>();
    protected final List<File> _submittedDraftFileList = new ArrayList<File>();
    protected int _alterSqlCount;
    protected SQLFailureException _breakCause;
    protected DfAlterCheckSavePreviousFailureException _savePreviousFailureEx;
    protected DfAlterCheckAlterSqlFailureException _alterSqlFailureEx;
    protected DfTakeFinallyAssertionFailureException _takeFinallyAssertionEx;
    protected DfAlterCheckReplaceSchemaFailureException _replaceSchemaFailureEx;
    protected DfAlterCheckDifferenceFoundException _diffFoundEx;

    public boolean hasAlterSqlExecution() {
        return _alterSqlCount > 0;
    }

    public boolean hasAlterCheckDiff() {
        return _diffFoundEx != null;
    }

    public void throwAlterCheckExceptionIfExists() {
        if (_breakCause != null) {
            throw _breakCause;
        }
        if (_savePreviousFailureEx != null) {
            throw _savePreviousFailureEx;
        }
        if (_alterSqlFailureEx != null) {
            throw _alterSqlFailureEx;
        }
        if (_takeFinallyAssertionEx != null) {
            throw _takeFinallyAssertionEx;
        }
        if (_replaceSchemaFailureEx != null) {
            throw _replaceSchemaFailureEx;
        }
        if (_diffFoundEx != null) {
            throw _diffFoundEx;
        }
    }

    public List<File> getAlterSqlFileList() {
        return _alterSqlFileList;
    }

    public void addAlterSqlFileAll(List<File> alterSqlFileList) {
        this._alterSqlFileList.addAll(alterSqlFileList);
    }

    public List<File> getSubmittedDraftFileList() {
        return _submittedDraftFileList;
    }

    public void addSubmittedDraftFileAll(List<File> submittedDraftFileList) {
        this._submittedDraftFileList.addAll(submittedDraftFileList);
    }

    public int getAlterSqlCount() {
        return _alterSqlCount;
    }

    public void setAlterSqlCount(int alterSqlCount) {
        this._alterSqlCount = alterSqlCount;
    }

    public SQLFailureException getBreakCause() {
        return _breakCause;
    }

    public void setBreakCause(SQLFailureException breakCause) {
        this._breakCause = breakCause;
    }

    public DfAlterCheckSavePreviousFailureException getSavePreviousFailureEx() {
        return _savePreviousFailureEx;
    }

    public void setSavePreviousFailureEx(DfAlterCheckSavePreviousFailureException savePreviousFailureEx) {
        _savePreviousFailureEx = savePreviousFailureEx;
    }

    public DfAlterCheckAlterSqlFailureException getAlterSqlFailureEx() {
        return _alterSqlFailureEx;
    }

    public void setAlterSqlFailureEx(DfAlterCheckAlterSqlFailureException alterSqlFailureEx) {
        this._alterSqlFailureEx = alterSqlFailureEx;
    }

    public DfTakeFinallyAssertionFailureException getTakeFinallyAssertionEx() {
        return _takeFinallyAssertionEx;
    }

    public void setTakeFinallyAssertionEx(DfTakeFinallyAssertionFailureException takeFinallyAssertionEx) {
        this._takeFinallyAssertionEx = takeFinallyAssertionEx;
    }

    public DfAlterCheckReplaceSchemaFailureException getReplaceSchemaFailureEx() {
        return _replaceSchemaFailureEx;
    }

    public void setReplaceSchemaFailureEx(DfAlterCheckReplaceSchemaFailureException replaceSchemaFailureEx) {
        this._replaceSchemaFailureEx = replaceSchemaFailureEx;
    }

    public DfAlterCheckDifferenceFoundException getDiffFoundEx() {
        return _diffFoundEx;
    }

    public void setDiffFoundEx(DfAlterCheckDifferenceFoundException diffFoundEx) {
        this._diffFoundEx = diffFoundEx;
    }
}
