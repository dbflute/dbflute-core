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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.SQLFailureException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.jdbc.DfRunnerInformation;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public abstract class DfSqlFileRunnerBase implements DfSqlFileRunner {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static Log _log = LogFactory.getLog(DfSqlFileRunnerBase.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfRunnerInformation _runInfo;
    protected DataSource _dataSource; // may be switched (e.g. LazyConnection)

    protected File _sqlFile;
    protected DfSqlFileRunnerResult _runnerResult;

    protected int _goodSqlCount = 0;
    protected int _totalSqlCount = 0;
    protected int _skippedSqlCount = 0;

    // for sub-class process use
    protected Connection _currentConnection;
    protected Statement _currentStatement;
    protected boolean _beginTransaction;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSqlFileRunnerBase(DfRunnerInformation runInfo, DataSource dataSource) {
        _runInfo = runInfo;
        _dataSource = dataSource;
    }

    public void prepare(File sqlFile) {
        _sqlFile = sqlFile;
        _runnerResult = new DfSqlFileRunnerResult(sqlFile);
    }

    // ===================================================================================
    //                                                                     Run Transaction
    //                                                                     ===============
    public DfSqlFileRunnerResult runTransaction() {
        _goodSqlCount = 0;
        _totalSqlCount = 0;
        _skippedSqlCount = 0;
        if (_sqlFile == null) {
            String msg = "The attribute '_srcFile' should not be null.";
            throw new IllegalStateException(msg);
        }

        boolean skippedFile = false;
        String currentSql = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(newInputStreamReader());
            final List<String> sqlList = extractSqlList(br);

            setupConnection();
            setupStatement();
            int sqlNumber = 0;
            for (String sql : sqlList) {
                ++sqlNumber;
                currentSql = sql;
                if (sqlNumber == 1 && !isTargetFile(sql)) { // first SQL only 
                    skippedFile = true;
                    break;
                }
                if (!isTargetSql(sql)) {
                    continue;
                }
                ++_totalSqlCount;
                final String realSql = filterSql(sql);
                if (!_runInfo.isSuppressLoggingSql()) {
                    traceSql(realSql);
                }
                execSQL(realSql);
            }
            rollbackOrCommit();
        } catch (SQLFailureException breakCause) {
            if (_runInfo.isBreakCauseThrow()) {
                throw breakCause;
            } else {
                _runnerResult.setGoodSqlCount(_goodSqlCount);
                _runnerResult.setTotalSqlCount(_totalSqlCount);
                _runnerResult.setBreakCause(breakCause);
                return _runnerResult;
            }
        } catch (SQLException e) {
            // here is for the exception except executing SQL
            // so it always does not continue
            throwSQLFailureException(currentSql, e);
        } finally {
            try {
                rollback();
            } catch (SQLException ignored) {
            }
            closeStatement();
            closeConnection();
            closeReader(br);
        }
        // re-calculate total count with skipped count
        _totalSqlCount = _totalSqlCount - _skippedSqlCount;

        traceResult(_goodSqlCount, _totalSqlCount);
        _runnerResult.setGoodSqlCount(_goodSqlCount);
        _runnerResult.setTotalSqlCount(_totalSqlCount);
        _runnerResult.setSkippedFile(skippedFile);
        return _runnerResult;
    }

    protected boolean isTargetFile(String sql) {
        return true;
    }

    protected boolean isTargetSql(String sql) {
        return true;
    }

    protected void traceSql(String sql) {
        if (sql.contains(ln())) {
            sql = ln() + sql;
        }
        _log.info(sql);
    }

    protected void traceResult(int goodSqlCount, int totalSqlCount) {
        _log.info(" -> success=" + goodSqlCount + " failure=" + (totalSqlCount - goodSqlCount));
    }

    protected String filterSql(String sql) { // for override
        return sql;
    }

    protected InputStreamReader newInputStreamReader() {
        try {
            final String encoding = _runInfo.isEncodingNull() ? "UTF-8" : _runInfo.getEncoding();
            return new InputStreamReader(new FileInputStream(_sqlFile), encoding);
        } catch (FileNotFoundException e) {
            throw new BuildException("The file does not exist: " + _sqlFile, e);
        } catch (UnsupportedEncodingException e) {
            throw new BuildException("The encoding is unsupported: " + _runInfo.getEncoding(), e);
        }
    }

    protected void setupConnection() {
        if (_dataSource == null) { // means lazy connection (temporarily connect other users)
            return;
        }
        try {
            _currentConnection = _dataSource.getConnection();

            final boolean autoCommit = _runInfo.isAutoCommit();
            _beginTransaction = !autoCommit; // means begin transaction
            _currentConnection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            String msg = "DataSource#getConnection() threw the exception:";
            msg = msg + " dataSource=" + _dataSource;
            throw new SQLFailureException(msg, e);
        }
    }

    protected void checkConnection() {
        if (_currentConnection == null) {
            String msg = "The connection should not be null at this timing!";
            throw new IllegalStateException(msg);
        }
    }

    protected void closeConnection() {
        try {
            if (_currentConnection != null) {
                _currentConnection.rollback();
            }
        } catch (SQLException ignored) {
        }
        try {
            if (_beginTransaction) {
                _currentConnection.setAutoCommit(true);
            }
        } catch (SQLException ignored) {
        }
        try {
            if (_currentConnection != null) {
                _currentConnection.close();
            }
        } catch (SQLException ignored) {
        } finally {
            _currentConnection = null;
        }
    }

    protected Boolean getAutoCommit() {
        Boolean autoCommit = null;
        try {
            autoCommit = _currentConnection.getAutoCommit();
        } catch (SQLException continued) {
            // because it is possible that the connection would have already closed
            _log.warn("Connection#getAutoCommit() said: " + continued.getMessage());
        }
        return autoCommit;
    }

    protected void rollbackOrCommit() throws SQLException {
        if (_currentConnection == null) {
            return;
        }
        final Boolean autoCommit = getAutoCommit();
        if (autoCommit == null || autoCommit) {
            return;
        }
        try {
            if (_runInfo.isRollbackOnly()) {
                _currentConnection.rollback();
            } else {
                _currentConnection.commit();
            }
        } catch (SQLException mayContinued) {
            if (_runInfo.isIgnoreTxError()) {
                // e.g. SQLite may throw an exception (actually said: Database is locked!)
                _log.warn("Connection#rollback()/commit() said: " + mayContinued.getMessage());
            } else {
                throw mayContinued;
            }
        }
    }

    protected void rollback() throws SQLException {
        if (_currentConnection == null) {
            return;
        }
        final Boolean autoCommit = getAutoCommit();
        if (autoCommit == null || autoCommit) {
            return;
        }
        try {
            _currentConnection.rollback();
        } catch (SQLException mayContinued) {
            if (_runInfo.isIgnoreTxError()) {
                // e.g. SQLite may throw an exception (actually said: Database is locked!)
                _log.warn("Connection#rollback()/commit() said: " + mayContinued.getMessage());
            } else {
                throw mayContinued;
            }
        }
    }

    protected void setupStatement() {
        if (_currentConnection == null) {
            return;
        }
        try {
            _currentStatement = _currentConnection.createStatement();
        } catch (SQLException e) {
            String msg = "Connection#createStatement() threw the exception:";
            msg = msg + " connection=" + _currentConnection;
            throw new SQLFailureException(msg, e);
        }
    }

    protected void checkStatement(String sql) {
        if (_currentStatement == null) {
            String msg = "The statement should not be null at this timing:";
            msg = msg + " sql=" + sql;
            throw new IllegalStateException(msg);
        }
    }

    protected void closeStatement() {
        try {
            if (_currentStatement != null) {
                _currentStatement.close();
            }
        } catch (SQLException ignored) {
        } finally {
            _currentStatement = null;
        }
    }

    protected void closeReader(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException ignored) {
        } finally {
            reader = null;
        }
    }

    // ===================================================================================
    //                                                                         Extract SQL
    //                                                                         ===========
    protected List<String> extractSqlList(BufferedReader br) {
        final List<String> sqlList = new ArrayList<String>();
        final DelimiterChanger delimiterChanger = newDelimterChanger();
        try {
            String sql = "";
            String line = "";
            boolean inGroup = false;
            boolean alwaysNeedsLineSeparator = false;
            boolean isAlreadyProcessUTF8Bom = false;
            while ((line = br.readLine()) != null) {
                if (!isAlreadyProcessUTF8Bom) {
                    line = removeUTF8BomIfNeeds(line);
                    isAlreadyProcessUTF8Bom = true;
                }
                if (!inGroup && isSqlTrimAndRemoveLineSeparator()) {
                    line = line.trim();
                }
                if (!alwaysNeedsLineSeparator && isSqlTrimAndRemoveLineSeparator()
                        && isHandlingCommentOnLineSeparator()) {
                    if (isDbCommentLine(line)) {
                        alwaysNeedsLineSeparator = true;
                    }
                }

                // SQL defines "--" as a comment to EOL
                // and in Oracle it may contain a hint
                // so we cannot just remove it, instead we must end it
                if (line.trim().startsWith("--")) { // If this line is comment only, ...
                    // = = = = = = = = = = =
                    // Line for Line Comment
                    // = = = = = = = = = = =

                    // Group Specification
                    // /- - - - - - - - - - - - - - - -
                    if (line.trim().contains("#df:begin#")) {
                        inGroup = true;
                        if (!sql.contains("#df:checkEnv(")) { // patch for checkEnv
                            sql = "";
                        }
                        continue;
                    } else if (line.trim().contains("#df:end#")) {
                        inGroup = false;
                        sql = removeTerminater4ToolIfNeeds(sql); // [DBFLUTE-309]
                        addSqlToList(sqlList, sql);

                        // End Point of SQL!
                        alwaysNeedsLineSeparator = false;
                        sql = "";
                        continue;
                    }
                    // - - - - - - - - - -/

                    // real line comment
                    line = replaceCommentQuestionMarkIfNeeds(line);

                    if (inGroup) {
                        sql = sql + line + ln();
                        continue;
                    }
                    sql = sql + line + ln();
                } else {
                    // = = = = = = = = = =
                    // Line for SQL Clause
                    // = = = = = = = = = =

                    if (inGroup) {
                        sql = sql + line + ln();
                        continue;
                    }

                    final String lineConnect;
                    if (isSqlTrimAndRemoveLineSeparator()) {
                        if (alwaysNeedsLineSeparator) {
                            lineConnect = ln();
                        } else {
                            lineConnect = " ";
                        }
                    } else {
                        lineConnect = "";
                    }
                    if (line.indexOf("--") >= 0) { // If this line contains both SQL and comment, ...
                        // With Line Comment
                        line = replaceCommentQuestionMarkIfNeeds(line);
                        sql = sql + lineConnect + line + ln();
                    } else {
                        // SQL Clause Only
                        final String lineTerminator = isSqlTrimAndRemoveLineSeparator() ? "" : ln();
                        sql = sql + lineConnect + line + lineTerminator;
                    }
                }

                if (sql.trim().endsWith(_runInfo.getDelimiter())) {
                    // = = = = = = = =
                    // End of the SQL
                    // = = = = = = = =

                    sql = sql.trim();
                    sql = sql.substring(0, sql.length() - _runInfo.getDelimiter().length());
                    sql = sql.trim();
                    if ("".equals(sql)) {
                        continue;
                    }
                    if (!delimiterChanger.isDelimiterChanger(sql)) {
                        addSqlToList(sqlList, sql);

                        // End Point of SQL!
                        alwaysNeedsLineSeparator = false;
                        sql = "";
                    } else {
                        _runInfo.setDelimiter(delimiterChanger.getNewDelimiter(sql, _runInfo.getDelimiter()));

                        // End Point of SQL!
                        alwaysNeedsLineSeparator = false;
                        sql = "";
                    }
                }
            }
            sql = sql.trim();
            if (sql.length() > 0) {
                addSqlToList(sqlList, sql); // for Last SQL
            }
        } catch (IOException e) {
            String msg = "The method 'extractSqlList()' threw the IOException!";
            throw new IllegalStateException(msg, e);
        }
        return sqlList;
    }

    protected void addSqlToList(List<String> sqlList, String sql) {
        if (isSqlLineCommentOnly(sql)) {
            return;
        }
        sqlList.add(removeCR(sql));
    }

    protected boolean isSqlLineCommentOnly(String sql) {
        sql = sql.trim();
        String[] lines = sql.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            if (line.startsWith("--")) {
                continue;
            }
            return false;
        }
        _log.info("The SQL has line comments only so skip it:" + ln() + sql);
        return true;
    }

    protected boolean isDbCommentLine(String line) {
        line = line.trim().toLowerCase();
        // basic pattern
        if (line.startsWith("comment on ") && line.contains("is") && line.contains("'")) {
            return true;
        }
        return false;
    }

    protected String removeTerminater4ToolIfNeeds(String sql) {
        String terminater = getTerminator4Tool();
        if (terminater == null || terminater.trim().length() == 0) {
            return sql;
        }
        sql = sql.trim();
        if (sql.endsWith(terminater)) {
            String rear = sql.length() > 30 ? ": ..." + sql.substring(sql.length() - 30) : ".";
            _log.info("...Removing terminater '" + terminater + "' for tools" + rear);
            sql = sql.substring(0, sql.length() - terminater.length());
        }
        return sql;
    }

    protected String getTerminator4Tool() { // for override.
        return null;
    }

    public DelimiterChanger newDelimterChanger() {
        final String databaseName = DfBuildProperties.getInstance().getBasicProperties().getTargetDatabase();
        final String className = DelimiterChanger.class.getName() + "_" + databaseName;
        DelimiterChanger changer = null;
        try {
            changer = (DelimiterChanger) Class.forName(className).newInstance();
        } catch (Exception ignore) {
            changer = new DelimiterChanger_null();
        }
        return changer;
    }

    protected String removeUTF8BomIfNeeds(String str) {
        if (_runInfo.isEncodingNull()) {
            return str;
        }
        if ("UTF-8".equalsIgnoreCase(_runInfo.getEncoding()) && str.length() > 0 && str.charAt(0) == '\uFEFF') {
            String front = str.length() > 5 ? ": " + str.substring(0, 5) + "..." : ".";
            _log.info("...Removing UTF-8 bom" + front);
            str = str.substring(1);
        }
        return str;
    }

    protected String removeCR(String str) {
        return str.replaceAll("\r", "");
    }

    protected String replaceCommentQuestionMarkIfNeeds(String line) {
        final int lineCommentIndex = line.indexOf("--");
        if (lineCommentIndex < 0) {
            return line;
        }
        final String sqlClause;
        if (lineCommentIndex == 0) {
            sqlClause = "";
        } else {
            sqlClause = line.substring(0, lineCommentIndex);
        }
        String lineComment = line.substring(lineCommentIndex);
        if (lineComment.indexOf("?") >= 0) {
            lineComment = Srl.replace(line, "?", "Q");
        }
        return sqlClause + lineComment;
    }

    // ===================================================================================
    //                                                                  Exception Handling
    //                                                                  ==================
    protected void throwSQLFailureException(String sql, SQLException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to execute the SQL!");
        br.addItem("SQL File");
        br.addElement(_sqlFile);
        br.addItem("Executed SQL");
        br.addElement(sql);
        br.addItem("SQLState");
        br.addElement(e.getSQLState());
        br.addItem("ErrorCode");
        br.addElement(e.getErrorCode());
        br.addItem("SQLException");
        br.addElement(e.getClass().getName());
        br.addElement(extractMessage(e));
        final SQLException nextEx = e.getNextException();
        if (nextEx != null) {
            br.addItem("NextException");
            br.addElement(nextEx.getClass().getName());
            br.addElement(extractMessage(nextEx));
            final SQLException nextNextEx = nextEx.getNextException();
            if (nextNextEx != null) {
                br.addItem("NextNextException");
                br.addElement(nextNextEx.getClass().getName());
                br.addElement(extractMessage(nextNextEx));
            }
        }
        final String msg = br.buildExceptionMessage();
        throw new SQLFailureException(msg, e);
    }

    protected String extractMessage(SQLException e) {
        String message = e.getMessage();

        // Because a message of Oracle contains a line separator.
        return message != null ? message.trim() : message;
    }

    // ===================================================================================
    //                                                                        For Override
    //                                                                        ============
    /**
     * Execute the SQL statement.
     * @param sql SQL. (NotNull)
     */
    protected abstract void execSQL(String sql);

    /**
     * @return The determination, true or false.
     */
    protected boolean isSqlTrimAndRemoveLineSeparator() {
        return false; // as default (keep plain for looks)
    }

    /**
     * @return The determination, true or false.
     */
    protected boolean isHandlingCommentOnLineSeparator() {
        return false; // as default (no handling about comment-on line separator)
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        // (DBFLUTE-264)
        return "\n";
    }

    // ===================================================================================
    //                                                                   Delimiter Changer
    //                                                                   =================
    protected static interface DelimiterChanger {
        public boolean isDelimiterChanger(String sql);

        public String getNewDelimiter(String sql, String preDelimiter);
    }

    protected static class DelimiterChanger_firebird implements DelimiterChanger {
        public static final String CHANGE_COMMAND = "set term ";
        public static final int CHANGE_COMMAND_LENGTH = CHANGE_COMMAND.length();

        public boolean isDelimiterChanger(String sql) {
            sql = sql.trim();
            if (sql.length() > CHANGE_COMMAND_LENGTH) {
                if (sql.substring(0, CHANGE_COMMAND_LENGTH).equalsIgnoreCase(CHANGE_COMMAND)) {
                    return true;
                }
            }
            return false;
        }

        public String getNewDelimiter(String sql, String preDelimiter) {
            String tmp = sql.substring(CHANGE_COMMAND.length());
            if (tmp.indexOf(" ") >= 0) {
                tmp = tmp.substring(0, tmp.indexOf(" "));
            }
            return tmp;
        }
    }

    protected static class DelimiterChanger_mysql implements DelimiterChanger {
        public static final String CHANGE_COMMAND = "delimiter ";
        public static final int CHANGE_COMMAND_LENGTH = CHANGE_COMMAND.length();

        public boolean isDelimiterChanger(String sql) {
            sql = sql.trim();
            if (sql.length() > CHANGE_COMMAND_LENGTH) {
                if (sql.substring(0, CHANGE_COMMAND_LENGTH).equalsIgnoreCase(CHANGE_COMMAND)) {
                    return true;
                }
            }
            return false;
        }

        public String getNewDelimiter(String sql, String preDelimiter) {
            String tmp = sql.substring(CHANGE_COMMAND.length());
            if (tmp.indexOf(" ") >= 0) {
                tmp = tmp.substring(0, tmp.indexOf(" "));
            }
            return tmp;
        }
    }

    protected static class DelimiterChanger_null implements DelimiterChanger {

        public boolean isDelimiterChanger(String sql) {
            return false;
        }

        public String getNewDelimiter(String sql, String preDelimiter) {
            return preDelimiter;
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public DfSqlFileRunnerResult getResult() {
        return _runnerResult;
    }
}
