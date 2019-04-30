/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.logic.replaceschema.process;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.exception.DfTakeFinallyAssertionFailureException;
import org.dbflute.exception.DfTakeFinallyNonAssertionSqlFoundException;
import org.dbflute.helper.jdbc.DfRunnerInformation;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.helper.jdbc.sqlfile.DfSqlFileFireMan;
import org.dbflute.helper.jdbc.sqlfile.DfSqlFileFireResult;
import org.dbflute.helper.jdbc.sqlfile.DfSqlFileRunner;
import org.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerDispatcher;
import org.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerExecute;
import org.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerExecute.DfRunnerDispatchResult;
import org.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerResult;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.helper.process.SystemScript;
import org.dbflute.logic.replaceschema.dataassert.DfDataAssertHandler;
import org.dbflute.logic.replaceschema.dataassert.DfDataAssertProvider;
import org.dbflute.logic.replaceschema.finalinfo.DfTakeFinallyFinalInfo;
import org.dbflute.logic.replaceschema.takefinally.conventional.DfConventionalTakeAsserter;
import org.dbflute.logic.replaceschema.takefinally.sequence.DfRepsSequenceIncrementer;
import org.dbflute.properties.DfReplaceSchemaProperties;
import org.dbflute.properties.assistant.reps.DfConventionalTakeAssertMap;
import org.dbflute.task.bs.assistant.DfDocumentSelector;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/29 Friday)
 */
public class DfTakeFinallyProcess extends DfAbstractRepsProcess {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfTakeFinallyProcess.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                        Basic Resource
    //                                        --------------
    protected final String _sqlRootDir;
    protected final DfSchemaSource _dataSource;
    protected final DfTakeFinallySqlFileProvider _sqlFileProvider; // null allowed: if null, use default
    protected final DfDocumentSelector _documentSelector; // conventionalTakeAssert needs, null allowed: depends on purpose

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    protected boolean _suppressConventionalTakeAssert;
    protected boolean _suppressSequenceIncrement;
    protected boolean _skipIfNonAssetionSql;
    protected boolean _restrictIfNonAssetionSql;
    protected boolean _rollbackTransaction;
    protected boolean _continueIfAssetionFailure;

    // -----------------------------------------------------
    //                                                Result
    //                                                ------
    protected final List<File> _executedSqlFileList = DfCollectionUtil.newArrayList();
    protected final List<DfTakeFinallyAssertionFailureException> _continuedExList = DfCollectionUtil.newArrayList();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfTakeFinallyProcess(String sqlRootDir, DataSource dataSource, UnifiedSchema mainSchema,
            DfTakeFinallySqlFileProvider sqlFileProvider, DfDocumentSelector documentSelector) {
        _sqlRootDir = sqlRootDir;
        _dataSource = new DfSchemaSource(dataSource, mainSchema);
        _sqlFileProvider = sqlFileProvider;
        _documentSelector = documentSelector;
    }

    public static interface DfTakeFinallySqlFileProvider {
        List<File> provide();
    }

    public static DfTakeFinallyProcess createAsCore(String sqlRootDir, DataSource dataSource, DfDocumentSelector documentSelector) {
        final UnifiedSchema mainSchema = getDatabaseProperties().getDatabaseSchema();
        return new DfTakeFinallyProcess(sqlRootDir, dataSource, mainSchema, null, documentSelector);
    }

    public static DfTakeFinallyProcess createAsTakeAssert(String sqlRootDir, DataSource dataSource) {
        final UnifiedSchema mainSchema = getDatabaseProperties().getDatabaseSchema();
        final DfTakeFinallyProcess process = new DfTakeFinallyProcess(sqlRootDir, dataSource, mainSchema, null, null);
        return process.suppressConventionalTakeAssert() // take-assert is not only for test data (if needs, option?)
                .suppressSequenceIncrement().skipIfNonAssetionSql().rollbackTransaction().continueIfAssetionFailure();
    }

    public static DfTakeFinallyProcess createAsPrevious(final String sqlRootDir, DataSource dataSource) {
        final UnifiedSchema mainSchema = getDatabaseProperties().getDatabaseSchema();
        final DfTakeFinallyProcess process = new DfTakeFinallyProcess(sqlRootDir, dataSource, mainSchema, null, null);
        // previous may not match with current sequence definition
        // and other settings are same as core
        return process.suppressConventionalTakeAssert().suppressSequenceIncrement();
    }

    public static DfTakeFinallyProcess createAsAlterSchema(final String sqlRootDir, DataSource dataSource) {
        final UnifiedSchema mainSchema = getDatabaseProperties().getDatabaseSchema();
        final DfTakeFinallySqlFileProvider provider = new DfTakeFinallySqlFileProvider() {
            public List<File> provide() {
                return getReplaceSchemaProperties().getMigrationAlterTakeFinallySqlFileList(sqlRootDir);
            }
        };
        final DfTakeFinallyProcess process = new DfTakeFinallyProcess(sqlRootDir, dataSource, mainSchema, provider, null);
        // this take-finally is only for assertion (so roll-back transaction)
        // but increment sequences for development after AlterCheck
        return process.suppressConventionalTakeAssert().restrictIfNonAssetionSql().rollbackTransaction();
    }

    protected DfTakeFinallyProcess suppressConventionalTakeAssert() {
        _suppressConventionalTakeAssert = true;
        return this;
    }

    protected DfTakeFinallyProcess suppressSequenceIncrement() {
        _suppressSequenceIncrement = true;
        return this;
    }

    protected DfTakeFinallyProcess skipIfNonAssetionSql() {
        _skipIfNonAssetionSql = true;
        return this;
    }

    protected DfTakeFinallyProcess restrictIfNonAssetionSql() {
        _restrictIfNonAssetionSql = true;
        return this;
    }

    protected DfTakeFinallyProcess rollbackTransaction() {
        _rollbackTransaction = true;
        return this;
    }

    protected DfTakeFinallyProcess continueIfAssetionFailure() {
        _continueIfAssetionFailure = true;
        return this;
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public DfTakeFinallyFinalInfo execute() {
        final DfRunnerInformation runInfo = createRunnerInformation();
        DfSqlFileFireResult fireResult = null;
        DfTakeFinallyAssertionFailureException assertionEx = null;
        try {
            fireResult = takeFinally(runInfo);
            if (_continueIfAssetionFailure && !_continuedExList.isEmpty()) {
                // override result with saved exceptions
                // this message uses the first exception
                fireResult = createFailureFireResult(_continuedExList.get(0), fireResult);
            }
        } catch (DfTakeFinallyAssertionFailureException e) {
            // if take-assert, the exception does not thrown
            fireResult = createFailureFireResult(e, null);
            assertionEx = e;
        }
        final DfTakeFinallyFinalInfo finalInfo = createFinalInfo(fireResult, assertionEx);
        if (!finalInfo.isFailure()) { // because it might fail to create sequence
            incrementSequenceToDataMax();
        }
        return finalInfo;
    }

    protected DfSqlFileFireResult createFailureFireResult(DfTakeFinallyAssertionFailureException e, DfSqlFileFireResult originalResult) {
        final DfSqlFileFireResult fireResult = new DfSqlFileFireResult();
        fireResult.setExistsError(true);
        fireResult.setResultMessage("{Take Finally}: *asserted");
        final StringBuilder sb = new StringBuilder();
        final String detailMessage = originalResult != null ? originalResult.getDetailMessage() : null;
        if (detailMessage != null) {
            sb.append(detailMessage).append(ln());
        } else { // means abort
            final int fileListSize = _executedSqlFileList.size();
            int index = 0;
            for (File executedSqlFile : _executedSqlFileList) {
                final String pureFileName = Srl.substringLastRear(executedSqlFile.getPath(), "/");
                if (index == fileListSize - 1) { // last loop
                    sb.append("x ");
                } else {
                    sb.append("o ");
                }
                sb.append(pureFileName).append(ln());
                ++index;
            }
        }
        sb.append(" >> ").append(DfTypeUtil.toClassTitle(e));
        sb.append(ln()).append(" (Look at the exception message: console or dbflute.log)");
        fireResult.setDetailMessage(sb.toString());
        return fireResult;
    }

    @Override
    protected boolean isRollbackTransaction() {
        // for example, take-assert task should not update data
        // the task cannot execute update statement basically
        // but it uses a safety connection the task uses just in case
        return _rollbackTransaction;
    }

    // ===================================================================================
    //                                                                        Take Finally
    //                                                                        ============
    protected DfSqlFileFireResult takeFinally(DfRunnerInformation runInfo) {
        _log.info("");
        _log.info("* * * * * * * **");
        _log.info("*              *");
        _log.info("* Take Finally *");
        _log.info("*              *");
        _log.info("* * * * * * * **");
        final DfSqlFileFireMan fireMan = createSqlFileFireMan();
        final DfSqlFileFireResult result = fireMan.fire(getSqlFileRunner4TakeFinally(runInfo), getTakeFinallySqlFileList());
        conventionalTakeAssertIfNeeds();
        return result;
    }

    protected DfSqlFileFireMan createSqlFileFireMan() { // similar to alter check
        final String[] scriptExtAry = SystemScript.getSupportedExtList().toArray(new String[] {});
        final SystemScript script = new SystemScript();
        final DfSqlFileFireMan fireMan = new DfSqlFileFireMan() {
            @Override
            protected DfSqlFileRunnerResult processSqlFile(DfSqlFileRunner runner, File sqlFile) {
                _executedSqlFileList.add(sqlFile);
                if (isScriptFile(sqlFile, scriptExtAry)) {
                    return processScriptFile(runner, script, sqlFile);
                } else { // mainly here
                    return super.processSqlFile(runner, sqlFile);
                }
            }
        };
        fireMan.setExecutorName("Take Finally");
        return fireMan;
    }

    protected DfSqlFileRunner getSqlFileRunner4TakeFinally(final DfRunnerInformation runInfo) {
        final DfReplaceSchemaProperties prop = getReplaceSchemaProperties();
        final DfSqlFileRunnerExecute runnerExecute = new DfSqlFileRunnerExecute(runInfo, _dataSource) {
            @Override
            protected String filterSql(String sql) {
                sql = super.filterSql(sql);
                sql = prop.resolveFilterVariablesIfNeeds(sql);
                return sql;
            }

            @Override
            protected boolean isHandlingCommentOnLineSeparator() {
                return true;
            }

            @Override
            protected boolean isDbCommentLine(String line) {
                final boolean commentLine = super.isDbCommentLine(line);
                if (commentLine) {
                    return commentLine;
                }
                // for irregular pattern
                return isDbCommentLineForIrregularPattern(line);
            }

            @Override
            protected String getTerminator4Tool() {
                return resolveTerminator4Tool();
            }

            @Override
            protected boolean isTargetFile(String sql) {
                return getReplaceSchemaProperties().isTargetRepsFile(sql);
            }
        };
        final String loadType = getReplaceSchemaProperties().getRepsEnvType();
        final DfDataAssertProvider dataAssertProvider = new DfDataAssertProvider(loadType);
        runnerExecute.setDispatcher(new DfSqlFileRunnerDispatcher() {
            public DfRunnerDispatchResult dispatch(File sqlFile, Statement st, String sql) throws SQLException {
                final DfDataAssertHandler dataAssertHandler = dataAssertProvider.provideDataAssertHandler(sql);
                if (dataAssertHandler == null) {
                    if (_skipIfNonAssetionSql) {
                        _log.info("*Skipped the statement because of not assertion SQL");
                        return DfRunnerDispatchResult.SKIPPED;
                    } else if (_restrictIfNonAssetionSql) {
                        throwTakeFinallyNonAssertionSqlFoundException(sqlFile, sql);
                    } else {
                        return DfRunnerDispatchResult.NONE;
                    }
                }
                try {
                    dataAssertHandler.handle(sqlFile, st, sql);
                } catch (DfTakeFinallyAssertionFailureException e) {
                    handleAssertionFailureException(e);
                }
                return DfRunnerDispatchResult.DISPATCHED;
            }
        });
        return runnerExecute;
    }

    protected void throwTakeFinallyNonAssertionSqlFoundException(File sqlFile, String sql) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the non-assertion SQL.");
        br.addItem("Advice");
        br.addElement("Confirm your SQL files for TakeFinally.");
        br.addElement("For example, non-assertion SQL on AlterCheck's TakeFinally is restricted.");
        br.addItem("SQL File");
        br.addElement(sqlFile.getPath());
        br.addItem("non-assertion SQL");
        br.addElement(sql);
        final String msg = br.buildExceptionMessage();
        throw new DfTakeFinallyNonAssertionSqlFoundException(msg);
    }

    protected void handleAssertionFailureException(DfTakeFinallyAssertionFailureException e) {
        if (_continueIfAssetionFailure) { // save for final message
            _continuedExList.add(e);
        } else {
            throw e;
        }
    }

    protected List<File> getTakeFinallySqlFileList() {
        if (_sqlFileProvider != null) {
            return _sqlFileProvider.provide();
        }
        final List<File> fileList = new ArrayList<File>();
        fileList.addAll(getReplaceSchemaProperties().getTakeFinallySqlFileList(_sqlRootDir));
        fileList.addAll(getReplaceSchemaProperties().getAppcalitionTakeFinallySqlFileList());
        return fileList;
    }

    // ===================================================================================
    //                                                             Conventional TakeAssert
    //                                                             =======================
    protected void conventionalTakeAssertIfNeeds() {
        if (_suppressConventionalTakeAssert) { // e.g. as previous, alter-check
            return;
        }
        final DfConventionalTakeAssertMap map = getReplaceSchemaProperties().getConventionalTakeAssertMap();
        if (!map.hasConventionalTakeAssert()) {
            return;
        }
        _log.info("");
        _log.info("...Executing conventional take-assert (in take-finally)");
        map.showProperties();
        final DfConventionalTakeAsserter asserter = createConventionalTakeAsserter(map);
        asserter.assertConventionally();
    }

    protected DfConventionalTakeAsserter createConventionalTakeAsserter(DfConventionalTakeAssertMap map) {
        return new DfConventionalTakeAsserter(_dataSource, () -> map.buildDispProperties(), () -> {
            return _documentSelector.lazyLoadIfNeedsCoreSchemaDiffList(); // for table/column first date
        });
    }

    // ===================================================================================
    //                                                                  Increment Sequence
    //                                                                  ==================
    protected void incrementSequenceToDataMax() {
        if (!getReplaceSchemaProperties().isIncrementSequenceToDataMax()) {
            return;
        }
        if (_suppressSequenceIncrement) {
            return;
        }
        _log.info("");
        _log.info("* * * * * * * * * * **");
        _log.info("*                    *");
        _log.info("* Increment Sequence *");
        _log.info("*                    *");
        _log.info("* * * * * * * * * * **");
        new DfRepsSequenceIncrementer(_dataSource).incrementSequenceToDataMax();
    }

    // ===================================================================================
    //                                                                          Final Info
    //                                                                          ==========
    protected DfTakeFinallyFinalInfo createFinalInfo(DfSqlFileFireResult fireResult, DfTakeFinallyAssertionFailureException assertionEx) {
        final DfTakeFinallyFinalInfo finalInfo = new DfTakeFinallyFinalInfo();
        finalInfo.addTakeFinallySqlFileAll(_executedSqlFileList);
        if (fireResult != null) {
            finalInfo.setResultMessage(fireResult.getResultMessage());
            final List<String> detailMessageList = extractDetailMessageList(fireResult);
            for (String detailMessage : detailMessageList) {
                finalInfo.addDetailMessage(detailMessage);
            }
            finalInfo.setBreakCause(fireResult.getBreakCause());
            finalInfo.setFailure(fireResult.isExistsError());
        }
        finalInfo.setAssertionEx(assertionEx);
        return finalInfo;
    }

    // ===================================================================================
    //                                                                     Batch Assertion
    //                                                                     ===============
    public List<DfTakeFinallyAssertionFailureException> getTakeAssertExList() {
        return _continuedExList;
    }
}
