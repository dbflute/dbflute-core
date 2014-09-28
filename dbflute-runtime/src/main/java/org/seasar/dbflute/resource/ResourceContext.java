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
package org.seasar.dbflute.resource;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import org.seasar.dbflute.AccessContext;
import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.bhv.core.BehaviorCommand;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.ConditionBeanContext;
import org.seasar.dbflute.cbean.cipher.ColumnFunctionCipher;
import org.seasar.dbflute.cbean.cipher.GearedCipherManager;
import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.cbean.sqlclause.SqlClauseCreator;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.DBMetaProvider;
import org.seasar.dbflute.exception.factory.SQLExceptionHandlerFactory;
import org.seasar.dbflute.exception.handler.SQLExceptionHandler;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.jdbc.ValueType;
import org.seasar.dbflute.twowaysql.SqlAnalyzer;
import org.seasar.dbflute.twowaysql.factory.SqlAnalyzerFactory;

/**
 * The context of resource.
 * @author jflute
 */
public class ResourceContext {

    // ===================================================================================
    //                                                                        Thread Local
    //                                                                        ============
    /** The thread-local for this. */
    private static final ThreadLocal<ResourceContext> threadLocal = new ThreadLocal<ResourceContext>();

    /**
     * Get the context of resource by the key.
     * @return The context of resource. (NullAllowed)
     */
    public static ResourceContext getResourceContextOnThread() {
        return threadLocal.get();
    }

    /**
     * Set the context of resource.
     * @param resourceContext The context of resource. (NotNull)
     */
    public static void setResourceContextOnThread(ResourceContext resourceContext) {
        threadLocal.set(resourceContext);
    }

    /**
     * Is existing the context of resource on thread?
     * @return The determination, true or false.
     */
    public static boolean isExistResourceContextOnThread() {
        return (threadLocal.get() != null);
    }

    /**
     * Clear the context of resource on thread.
     */
    public static void clearResourceContextOnThread() {
        threadLocal.set(null);
    }

    // ===================================================================================
    //                                                                         Easy-to-Use
    //                                                                         ===========
    /**
     * @return The behavior command. (NotNull)
     */
    public static BehaviorCommand<?> behaviorCommand() {
        assertResourceContextExists();
        final ResourceContext context = getResourceContextOnThread();
        final BehaviorCommand<?> behaviorCommand = context.getBehaviorCommand();
        if (behaviorCommand == null) {
            String msg = "The behavior command should exist: context=" + context;
            throw new IllegalStateException(msg);
        }
        return behaviorCommand;
    }

    /**
     * @return The current database definition. (NotNull)
     */
    public static DBDef currentDBDef() {
        if (!isExistResourceContextOnThread()) {
            return DBDef.Unknown;
        }
        final DBDef currentDBDef = getResourceContextOnThread().getCurrentDBDef();
        if (currentDBDef == null) {
            return DBDef.Unknown;
        }
        return currentDBDef;
    }

    public static boolean isCurrentDBDef(DBDef targetDBDef) {
        return currentDBDef().equals(targetDBDef);
    }

    /**
     * @return The provider of DB meta. (NotNull)
     */
    public static DBMetaProvider dbmetaProvider() {
        assertResourceContextExists();
        final ResourceContext context = getResourceContextOnThread();
        final DBMetaProvider provider = context.getDBMetaProvider();
        if (provider == null) {
            String msg = "The provider of DB meta should exist: context=" + context;
            throw new IllegalStateException(msg);
        }
        return provider;
    }

    /**
     * @param tableFlexibleName The flexible name of table. (NotNull)
     * @return The instance of DB meta. (NullAllowed: if null, means not found)
     */
    public static DBMeta provideDBMeta(String tableFlexibleName) {
        if (!isExistResourceContextOnThread()) {
            return null;
        }
        final DBMetaProvider provider = getResourceContextOnThread().getDBMetaProvider();
        return provider != null ? provider.provideDBMeta(tableFlexibleName) : null;
    }

    /**
     * @param entityType The entity type of table. (NotNull)
     * @return The instance of DB meta. (NullAllowed)
     */
    public static DBMeta provideDBMeta(Class<?> entityType) {
        if (!isExistResourceContextOnThread()) {
            return null;
        }
        final DBMetaProvider provider = getResourceContextOnThread().getDBMetaProvider();
        return provider != null ? provider.provideDBMeta(entityType) : null;
    }

    /**
     * @param tableFlexibleName The flexible name of table. (NotNull)
     * @return The instance of DB meta. (NotNull)
     */
    public static DBMeta provideDBMetaChecked(String tableFlexibleName) {
        assertResourceContextExists();
        final ResourceContext context = getResourceContextOnThread();
        final DBMetaProvider provider = context.getDBMetaProvider();
        if (provider == null) {
            String msg = "The provider of DB meta should exist:";
            msg = msg + " tableFlexibleName=" + tableFlexibleName + " context=" + context;
            throw new IllegalStateException(msg);
        }
        return provider.provideDBMetaChecked(tableFlexibleName);
    }

    /**
     * @param entityType The entity type of table. (NotNull)
     * @return The instance of DB meta. (NotNull)
     */
    public static DBMeta provideDBMetaChecked(Class<?> entityType) {
        assertResourceContextExists();
        final ResourceContext context = getResourceContextOnThread();
        final DBMetaProvider provider = context.getDBMetaProvider();
        if (provider == null) {
            String msg = "The provider of DB meta should exist:";
            msg = msg + " entityType=" + entityType + " context=" + context;
            throw new IllegalStateException(msg);
        }
        return provider.provideDBMetaChecked(entityType);
    }

    public static SqlAnalyzer createSqlAnalyzer(String sql, boolean blockNullParameter) {
        assertResourceContextExists();
        final ResourceContext context = getResourceContextOnThread();
        final SqlAnalyzerFactory factory = context.getSqlAnalyzerFactory();
        if (factory == null) {
            String msg = "The factory of SQL analyzer should exist:";
            msg = msg + " sql=" + sql + " blockNullParameter=" + blockNullParameter;
            throw new IllegalStateException(msg);
        }
        final SqlAnalyzer created = factory.create(sql, blockNullParameter);
        if (created != null) {
            return created;
        }
        String msg = "The factory should not return null:";
        msg = msg + " sql=" + sql + " factory=" + factory;
        throw new IllegalStateException(msg);
    }

    public static SQLExceptionHandler createSQLExceptionHandler() {
        assertResourceContextExists();
        final ResourceContext context = getResourceContextOnThread();
        final SQLExceptionHandlerFactory factory = context.getSQLExceptionHandlerFactory();
        if (factory == null) {
            String msg = "The factory of SQLException handler should exist.";
            throw new IllegalStateException(msg);
        }
        final SQLExceptionHandler created = factory.create();
        if (created != null) {
            return created;
        }
        String msg = "The factory should not return null: factory=" + factory;
        throw new IllegalStateException(msg);
    }

    /**
     * Is the SQLException from unique constraint? {Use both SQLState and ErrorCode}
     * @param sqlState SQLState of the SQLException. (NullAllowed)
     * @param errorCode ErrorCode of the SQLException. (NullAllowed)
     * @return Is the SQLException from unique constraint?
     */
    public static boolean isUniqueConstraintException(String sqlState, Integer errorCode) {
        if (!isExistResourceContextOnThread()) {
            return false;
        }
        final SqlClauseCreator sqlClauseCreator = getResourceContextOnThread().getSqlClauseCreator();
        if (sqlClauseCreator == null) {
            return false;
        }
        return currentDBDef().dbway().isUniqueConstraintException(sqlState, errorCode);
    }

    public static ColumnFunctionCipher findColumnFunctionCipher(String tableDbName, String columnDbName) {
        assertResourceContextExists();
        final ResourceContext context = getResourceContextOnThread();
        final GearedCipherManager manager = context.getGearedCipherManager();
        return manager != null ? manager.findColumnFunctionCipher(tableDbName, columnDbName) : null;
    }

    public static String getOutsideSqlPackage() {
        final ResourceParameter resourceParameter = resourceParameter();
        return resourceParameter != null ? resourceParameter.getOutsideSqlPackage() : null;
    }

    public static String getLogDateFormat() {
        final ResourceParameter resourceParameter = resourceParameter();
        return resourceParameter != null ? resourceParameter.getLogDateFormat() : null;
    }

    public static String getLogTimestampFormat() {
        final ResourceParameter resourceParameter = resourceParameter();
        return resourceParameter != null ? resourceParameter.getLogTimestampFormat() : null;
    }

    public static boolean isInternalDebug() {
        final ResourceParameter resourceParameter = resourceParameter();
        return resourceParameter != null ? resourceParameter.isInternalDebug() : false;
    }

    protected static ResourceParameter resourceParameter() {
        if (!isExistResourceContextOnThread()) {
            return null;
        }
        return getResourceContextOnThread().getResourceParameter();
    }

    protected static void assertResourceContextExists() {
        if (!isExistResourceContextOnThread()) {
            String msg = "The resource context should exist!";
            throw new IllegalStateException(msg);
        }
    }

    // -----------------------------------------------------
    //                                           Access Date
    //                                           -----------
    public static Date getAccessDate() {
        return AccessContext.getAccessDateOnThread();
    }

    public static Timestamp getAccessTimestamp() {
        return AccessContext.getAccessTimestampOnThread();
    }

    // -----------------------------------------------------
    //                                         Select Column
    //                                         -------------
    public static Map<String, String> createSelectColumnMap(ResultSet rs) throws SQLException {
        final ResultSetMetaData rsmd = rs.getMetaData();
        final int count = rsmd.getColumnCount();
        final Map<String, String> selectColumnKeyNameMap = getSelectColumnKeyNameMap();

        // flexible for resolving non-compilable connectors and reservation words
        final Map<String, String> columnMap = StringKeyMap.createAsFlexible();

        for (int i = 0; i < count; ++i) {
            String columnLabel = rsmd.getColumnLabel(i + 1);
            final int dotIndex = columnLabel.lastIndexOf('.');
            if (dotIndex >= 0) { // basically for SQLite
                columnLabel = columnLabel.substring(dotIndex + 1);
            }
            final String realColumnName;
            if (selectColumnKeyNameMap != null) { // use select index
                final String mappedName = selectColumnKeyNameMap.get(columnLabel);
                if (mappedName != null) { // mainly true
                    realColumnName = mappedName; // switch on-query-name to column DB names
                } else { // for derived columns and so on
                    realColumnName = columnLabel;
                }
            } else {
                realColumnName = columnLabel;
            }
            columnMap.put(realColumnName, realColumnName);
        }
        return columnMap;
    }

    protected static Map<String, String> getSelectColumnKeyNameMap() {
        if (!ConditionBeanContext.isExistConditionBeanOnThread()) {
            return null;
        }
        final ConditionBean cb = ConditionBeanContext.getConditionBeanOnThread();
        return cb.getSqlClause().getSelectColumnKeyNameMap();
    }

    // -----------------------------------------------------
    //                                          Select Index
    //                                          ------------
    /**
     * Get the map of select index. map:{entityNo(e.g. loc00 or _0_3) = map:{selectColumnKeyName = selectIndex}}
     * @return The map of select index. (NullAllowed: null means select index is disabled)
     */
    public static Map<String, Map<String, Integer>> getSelectIndexMap() {
        if (!ConditionBeanContext.isExistConditionBeanOnThread()) {
            return null;
        }
        // basically only used by getLocalValue() or getRelationValue()
        // but argument style for performance
        final ConditionBean cb = ConditionBeanContext.getConditionBeanOnThread();
        return cb.getSqlClause().getSelectIndexMap();
    }

    public static Object getLocalValue(ResultSet rs, String columnName, ValueType valueType,
            Map<String, Map<String, Integer>> selectIndexMap) throws SQLException {
        return doGetValue(rs, SqlClause.BASE_POINT_HANDLING_ENTITY_NO, columnName, valueType, selectIndexMap);
    }

    public static Object getRelationValue(ResultSet rs, String relationNoSuffix, String columnName,
            ValueType valueType, Map<String, Map<String, Integer>> selectIndexMap) throws SQLException {
        return doGetValue(rs, relationNoSuffix, columnName, valueType, selectIndexMap);
    }

    protected static Object doGetValue(ResultSet rs, String entityNo, String columnName, ValueType valueType,
            Map<String, Map<String, Integer>> selectIndexMap) throws SQLException {
        final Map<String, Integer> innerMap = selectIndexMap != null ? selectIndexMap.get(entityNo) : null;
        final Integer selectIndex = innerMap != null ? innerMap.get(columnName) : null;
        if (selectIndex != null) {
            return valueType.getValue(rs, selectIndex);
        } else {
            return valueType.getValue(rs, columnName);
        }
    }

    public static boolean isOutOfLocalSelectIndex(String columnDbName, Map<String, Map<String, Integer>> selectIndexMap)
            throws SQLException {
        // if use select index (basically ConditionBean) but no select index for the column,
        // the column is not set up in select clause (and selectColumnMap also does not contain it)
        // this determination is to avoid...
        //  o exists duplicate key name, FOO_0 and FOO(_0)
        //  o either is excepted column by SpecifyColumn
        // in this case, selectColumnMap returns true in both column
        // so it determines existence in select clause by this method
        return selectIndexMap != null && !hasLocalSelectIndex(columnDbName, selectIndexMap);
    }

    public static boolean isOutOfRelationSelectIndex(String relationNoSuffix, String columnDbName,
            Map<String, Map<String, Integer>> selectIndexMap) throws SQLException {
        // see comment on the method for local
        return selectIndexMap != null && !hasRelationSelectIndex(relationNoSuffix, columnDbName, selectIndexMap);
    }

    protected static boolean hasLocalSelectIndex(String columnName, Map<String, Map<String, Integer>> selectIndexMap)
            throws SQLException {
        return doHasSelectIndex(SqlClause.BASE_POINT_HANDLING_ENTITY_NO, columnName, selectIndexMap);
    }

    protected static boolean hasRelationSelectIndex(String relationNoSuffix, String columnName,
            Map<String, Map<String, Integer>> selectIndexMap) throws SQLException {
        return doHasSelectIndex(relationNoSuffix, columnName, selectIndexMap);
    }

    protected static boolean doHasSelectIndex(String entityNo, String columnName,
            Map<String, Map<String, Integer>> selectIndexMap) throws SQLException {
        final Map<String, Integer> innerMap = selectIndexMap != null ? selectIndexMap.get(entityNo) : null;
        return innerMap != null && innerMap.containsKey(columnName);
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected ResourceContext _parentContext; // not null only when recursive invoking
    protected BehaviorCommand<?> _behaviorCommand;
    protected DBDef _currentDBDef;
    protected DBMetaProvider _dbmetaProvider;
    protected SqlClauseCreator _sqlClauseCreator;
    protected SqlAnalyzerFactory _sqlAnalyzerFactory;
    protected SQLExceptionHandlerFactory _sqlExceptionHandlerFactory;
    protected GearedCipherManager _gearedCipherManager;
    protected ResourceParameter _resourceParameter;

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _behaviorCommand + ", " + _currentDBDef // core resources
                + ", " + _dbmetaProvider + ", " + _sqlClauseCreator // basic resources
                + ", " + _sqlAnalyzerFactory + ", " + _sqlExceptionHandlerFactory // factories
                + ", " + _gearedCipherManager + ", " + _resourceParameter + "}"; // various
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public ResourceContext getParentContext() {
        return _parentContext;
    }

    public void setParentContext(ResourceContext parentContext) {
        _parentContext = parentContext;
    }

    public BehaviorCommand<?> getBehaviorCommand() {
        return _behaviorCommand;
    }

    public void setBehaviorCommand(BehaviorCommand<?> behaviorCommand) {
        _behaviorCommand = behaviorCommand;
    }

    public DBDef getCurrentDBDef() {
        return _currentDBDef;
    }

    public void setCurrentDBDef(DBDef currentDBDef) {
        _currentDBDef = currentDBDef;
    }

    public DBMetaProvider getDBMetaProvider() {
        return _dbmetaProvider;
    }

    public void setDBMetaProvider(DBMetaProvider dbmetaProvider) {
        _dbmetaProvider = dbmetaProvider;
    }

    public SqlClauseCreator getSqlClauseCreator() {
        return _sqlClauseCreator;
    }

    public void setSqlClauseCreator(SqlClauseCreator sqlClauseCreator) {
        _sqlClauseCreator = sqlClauseCreator;
    }

    public SqlAnalyzerFactory getSqlAnalyzerFactory() {
        return _sqlAnalyzerFactory;
    }

    public void setSqlAnalyzerFactory(SqlAnalyzerFactory sqlAnalyzerFactory) {
        _sqlAnalyzerFactory = sqlAnalyzerFactory;
    }

    public SQLExceptionHandlerFactory getSQLExceptionHandlerFactory() {
        return _sqlExceptionHandlerFactory;
    }

    public void setSQLExceptionHandlerFactory(SQLExceptionHandlerFactory sqlExceptionHandlerFactory) {
        _sqlExceptionHandlerFactory = sqlExceptionHandlerFactory;
    }

    public GearedCipherManager getGearedCipherManager() {
        return _gearedCipherManager;
    }

    public void setGearedCipherManager(GearedCipherManager gearedCipherManager) {
        _gearedCipherManager = gearedCipherManager;
    }

    public ResourceParameter getResourceParameter() {
        return _resourceParameter;
    }

    public void setResourceParameter(ResourceParameter resourceParameter) {
        _resourceParameter = resourceParameter;
    }
}
