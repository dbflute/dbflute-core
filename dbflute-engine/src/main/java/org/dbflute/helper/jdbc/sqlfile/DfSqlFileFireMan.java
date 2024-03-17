/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.helper.jdbc.sqlfile;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import org.dbflute.exception.DfFireSqlScriptSQLException;
import org.dbflute.exception.SQLFailureException;
import org.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerResult.ErrorContinuedSql;
import org.dbflute.helper.process.ProcessResult;
import org.dbflute.helper.process.SystemScript;
import org.dbflute.helper.process.exception.SystemScriptUnsupportedScriptException;
import org.dbflute.helper.token.line.LineToken;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 */
public class DfSqlFileFireMan {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfSqlFileFireMan.class);

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

    protected void buildResultMessage(List<File> sqlFileList, final DfSqlFileFireResult fireResult, int goodSqlCount, int totalSqlCount,
            final String title) {
        final StringBuilder resultSb = new StringBuilder();
        resultSb.append(title).append(": success=").append(goodSqlCount);
        resultSb.append(", failure=");
        if (fireResult.getBreakCause() != null) {
            resultSb.append("1 *break");
        } else { // normal or continue-error
            resultSb.append(totalSqlCount - goodSqlCount);
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
                final List<String> tokenizedList = lineToken.tokenize(message, op -> op.delimitateBy(ln()));
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
    protected DfSqlFileRunnerResult processSqlFile(DfSqlFileRunner runner, File sqlFile) { // may be overridden
        runner.prepare(sqlFile);
        return runner.runTransaction();
    }

    protected boolean isShowSQLState(SQLException sqlEx) {
        if (sqlEx instanceof DfFireSqlScriptSQLException) {
            return false;
        }
        return true;
    }

    // for extension at sub class
    protected boolean isScriptFile(File sqlFile, String[] scriptExtAry) {
        return Srl.endsWith(resolvePath(sqlFile), scriptExtAry);
    }

    protected DfSqlFileRunnerResult processScriptFile(DfSqlFileRunner runner, SystemScript script, File sqlFile) {
        runner.prepare(sqlFile);
        return executeScriptFile(runner, script, sqlFile);
    }

    protected DfSqlFileRunnerResult executeScriptFile(DfSqlFileRunner runner, SystemScript script, File sqlFile) {
        final String sqlPath = resolvePath(sqlFile);
        final String baseDir = Srl.substringLastFront(sqlPath, "/");
        final String scriptName = Srl.substringLastRear(sqlPath, "/");
        _log.info("...Executing the script: " + sqlPath);
        final ProcessResult processResult;
        try {
            processResult = script.execute(new File(baseDir), scriptName);
        } catch (SystemScriptUnsupportedScriptException ignored) {
            _log.info("Skipped the script for system mismatch: " + scriptName);
            return null;
        }
        final String console = processResult.getConsole();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(console)) {
            _log.info("Caught the console for " + scriptName + ":" + ln() + console);
        }
        final DfSqlFileRunnerResult runnerResult = new DfSqlFileRunnerResult(sqlFile);
        runnerResult.setTotalSqlCount(1);
        final int exitCode = processResult.getExitCode();
        if (exitCode != 0) {
            final String msg = "The script failed: " + scriptName + " exitCode=" + exitCode;
            // wrapping quickly because SQLFailureException needs SQLException
            // (and nested exception message has debug information so simple message here)
            final SQLException sqlEx = new DfFireSqlScriptSQLException(msg);
            final SQLFailureException failureEx = new SQLFailureException("Break the process for script failure.", sqlEx);
            // no error continue, script error is treated as programming error
            // because you can freely skip SQL failure in your script
            runnerResult.setBreakCause(failureEx);
            return runnerResult;
        } else {
            runnerResult.setGoodSqlCount(1);
            return runnerResult;
        }
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return "\n";
    }

    protected String resolvePath(File file) {
        return Srl.replace(file.getPath(), "\\", "/");
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
