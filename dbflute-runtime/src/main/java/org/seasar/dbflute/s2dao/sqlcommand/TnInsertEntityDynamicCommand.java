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
import java.util.Set;

import javax.sql.DataSource;

import org.seasar.dbflute.Entity;
import org.seasar.dbflute.bhv.InsertOption;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.jdbc.StatementConfig;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.resource.InternalMapContext;
import org.seasar.dbflute.s2dao.identity.TnIdentifierGenerator;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;
import org.seasar.dbflute.s2dao.sqlhandler.TnInsertEntityHandler;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnInsertEntityDynamicCommand extends TnAbstractEntityDynamicCommand {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnInsertEntityDynamicCommand(DataSource dataSource, StatementFactory statementFactory) {
        super(dataSource, statementFactory);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public Object execute(Object[] args) {
        final Object bean = extractBeanFromArgsChecked(args);
        final InsertOption<ConditionBean> option = extractInsertOptionChecked(args);
        prepareStatementConfigOnThreadIfExists(option);

        final TnBeanMetaData bmd = _beanMetaData;
        final TnPropertyType[] propertyTypes = createInsertPropertyTypes(bmd, bean, _propertyNames, option);
        final String sql = filterExecutedSql(createInsertSql(bmd, propertyTypes, option));
        return doExecute(bean, propertyTypes, sql, option);
    }

    protected InsertOption<ConditionBean> extractInsertOptionChecked(Object[] args) {
        if (args.length < 2 || args[1] == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        final InsertOption<ConditionBean> option = (InsertOption<ConditionBean>) args[1];
        return option;
    }

    protected void prepareStatementConfigOnThreadIfExists(InsertOption<ConditionBean> option) {
        final StatementConfig config = option != null ? option.getInsertStatementConfig() : null;
        if (config != null) {
            InternalMapContext.setUpdateStatementConfig(config);
        }
    }

    protected Object doExecute(Object bean, TnPropertyType[] propertyTypes, String sql,
            InsertOption<ConditionBean> option) {
        final TnInsertEntityHandler handler = createInsertEntityHandler(propertyTypes, sql, option);
        final Object[] realArgs = new Object[] { bean };
        handler.setExceptionMessageSqlArgs(realArgs);
        final int rows = handler.execute(realArgs);
        return Integer.valueOf(rows);
    }

    // ===================================================================================
    //                                                                       Insert Column
    //                                                                       =============
    protected TnPropertyType[] createInsertPropertyTypes(TnBeanMetaData bmd, Object bean, String[] propertyNames,
            InsertOption<ConditionBean> option) {
        if (0 == propertyNames.length) {
            String msg = "The property name was not found in the bean: " + bean;
            throw new IllegalStateException(msg);
        }
        final List<TnPropertyType> typeList = new ArrayList<TnPropertyType>();
        final Set<?> modifiedSet = getModifiedPropertyNames(bean);
        final String timestampProp = bmd.getTimestampPropertyName();
        final String versionNoProp = bmd.getVersionNoPropertyName();

        for (int i = 0; i < propertyNames.length; ++i) {
            final TnPropertyType pt = bmd.getPropertyType(propertyNames[i]);
            if (pt.isPrimaryKey()) {
                if (option == null || !option.isPrimaryKeyIdentityDisabled()) {
                    final TnIdentifierGenerator generator = bmd.getIdentifierGenerator(pt.getPropertyName());
                    if (!generator.isSelfGenerate()) {
                        continue;
                    }
                }
                typeList.add(pt);
            } else {
                if (isOptimisticLockProperty(timestampProp, versionNoProp, pt) // OptimisticLock
                        || isSpecifiedProperty(bean, option, modifiedSet, pt)) { // Specified
                    typeList.add(pt);
                }
            }
        }
        if (typeList.isEmpty()) {
            throwEntityInsertPropertyNotFoundException(bmd, bean);
        }
        return (TnPropertyType[]) typeList.toArray(new TnPropertyType[typeList.size()]);
    }

    protected Set<?> getModifiedPropertyNames(Object bean) {
        return _beanMetaData.getModifiedPropertyNames(bean);
    }

    protected boolean isOptimisticLockProperty(String timestampProp, String versionNoProp, TnPropertyType pt) {
        final String propertyName = pt.getPropertyName();
        return propertyName.equalsIgnoreCase(timestampProp) || propertyName.equalsIgnoreCase(versionNoProp);
    }

    protected boolean isSpecifiedProperty(Object bean, InsertOption<ConditionBean> option, Set<?> modifiedSet,
            TnPropertyType pt) {
        if (option != null && option.hasSpecifiedInsertColumn()) { // basically BatchUpdate
            // BatchUpdate's modified properties are translated to specified columns
            // so all BatchUpdate commands are here
            return option.isSpecifiedInsertColumn(pt.getColumnDbName());
        } else { // basically EntityInsert
            if (isEntityCreatedBySelect(bean)) { // e.g. copy insert
                return true; // every column
            } else { // new-created entity: mainly here
                if (option != null && option.xisCompatibleInsertColumnNotNullOnly()) { // for compatible
                    return isNotNullProperty(bean, pt);
                } else { // mainly here
                    return isModifiedProperty(modifiedSet, pt); // process for ModifiedColumnInsert
                }
            }
        }
    }

    protected boolean isEntityCreatedBySelect(Object bean) {
        if (bean instanceof Entity) {
            Entity entity = (Entity) bean;
            return entity.createdBySelect();
        }
        return false;
    }

    protected boolean isNotNullProperty(Object bean, TnPropertyType pt) {
        return pt.getPropertyAccessor().getValue(bean) != null;
    }

    protected boolean isModifiedProperty(Set<?> modifiedSet, TnPropertyType pt) {
        return modifiedSet.contains(pt.getPropertyName());
    }

    protected void throwEntityInsertPropertyNotFoundException(TnBeanMetaData bmd, Object bean) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The insert property of the entity was not found.");
        br.addItem("Advice");
        br.addElement("The entity should have one or more insert properties.");
        br.addElement("For example, an identity-column-only table is unsupported.");
        br.addItem("Table");
        br.addElement(bmd.getTableName());
        br.addItem("Entity");
        br.addElement(bean != null ? bean.getClass() : null);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                          Insert SQL
    //                                                                          ==========
    protected String createInsertSql(TnBeanMetaData bmd, TnPropertyType[] propertyTypes,
            InsertOption<ConditionBean> option) {
        final String tableDbName = _targetDBMeta.getTableDbName();
        final StringBuilder columnSb = new StringBuilder(48);
        final StringBuilder valuesSb = new StringBuilder(48);
        for (int i = 0; i < propertyTypes.length; ++i) {
            final TnPropertyType pt = propertyTypes[i];
            final ColumnSqlName columnSqlName = pt.getColumnSqlName();
            if (i > 0) {
                columnSb.append(", ");
                valuesSb.append(", ");
            }
            columnSb.append(columnSqlName);
            final String columnDbName = pt.getColumnDbName();
            valuesSb.append(encryptIfNeeds(tableDbName, columnDbName, "?"));
        }
        final StringBuilder sb = new StringBuilder(128);
        sb.append("insert into ").append(_targetDBMeta.getTableSqlName());
        sb.append(" (").append(columnSb).append(")");
        sb.append(ln()).append(" values (").append(valuesSb).append(")");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                             Handler
    //                                                                             =======
    protected TnInsertEntityHandler createInsertEntityHandler(TnPropertyType[] boundPropTypes, String sql,
            InsertOption<ConditionBean> option) {
        final TnInsertEntityHandler handler = new TnInsertEntityHandler(_dataSource, _statementFactory, sql,
                _beanMetaData, boundPropTypes);
        handler.setInsertOption(option);
        return handler;
    }
}
