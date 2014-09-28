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

import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.seasar.dbflute.CallbackContext;
import org.seasar.dbflute.Entity;
import org.seasar.dbflute.bhv.SqlStringFilter;
import org.seasar.dbflute.bhv.core.BehaviorCommandMeta;
import org.seasar.dbflute.cbean.cipher.ColumnFunctionCipher;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public abstract class TnAbstractEntityDynamicCommand extends TnAbstractBasicSqlCommand {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The meta data of the bean. (NotNull: after initialization) */
    protected TnBeanMetaData _beanMetaData;

    /** The DB meta of the table. (NotNull: after initialization) */
    protected DBMeta _targetDBMeta;

    /** The array of property name for persistent columns. (NotNull: after initialization) */
    protected String[] _propertyNames;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnAbstractEntityDynamicCommand(DataSource dataSource, StatementFactory statementFactory) {
        super(dataSource, statementFactory);
    }

    // ===================================================================================
    //                                                                        Extract Bean
    //                                                                        ============
    protected Object extractBeanFromArgsChecked(Object[] args) {
        if (args == null || args.length == 0) {
            String msg = "The argument 'args' should not be null or empty.";
            throw new IllegalArgumentException(msg);
        }
        final Object bean = args[0];
        if (bean == null) {
            String msg = "The argument 'args' should have not-null bean at first element.";
            throw new IllegalArgumentException(msg);
        }
        return bean;
    }

    protected List<?> extractBeanListFromBeanChecked(Object bean) {
        final List<?> beanList;
        if (bean instanceof List<?>) {
            beanList = (List<?>) bean;
        } else {
            String msg = "The argument 'args[0]' should be list: " + bean;
            throw new IllegalArgumentException(msg);
        }
        return beanList;
    }

    // ===================================================================================
    //                                                                          Create SQL
    //                                                                          ==========
    protected void checkPrimaryKey() {
        final TnBeanMetaData bmd = _beanMetaData;
        if (bmd.getPrimaryKeySize() == 0) {
            String msg = "The table '" + _targetDBMeta.getTableDbName() + "' should have primary key.";
            throw new IllegalStateException(msg);
        }
    }

    protected Set<String> extractUniqueDrivenPropSet(Object bean) {
        if (bean instanceof Entity) {
            final Set<String> propSet = ((Entity) bean).myuniqueDrivenProperties();
            if (propSet != null && !propSet.isEmpty()) {
                return propSet;
            }
        }
        return null;
    }

    protected void setupUpdateWhere(StringBuilder sb, Set<String> uniqueDrivenPropSet, boolean optimisticLockHandling) {
        final TnBeanMetaData bmd = _beanMetaData;
        sb.append(" where ");
        prepareWherePrimaryKey(sb, uniqueDrivenPropSet);
        if (optimisticLockHandling && bmd.hasVersionNoPropertyType()) {
            final TnPropertyType pt = bmd.getVersionNoPropertyType();
            sb.append(" and ").append(pt.getColumnSqlName()).append(" = ?");
        }
        if (optimisticLockHandling && bmd.hasTimestampPropertyType()) {
            final TnPropertyType pt = bmd.getTimestampPropertyType();
            sb.append(" and ").append(pt.getColumnSqlName()).append(" = ?");
        }
    }

    protected void prepareWherePrimaryKey(StringBuilder sb, Set<String> uniqueDrivenPropSet) {
        final String bindingSuffix = " = ?";
        final String connectorSuffix = " and ";
        if (uniqueDrivenPropSet != null && !uniqueDrivenPropSet.isEmpty()) {
            for (String uniqueProp : uniqueDrivenPropSet) {
                final ColumnSqlName sqlName = _targetDBMeta.findColumnInfo(uniqueProp).getColumnSqlName();
                sb.append(sqlName).append(bindingSuffix).append(connectorSuffix);
            }
        } else { // basically here
            for (int i = 0; i < _beanMetaData.getPrimaryKeySize(); i++) { // never zero loop
                ColumnSqlName sqlName = _beanMetaData.getPrimaryKeySqlName(i);
                sb.append(sqlName).append(bindingSuffix).append(connectorSuffix);
            }
        }
        sb.setLength(sb.length() - connectorSuffix.length()); // for deleting extra ' and '
    }

    // ===================================================================================
    //                                                                       Filter Helper
    //                                                                       =============
    protected String filterExecutedSql(String executedSql) {
        return doFilterExecutedSqlByCallbackFilter(executedSql);
    }

    protected String doFilterExecutedSqlByCallbackFilter(String executedSql) {
        final SqlStringFilter sqlStringFilter = getSqlStringFilter();
        if (sqlStringFilter != null) {
            final BehaviorCommandMeta meta = ResourceContext.behaviorCommand();
            final String filteredSql = sqlStringFilter.filterEntityUpdate(meta, executedSql);
            return filteredSql != null ? filteredSql : executedSql;
        }
        return executedSql;
    }

    protected SqlStringFilter getSqlStringFilter() {
        if (!CallbackContext.isExistSqlStringFilterOnThread()) {
            return null;
        }
        return CallbackContext.getCallbackContextOnThread().getSqlStringFilter();
    }

    // ===================================================================================
    //                                                                       Cipher Helper
    //                                                                       =============
    protected String encryptIfNeeds(String tableDbName, String columnDbName, String valueExp) {
        final ColumnFunctionCipher cipher = ResourceContext.findColumnFunctionCipher(tableDbName, columnDbName);
        return cipher != null ? cipher.encrypt(valueExp) : valueExp;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setBeanMetaData(TnBeanMetaData beanMetaData) {
        _beanMetaData = beanMetaData;
    }

    public void setTargetDBMeta(DBMeta targetDBMeta) {
        _targetDBMeta = targetDBMeta;
    }

    public void setPropertyNames(String[] propertyNames) {
        _propertyNames = propertyNames;
    }
}
