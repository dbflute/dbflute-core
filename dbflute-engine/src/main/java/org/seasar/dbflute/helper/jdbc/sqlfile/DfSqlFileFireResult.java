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
package org.seasar.dbflute.helper.jdbc.sqlfile;

import java.util.ArrayList;
import java.util.List;

import org.seasar.dbflute.exception.SQLFailureException;

/**
 * @author jflute
 */
public class DfSqlFileFireResult {

    protected String _resultMessage;
    protected String _detailMessage;
    protected SQLFailureException _breakCause;
    protected boolean _existsError; // break or continue-error
    protected final List<DfSqlFileRunnerResult> _runnerResultList = new ArrayList<DfSqlFileRunnerResult>();

    public int getTotalSqlCount() {
        int totalSqlCount = 0;
        for (DfSqlFileRunnerResult runnerResult : _runnerResultList) {
            final int currentCount = runnerResult.getTotalSqlCount();
            if (currentCount > 0) { // may be -1
                totalSqlCount = totalSqlCount + currentCount;
            }
        }
        return totalSqlCount;
    }

    public String getResultMessage() {
        return _resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this._resultMessage = resultMessage;
    }

    public String getDetailMessage() {
        return _detailMessage;
    }

    public void setDetailMessage(String detailMessage) {
        this._detailMessage = detailMessage;
    }

    public SQLFailureException getBreakCause() {
        return _breakCause;
    }

    public void setBreakCause(SQLFailureException breakCause) {
        this._breakCause = breakCause;
    }

    public boolean isExistsError() {
        return _existsError;
    }

    public void setExistsError(boolean existsError) {
        this._existsError = existsError;
    }

    public List<DfSqlFileRunnerResult> getRunnerResultList() {
        return _runnerResultList;
    }

    public void addRunnerResult(DfSqlFileRunnerResult runnerResult) {
        this._runnerResultList.add(runnerResult);
    }
}
