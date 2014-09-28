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

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.helper.jdbc.DfRunnerInformation;

/**
 * @author jflute
 */
public class DfSqlFileRunnerExecute extends DfSqlFileRunnerBase {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static Log _log = LogFactory.getLog(DfSqlFileRunnerExecute.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DfSqlFileRunnerDispatcher _dispatcher;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSqlFileRunnerExecute(DfRunnerInformation runInfo, DataSource dataSource) {
        super(runInfo, dataSource);
    }

    // ===================================================================================
    //                                                                         Execute SQL
    //                                                                         ===========
    /**
     * {@inheritDoc}
     */
    protected void execSQL(String sql) {
        boolean lazyConnectFailed = false;
        try {
            final DfRunnerDispatchResult dispatchResult = dispatch(sql);
            if (DfRunnerDispatchResult.NONE.equals(dispatchResult)) {
                try {
                    lazyConnectIfNeeds();
                } catch (SQLException e) {
                    lazyConnectFailed = true;
                    throw e;
                }
                processNonDispatch(sql);
                _goodSqlCount++; // success (mainly here)
            } else if (DfRunnerDispatchResult.SKIPPED.equals(dispatchResult)) {
                _skippedSqlCount++;
            } else { // means dispatched successfully
                _goodSqlCount++;
            }
        } catch (SQLException e) {
            if (!lazyConnectFailed && _runInfo.isErrorContinue()) { // when continue option
                if (isContinueWarnLogEnabled()) {
                    showContinueWarnLog(sql, e);
                }
                _runnerResult.addErrorContinuedSql(sql, e);
            } else { // main root @since 0.9.9.2F
                throwSQLFailureException(sql, e);
            }
        }
    }

    protected DfRunnerDispatchResult dispatch(String sql) throws SQLException {
        if (_dispatcher == null) {
            return DfRunnerDispatchResult.NONE;
        }
        return _dispatcher.dispatch(_sqlFile, _currentStatement, sql);
    }

    public enum DfRunnerDispatchResult {
        DISPATCHED, NONE, SKIPPED
    }

    protected void processNonDispatch(String sql) throws SQLException {
        checkStatement(sql);
        _currentStatement.execute(sql);
    }

    protected void lazyConnectIfNeeds() throws SQLException {
        // override if it needs
    }

    protected boolean isContinueWarnLogEnabled() {
        return true;
    }

    protected void showContinueWarnLog(String sql, SQLException e) {
        _log.warn(buildContinueWarnLog(e));
    }

    protected String buildContinueWarnLog(SQLException e) {
        final StringBuilder sb = new StringBuilder();
        sb.append("*Failure: ").append(e.getClass().getName()).append(ln());
        sb.append("/nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
        sb.append(ln());
        sb.append(extractMessage(e)).append(ln());
        buildAdditionalErrorInfo(sb, e).append(ln());
        final SQLException nextEx = e.getNextException();
        if (nextEx != null) {
            sb.append("- - - - - - - - - -").append(ln());
            sb.append(extractMessage(nextEx)).append(ln());
            buildAdditionalErrorInfo(sb, nextEx).append(ln());
            final SQLException nextNextEx = nextEx.getNextException();
            if (nextNextEx != null) {
                sb.append("- - - - - - - - - -").append(ln());
                sb.append(extractMessage(nextNextEx)).append(ln());
                buildAdditionalErrorInfo(sb, nextNextEx).append(ln());
            }
        }
        sb.append("nnnnnnnnnnnnnnnnnnnn/");
        return sb.toString();
    }

    @Override
    protected String extractMessage(SQLException e) {
        final String message = e.getMessage();

        // Because a message of Oracle contains a line separator.
        return message != null ? message.trim() : message;
    }

    protected StringBuilder buildAdditionalErrorInfo(StringBuilder sb, SQLException e) {
        sb.append("(SQLState=").append(e.getSQLState()).append(" ErrorCode=").append(e.getErrorCode()).append(")");
        return sb;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public DfSqlFileRunnerDispatcher getDispatcher() {
        return _dispatcher;
    }

    public void setDispatcher(DfSqlFileRunnerDispatcher dispatcher) {
        this._dispatcher = dispatcher;
    }
}
