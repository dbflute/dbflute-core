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
package org.dbflute.bhv.core.command;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dbflute.Entity;
import org.dbflute.bhv.core.SqlExecution;
import org.dbflute.bhv.core.SqlExecutionCreator;
import org.dbflute.bhv.core.execution.SelectNextValExecution;
import org.dbflute.bhv.core.supplement.SequenceCache;
import org.dbflute.bhv.core.supplement.SequenceCacheHandler;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.exception.SequenceCacheIncrementSizeInvalidException;
import org.dbflute.exception.SequenceSelectIllegalStateException;
import org.dbflute.outsidesql.OutsideSqlOption;
import org.dbflute.s2dao.jdbc.TnResultSetHandler;

/**
 * The command to select next values of sequence for primary key.
 * @author jflute
 * @param <RESULT> The type of result.
 */
public class SelectNextValCommand<RESULT> extends AbstractAllBehaviorCommand<RESULT> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The type of result. (NotNull) */
    protected Class<RESULT> _resultType;

    /** The provider of DB meta. (NotNull) */
    protected DBMeta _dbmeta;

    /** The handler of sequence cache. (NotNull) */
    protected SequenceCacheHandler _sequenceCacheHandler;

    // ===================================================================================
    //                                                                   Basic Information
    //                                                                   =================
    public String getCommandName() {
        return "selectNextVal";
    }

    public Class<?> getCommandReturnType() {
        return _resultType;
    }

    // ===================================================================================
    //                                                                  Detail Information
    //                                                                  ==================
    public boolean isConditionBean() {
        return false;
    }

    public boolean isOutsideSql() {
        return false;
    }

    public boolean isProcedure() {
        return false;
    }

    public boolean isSelect() {
        return true;
    }

    public boolean isSelectCount() {
        return false;
    }

    public boolean isSelectCursor() {
        return false;
    }

    public boolean isInsert() {
        return false;
    }

    public boolean isUpdate() {
        return false;
    }

    public boolean isDelete() {
        return false;
    }

    public boolean isEntityUpdateFamily() {
        return false;
    }

    public boolean isBatchUpdateFamily() {
        return false;
    }

    public boolean isQueryUpdateFamily() {
        return false;
    }

    // ===================================================================================
    //                                                                    Process Callback
    //                                                                    ================
    public void beforeGettingSqlExecution() {
    }

    public void afterExecuting() {
    }

    // ===================================================================================
    //                                                               SqlExecution Handling
    //                                                               =====================
    public String buildSqlExecutionKey() {
        assertStatus("buildSqlExecutionKey");
        return _tableDbName + ":" + getCommandName() + "()";
    }

    public SqlExecutionCreator createSqlExecutionCreator() {
        assertStatus("createSqlExecutionCreator");
        return () -> {
            final TnResultSetHandler handler = createDynamicScalarResultSetHandler(_resultType);
            return createSelectNextValExecution(handler);
        };
    }

    protected SqlExecution createSelectNextValExecution(TnResultSetHandler handler) {
        assertStatus("createSelectNextValExecution");
        final DBMeta dbmeta = _dbmeta;
        assertTableHasSequence();
        String sql = getSequenceNextValSql(); // filtered later
        assertSequenceReturnsNotNull(sql, dbmeta);

        // handling for sequence cache
        final SequenceCache sequenceCache = findSequenceCache(dbmeta);
        sql = prepareSequenceCache(sql, sequenceCache);

        return createSequenceExecution(handler, sql, sequenceCache);
    }

    protected String getSequenceNextValSql() {
        return _dbmeta.getSequenceNextValSql();
    }

    protected String prepareSequenceCache(String sql, SequenceCache sequenceCache) {
        final DBMeta dbmeta = _dbmeta;
        final Integer incrementSize = dbmeta.getSequenceIncrementSize();
        final Integer cacheSize = dbmeta.getSequenceCacheSize();
        return doPrepareSequenceCache(sql, sequenceCache, incrementSize, cacheSize);
    }

    protected String doPrepareSequenceCache(String sql, SequenceCache sequenceCache, Integer incrementSize, Integer cacheSize) {
        if (sequenceCache != null) {
            final DBMeta dbmeta = _dbmeta;
            if (incrementSize != null) {
                assertIncrementSizeNotMinusAndNotZero(incrementSize, dbmeta);
                // cacheSize is not null here because the sequence cache has been found
                sql = _sequenceCacheHandler.filterNextValSql(cacheSize, incrementSize, sql);
            }
        }
        return sql;
    }

    protected void assertTableHasSequence() {
        if (!_dbmeta.hasSequence()) {
            String msg = "If it uses sequence, the table should be related to a sequence:";
            msg = msg + " table=" + _dbmeta.getTableDbName() + " sequence=" + _dbmeta.getSequenceName();
            throw new SequenceSelectIllegalStateException(msg);
        }
    }

    protected void assertSequenceReturnsNotNull(String nextValSql, DBMeta dbmeta) {
        if (nextValSql == null) {
            String msg = "SQL for sequence was null, confirm your DBMS settings (Is sequence supported in the DBMS?):";
            msg = msg + " table=" + dbmeta.getTableDbName() + " sequence=" + dbmeta.getSequenceName();
            throw new SequenceSelectIllegalStateException(msg);
        }
    }

    protected SequenceCache findSequenceCache(DBMeta dbmeta) {
        final String tableName = dbmeta.getTableDbName();
        final String sequenceName = dbmeta.getSequenceName();
        final Integer cacheSize = dbmeta.getSequenceCacheSize();
        final Integer incrementSize = dbmeta.getSequenceIncrementSize();
        return doFindSequenceCache(tableName, sequenceName, cacheSize, incrementSize);
    }

    protected SequenceCache doFindSequenceCache(String tableName, String sequenceName, Integer cacheSize, Integer incrementSize) {
        return _sequenceCacheHandler.findSequenceCache(tableName, sequenceName, _dataSource, _resultType, cacheSize, incrementSize);
    }

    protected void assertIncrementSizeNotMinusAndNotZero(Integer incrementSize, DBMeta dbmeta) { // precondition: not null
        if (incrementSize <= 0) {
            String msg = "The increment size should not be minus or zero if you use sequence cache:";
            msg = msg + " table=" + dbmeta.getTableDbName() + " sequence=" + dbmeta.getSequenceName();
            msg = msg + " cacheSize=" + dbmeta.getSequenceCacheSize();
            msg = msg + " incrementSize=" + dbmeta.getSequenceIncrementSize();
            throw new SequenceCacheIncrementSizeInvalidException(msg);
        }
    }

    protected SelectNextValExecution createSequenceExecution(TnResultSetHandler handler, String sql, final SequenceCache sequenceCache) {
        final Map<String, Class<?>> argNameTypeMap = newArgNameTypeMap();
        return newSelectNextValExecution(argNameTypeMap, sql, handler, sequenceCache);
    }

    protected SelectNextValExecution newSelectNextValExecution(Map<String, Class<?>> argNameTypeMap, String sql, TnResultSetHandler handler,
            final SequenceCache sequenceCache) {
        return new SelectNextValExecution(_dataSource, _statementFactory, argNameTypeMap, sql, handler, sequenceCache);
    }

    public Object[] getSqlExecutionArgument() {
        assertStatus("getSqlExecutionArgument");
        return new Object[] {};
    }

    // ===================================================================================
    //                                                                Argument Information
    //                                                                ====================
    public ConditionBean getConditionBean() {
        return null;
    }

    public Entity getEntity() {
        return null;
    }

    public List<Entity> getEntityList() {
        return Collections.emptyList();
    }

    public String getOutsideSqlPath() {
        return null;
    }

    public String getParameterBean() {
        return null;
    }

    public OutsideSqlOption getOutsideSqlOption() {
        return null;
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertStatus(String methodName) {
        assertBasicProperty(methodName);
        assertComponentProperty(methodName);
        if (_dbmeta == null) {
            throw new IllegalStateException(buildAssertMessage("_dbmeta", methodName));
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setResultType(Class<RESULT> resultType) {
        _resultType = resultType;
    }

    public void setDBMeta(DBMeta dbmeta) {
        _dbmeta = dbmeta;
    }

    public void setSequenceCacheHandler(SequenceCacheHandler sequenceCacheHandler) {
        _sequenceCacheHandler = sequenceCacheHandler;
    }
}
