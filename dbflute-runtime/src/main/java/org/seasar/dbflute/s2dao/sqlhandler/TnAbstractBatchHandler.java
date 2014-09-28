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
package org.seasar.dbflute.s2dao.sqlhandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.XLog;
import org.seasar.dbflute.exception.BatchEntityAlreadyUpdatedException;
import org.seasar.dbflute.exception.EntityAlreadyDeletedException;
import org.seasar.dbflute.exception.EntityDuplicatedException;
import org.seasar.dbflute.exception.handler.SQLExceptionResource;
import org.seasar.dbflute.jdbc.SqlLogInfo;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public abstract class TnAbstractBatchHandler extends TnAbstractEntityHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(TnAbstractBatchHandler.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // non-thread-safe because handler is created per execution
    protected StringBuilder _batchLoggingSb;
    protected int _loggingRecordCount;
    protected int _loggingScopeSize;
    protected boolean _existsSkippedLogging;
    protected boolean _alreadySavedToResultInfo;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnAbstractBatchHandler(DataSource dataSource, StatementFactory statementFactory, String sql,
            TnBeanMetaData beanMetaData, TnPropertyType[] boundPropTypes) {
        super(dataSource, statementFactory, sql, beanMetaData, boundPropTypes);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    public int execute(Object[] args) {
        String msg = "This method should not be called when BatchUpdate.";
        throw new IllegalStateException(msg);
    }

    public int[] executeBatch(List<?> beanList) {
        if (beanList == null) {
            String msg = "The argument 'beanList' should not be null";
            throw new IllegalArgumentException(msg);
        }
        if (beanList.isEmpty()) {
            if (_log.isDebugEnabled()) {
                _log.debug("Skip executeBatch() bacause of the empty list.");
            }
            return new int[0];
        }
        final Connection conn = getConnection();
        try {
            processBefore(conn, beanList);
            RuntimeException sqlEx = null;
            final PreparedStatement ps = prepareStatement(conn);
            int[] result = null;
            try {
                for (Object bean : beanList) {
                    processBatchBefore(bean);
                    prepareBatchElement(conn, ps, bean);
                }
                handleBatchLogging(); // last scope handling
                result = executeBatch(ps, beanList);
                handleBatchUpdateResultWithOptimisticLock(ps, beanList, result);
            } catch (RuntimeException e) {
                // not SQLFailureException because
                // a wrapper of JDBC may throw an other exception
                sqlEx = e;
                throw e;
            } finally {
                close(ps);
                processFinally(conn, beanList, sqlEx);
            }
            // a value of exclusive control column should be synchronized
            // after handling optimistic lock
            int index = 0;
            for (Object bean : beanList) {
                processBatchSuccess(bean, index);
                ++index;
            }
            processSuccess(conn, beanList, result.length);
            return result;
        } finally {
            close(conn);
        }
    }

    protected void prepareBatchElement(Connection conn, PreparedStatement ps, Object bean) {
        setupBindVariables(bean);
        final Object[] bindVariables = _bindVariables;
        logSql(bindVariables, getArgTypes(bindVariables));
        bindArgs(conn, ps, bindVariables, _bindVariableValueTypes);
        addBatch(ps);
    }

    // ===================================================================================
    //                                                                         SQL Logging
    //                                                                         ===========
    @Override
    protected void logSql(Object[] args, Class<?>[] argTypes) {
        if (isBatchLoggingOver()) {
            _existsSkippedLogging = true;
            return;
        }
        super.logSql(args, argTypes);
    };

    protected boolean isBatchLoggingOver() {
        final Integer batchLoggingLimit = getBatchLoggingLimit();
        if (batchLoggingLimit == null || batchLoggingLimit < 0) {
            return false;
        }
        return _loggingRecordCount >= batchLoggingLimit;
    }

    protected abstract Integer getBatchLoggingLimit();

    @Override
    protected boolean processBeforeLogging(Object[] args, Class<?>[] argTypes, boolean logEnabled,
            boolean hasSqlFireHook, boolean hasSqlLog, boolean hasSqlResult, Object sqlLogRegistry) {
        if (_batchLoggingSb == null) {
            _batchLoggingSb = new StringBuilder(1000);
        }
        final String displaySql = buildDisplaySql(_sql, args);
        saveBatchLoggingSql(displaySql);
        doLogSql(args, argTypes, false, false, hasSqlLog, false, sqlLogRegistry); // process non-batch handling
        if (needsBreakLoggingScope()) {
            handleBatchLogging(); // and also cleared
        }
        return true;
    };

    protected void saveBatchLoggingSql(String displaySql) {
        ++_loggingRecordCount;
        ++_loggingScopeSize;
        _batchLoggingSb.append(ln()).append(displaySql).append(";");
    }

    protected boolean needsBreakLoggingScope() {
        return _loggingScopeSize >= 100; // per 100 statements
    }

    protected String handleBatchLogging() {
        if (_batchLoggingSb == null) { // may be limited by option
            handleBatchResultSqlSaving(null); // but SqlResultHandler handling is needed
            return null;
        }
        final String batchSql = _batchLoggingSb.toString();
        if (isLogEnabled()) {
            log(batchSql); // batch logging (always starts with a line separator)
        }
        clearBatchLogging();
        handleBatchResultSqlSaving(batchSql);
        return batchSql;
    }

    protected void handleBatchResultSqlSaving(String batchSql) {
        final boolean hasSqlFireHook = hasSqlFireHook();
        final boolean hasSqlResultHandler = hasSqlResultHandler();
        if (!_alreadySavedToResultInfo && (hasSqlFireHook || hasSqlResultHandler)) { // only first scope is saved
            final Object[] bindArgs = _bindVariables;
            final Class<?>[] bindArgTypes = getArgTypes(bindArgs);
            final String savedDisplaySql = batchSql != null ? batchSql.trim() : null; // remove first line separator
            final SqlLogInfo sqlLogInfo = prepareSqlLogInfo(bindArgs, bindArgTypes, savedDisplaySql);
            if (hasSqlFireHook) {
                super.saveHookSqlLogInfo(sqlLogInfo); // super's because one of this class is overridden
            }
            if (hasSqlResultHandler) {
                super.saveResultSqlLogInfo(sqlLogInfo); // super's because one of this class is overridden
            }
            _alreadySavedToResultInfo = true;
        }
    }

    @Override
    protected void saveHookSqlLogInfo(SqlLogInfo sqlLogInfo) {
        // do nothing because it saves later
    }

    @Override
    protected void saveResultSqlLogInfo(SqlLogInfo sqlLogInfo) {
        // do nothing because it saves later
    }

    protected void clearBatchLogging() {
        _batchLoggingSb = null;
        _loggingScopeSize = 0;
    }

    // ===================================================================================
    //                                                                   Extension Process
    //                                                                   =================
    @Override
    protected void processBefore(Connection conn, Object beanList) {
        super.processBefore(conn, beanList);
    }

    @Override
    protected void processFinally(Connection conn, Object beanList, RuntimeException sqlEx) {
        super.processFinally(conn, beanList, sqlEx);
        noticeBatchLoggingOver();

        // clear just in case
        _existsSkippedLogging = false;
        _alreadySavedToResultInfo = false;
    }

    protected void noticeBatchLoggingOver() {
        if (_existsSkippedLogging) {
            if (XLog.isLogEnabled()) {
                final Integer batchLoggingLimit = getBatchLoggingLimit();
                XLog.log("...Skipping several loggings by the limit option: " + batchLoggingLimit);
            }
        }
    }

    @Override
    protected void processSuccess(Connection conn, Object beanList, int ret) {
        super.processSuccess(conn, beanList, ret);
    }

    protected void processBatchBefore(Object bean) {
    }

    // *after case about identity is unsupported at Batch Update   
    protected void processBatchSuccess(Object bean, int index) {
        updateTimestampIfNeed(bean, index);
        updateVersionNoIfNeed(bean, index);
    }

    // ===================================================================================
    //                                                                     Optimistic Lock
    //                                                                     ===============
    protected void handleBatchUpdateResultWithOptimisticLock(PreparedStatement ps, List<?> list, int[] result) {
        if (isCurrentDBDef(DBDef.Oracle)) {
            final int updateCount;
            try {
                updateCount = ps.getUpdateCount();
            } catch (SQLException e) {
                final SQLExceptionResource resource = createSQLExceptionResource();
                resource.setNotice("Failed to get update count.");
                handleSQLException(e, resource);
                return; // unreachable
            }
            handleBatchUpdateResultWithOptimisticLockByUpdateCount(list, updateCount);
        } else {
            handleBatchUpdateResultWithOptimisticLockByResult(list, result);
        }
    }

    protected boolean isCurrentDBDef(DBDef currentDBDef) {
        return ResourceContext.isCurrentDBDef(currentDBDef);
    }

    protected void handleBatchUpdateResultWithOptimisticLockByUpdateCount(List<?> list, int updateCount) {
        if (list.isEmpty()) {
            return; // for safety
        }
        if (updateCount < 0) {
            return; // for safety
        }
        final int entityCount = list.size();
        if (updateCount < entityCount) {
            if (_optimisticLockHandling) {
                throw new BatchEntityAlreadyUpdatedException(list.get(0), 0, updateCount);
            } else {
                String msg = "The entity was NOT found! it has already been deleted.";
                msg = msg + " updateCount=" + updateCount;
                msg = msg + " entityCount=" + entityCount;
                msg = msg + " allEntities=" + list;
                throw new EntityAlreadyDeletedException(msg);
            }
        }
    }

    protected void handleBatchUpdateResultWithOptimisticLockByResult(List<?> list, int[] result) {
        if (list.isEmpty()) {
            return; // for safety
        }
        final int[] updatedCountArray = result;
        final int entityCount = list.size();
        int index = 0;
        boolean alreadyUpdated = false;
        for (int oneUpdateCount : updatedCountArray) {
            if (entityCount <= index) {
                break; // for safety
            }
            if (oneUpdateCount == 0) {
                alreadyUpdated = true;
                break;
            } else if (oneUpdateCount > 1) {
                String msg = "The entity updated two or more records in batch update:";
                msg = msg + " entity=" + list.get(index);
                msg = msg + " updatedCount=" + oneUpdateCount;
                msg = msg + " allEntities=" + list;
                throw new EntityDuplicatedException(msg);
            }
            ++index;
        }
        if (alreadyUpdated) {
            int updateCount = 0;
            for (int oneUpdateCount : updatedCountArray) {
                updateCount = updateCount + oneUpdateCount;
            }
            if (_optimisticLockHandling) {
                throw new BatchEntityAlreadyUpdatedException(list.get(index), 0, updateCount);
            } else {
                String msg = "The entity was NOT found! it has already been deleted:";
                msg = msg + " entity=" + list.get(index);
                msg = msg + " updateCount=" + updateCount;
                msg = msg + " allEntities=" + list;
                throw new EntityAlreadyDeletedException(msg);
            }
        }
    }

    // ===================================================================================
    //                                                                       Bind Variable
    //                                                                       =============
    @Override
    protected Set<String> extractUniqueDrivenPropSet(Object bean) {
        return null; // cannot use unique-driven for batch
    }

    // ===================================================================================
    //                                                                         SQL Logging
    //                                                                         ===========
    @Override
    protected String buildExceptionMessageSql() {
        if (_exceptionMessageSqlArgs == null && _bindVariables != null) {
            _exceptionMessageSqlArgs = _bindVariables; // as current bean
        }
        return super.buildExceptionMessageSql();
    }
}
