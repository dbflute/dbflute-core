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
package org.seasar.dbflute.s2dao.sqlcommand;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.seasar.dbflute.Entity;
import org.seasar.dbflute.bhv.UpdateOption;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.jdbc.StatementConfig;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.resource.InternalMapContext;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;
import org.seasar.dbflute.s2dao.sqlhandler.TnCommandContextHandler;
import org.seasar.dbflute.twowaysql.context.CommandContext;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class TnQueryUpdateDynamicCommand extends TnAbstractQueryDynamicCommand {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected TnBeanMetaData _beanMetaData;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnQueryUpdateDynamicCommand(DataSource dataSource, StatementFactory statementFactory) {
        super(dataSource, statementFactory);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public Object execute(Object[] args) {
        // analyze arguments
        final Entity entity = extractEntityWithCheck(args);
        final ConditionBean cb = extractConditionBeanWithCheck(args);
        final UpdateOption<ConditionBean> option = extractUpdateOptionWithCheck(args);
        prepareStatementConfigOnThreadIfExists(option);

        // arguments for execution (not contains an option)
        final String[] argNames = new String[] { "entity", "pmb" };
        final Class<?>[] argTypes = new Class<?>[] { entity.getClass(), cb.getClass() };
        final Object[] realArgs = new Object[] { entity, cb };

        // prepare context
        final List<TnPropertyType> boundPropTypeList = new ArrayList<TnPropertyType>();
        final CommandContext context;
        {
            final String twoWaySql = buildQueryUpdateTwoWaySql(entity, cb, option, boundPropTypeList);
            if (twoWaySql == null) { // means non-modification
                return 0; // non execute
            }
            context = createCommandContext(twoWaySql, argNames, argTypes, realArgs);
        }

        // execute
        final TnCommandContextHandler handler = createCommandContextHandler(context);
        handler.setExceptionMessageSqlArgs(context.getBindVariables());
        handler.setFirstBoundPropTypeList(boundPropTypeList);
        final int rows = handler.execute(realArgs);
        return Integer.valueOf(rows);
    }

    // ===================================================================================
    //                                                                    Analyze Argument
    //                                                                    ================
    protected Entity extractEntityWithCheck(Object[] args) {
        assertArgument(args);
        final Object firstArg = args[0];
        if (!(firstArg instanceof Entity)) {
            String msg = "The type of first argument should be " + Entity.class + ":";
            msg = msg + " type=" + firstArg.getClass();
            throw new IllegalArgumentException(msg);
        }
        return (Entity) firstArg;
    }

    protected ConditionBean extractConditionBeanWithCheck(Object[] args) {
        assertArgument(args);
        final Object secondArg = args[1];
        if (!(secondArg instanceof ConditionBean)) {
            String msg = "The type of second argument should be " + ConditionBean.class + ":";
            msg = msg + " type=" + secondArg.getClass();
            throw new IllegalArgumentException(msg);
        }
        return (ConditionBean) secondArg;
    }

    protected UpdateOption<ConditionBean> extractUpdateOptionWithCheck(Object[] args) {
        assertArgument(args);
        if (args.length < 3) {
            return null;
        }
        final Object thirdArg = args[2];
        if (thirdArg == null) {
            return null;
        }
        if (!(thirdArg instanceof UpdateOption<?>)) {
            String msg = "The type of third argument should be " + UpdateOption.class + ":";
            msg = msg + " type=" + thirdArg.getClass();
            throw new IllegalArgumentException(msg);
        }
        @SuppressWarnings("unchecked")
        final UpdateOption<ConditionBean> option = (UpdateOption<ConditionBean>) thirdArg;
        return option;
    }

    protected void prepareStatementConfigOnThreadIfExists(UpdateOption<ConditionBean> option) {
        final StatementConfig config = option != null ? option.getUpdateStatementConfig() : null;
        if (config != null) {
            InternalMapContext.setUpdateStatementConfig(config);
        }
    }

    protected void assertArgument(Object[] args) {
        if (args == null || args.length <= 1) {
            String msg = "The arguments should have two argument at least! But:";
            msg = msg + " args=" + (args != null ? args.length : null);
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                           Build SQL
    //                                                                           =========
    /**
     * @param entity The entity for update. (NotNull)
     * @param cb The condition-bean for query. (NotNull)
     * @param option The option of update. (NullAllowed)
     * @param boundPropTypeList The type list of bound property. (NotNull, AlwaysEmpty)
     * @return The two-way SQL of query update. (NullAllowed: if non-modification, return null)
     */
    protected String buildQueryUpdateTwoWaySql(Entity entity, ConditionBean cb,
            final UpdateOption<ConditionBean> option, List<TnPropertyType> boundPropTypeList) {
        final Map<String, Object> columnParameterMap = new LinkedHashMap<String, Object>();
        final DBMeta dbmeta = entity.getDBMeta();
        final Set<String> modifiedPropertyNames = entity.modifiedProperties();
        final List<ColumnInfo> columnInfoList = dbmeta.getColumnInfoList();
        for (final ColumnInfo columnInfo : columnInfoList) {
            if (columnInfo.isOptimisticLock()) {
                continue; // exclusive control columns are processed after here
            }
            final String columnDbName = columnInfo.getColumnDbName();
            if (option != null && option.hasStatement(columnDbName)) {
                columnParameterMap.put(columnDbName, new SqlClause.QueryUpdateSetCalculationHandler() {
                    public String buildStatement(String aliasName) {
                        return option.buildStatement(columnDbName, aliasName);
                    }
                });
                continue;
            }
            final String propertyName = columnInfo.getPropertyName();
            if (modifiedPropertyNames.contains(propertyName)) {
                final Object value = columnInfo.read(entity);
                if (value != null) {
                    columnParameterMap.put(columnDbName, "/*entity." + propertyName + "*/null");

                    // add bound property type
                    final TnPropertyType propertyType = _beanMetaData.getPropertyType(propertyName);
                    boundPropTypeList.add(propertyType);
                } else {
                    // it uses null literal on query
                    // because the SQL analyzer blocks null parameters
                    // (the analyzer should do it for condition-bean)
                    columnParameterMap.put(columnDbName, "null");
                }
                continue;
            }
        }
        if (columnParameterMap.isEmpty()) {
            return null;
        }
        if (dbmeta.hasVersionNo()) {
            final ColumnInfo columnInfo = dbmeta.getVersionNoColumnInfo();
            final String columnDbName = columnInfo.getColumnDbName();
            columnParameterMap.put(columnDbName, new SqlClause.QueryUpdateSetCalculationHandler() {
                public String buildStatement(String aliasName) {
                    // cipher for versionNo is unsupported
                    final ColumnSqlName columnSqlName = columnInfo.getColumnSqlName();
                    return (aliasName != null ? aliasName : "") + columnSqlName + " + 1";
                }
            });
        }
        if (dbmeta.hasUpdateDate()) {
            ColumnInfo columnInfo = dbmeta.getUpdateDateColumnInfo();
            columnInfo.write(entity, ResourceContext.getAccessTimestamp());
            final String columnDbName = columnInfo.getColumnDbName();
            final String propertyName = columnInfo.getPropertyName();
            columnParameterMap.put(columnDbName, "/*entity." + propertyName + "*/null");

            // add bound property type
            boundPropTypeList.add(_beanMetaData.getPropertyType(propertyName));
        }
        if (option != null && option.isQueryUpdateForcedDirectAllowed()) {
            cb.getSqlClause().enableQueryUpdateForcedDirect();
        }
        return cb.getSqlClause().getClauseQueryUpdate(columnParameterMap);
    }

    // ===================================================================================
    //                                                                       Process Title
    //                                                                       =============
    @Override
    protected String getUpdateSQLFailureProcessTitle() {
        return "query update";
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String replace(String text, String fromText, String toText) {
        return Srl.replace(text, fromText, toText);
    }

    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setBeanMetaData(TnBeanMetaData beanMetaData) {
        this._beanMetaData = beanMetaData;
    }
}
