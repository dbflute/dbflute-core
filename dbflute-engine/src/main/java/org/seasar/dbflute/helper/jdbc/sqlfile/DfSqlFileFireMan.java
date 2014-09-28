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

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.exception.DfAlterCheckAlterScriptSQLException;
import org.seasar.dbflute.exception.SQLFailureException;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerResult.ErrorContinuedSql;
import org.seasar.dbflute.helper.token.line.LineToken;
import org.seasar.dbflute.helper.token.line.LineTokenizingOption;

/**
 * @author jflute
 */
public class DfSqlFileFireMan {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static Log _log = LogFactory.getLog(DfSqlFileFireMan.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _executorName;

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    /**
     * Load the SQL files and then fire them.
     * @return The result about firing SQL. (NotNull)
     */
    public DfSqlFileFireResult fire(DfSqlFileRunner runner, List<File> sqlFileList) {
        final DfSqlFileFireResult fireResult = new DfSqlFileFireResult();
        SQLFailureException breakCause = null;
        int goodSqlCount = 0;
        int totalSqlCount = 0;
        for (final File sqlFile : sqlFileList) {
            if (!sqlFile.exists()) {
                String msg = "The file was not found: " + sqlFile;
                throw new IllegalStateException(msg);
            }

            if (_log.isInfoEnabled()) {
                _log.info("...Firing: " + sqlFile.getName());
            }

            final DfSqlFileRunnerResult runnerResult = processSqlFile(runner, sqlFile);
            if (runnerResult != null) {
                fireResult.addRunnerResult(runnerResult);
                goodSqlCount = goodSqlCount + runnerResult.getGoodSqlCount();
                breakCause = runnerResult.getBreakCause();
                if (breakCause != null) {
                    break;
                } else {
                    totalSqlCount = totalSqlCount + runnerResult.getTotalSqlCount();
                }
            }
        }
        final String title = _executorName != null ? _executorName : "Fired SQL";

        // Break Cause
        fireResult.setBreakCause(breakCause);

        // Exists Error
        fireResult.setExistsError((breakCause != null) || (totalSqlCount > goodSqlCount));

        // Result Message
        buildResultMessage(sqlFileList, fireResult, goodSqlCount, totalSqlCount, title);
        _log.info(fireResult.getResultMessage());

        // Detail Message
        fireResult.setDetailMessage(buildDetailMessage(fireResult));
        return fireResult;
    }

    protected void buildResultMessage(List<File> sqlFileList, final DfSqlFileFireResult fireResult, int goodSqlCount,
            int totalSqlCount, final String title) {
        final StringBuilder resultSb = new StringBuilder();
        resultSb.append("{").append(title).append("}: success=").append(goodSqlCount);
        if (fireResult.getBreakCause() != null) {
            resultSb.append(" failure=1 *break");
        } else { // normal or continue-error
            resultSb.append(" failure=").append(totalSqlCount - goodSqlCount);
        }
        resultSb.append(" (in ").append(sqlFileList.size()).append(" files)");
        fireResult.setResultMessage(resultSb.toString());
    }

    protected String buildDetailMessage(DfSqlFileFireResult fireResult) {
        final StringBuilder sb = new StringBuilder();
        final List<DfSqlFileRunnerResult> runnerResultList = fireResult.getRunnerResultList();
        for (DfSqlFileRunnerResult runnerResult : runnerResultList) {
            final List<ErrorContinuedSql> errorContinuedSqlList = runnerResult.getErrorContinuedSqlList();
            final String fileName = runnerResult.getSqlFile().getName();
            final SQLFailureException breakCause = runnerResult.getBreakCause();
            if (sb.length() > 0) {
                sb.append(ln());
            }
            if (breakCause != null) { // break by error
                sb.append("x ").append(fileName);
                sb.append(ln()).append(" >> (failed: Look at the exception message)");
            } else { // normal or error-continued
                if (errorContinuedSqlList.isEmpty()) { // OK or skipped
                    sb.append(!runnerResult.isSkippedFile() ? "o " : "v ").append(fileName);
                } else {
                    sb.append("x ").append(fileName);
                    doBuildErrorContinuedMessage(sb, errorContinuedSqlList);
                }
            }
        }
        return sb.toString();
    }

    protected void doBuildErrorContinuedMessage(StringBuilder sb, List<ErrorContinuedSql> errorContinuedSqlList) {
        for (ErrorContinuedSql errorContinuedSql : errorContinuedSqlList) {
            final String sql = errorContinuedSql.getSql();
            sb.append(ln()).append(sql);
            final SQLException sqlEx = errorContinuedSql.getSqlEx();
            String message = sqlEx.getMessage();
            if (sqlEx != null && message != null) {
                message = message.trim();
                final LineToken lineToken = new LineToken();
                final LineTokenizingOption lineTokenizingOption = new LineTokenizingOption();
                lineTokenizingOption.setDelimiter(ln());
                final List<String> tokenizedList = lineToken.tokenize(message, lineTokenizingOption);
                int elementIndex = 0;
                for (String element : tokenizedList) {
                    if (elementIndex == 0) {
                        sb.append(ln()).append(" >> ").append(element);
                    } else {
                        sb.append(ln()).append("    ").append(element);
                    }
                    ++elementIndex;
                }
                if (isShowSQLState(sqlEx)) {
                    sb.append(ln());
                    sb.append("    (SQLState=").append(sqlEx.getSQLState());
                    sb.append(" ErrorCode=").append(sqlEx.getErrorCode()).append(")");
                }
            }
        }
    }

    /**
     * @param runner The instance of runner. (NotNull)
     * @param sqlFile The SQL file. (NotNull)
     * @return The result of the running. (NullAllowed: means skipped)
     */
    protected DfSqlFileRunnerResult processSqlFile(DfSqlFileRunner runner, File sqlFile) {
        runner.prepare(sqlFile);
        return runner.runTransaction();
    }

    protected boolean isShowSQLState(SQLException sqlEx) {
        if (sqlEx instanceof DfAlterCheckAlterScriptSQLException) {
            return false;
        }
        return true;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return "\n";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getExecutorName() {
        return _executorName;
    }

    public void setExecutorName(String executorName) {
        this._executorName = executorName;
    }
}
