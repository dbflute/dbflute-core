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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.seasar.dbflute.Entity;
import org.seasar.dbflute.XLog;
import org.seasar.dbflute.bhv.UpdateOption;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.exception.VaryingUpdateInvalidColumnSpecificationException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.jdbc.StatementConfig;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.resource.InternalMapContext;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;
import org.seasar.dbflute.s2dao.sqlhandler.TnUpdateEntityHandler;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnUpdateEntityDynamicCommand extends TnAbstractEntityDynamicCommand {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The result for no update as normal execution. */
    private static final Integer NON_UPDATE = Integer.valueOf(1);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _optimisticLockHandling;
    protected boolean _versionNoAutoIncrementOnMemory;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnUpdateEntityDynamicCommand(DataSource dataSource, StatementFactory statementFactory) {
        super(dataSource, statementFactory);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public Object execute(Object[] args) {
        final Object bean = extractBeanFromArgsChecked(args);
        final UpdateOption<ConditionBean> option = extractUpdateOptionChecked(args);
        prepareStatementConfigOnThreadIfExists(option);

        final TnPropertyType[] propertyTypes = createUpdatePropertyTypes(bean, option);
        if (propertyTypes.length == 0) {
            if (isLogEnabled()) {
                log(createNonUpdateLogMessage(bean));
            }
            return getNonUpdateReturn();
        }
        final String sql = filterExecutedSql(createUpdateSql(bean, propertyTypes, option));
        return doExecute(bean, propertyTypes, sql, option);
    }

    protected UpdateOption<ConditionBean> extractUpdateOptionChecked(Object[] args) {
        if (args.length < 2 || args[1] == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        final UpdateOption<ConditionBean> option = (UpdateOption<ConditionBean>) args[1];
        option.xcheckSpecifiedUpdateColumnPrimaryKey();
        return option;
    }

    protected void prepareStatementConfigOnThreadIfExists(UpdateOption<ConditionBean> option) {
        final StatementConfig config = option != null ? option.getUpdateStatementConfig() : null;
        if (config != null) {
            InternalMapContext.setUpdateStatementConfig(config);
        }
    }

    protected Object doExecute(Object bean, TnPropertyType[] propertyTypes, String sql,
            UpdateOption<ConditionBean> option) {
        final TnUpdateEntityHandler handler = createUpdateEntityHandler(propertyTypes, sql, option);
        final Object[] realArgs = new Object[] { bean };
        handler.setExceptionMessageSqlArgs(realArgs);
        final int result = handler.execute(realArgs);
        return Integer.valueOf(result);
    }

    // ===================================================================================
    //                                                                       Update Column
    //                                                                       =============
    protected TnPropertyType[] createUpdatePropertyTypes(Object bean, UpdateOption<ConditionBean> option) {
        final Set<String> modifiedSet = getModifiedPropertyNames(bean);
        final List<TnPropertyType> typeList = new ArrayList<TnPropertyType>();
        final String timestampProp = _beanMetaData.getTimestampPropertyName();
        final String versionNoProp = _beanMetaData.getVersionNoPropertyName();
        final String[] propertyNames = _propertyNames;
        for (int i = 0; i < propertyNames.length; ++i) {
            final TnPropertyType pt = _beanMetaData.getPropertyType(propertyNames[i]);
            if (pt.isPrimaryKey()) {
                continue;
            }
            if (isOptimisticLockProperty(timestampProp, versionNoProp, pt) // OptimisticLock
                    || isSpecifiedProperty(option, modifiedSet, pt) // Specified
                    || isStatementProperty(option, pt)) { // Statement
                typeList.add(pt);
            }
        }
        return typeList.toArray(new TnPropertyType[typeList.size()]);
    }

    protected Set<String> getModifiedPropertyNames(Object bean) {
        return _beanMetaData.getModifiedPropertyNames(bean);
    }

    protected boolean isOptimisticLockProperty(String timestampProp, String versionNoProp, TnPropertyType pt) {
        final String propertyName = pt.getPropertyName();
        return propertyName.equalsIgnoreCase(timestampProp) || propertyName.equalsIgnoreCase(versionNoProp);
    }

    protected boolean isSpecifiedProperty(UpdateOption<ConditionBean> option, Set<?> modifiedSet, TnPropertyType pt) {
        if (option != null && option.hasSpecifiedUpdateColumn()) { // BatchUpdate
            return option.isSpecifiedUpdateColumn(pt.getColumnDbName());
        } else { // EntityUpdate
            return isModifiedProperty(modifiedSet, pt); // process for ModifiedColumnUpdate
        }
    }

    protected boolean isModifiedProperty(Set<?> modifiedSet, TnPropertyType pt) {
        return modifiedSet.contains(pt.getPropertyName());
    }

    protected boolean isStatementProperty(UpdateOption<ConditionBean> option, TnPropertyType pt) {
        return option != null && option.hasStatement(pt.getColumnDbName());
    }

    // ===================================================================================
    //                                                                          Update SQL
    //                                                                          ==========
    /**
     * Create update SQL. The update is by the primary keys or unique keys.
     * @param bean The bean of the entity to update. (NotNull)
     * @param propertyTypes The types of property for update. (NotNull)
     * @param option An option of update. (NullAllowed)
     * @return The update SQL. (NotNull)
     */
    protected String createUpdateSql(Object bean, TnPropertyType[] propertyTypes, UpdateOption<ConditionBean> option) {
        checkPrimaryKey();
        final String tableDbName = _targetDBMeta.getTableDbName();
        final Set<String> uniqueDrivenPropSet = extractUniqueDrivenPropSet(bean);
        final StringBuilder sb = new StringBuilder(96);
        sb.append("update ").append(_targetDBMeta.getTableSqlName()).append(" set ");
        final String versionNoPropertyName = _beanMetaData.getVersionNoPropertyName();
        int columnCount = 0;
        for (TnPropertyType pt : propertyTypes) {
            final String columnDbName = pt.getColumnDbName();
            final ColumnSqlName columnSqlName = pt.getColumnSqlName();
            final String propertyName = pt.getPropertyName();
            if (uniqueDrivenPropSet != null && uniqueDrivenPropSet.contains(propertyName)) {
                if (option != null && option.hasStatement(columnDbName)) {
                    throwUniqueDrivenColumnUpdateStatementException(tableDbName, columnDbName, uniqueDrivenPropSet);
                }
                continue;
            }
            if (columnCount > 0) {
                sb.append(", ");
            }
            ++columnCount;
            if (propertyName.equalsIgnoreCase(versionNoPropertyName)) {
                if (!isVersionNoAutoIncrementOnMemory()) {
                    setupVersionNoAutoIncrementOnQuery(sb, columnSqlName);
                    continue;
                }
            }
            sb.append(columnSqlName).append(" = ");
            final String valueExp;
            if (option != null && option.hasStatement(columnDbName)) {
                final String statement = option.buildStatement(columnDbName);
                valueExp = encryptIfNeeds(tableDbName, columnDbName, statement);
            } else {
                valueExp = encryptIfNeeds(tableDbName, columnDbName, "?");
            }
            sb.append(valueExp);
        }
        sb.append(ln());
        setupUpdateWhere(sb, uniqueDrivenPropSet, _optimisticLockHandling);
        return sb.toString();
    }

    protected void throwUniqueDrivenColumnUpdateStatementException(String tableDbName, String columnDbName,
            Set<String> uniqueDrivenPropSet) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Cannot use the column specified as unique driven as update statement.");
        br.addItem("Table");
        br.addElement(tableDbName);
        br.addItem("Statement Column");
        br.addElement(columnDbName);
        br.addItem("UniqueDriven Properties");
        br.addElement(uniqueDrivenPropSet);
        final String msg = br.buildExceptionMessage();
        throw new VaryingUpdateInvalidColumnSpecificationException(msg);
    }

    protected void setupVersionNoAutoIncrementOnQuery(StringBuilder sb, ColumnSqlName columnSqlName) {
        sb.append(columnSqlName).append(" = ").append(columnSqlName).append(" + 1");
    }

    // ===================================================================================
    //                                                                             Handler
    //                                                                             =======
    protected TnUpdateEntityHandler createUpdateEntityHandler(TnPropertyType[] boundPropTypes, String sql,
            UpdateOption<ConditionBean> option) {
        final TnUpdateEntityHandler handler = newUpdateEntityHandler(boundPropTypes, sql, option);
        handler.setOptimisticLockHandling(_optimisticLockHandling); // [DBFlute-0.8.0]
        handler.setVersionNoAutoIncrementOnMemory(_versionNoAutoIncrementOnMemory);
        handler.setUpdateOption(option);
        return handler;
    }

    protected TnUpdateEntityHandler newUpdateEntityHandler(TnPropertyType[] boundPropTypes, String sql,
            UpdateOption<ConditionBean> option) {
        return new TnUpdateEntityHandler(_dataSource, _statementFactory, sql, _beanMetaData, boundPropTypes);
    }

    // ===================================================================================
    //                                                                  Non Update Message
    //                                                                  ==================
    protected String createNonUpdateLogMessage(final Object bean) {
        final StringBuilder sb = new StringBuilder();
        final String tableDbName = _targetDBMeta.getTableDbName();
        sb.append("...Skipping update because of non-modification: table=").append(tableDbName);
        if (_targetDBMeta.hasPrimaryKey() && (bean instanceof Entity)) {
            final Entity entity = (Entity) bean;
            final Map<String, Object> pkMap = _targetDBMeta.extractPrimaryKeyMap(entity);
            sb.append(", primaryKey=").append(pkMap);
        }
        return sb.toString();
    }

    protected Object getNonUpdateReturn() {
        return NON_UPDATE;
    }

    // ===================================================================================
    //                                                                  Execute Status Log
    //                                                                  ==================
    protected void log(String msg) {
        XLog.log(msg);
    }

    protected boolean isLogEnabled() {
        return XLog.isLogEnabled();
    }

    public void setOptimisticLockHandling(boolean optimisticLockHandling) {
        _optimisticLockHandling = optimisticLockHandling;
    }

    protected boolean isVersionNoAutoIncrementOnMemory() {
        return _versionNoAutoIncrementOnMemory;
    }

    public void setVersionNoAutoIncrementOnMemory(boolean versionNoAutoIncrementOnMemory) {
        _versionNoAutoIncrementOnMemory = versionNoAutoIncrementOnMemory;
    }
}
