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
package org.seasar.dbflute.task;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.exception.DfOutsideSqlTestFailureFoundException;
import org.seasar.dbflute.exception.SQLFailureException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.jdbc.DfRunnerInformation;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileFireMan;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileFireResult;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerExecute;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerResult;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerResult.ErrorContinuedSql;
import org.seasar.dbflute.logic.outsidesqltest.DfOutsideSqlChecker;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfOutsideSqlPack;
import org.seasar.dbflute.properties.DfOutsideSqlProperties;
import org.seasar.dbflute.task.DfDBFluteTaskStatus.TaskType;
import org.seasar.dbflute.task.bs.DfAbstractTask;
import org.seasar.dbflute.task.bs.assistant.DfSpecifiedSqlFile;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;
import org.seasar.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 */
public class DfOutsideSqlTestTask extends DfAbstractTask {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfOutsideSqlTestTask.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The set of non-target SQL file. */
    protected final Set<File> _nonTargetSqlFileSet = new HashSet<File>();

    /** The result of SqlFile fire. (NotNull after fire) */
    protected DfSqlFileFireResult _fireResult;

    // ===================================================================================
    //                                                                           Beginning
    //                                                                           =========
    @Override
    protected boolean begin() {
        if (getBasicProperties().isSuppressOutsideSqlTestTask()) {
            _log.info("...Suppressing OutsideSqlTest task as basicInfoMap.dfprop");
            return false;
        }
        _log.info("+------------------------------------------+");
        _log.info("|                                          |");
        _log.info("|              OutsideSqlTest              |");
        _log.info("|                                          |");
        _log.info("+------------------------------------------+");
        DfDBFluteTaskStatus.getInstance().setTaskType(TaskType.OutsideSqlTest);
        return true;
    }

    // ===================================================================================
    //                                                                          DataSource
    //                                                                          ==========
    @Override
    protected boolean isUseDataSource() {
        return true;
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected void doExecute() {
        final DfRunnerInformation runInfo = createRunnerInformation();
        final DfSqlFileFireMan fireMan = createSqlFileFireMan();
        final List<File> sqlFileList = getTargetSqlFileList();
        _fireResult = fireMan.fire(getSqlFileRunner(runInfo), sqlFileList);
        handleSqlFileFailure(_fireResult, sqlFileList);
    }

    // ===================================================================================
    //                                                                        SqlFile Fire
    //                                                                        ============
    protected DfSqlFileFireMan createSqlFileFireMan() {
        return new DfSqlFileFireMan();
    }

    protected DfRunnerInformation createRunnerInformation() {
        final DfRunnerInformation runInfo = new DfRunnerInformation();
        runInfo.setDriver(getDriver());
        runInfo.setUrl(getUrl());
        runInfo.setUser(getUser());
        runInfo.setPassword(getPassword());
        runInfo.setBreakCauseThrow(isBreakCauseThrow());
        runInfo.setErrorContinue(isErrorContinue());
        runInfo.setAutoCommit(isAutoCommit());
        runInfo.setRollbackOnly(isRollbackOnly());
        runInfo.setIgnoreTxError(isIgnoreTxError());
        customizeRunnerInformation(runInfo);
        return runInfo;
    }

    // ===================================================================================
    //                                                                       Main Override
    //                                                                       =============
    protected List<File> getTargetSqlFileList() {
        final DfOutsideSqlPack outsideSqlPack = collectOutsideSqlChecked();
        final String specifiedSqlFile = DfSpecifiedSqlFile.getInstance().getSpecifiedSqlFile();
        if (specifiedSqlFile != null) {
            final List<File> filteredList = new ArrayList<File>();
            for (File sqlFile : outsideSqlPack.getPhysicalFileList()) {
                final String fileName = sqlFile.getName();
                if (specifiedSqlFile.equals(fileName)) {
                    filteredList.add(sqlFile);
                }
            }
            return filteredList;
        } else {
            return outsideSqlPack.getPhysicalFileList();
        }
    }

    protected DfSqlFileRunnerExecute getSqlFileRunner(final DfRunnerInformation runInfo) {
        final String nonTargetMark = "df:x";
        final DBDef currentDBDef = getDatabaseTypeFacadeProp().getCurrentDBDef();
        return new DfSqlFileRunnerExecute(runInfo, getDataSource()) {
            @Override
            protected String filterSql(String sql) {
                // /- - - - - - - - - - - - - - - - - - - - - - - - - - 
                // check parameter comments in the SQL before filtering
                // - - - - - - - - - -/
                checkParameterComment(_sqlFile, sql);

                // filter comments if it needs.
                if (!currentDBDef.dbway().isBlockCommentSupported()) {
                    sql = removeBlockComment(sql);
                }
                if (!currentDBDef.dbway().isLineCommentSupported()) {
                    sql = removeLineComment(sql);
                }

                return super.filterSql(sql);
            }

            protected String removeBlockComment(final String sql) {
                return Srl.removeBlockComment(sql);
            }

            protected String removeLineComment(final String sql) {
                return Srl.removeLineComment(sql);
            }

            @Override
            protected boolean isTargetSql(String sql) {
                final String entityName = getEntityName(sql);
                if (entityName != null && nonTargetMark.equalsIgnoreCase(entityName)) { // non-target SQL
                    _nonTargetSqlFileSet.add(_sqlFile);
                    _log.info("...Skipping the SQL by non-target mark '" + nonTargetMark + "'");
                    return false;
                }
                return super.isTargetSql(sql);
            }

            @Override
            protected void traceSql(String sql) {
                _log.info("SQL:" + ln() + sql);
            }

            @Override
            protected void traceResult(int goodSqlCount, int totalSqlCount) {
                _log.info(" -> success=" + goodSqlCount + " failure=" + (totalSqlCount - goodSqlCount) + ln());
            }

            protected String getEntityName(final String sql) {
                return getTargetString(sql, "#");
            }

            protected String getTargetString(final String sql, final String mark) {
                final List<String> targetList = getTargetList(sql, mark);
                return !targetList.isEmpty() ? targetList.get(0) : null;
            }

            protected List<String> getTargetList(final String sql, final String mark) {
                if (sql == null || sql.trim().length() == 0) {
                    String msg = "The sql is invalid: " + sql;
                    throw new IllegalArgumentException(msg);
                }
                final List<String> betweenBeginEndMarkList = getListBetweenBeginEndMark(sql, "--" + mark, mark);
                if (!betweenBeginEndMarkList.isEmpty()) {
                    return betweenBeginEndMarkList;
                } else {
                    // basically for MySQL 
                    return getListBetweenBeginEndMark(sql, "-- " + mark, mark);
                }
            }

            protected List<String> getListBetweenBeginEndMark(String targetStr, String beginMark, String endMark) {
                final List<ScopeInfo> scopeList = Srl.extractScopeList(targetStr, beginMark, endMark);
                final List<String> resultList = DfCollectionUtil.newArrayList();
                for (ScopeInfo scope : scopeList) {
                    resultList.add(scope.getContent());
                }
                return resultList;
            }
        };
    }

    protected void checkParameterComment(File sqlFile, String sql) {
        final DfOutsideSqlProperties outsideSqlProp = getOutsideSqlProperties();
        if (outsideSqlProp.isSuppressParameterCommentCheck()) {
            return;
        }
        final DfOutsideSqlChecker checker = new DfOutsideSqlChecker();
        if (outsideSqlProp.isRequiredSqlTitle()) {
            checker.enableRequiredTitleCheck();
        }
        if (outsideSqlProp.isRequiredSqlDescription()) {
            checker.enableRequiredDescriptionCheck();
        }
        if (getBasicProperties().getLanguageDependency().getLanguageImplStyle().isIfCommentExpressionCheckEnabled()) {
            checker.enableIfCommentExpressionCheck(); // might be different specification between language
        }
        checker.check(sqlFile.getName(), sql);
    }

    protected boolean isAutoCommit() {
        return false;
    }

    protected boolean isBreakCauseThrow() {
        return false;
    }

    protected boolean isErrorContinue() {
        return true;
    }

    protected boolean isRollbackOnly() {
        return true; // this task does not commit 
    }

    protected boolean isIgnoreTxError() {
        if (getBasicProperties().isDatabaseSQLite()) {
            // SQLite may throw an exception when roll-back
            // (actually said, "Database is locked" at OutsideSqlTest only)
            return true;
        } else {
            return false; // requires to be roll-backed correctly
        }
    }

    protected void customizeRunnerInformation(DfRunnerInformation runInfo) {
        runInfo.setEncoding(getOutsideSqlProperties().getSqlFileEncoding());
    }

    // ===================================================================================
    //                                                                  Exception Handling
    //                                                                  ==================
    protected void handleSqlFileFailure(DfSqlFileFireResult fireResult, List<File> sqlFileList) {
        final SQLFailureException topCause = fireResult.getBreakCause();
        if (topCause != null) {
            throw topCause;
        }
        final List<DfSqlFileRunnerResult> resultList = fireResult.getRunnerResultList();
        for (DfSqlFileRunnerResult runnerResult : resultList) {
            final SQLFailureException elementCause = runnerResult.getBreakCause();
            if (elementCause != null) {
                throw elementCause;
            }
            final List<ErrorContinuedSql> continuedSqlList = runnerResult.getErrorContinuedSqlList();
            if (!continuedSqlList.isEmpty()) {
                throwOutsideSqlTestFailureFoundException();
            }
        }
    }

    protected void throwOutsideSqlTestFailureFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the failure SQL by the OutsideSqlTest.");
        br.addItem("Advice");
        br.addElement("You can see the exception info");
        br.addElement("after each SQL logging like this:");
        br.addElement("");
        br.addElement("  ...Firing [SQL-file]");
        br.addElement("  SQL: [SQL-string]");
        br.addElement("  *Failure: [SQLException-class]");
        br.addElement("  /nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn...");
        br.addElement("  [SQLException-message]");
        br.addElement("  [SQLState-info]");
        br.addElement("  nnnnnnnnnn/");
        br.addElement("");
        final String msg = br.buildExceptionMessage();
        throw new DfOutsideSqlTestFailureFoundException(msg);
    }

    // ===================================================================================
    //                                                                          Final Info
    //                                                                          ==========
    @Override
    protected String getFinalInformation() {
        return buildFinalMessage();
    }

    protected String buildFinalMessage() {
        if (_fireResult == null) {
            return null;
        }
        final DfSqlFileFireResult fireResult = _fireResult;
        int countOK = 0;
        int countSkipped = 0;
        int countFailure = 0;
        final StringBuilder sb = new StringBuilder();
        sb.append(" {Checked SQL}");
        final List<DfSqlFileRunnerResult> runnerResultList = fireResult.getRunnerResultList();
        for (DfSqlFileRunnerResult runnerResult : runnerResultList) {
            final File sqlFile = runnerResult.getSqlFile();
            final List<ErrorContinuedSql> continuedSqlList = runnerResult.getErrorContinuedSqlList();
            sb.append(ln());
            if (continuedSqlList.isEmpty()) {
                if (_nonTargetSqlFileSet.contains(sqlFile)) {
                    // accurately 'v' means the SQL file may have skipped SQLs
                    // however SQL file for OutsideSqlTest has only one SQL normally
                    sb.append("  v ");
                    ++countSkipped;
                } else {
                    sb.append("  o ");
                    ++countOK;
                }
            } else {
                // you can say same as 'v'
                // (anyway, look at the log for detail)
                sb.append("  x ");
                ++countFailure;
            }
            sb.append(sqlFile.getName());
            for (ErrorContinuedSql errorContinuedSql : continuedSqlList) {
                final SQLException sqlEx = errorContinuedSql.getSqlEx();
                String sqlMsg = sqlEx.getMessage();
                if (sqlMsg != null) {
                    sqlMsg = sqlMsg.trim();
                    if (sqlMsg.contains(ln())) {
                        sqlMsg = Srl.substringFirstFront(sqlMsg, ln()).trim() + "...";
                    }
                }
                sb.append(ln()).append("   -> ").append(sqlMsg);
            }
        }
        if (!runnerResultList.isEmpty()) {
            sb.append(ln());
            sb.append(ln()).append("   o: OK (").append(countOK).append(")");
            if (countSkipped > 0) {
                sb.append(ln()).append("   v: Skipped exists (").append(countSkipped).append(")");
            }
            if (countFailure > 0) {
                sb.append(ln()).append("   x: Failure exists (").append(countFailure).append(")");
            }
        }
        return sb.toString();
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected DfOutsideSqlProperties getOutsideSqlProperties() {
        return getProperties().getOutsideSqlProperties();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setSpecifiedSqlFile(String specifiedSqlFile) {
        DfSpecifiedSqlFile.getInstance().setSpecifiedSqlFile(specifiedSqlFile);
    }
}
