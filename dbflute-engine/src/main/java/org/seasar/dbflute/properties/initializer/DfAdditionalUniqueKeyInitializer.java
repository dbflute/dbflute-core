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
package org.seasar.dbflute.properties.initializer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.Table;
import org.apache.torque.engine.database.model.Unique;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.DfPropertySettingColumnNotFoundException;
import org.seasar.dbflute.exception.DfPropertySettingTableNotFoundException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.properties.DfAdditionalUniqueKeyProperties;

/**
 * The initializer of additional unique key.
 * @author jflute
 * @since 0.9.5.3 (2009/08/01 Saturday)
 */
public class DfAdditionalUniqueKeyInitializer {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfAdditionalUniqueKeyInitializer.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Database _database;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfAdditionalUniqueKeyInitializer(Database database) {
        _database = database;
    }

    // ===================================================================================
    //                                                                 AdditionalUniqueKey
    //                                                                 ===================
    public void initializeAdditionalUniqueKey() {
        _log.info("/=======================================");
        _log.info("...Initializing additional unique keys.");

        final Map<String, Map<String, String>> additionalUniqueKeyMap = getAdditionalUniqueKeyMap();
        final Set<String> uniqueKeyNameSet = additionalUniqueKeyMap.keySet();
        for (String uniqueKeyName : uniqueKeyNameSet) {
            final String tableName = getTableName(uniqueKeyName);
            assertTable(uniqueKeyName, tableName);
            final List<String> columnNameList = getLocalColumnNameList(uniqueKeyName);
            assertColumnList(uniqueKeyName, tableName, columnNameList);
            final Table table = getTable(tableName);
            final Unique unique = new Unique();
            unique.setAdditional(true);
            unique.setName(uniqueKeyName);
            unique.setTable(table);
            for (String columnName : columnNameList) {
                unique.addColumn(columnName);
            }
            table.addUnique(unique);
            showResult(uniqueKeyName, table, columnNameList);
        }
        _log.info("==========/");
    }

    protected void showResult(String uniqueKeyName, Table table, List<String> columnNameList) {
        _log.info("  " + uniqueKeyName);
        if (columnNameList.size() == 1) {
            _log.info("    Add unique key " + table.getTableDbName() + "." + columnNameList.get(0));
        } else {
            _log.info("    Add unique key " + table.getTableDbName() + "." + columnNameList);
        }
    }

    protected DfAdditionalUniqueKeyProperties getProperties() {
        return DfBuildProperties.getInstance().getAdditionalUniqueKeyProperties();
    }

    protected void assertTable(final String primaryKeyName, final String tableName) {
        if (getTable(tableName) != null) {
            return;
        }
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found table by the tableName of additionalUniqueKey.");
        br.addItem("Additional UQ");
        br.addElement(primaryKeyName);
        br.addItem("NotFound Table");
        br.addElement(tableName);
        final String msg = br.buildExceptionMessage();
        throw new DfPropertySettingTableNotFoundException(msg);
    }

    protected void assertColumnList(final String primaryKeyName, final String tableName, List<String> columnNameList) {
        if (getTable(tableName).containsColumn(columnNameList)) {
            return;
        }
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found table by the tableName of additionalUniqueKey.");
        br.addItem("Additional UQ");
        br.addElement(primaryKeyName);
        br.addItem("Table");
        br.addElement(tableName);
        br.addItem("NotFound Column");
        br.addElement(columnNameList);
        final String msg = br.buildExceptionMessage();
        throw new DfPropertySettingColumnNotFoundException(msg);
    }

    protected String getTableName(String primaryKeyName) {
        return getProperties().findTableName(primaryKeyName);
    }

    protected List<String> getLocalColumnNameList(String primaryKeyName) {
        return getProperties().findColumnNameList(primaryKeyName);
    }

    protected Map<String, Map<String, String>> getAdditionalUniqueKeyMap() {
        return getProperties().getAdditionalUniqueKeyMap();
    }

    protected Table getTable(String tableName) {
        return getDatabase().getTable(tableName);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    protected Database getDatabase() {
        return _database;
    }
}