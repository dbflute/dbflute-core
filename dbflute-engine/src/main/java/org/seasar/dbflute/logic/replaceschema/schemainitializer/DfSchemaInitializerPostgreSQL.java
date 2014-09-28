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
package org.seasar.dbflute.logic.replaceschema.schemainitializer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.helper.StringSet;
import org.seasar.dbflute.helper.jdbc.facade.DfJdbcFacade;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureColumnMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureColumnMeta.DfProcedureColumnType;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTableMeta;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfSchemaInitializerPostgreSQL extends DfSchemaInitializerJdbc {

    private static final Log _log = LogFactory.getLog(DfSchemaInitializerPostgreSQL.class);

    // ===================================================================================
    //                                                                    Drop Foreign Key
    //                                                                    ================
    @Override
    protected String filterDropForeignKeyName(String foreignKeyName) {
        if (needsQuotedForeignKeyName(foreignKeyName)) {
            return Srl.quoteDouble(foreignKeyName);
        }
        return foreignKeyName;
    }

    protected boolean needsQuotedForeignKeyName(String foreignKeyName) {
        final char[] charArray = foreignKeyName.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if (Character.isUpperCase(charArray[i])) { // e.g. Fk_Vendor_ForeignKey_NAME_CaseCrisis
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                          Drop Table
    //                                                                          ==========
    @Override
    protected List<DfTableMeta> prepareSortedTableList(Connection conn, List<DfTableMeta> viewList,
            List<DfTableMeta> otherList) {
        // order for inherit tables
        final List<Map<String, String>> resultList = selectInheritList(conn);
        final Set<String> childSet = StringSet.createAsCaseInsensitive();
        for (Map<String, String> elementMap : resultList) {
            childSet.add(elementMap.get("child_name"));
        }
        final Set<String> parentSet = StringSet.createAsCaseInsensitive();
        for (Map<String, String> elementMap : resultList) {
            parentSet.add(elementMap.get("parent_name"));
        }
        final List<DfTableMeta> firstPriorityList = new ArrayList<DfTableMeta>();
        final List<DfTableMeta> secondPriorityList = new ArrayList<DfTableMeta>();
        final List<DfTableMeta> thirdPriorityList = new ArrayList<DfTableMeta>();
        for (DfTableMeta meta : otherList) {
            final String tableDbName = meta.getTableDbName();
            if (childSet.contains(tableDbName)) {
                if (!parentSet.contains(tableDbName)) { // both inherited and inherits
                    secondPriorityList.add(meta);
                } else { // inherits any table
                    firstPriorityList.add(meta);
                }
            } else { // no inheritance
                thirdPriorityList.add(meta);
            }
        }
        final List<DfTableMeta> sortedList = new ArrayList<DfTableMeta>();
        sortedList.addAll(viewList); // should be before dropping reference table
        sortedList.addAll(firstPriorityList);
        sortedList.addAll(secondPriorityList);
        sortedList.addAll(thirdPriorityList);
        return sortedList;
    }

    protected List<Map<String, String>> selectInheritList(Connection conn) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select rits.inhrelid, child_cls.relname as child_name");
        sb.append(", rits.inhparent, parent_cls.relname as parent_name, inhseqno");
        sb.append(" from pg_inherits rits");
        sb.append(" left outer join pg_class child_cls on inhrelid = oid");
        sb.append(" left outer join pg_class parent_cls on inhparent = oid");
        final String sql = sb.toString();
        final List<String> colList = Arrays.asList("inhrelid", "child_name", "inhparent", "parent_name", "inhseqno");
        final DfJdbcFacade jdbcFacade = new DfJdbcFacade(conn);
        try {
            return jdbcFacade.selectStringList(sql, colList);
        } catch (RuntimeException continued) {
            _log.info("*Failed to select pg_inherits for priority of dropping table: " + continued.getMessage());
            return DfCollectionUtil.emptyList();
        }
    }

    // ===================================================================================
    //                                                                       Drop Sequence
    //                                                                       =============
    @Override
    protected void dropSequence(Connection conn, List<DfTableMeta> tableMetaList) {
        final String catalog = _unifiedSchema.existsPureCatalog() ? _unifiedSchema.getPureCatalog() : null;
        final String schema = _unifiedSchema.getPureSchema();
        final List<String> sequenceNameList = new ArrayList<String>();
        final DfJdbcFacade jdbcFacade = new DfJdbcFacade(conn);
        final String sequenceColumnName = "sequence_name";
        final StringBuilder sb = new StringBuilder();
        sb.append("select ").append(sequenceColumnName).append(" from information_schema.sequences");
        sb.append(" where ");
        if (Srl.is_NotNull_and_NotTrimmedEmpty(catalog)) {
            sb.append("sequence_catalog = '").append(catalog).append("'").append(" and ");
        }
        sb.append("sequence_schema = '").append(schema).append("'");
        final String sql = sb.toString();
        final List<String> sequenceColumnList = Arrays.asList(sequenceColumnName);
        final List<Map<String, String>> resultList = jdbcFacade.selectStringList(sql, sequenceColumnList);
        for (Map<String, String> recordMap : resultList) {
            sequenceNameList.add(recordMap.get(sequenceColumnName));
        }
        for (String sequenceName : sequenceNameList) {
            if (isSequenceExcept(sequenceName)) {
                continue;
            }
            final String sequenceSqlName = _unifiedSchema.buildSqlName(sequenceName);
            final String dropSequenceSql = "drop sequence " + sequenceSqlName;
            logReplaceSql(dropSequenceSql);
            jdbcFacade.execute(dropSequenceSql);
        }
    }

    // ===================================================================================
    //                                                                      Drop Procedure
    //                                                                      ==============
    @Override
    protected String buildProcedureSqlName(DfProcedureMeta metaInfo) {
        final String expression = "(" + buildProcedureArgExpression(metaInfo) + ")";
        return super.buildProcedureSqlName(metaInfo) + expression;
    }

    @Override
    protected boolean isDropFunctionFirst() {
        return true; // because PostgreSQL supports function only
    }

    protected String buildProcedureArgExpression(DfProcedureMeta metaInfo) {
        final List<DfProcedureColumnMeta> metaInfoList = metaInfo.getProcedureColumnList();
        final StringBuilder sb = new StringBuilder();
        for (DfProcedureColumnMeta columnMetaInfo : metaInfoList) {
            final String dbTypeName = columnMetaInfo.getDbTypeName();
            final String columnName = columnMetaInfo.getColumnName();
            final DfProcedureColumnType columnType = columnMetaInfo.getProcedureColumnType();
            if (DfProcedureColumnType.procedureColumnReturn.equals(columnType)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(columnName);
            if (DfProcedureColumnType.procedureColumnIn.equals(columnType)) {
                sb.append(" in ");
            } else if (DfProcedureColumnType.procedureColumnOut.equals(columnType)) {
                sb.append(" out ");
            } else if (DfProcedureColumnType.procedureColumnInOut.equals(columnType)) {
                sb.append(" inout ");
            } else {
                sb.append(" ");
            }
            sb.append(dbTypeName);
        }
        return sb.toString();
    }
}