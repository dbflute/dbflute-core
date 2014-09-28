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
package org.seasar.dbflute.dbmeta.alter;

import java.math.BigDecimal;
import java.util.Map;

import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.info.ForeignInfo;

/**
 * @author jflute
 */
public class AlterGeneratorMySql extends AbstractAlterGenerator {

    // ===================================================================================
    //                                                                               Table
    //                                                                               =====
    // alter table [old_table_name] rename to [new_table_name]
    public String generateTableRename(String oldTableName, DBMeta newMeta) {
        final StringBuilder sb = new StringBuilder();
        doBuildAlterTable(sb, oldTableName);
        sb.append(" rename to ").append(newMeta.getTableSqlName());
        return sb.toString();
    }

    // alter table [old_table_name] rename to [new_table_name]
    public String generateTableRename(DBMeta oldMeta, String newTableName) {
        final StringBuilder sb = new StringBuilder();
        doBuildAlterTable(sb, oldMeta);
        sb.append(" rename to ").append(newTableName);
        return sb.toString();
    }

    // ===================================================================================
    //                                                                              Column
    //                                                                              ======
    // alter table [table_name] modify [column_name] [column_type] [column_constraints]
    public String generateColumnDefChange(ColumnInfo columnInfo) {
        final StringBuilder sb = new StringBuilder();
        doBuildAlterTable(sb, columnInfo.getDBMeta());
        sb.append(" modify ").append(columnInfo.getColumnSqlName());
        doBuildColumnDef(sb, columnInfo, null);
        return sb.toString();
    }

    // alter table [table_name] modify [column_name] [column_type] [column_constraints] after [after-column-name]
    public String generateColumnDefChange(ColumnInfo columnInfo, String afterColumnName) { // MySQL original
        final StringBuilder sb = new StringBuilder();
        doBuildAlterTable(sb, columnInfo.getDBMeta());
        sb.append(" modify ").append(columnInfo.getColumnSqlName());
        doBuildColumnDef(sb, columnInfo, afterColumnName);
        return sb.toString();
    }

    // alter table [table_name] drop column [column_name]
    public String generateColumnDrop(ColumnInfo columnInfo) {
        return generateColumnDrop(columnInfo.getDBMeta(), columnInfo.getColumnSqlName().toString());
    }

    // alter table [table_name] drop column [column_name]
    public String generateColumnDrop(DBMeta dbmeta, String columnName) {
        final StringBuilder sb = new StringBuilder();
        doBuildAlterTable(sb, dbmeta);
        sb.append(" drop column ").append(columnName);
        return sb.toString();
    }

    // alter table [table_name] change column [column_name] [column_type] [column_constraints]
    public String generateColumnRename(String oldColumnName, ColumnInfo newColumnInfo) {
        final StringBuilder sb = new StringBuilder();
        doBuildAlterTable(sb, newColumnInfo.getDBMeta());
        sb.append(" change column ").append(oldColumnName);
        sb.append(" ").append(newColumnInfo.getColumnSqlName());
        doBuildColumnDef(sb, newColumnInfo, null);
        return sb.toString();
    }

    // alter table [table_name] change column [column_name] [column_type] [column_constraints]
    public String generateColumnRename(ColumnInfo oldColumnInfo, String newColumnName) {
        final StringBuilder sb = new StringBuilder();
        doBuildAlterTable(sb, oldColumnInfo.getDBMeta());
        sb.append(" change column ").append(oldColumnInfo.getColumnSqlName());
        sb.append(" ").append(newColumnName);
        doBuildColumnDef(sb, oldColumnInfo, null);
        return sb.toString();
    }

    protected void doBuildColumnDef(StringBuilder sb, ColumnInfo columnInfo, String afterColumnName) {
        sb.append(" ").append(columnInfo.getColumnDbType());
        final Integer columnSize = columnInfo.getColumnSize();
        final Integer decimalDigits = columnInfo.getDecimalDigits();
        if (columnSize != null && needsColumnSize(columnInfo)) {
            sb.append("(").append(columnSize);
            if (decimalDigits != null && decimalDigits > 0) {
                sb.append(", ").append(decimalDigits);
            }
            sb.append(")");
        }
        if (columnInfo.isNotNull()) {
            sb.append(" NOT NULL");
        }
        if (columnInfo.isAutoIncrement()) {
            sb.append(" AUTO_INCREMENT");
        } else {
            final String defaultValue = columnInfo.getDefaultValue();
            if (defaultValue != null) {
                if (columnInfo.isObjectNativeTypeString()) {
                    sb.append(" DEFAULT '").append(defaultValue).append("'");
                } else {
                    sb.append(" DEFAULT ").append(defaultValue);
                }
            }
        }
        if (afterColumnName != null) {
            sb.append(" after ").append(afterColumnName);
        }
    }

    protected boolean needsColumnSize(ColumnInfo columnInfo) {
        final boolean decimalType = BigDecimal.class.isAssignableFrom(columnInfo.getObjectNativeType());
        if (columnInfo.isObjectNativeTypeNumber() && !decimalType) {
            return false;
        }
        if (columnInfo.isObjectNativeTypeDate()) {
            return false;
        }
        if (columnInfo.getColumnDbType().toLowerCase().endsWith("text")) {
            return false;
        }
        return true;
    }

    // ===================================================================================
    //                                                                         Primary Key
    //                                                                         ===========
    // alter table [table_name] add primary key ([column_names...])
    public String generatePrimaryKeyAdd(ColumnInfo columnInfo) {
        final StringBuilder sb = new StringBuilder();
        doBuildAlterTable(sb, columnInfo.getDBMeta());
        sb.append(" add primary key (").append(columnInfo.getColumnSqlName()).append(")");
        return sb.toString();
    }

    // alter table [table_name] drop primary key
    public String generatePrimaryKeyDrop(DBMeta dbmeta) {
        final StringBuilder sb = new StringBuilder();
        doBuildAlterTable(sb, dbmeta);
        sb.append(" drop primary key");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                         Foreign Key
    //                                                                         ===========
    // alter table [table_name] add constraint [constraint_name] foreign key ([column_names...])
    //  references [foreign_table_name]([foreign_column_names...])
    public String generateForeignKeyAdd(ForeignInfo foreignInfo) {
        if (!foreignInfo.isPureFK()) {
            String msg = "The foreign info should be from pure FK: " + foreignInfo;
            throw new IllegalArgumentException(msg);
        }
        final StringBuilder sb = new StringBuilder();
        doBuildAlterTable(sb, foreignInfo.getLocalDBMeta());
        sb.append(" add constraint ").append(foreignInfo.getConstraintName());
        sb.append(" foreign key (");
        final Map<ColumnInfo, ColumnInfo> columnInfoMap = foreignInfo.getLocalForeignColumnInfoMap();
        {
            int index = 0;
            for (ColumnInfo columnInfo : columnInfoMap.keySet()) {
                if (index > 0) {
                    sb.append(", ");
                }
                sb.append(columnInfo.getColumnSqlName());
                ++index;
            }
        }
        sb.append(") references ").append(foreignInfo.getForeignDBMeta().getTableSqlName()).append("(");
        {
            int index = 0;
            for (ColumnInfo columnInfo : columnInfoMap.values()) {
                if (index > 0) {
                    sb.append(", ");
                }
                sb.append(columnInfo.getColumnSqlName());
                ++index;
            }
        }
        sb.append(")");
        return sb.toString();
    }

    // alter table [table_name] drop foreign key [FK_constraint_name]
    public String generateForeignKeyDrop(ForeignInfo foreignInfo) {
        if (!foreignInfo.isPureFK()) {
            String msg = "The foreign info should be from pure FK: " + foreignInfo;
            throw new IllegalArgumentException(msg);
        }
        final StringBuilder sb = new StringBuilder();
        doBuildAlterTable(sb, foreignInfo.getLocalDBMeta());
        sb.append(" drop foreign key ").append(foreignInfo.getConstraintName());
        return sb.toString();
    }
}
