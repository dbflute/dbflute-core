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
package org.seasar.dbflute.logic.replaceschema.process;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.exception.DfTakeFinallyAssertionFailureException;
import org.seasar.dbflute.exception.DfTakeFinallyNonAssertionSqlFoundException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.jdbc.DfRunnerInformation;
import org.seasar.dbflute.helper.jdbc.context.DfSchemaSource;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileFireMan;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileFireResult;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunner;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerDispatcher;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerExecute;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerExecute.DfRunnerDispatchResult;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerResult;
import org.seasar.dbflute.logic.replaceschema.dataassert.DfDataAssertHandler;
import org.seasar.dbflute.logic.replaceschema.dataassert.DfDataAssertProvider;
import org.seasar.dbflute.logic.replaceschema.finalinfo.DfTakeFinallyFinalInfo;
import org.seasar.dbflute.logic.replaceschema.takefinally.sequence.DfSequenceHandler;
import org.seasar.dbflute.logic.replaceschema.takefinally.sequence.factory.DfSequenceHandlerFactory;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.properties.DfReplaceSchemaProperties;
import org.seasar.dbflute.properties.DfSequenceIdentityProperties;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/29 Friday)
 */
public class DfTakeFinallyProcess extends DfAbstractReplaceSchemaProcess {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfTakeFinallyProcess.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                        Basic Resource
    //                                        --------------
    protected final String _sqlRootDir;
    protected final DfSchemaSource _dataSource;
    protected final DfTakeFinallySqlFileProvider _sqlFileProvider; // NullAllowed: if null, use default

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
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
            DfTakeFinallySqlFileProvider sqlFileProvider) {
        _sqlRootDir = sqlRootDir;
        _dataSource = new DfSchemaSource(dataSource, mainSchema);
        _sqlFileProvider = sqlFileProvider;
    }

    public static interface DfTakeFinallySqlFileProvider {
        List<File> provide();
    }

    public static DfTakeFinallyProcess createAsCore(String sqlRootDir, DataSource dataSource) {
        final UnifiedSchema mainSchema = getDatabaseProperties().getDatabaseSchema();
        return new DfTakeFinallyProcess(sqlRootDir, dataSource, mainSchema, null);
    }

    public static DfTakeFinallyProcess createAsTakeAssert(String sqlRootDir, DataSource dataSource) {
        final UnifiedSchema mainSchema = getDatabaseProperties().getDatabaseSchema();
        final DfTakeFinallyProcess process = new DfTakeFinallyProcess(sqlRootDir, dataSource, mainSchema, null);
        return process.suppressSequenceIncrement().skipIfNonAssetionSql().rollbackTransaction()
                .continueIfAssetionFailure();
    }

    public static DfTakeFinallyProcess createAsAlterCheck(final String sqlRootDir, DataSource dataSource) {
        final UnifiedSchema mainSchema = getDatabaseProperties().getDatabaseSchema();
        final DfTakeFinallySqlFileProvider provider = new DfTakeFinallySqlFileProvider() {
            public List<File> provide() {
                return getReplaceSchemaProperties().getMigrationAlterTakeFinallySqlFileList(sqlRootDir);
            }
        };
        final DfTakeFinallyProcess process = new DfTakeFinallyProcess(sqlRootDir, dataSource, mainSchema, provider);
        return process.suppressSequenceIncrement().restrictIfNonAssetionSql().rollbackTransaction();
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

    protected DfSqlFileFireResult createFailureFireResult(DfTakeFinallyAssertionFailureException e,
            DfSqlFileFireResult originalResult) {
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

    // -----------------------------------------------------
    //                                          Take Finally
    //                                          ------------
    protected DfSqlFileFireResult takeFinally(DfRunnerInformation runInfo) {
        _log.info("");
        _log.info("* * * * * * * **");
        _log.info("*              *");
        _log.info("* Take Finally *");
        _log.info("*              *");
        _log.info("* * * * * * * **");
        final DfSqlFileFireMan fireMan = new DfSqlFileFireMan() {
            @Override
            protected DfSqlFileRunnerResult processSqlFile(DfSqlFileRunner runner, File sqlFile) {
                _executedSqlFileList.add(sqlFile);
                return super.processSqlFile(runner, sqlFile);
            }
        };
        fireMan.setExecutorName("Take Finally");
        return fireMan.fire(getSqlFileRunner4TakeFinally(runInfo), getTakeFinallySqlFileList());
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

    // -----------------------------------------------------
    //                                    Increment Sequence
    //                                    ------------------
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
        final DfSequenceIdentityProperties sequenceProp = getProperties().getSequenceIdentityProperties();
        final Map<String, String> tableSequenceMap = sequenceProp.getTableSequenceMap();
        final DfDatabaseTypeFacadeProp dbTypeProp = getDatabaseTypeFacadeProp();
        final DfDatabaseProperties databaseProp = getDatabaseProperties();
        final DfSequenceHandlerFactory factory = new DfSequenceHandlerFactory(_dataSource, dbTypeProp, databaseProp);
        final DfSequenceHandler sequenceHandler = factory.createSequenceHandler();
        if (sequenceHandler == null) {
            String databaseType = dbTypeProp.getTargetDatabase();
            String msg = "Unsupported isIncrementSequenceToDataMax at " + databaseType;
            throw new UnsupportedOperationException(msg);
        }
        sequenceHandler.incrementSequenceToDataMax(tableSequenceMap);
    }

    // ===================================================================================
    //                                                                          Final Info
    //                                                                          ==========
    protected DfTakeFinallyFinalInfo createFinalInfo(DfSqlFileFireResult fireResult,
            DfTakeFinallyAssertionFailureException assertionEx) {
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
