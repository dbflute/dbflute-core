/*
 * Copyright 2014-2017 the original author or authors.
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
package org.dbflute.logic.doc.policycheck;

import java.util.List;
import java.util.Map;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.ForeignKey;
import org.apache.torque.engine.database.model.Index;
import org.apache.torque.engine.database.model.Table;
import org.apache.torque.engine.database.model.Unique;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.1.2 (2016/12/29 Thursday at higashi-ginza)
 */
public class DfSchemaPolicyTableStatement {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaPolicyMiscSecretary _secretary = new DfSchemaPolicyMiscSecretary();

    // ===================================================================================
    //                                                                    Table Statement
    //                                                                    ================
    public void checkTableStatement(Table table, Map<String, Object> tableMap, DfSchemaPolicyResult result) {
        processTableStatement(table, tableMap, result);
    }

    protected void processTableStatement(Table table, Map<String, Object> tableMap, DfSchemaPolicyResult result) {
        @SuppressWarnings("unchecked")
        final List<String> statementList = (List<String>) tableMap.get("statementList");
        if (statementList != null) {
            for (String statement : statementList) {
                evaluateTableIfClause(table, statement, result, _secretary.extractIfClause(statement));
            }
        }
    }

    // ===================================================================================
    //                                                                            Evaluate
    //                                                                            ========
    // -----------------------------------------------------
    //                                             If Clause
    //                                             ---------
    // e.g.
    //  if tableName is suffix:_ID then bad
    //  if tableName is suffix:_HISTORY then pkDbType is bigint
    protected void evaluateTableIfClause(Table table, String statement, DfSchemaPolicyResult result, DfSchemaPolicyIfClause ifClause) {
        // #hope if tableName is ... and pkDbType is ... then ... by jflute (2016/12/29)
        final String ifItem = ifClause.getIfItem();
        final String ifValue = ifClause.getIfValue();
        final boolean notIfValue = ifClause.isNotIfValue();
        if (ifItem.equalsIgnoreCase("tableName")) {
            if (isHitTable(toTableName(table), ifValue) == !notIfValue) {
                evaluateTableThenClause(table, statement, result, ifClause);
            }
        } else if (ifItem.equalsIgnoreCase("alias")) {
            if (isHitTable(table.getAlias(), ifValue) == !notIfValue) {
                evaluateTableThenClause(table, statement, result, ifClause);
            }
        } else if (ifItem.equalsIgnoreCase("pkDbType")) { // e.g. if pkDbType is char
            if (table.hasPrimaryKey()) {
                final List<Column> pkList = table.getPrimaryKey();
                for (Column pk : pkList) {
                    if (isHitTable(pk.getDbType(), ifValue) == !notIfValue) {
                        evaluateTableThenClause(table, statement, result, ifClause);
                    }
                }
            }
        } else {
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown if-item: " + ifItem);
        }
    }

    // -----------------------------------------------------
    //                                           Then Clause
    //                                           -----------
    protected void evaluateTableThenClause(Table table, String statement, DfSchemaPolicyResult result, DfSchemaPolicyIfClause ifClause) {
        final String policy = toPolicy(ifClause);
        final String thenClause = ifClause.getThenClause();
        if (ifClause.getThenItem() != null) { // e.g. dbType is integer
            evaluateTableThenItemValue(table, statement, result, ifClause);
        } else {
            final boolean notThenClause = ifClause.isNotThenClause();
            final String notOr = notThenClause ? "not " : "";
            if (thenClause.equalsIgnoreCase("bad") == !notThenClause) { // "not bad" is non-sense
                result.addViolation(policy, "The table is no good: " + toTableDisp(table));
            } else if (thenClause.contains("hasCommonColumn")) {
                if (!table.hasAllCommonColumn() == !notThenClause) {
                    result.addViolation(policy, "The table should " + notOr + "have common columns: " + toTableDisp(table));
                }
            } else {
                throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown then-clause: " + thenClause);
            }
        }
    }

    protected void evaluateTableThenItemValue(Table table, String statement, DfSchemaPolicyResult result, DfSchemaPolicyIfClause ifClause) {
        final String policy = toPolicy(ifClause);
        final String thenItem = ifClause.getThenItem();
        final String thenValue = ifClause.getThenValue();
        final boolean notThenValue = ifClause.isNotThenValue();
        final String notOr = notThenValue ? "not " : "";
        if (thenItem.equalsIgnoreCase("tableName")) { // e.g. tableName is prefix:CLS_
            final String tableName = toTableName(table);
            if (!isHitExp(tableName, thenValue) == !notThenValue) {
                result.addViolation(policy,
                        "The table name should " + notOr + "be " + thenValue + " but " + tableName + ": " + toTableDisp(table));
            }
        } else if (thenItem.equalsIgnoreCase("alias")) { // e.g. alias is suffix:History
            if (table.hasAlias()) {
                final String alias = table.getAlias();
                if (!isHitExp(alias, thenValue) == !notThenValue) {
                    result.addViolation(policy,
                            "The table alias should " + notOr + "be " + thenValue + " but " + alias + ": " + toTableDisp(table));
                }
            }
        } else if (thenItem.equalsIgnoreCase("comment")) { // e.g. comment is contain:SEA
            if (table.hasAlias()) {
                final String comment = table.getComment();
                if (!isHitExp(comment, thenValue) == !notThenValue) {
                    result.addViolation(policy,
                            "The table comment should " + notOr + "be " + thenValue + " but " + comment + ": " + toTableDisp(table));
                }
            }
        } else if (thenItem.equalsIgnoreCase("pkDbType")) { // e.g. pkDbType is char
            if (table.hasPrimaryKey()) {
                final List<Column> pkList = table.getPrimaryKey();
                for (Column pk : pkList) {
                    final String pkDbName = pk.getDbType();
                    if (!isHitExp(pkDbName, thenValue) == !notThenValue) {
                        result.addViolation(policy, "The PK column DB type should " + notOr + "be " + thenValue + " but " + pkDbName + ": "
                                + toTableDisp(table));
                    }
                }
            }
        } else if (thenItem.equalsIgnoreCase("pkName")) { // e.g. pkName is prefix:PK_
            if (table.hasPrimaryKey()) {
                final Column pk = table.getPrimaryKey().get(0); // same name if compound
                final String pkName = pk.getPrimaryKeyName();
                final String comparingValue = toConstraintComparingValue(table, thenValue);
                if (!isHitExp(pkName, comparingValue) == !notThenValue) {
                    result.addViolation(policy, "The PK constraint name should " + notOr + "be " + comparingValue + " but " + pkName + ": "
                            + toTableDisp(table));
                }
            }
        } else if (thenItem.equalsIgnoreCase("fkName")) { // e.g. fkName is prefix:FK_
            for (ForeignKey fk : table.getForeignKeyList()) {
                final String fkName = fk.getName();
                final String comparingValue = toConstraintComparingValue(table, thenValue);
                if (!isHitExp(fkName, comparingValue) == !notThenValue) {
                    result.addViolation(policy, "The FK constraint name should " + notOr + "be " + comparingValue + " but " + fkName + ": "
                            + toTableDisp(table));
                }
            }
        } else if (thenItem.equalsIgnoreCase("uniqueName")) { // e.g. uniqueName is prefix:UQ_ 
            for (Unique uq : table.getUniqueList()) {
                final String uqName = uq.getName();
                final String comparingValue = toConstraintComparingValue(table, thenValue);
                if (!isHitExp(uqName, comparingValue)) {
                    result.addViolation(policy, "The unique constraint name should " + notOr + "be " + comparingValue + " but " + uqName
                            + ": " + toTableDisp(table));
                }
            }
        } else if (thenItem.equalsIgnoreCase("indexName")) { // e.g. indexName is prefix:IX_ 
            for (Index ix : table.getIndexList()) {
                final String ixName = ix.getName();
                final String comparingValue = toConstraintComparingValue(table, thenValue);
                if (!isHitExp(ixName, comparingValue) == !notThenValue) {
                    result.addViolation(policy,
                            "The index name should " + notOr + "be " + comparingValue + " but " + ixName + ": " + toTableDisp(table));
                }
            }
        } else {
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown then-item: " + thenItem);
        }
    }

    protected String toConstraintComparingValue(Table table, String thenValue) {
        final String tableName = toTableName(table);
        String comparingValue = thenValue;
        comparingValue = Srl.replace(comparingValue, "$$table$$", tableName);
        comparingValue = Srl.replace(comparingValue, "$$Table$$", tableName);
        comparingValue = Srl.replace(comparingValue, "$$TABLE$$", tableName);
        return comparingValue;
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected boolean isHitTable(String tableName, String hint) {
        return _secretary.isHitTable(tableName, hint);
    }

    protected boolean isHitExp(String exp, String hint) {
        return _secretary.isHitExp(exp, hint);
    }

    protected String toTableName(Table table) {
        return _secretary.toTableName(table);
    }

    protected String toTableDisp(Table table) {
        return _secretary.toTableDisp(table);
    }

    protected String toPolicy(DfSchemaPolicyIfClause ifClause) {
        return "table.statement: " + ifClause.getStatement();
    }

    // ===================================================================================
    //                                                                           Exception
    //                                                                           =========
    protected void throwSchemaPolicyCheckUnknownThemeException(String theme, String targetType) {
        _secretary.throwSchemaPolicyCheckUnknownThemeException(theme, targetType);
    }

    protected void throwSchemaPolicyCheckUnknownPropertyException(String property) {
        _secretary.throwSchemaPolicyCheckUnknownPropertyException(property);
    }

    protected void throwSchemaPolicyCheckIllegalIfThenStatementException(String statement, String additional) {
        _secretary.throwSchemaPolicyCheckIllegalIfThenStatementException(statement, additional);
    }
}
