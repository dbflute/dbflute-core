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
package org.dbflute.outsidesql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.DBMetaProvider;
import org.dbflute.exception.OutsideSqlNotFoundException;
import org.dbflute.exception.OutsideSqlReadFailureException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.jdbc.CursorHandler;
import org.dbflute.jdbc.StatementConfig;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfResourceUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The context of outside-SQL.
 * @author jflute
 */
public class OutsideSqlContext {

    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(OutsideSqlContext.class);

    // ===================================================================================
    //                                                                        Thread Local
    //                                                                        ============
    /** The thread-local for this. */
    private static final ThreadLocal<OutsideSqlContext> _threadLocal = new ThreadLocal<OutsideSqlContext>();

    /**
     * Get outside-SQL context on thread.
     * @return The context of outside-SQL. (NullAllowed)
     */
    public static OutsideSqlContext getOutsideSqlContextOnThread() {
        return (OutsideSqlContext) _threadLocal.get();
    }

    /**
     * Set outside-SQL context on thread.
     * @param outsideSqlContext The context of outside-SQL. (NotNull)
     */
    public static void setOutsideSqlContextOnThread(OutsideSqlContext outsideSqlContext) {
        if (outsideSqlContext == null) {
            String msg = "The argument[outsideSqlContext] must not be null.";
            throw new IllegalArgumentException(msg);
        }
        _threadLocal.set(outsideSqlContext);
    }

    /**
     * Is existing the context of outside-SQL on thread?
     * @return The determination, true or false.
     */
    public static boolean isExistOutsideSqlContextOnThread() {
        return (_threadLocal.get() != null);
    }

    /**
     * Clear the context of outside-SQL on thread.
     */
    public static void clearOutsideSqlContextOnThread() {
        _threadLocal.set(null);
    }

    // ===================================================================================
    //                                                                          Unique Key
    //                                                                          ==========
    public static String generateSpecifiedOutsideSqlUniqueKey(String methodName, String path, Object pmb, OutsideSqlOption option,
            Class<?> resultType) {
        final String pmbKey = (pmb != null ? pmb.getClass().getName() : "null");
        final String resultKey;
        if (resultType != null) {
            resultKey = resultType.getName();
        } else {
            resultKey = "null";
        }
        final String tableDbName = option.getTableDbName();
        final String generatedUniqueKey = option.generateUniqueKey();
        return tableDbName + ":" + methodName + "():" + path + ":" + pmbKey + ":" + generatedUniqueKey + ":" + resultKey;
    }

    // ===================================================================================
    //                                                                  Exception Handling
    //                                                                  ==================
    public static void throwOutsideSqlNotFoundException(String path) {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The outsideSql was not found!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "Please confirm the existence of your target file of outsideSql on your classpath." + ln();
        msg = msg + "And please confirm the file name and the file path STRICTLY!" + ln();
        msg = msg + ln();
        msg = msg + "[Specified OutsideSql Path]" + ln() + path + ln();
        msg = msg + "* * * * * * * * * */";
        throw new OutsideSqlNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    /** The provider of DB meta. (NotNull) */
    protected final DBMetaProvider _dbmetaProvider;

    /** The package of outside-SQL. (NullAllowed: if null, use behavior package path) */
    protected final String _outsideSqlPackage;

    /** The path of outside-SQL. (The mark of specified outside-SQL) */
    protected String _outsideSqlPath;

    /** The instance of specified parameter bean. (NullAllowed: but almost not-null) */
    protected Object _parameterBean;

    /** The data type of outsideSql result, e.g. Entity, Integer. (basically NotNull) */
    protected Class<?> _resultType;

    /** The handler of cursor select. (NullAllowed: when not cursor) */
    protected CursorHandler _cursorHandler;

    /** The command name of outsideSql e.g. outsideSql executor's method. (basically NotNull) */
    protected String _methodName;

    /** The configuration of statement. (NullAllowed) */
    protected StatementConfig _statementConfig;

    /** The DB name of table for using behavior-SQL-path. (NullAllowed) */
    protected String _tableDbName;

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    protected boolean _offsetByCursorForcedly;
    protected boolean _limitByCursorForcedly;
    protected boolean _autoPagingLogging; // for logging
    protected OutsideSqlFilter _outsideSqlFilter;
    protected boolean _removeBlockComment;
    protected boolean _removeLineComment;
    protected boolean _formatSql;
    protected boolean _nonSpecifiedColumnAccessAllowed; // when domain entity
    protected boolean _internalDebug;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param dbmetaProvider The provider of DB meta. (NotNull)
     * @param outsideSqlPackage The package of outside SQL. (NullAllowed: If null, use behavior package path)
     */
    public OutsideSqlContext(DBMetaProvider dbmetaProvider, String outsideSqlPackage) {
        if (dbmetaProvider == null) {
            String msg = "The argument 'dbmetaProvider' should not be null!";
            throw new IllegalArgumentException(msg);
        }
        _dbmetaProvider = dbmetaProvider;
        _outsideSqlPackage = outsideSqlPackage;
    }

    // ===================================================================================
    //                                                                            Read SQL
    //                                                                            ========
    /**
     * Read outside-SQL with filter. Required attribute is 'outsideSqlPath'.
     * @param sqlFileEncoding The encoding of SQL file. (NotNull)
     * @param dbmsSuffix The suffix of DBMS. (NotNull)
     * @return The filtered outside-SQL. (NotNull)
     * @throws org.dbflute.exception.OutsideSqlNotFoundException When the SQL is not found.
     */
    public String readFilteredOutsideSql(String sqlFileEncoding, String dbmsSuffix) { // entry here
        String sql = readPlainOutsideSql(sqlFileEncoding, dbmsSuffix);
        sql = replaceOutsideSqlBindCharacterOnLineComment(sql);
        if (_outsideSqlFilter != null) {
            sql = _outsideSqlFilter.filterReading(sql);
        }
        return sql;
    }

    protected String replaceOutsideSqlBindCharacterOnLineComment(String sql) {
        final String bindCharacter = "?";
        if (sql.indexOf(bindCharacter) < 0) {
            return sql;
        }
        final String lineSeparator = "\n";
        if (sql.indexOf(lineSeparator) < 0) {
            return sql;
        }
        final String lineCommentMark = "--";
        if (sql.indexOf(lineCommentMark) < 0) {
            return sql;
        }
        final StringBuilder sb = new StringBuilder();
        final String[] lines = sql.split(lineSeparator);
        for (String line : lines) {
            final int lineCommentIndex = line.indexOf("--");
            if (lineCommentIndex < 0) {
                sb.append(line).append(lineSeparator);
                continue;
            }
            final String lineComment = line.substring(lineCommentIndex);
            if (lineComment.contains("ELSE") || !lineComment.contains(bindCharacter)) {
                sb.append(line).append(lineSeparator);
                continue;
            }

            if (_log.isDebugEnabled()) {
                _log.debug("...Replacing bind character on line comment: " + lineComment);
            }
            final String filteredLineComment = replaceString(lineComment, bindCharacter, "Q");
            sb.append(line.substring(0, lineCommentIndex)).append(filteredLineComment).append(lineSeparator);
        }
        return sb.toString();
    }

    /**
     * Read outside-SQL without filter. Required attribute is 'outsideSqlPath'.
     * @param sqlFileEncoding The encoding of SQL file. (NotNull)
     * @param dbmsSuffix The suffix of DBMS. (NotNull)
     * @return The text of SQL. (NotNull)
     * @throws org.dbflute.exception.OutsideSqlNotFoundException When the SQL is not found.
     */
    protected String readPlainOutsideSql(String sqlFileEncoding, String dbmsSuffix) {
        final String standardPath = _outsideSqlPath;
        String readSql = doReadPlainOutsideSql(sqlFileEncoding, dbmsSuffix, standardPath);
        if (readSql != null) {
            return readSql;
        }
        // means not found
        final String pureName = Srl.substringLastRear(standardPath, "/");
        if (pureName.contains("Bhv_")) { // retry for ApplicationBehavior
            final String dir = Srl.substringLastFront(standardPath, "/");
            final String filtered = Srl.replace(pureName, "Bhv_", "BhvAp_");
            final String bhvApPath = dir + "/" + filtered;
            readSql = doReadPlainOutsideSql(sqlFileEncoding, dbmsSuffix, bhvApPath);
        }
        if (readSql != null) {
            return readSql;
        }
        throwOutsideSqlNotFoundException(standardPath);
        return null; // unreachable
    }

    protected String doReadPlainOutsideSql(String sqlFileEncoding, String dbmsSuffix, String standardPath) {
        final String dbmsPath = buildDbmsPath(standardPath, dbmsSuffix);
        if (_internalDebug && _log.isDebugEnabled()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("...Reading the outside-SQL: ").append(standardPath);
            sb.append(" {").append(sqlFileEncoding).append(", ").append(dbmsSuffix).append("}");
            _log.debug(sb.toString());
        }
        final String sql;
        if (isExistResource(dbmsPath)) { // at first
            if (_internalDebug && _log.isDebugEnabled()) {
                _log.debug("Found the outside-SQL for the DBMS: " + dbmsPath);
            }
            sql = readText(dbmsPath, sqlFileEncoding);
        } else {
            final String resolvedSql = doReadOutsideSqlWithAliasSuffix(standardPath, sqlFileEncoding, dbmsSuffix);
            if (resolvedSql != null) {
                sql = resolvedSql;
            } else if (isExistResource(standardPath)) { // main
                sql = readText(standardPath, sqlFileEncoding);
            } else {
                return null; // means not found
            }
        }
        return removeInitialUnicodeBomIfNeeds(sqlFileEncoding, sql);
    }

    protected String doReadOutsideSqlWithAliasSuffix(String standardPath, String sqlFileEncoding, String dbmsSuffix) {
        String anotherPath = null;
        if ("_postgresql".equals(dbmsSuffix)) {
            anotherPath = buildDbmsPath(standardPath, "_postgre");
        } else if ("_sqlserver".equals(dbmsSuffix)) {
            anotherPath = buildDbmsPath(standardPath, "_mssql");
        }
        if (anotherPath != null && isExistResource(anotherPath)) { // patch for name difference
            return readText(anotherPath, sqlFileEncoding);
        } else {
            return null;
        }
    }

    protected String buildDbmsPath(String standardPath, String dbmsSuffix) {
        final String dbmsPath;
        final int lastIndexOfDot = standardPath.lastIndexOf(".");
        if (lastIndexOfDot >= 0 && !standardPath.substring(lastIndexOfDot).contains("/")) {
            final String base = standardPath.substring(0, lastIndexOfDot);
            dbmsPath = base + dbmsSuffix + standardPath.substring(lastIndexOfDot);
        } else {
            dbmsPath = standardPath + dbmsSuffix;
        }
        return dbmsPath;
    }

    protected String removeInitialUnicodeBomIfNeeds(String sqlFileEncoding, String sql) {
        if ("UTF-8".equalsIgnoreCase(sqlFileEncoding) && sql.length() > 0 && sql.charAt(0) == '\uFEFF') {
            sql = sql.substring(1);
        }
        return sql;
    }

    // ===================================================================================
    //                                                                 Behavior Query Path
    //                                                                 ===================
    public void setupBehaviorQueryPathIfNeeds() {
        if (!isBehaviorQueryPathEnabled()) {
            return;
        }
        if (_outsideSqlPath.contains(":")) {
            final String subDirectoryValue = _outsideSqlPath.substring(0, _outsideSqlPath.lastIndexOf(":"));
            final String subDirectoryPath = replaceString(subDirectoryValue, ":", "/");
            final String behaviorQueryPath = _outsideSqlPath.substring(_outsideSqlPath.lastIndexOf(":") + ":".length());
            final String behaviorClassPath = replaceString(buildBehaviorSqlPackageName(), ".", "/");
            final String behaviorPackagePath = behaviorClassPath.substring(0, behaviorClassPath.lastIndexOf("/"));
            final String behaviorClassName = behaviorClassPath.substring(behaviorClassPath.lastIndexOf("/") + "/".length());
            _outsideSqlPath = behaviorPackagePath + "/" + subDirectoryPath + "/" + behaviorClassName + "_" + behaviorQueryPath + ".sql";
        } else {
            _outsideSqlPath = replaceString(buildBehaviorSqlPackageName(), ".", "/") + "_" + _outsideSqlPath + ".sql";
        }
    }

    protected String buildBehaviorSqlPackageName() {
        final DBMeta dbmeta = _dbmetaProvider.provideDBMetaChecked(_tableDbName);
        final String behaviorType = dbmeta.getBehaviorTypeName();
        final String outsideSqlPackage = _outsideSqlPackage;
        if (outsideSqlPackage != null && outsideSqlPackage.trim().length() > 0) {
            return mergeBehaviorSqlPackage(behaviorType, outsideSqlPackage);
        } else {
            return behaviorType;
        }
    }

    protected String mergeBehaviorSqlPackage(String behaviorType, String outsideSqlPackage) {
        final String dot = ".";
        final String bhvClass = behaviorType.substring(behaviorType.lastIndexOf(dot) + dot.length());
        final String bhvPkg = behaviorType.substring(0, behaviorType.lastIndexOf(dot));
        final String exbhvName = bhvPkg.contains(dot) ? bhvPkg.substring(bhvPkg.lastIndexOf(dot) + dot.length()) : bhvPkg;
        final String exbhvSuffix = dot + exbhvName;
        final String adjustedSqlPkg = Srl.removeSuffix(outsideSqlPackage, exbhvSuffix); // avoid 'exbhv.exbhv'
        return adjustedSqlPkg + exbhvSuffix + dot + bhvClass;
    }

    protected boolean isBehaviorQueryPathEnabled() {
        if (isProcedure()) { // [DBFlute-0.7.5]
            return false;
        }
        return _outsideSqlPath != null && !_outsideSqlPath.contains("/") && !_outsideSqlPath.contains(".") && _tableDbName != null;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean isSpecifiedOutsideSql() {
        return _outsideSqlPath != null;
    }

    // [DBFlute-0.7.5]
    public boolean isProcedure() {
        return _methodName != null && _methodName.startsWith("call");
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected boolean isExistResource(String path) {
        return DfResourceUtil.isExist(path);
    }

    protected String readText(final String path, String sqlFileEncoding) {
        final InputStream ins = DfResourceUtil.getResourceStream(path);
        Reader reader = null;
        try {
            reader = createInputStreamReader(ins, sqlFileEncoding);
            return readText(reader);
        } catch (IOException e) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Failed to read the text for outside-SQL.");
            br.addItem("OutsideSql Path");
            br.addElement(path);
            br.addItem("SQL File Encoding");
            br.addElement(sqlFileEncoding);
            final String msg = br.buildExceptionMessage();
            throw new OutsideSqlReadFailureException(msg);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                    if (_internalDebug && _log.isDebugEnabled()) {
                        _log.debug("Failed to close the reader: path=" + path, ignored);
                    }
                }
            }
        }
    }

    protected Reader createInputStreamReader(InputStream ins, String encoding) throws IOException {
        return new InputStreamReader(ins, encoding);
    }

    public String readText(Reader reader) throws IOException {
        final StringBuilder sb = new StringBuilder(100);
        BufferedReader br = null;
        try {
            br = new BufferedReader(reader);
            final char[] buf = new char[8192];
            int n;
            while ((n = br.read(buf)) >= 0) {
                sb.append(buf, 0, n);
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {}
            }
        }
        return sb.toString();
    }

    protected static String replaceString(String text, String fromText, String toText) {
        return Srl.replace(text, fromText, toText);
    }

    protected static String ln() {
        return DBFluteSystem.ln();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    public String getOutsideSqlPath() {
        return _outsideSqlPath;
    }

    public void setOutsideSqlPath(String outsideSqlPath) {
        _outsideSqlPath = outsideSqlPath;
    }

    public Object getParameterBean() {
        return _parameterBean;
    }

    public void setParameterBean(Object parameterBean) {
        _parameterBean = parameterBean;
    }

    public Class<?> getResultType() {
        return _resultType;
    }

    public void setResultType(Class<?> resultType) {
        _resultType = resultType;
    }

    public CursorHandler getCursorHandler() {
        return _cursorHandler;
    }

    public void setCursorHandler(CursorHandler handler) {
        _cursorHandler = handler;
    }

    public String getMethodName() {
        return _methodName;
    }

    public void setMethodName(String methodName) {
        _methodName = methodName;
    }

    public StatementConfig getStatementConfig() {
        return _statementConfig;
    }

    public void setStatementConfig(StatementConfig statementConfig) {
        _statementConfig = statementConfig;
    }

    public String getTableDbName() {
        return _tableDbName;
    }

    public void setTableDbName(String tableDbName) {
        _tableDbName = tableDbName;
    }

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    public boolean isOffsetByCursorForcedly() {
        return _offsetByCursorForcedly;
    }

    public void setOffsetByCursorForcedly(boolean offsetByCursorForcedly) {
        _offsetByCursorForcedly = offsetByCursorForcedly;
    }

    public boolean isLimitByCursorForcedly() {
        return _limitByCursorForcedly;
    }

    public void setLimitByCursorForcedly(boolean limitByCursorForcedly) {
        _limitByCursorForcedly = limitByCursorForcedly;
    }

    public boolean isAutoPagingLogging() { // for logging
        return _autoPagingLogging;
    }

    public void setAutoPagingLogging(boolean autoPagingLogging) { // for logging
        _autoPagingLogging = autoPagingLogging;
    }

    public OutsideSqlFilter getOutsideSqlFilter() {
        return _outsideSqlFilter;
    }

    public void setOutsideSqlFilter(OutsideSqlFilter outsideSqlFilter) {
        _outsideSqlFilter = outsideSqlFilter;
    }

    public boolean isRemoveBlockComment() {
        return _removeBlockComment;
    }

    public void setRemoveBlockComment(boolean removeBlockComment) {
        _removeBlockComment = removeBlockComment;
    }

    public boolean isRemoveLineComment() {
        return _removeLineComment;
    }

    public void setRemoveLineComment(boolean removeLineComment) {
        _removeLineComment = removeLineComment;
    }

    public boolean isFormatSql() {
        return _formatSql;
    }

    public void setFormatSql(boolean formatSql) {
        _formatSql = formatSql;
    }

    public boolean isNonSpecifiedColumnAccessAllowed() {
        return _nonSpecifiedColumnAccessAllowed;
    }

    public void setNonSpecifiedColumnAccessAllowed(boolean nonSpecifiedColumnAccessAllowed) {
        _nonSpecifiedColumnAccessAllowed = nonSpecifiedColumnAccessAllowed;
    }

    public boolean isInternalDebug() {
        return _internalDebug;
    }

    public void setInternalDebug(boolean internalDebug) {
        _internalDebug = internalDebug;
    }
}
