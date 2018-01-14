/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute.logic.doc.spolicy;

import java.util.List;
import java.util.function.Function;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.ForeignKey;
import org.apache.torque.engine.database.model.Index;
import org.apache.torque.engine.database.model.Table;
import org.apache.torque.engine.database.model.Unique;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement.DfSPolicyIfPart;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement.DfSPolicyThenClause;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement.DfSPolicyThenPart;
import org.dbflute.logic.doc.spolicy.result.DfSPolicyResult;
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyMiscSecretary;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.1.2 (2016/12/29 Thursday at higashi-ginza)
 */
public class DfSPolicyTableStatementChecker {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSPolicyChecker _spolicyChecker;
    protected final DfSPolicyMiscSecretary _secretary = new DfSPolicyMiscSecretary();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyTableStatementChecker(DfSPolicyChecker spolicyChecker) {
        _spolicyChecker = spolicyChecker;
    }

    // ===================================================================================
    //                                                                    Table Statement
    //                                                                    ================
    public void checkTableStatement(List<DfSPolicyStatement> statementList, DfSPolicyResult result, Table table) {
        for (DfSPolicyStatement statement : statementList) {
            evaluateTableIfClause(statement, result, table);
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
    protected void evaluateTableIfClause(DfSPolicyStatement statement, DfSPolicyResult result, Table table) {
        if (statement.getIfClause().evaluate(ifPart -> isIfTrue(statement, ifPart, table))) {
            evaluateTableThenClause(statement, result, table);
        }
    }

    protected boolean isIfTrue(DfSPolicyStatement statement, DfSPolicyIfPart ifPart, Table table) {
        final String ifItem = ifPart.getIfItem();
        final String ifValue = ifPart.getIfValue();
        final boolean notIfValue = ifPart.isNotIfValue();
        if (ifItem.equalsIgnoreCase("tableName")) {
            return isHitExp(toComparingTableName(table), ifValue) == !notIfValue;
        } else if (ifItem.equalsIgnoreCase("alias")) {
            return isHitExp(table.getAlias(), ifValue) == !notIfValue;
        } else if (ifItem.equalsIgnoreCase("pk_dbType") || ifItem.equalsIgnoreCase("pkDbType")) { // for compatible
            if (table.hasPrimaryKey()) {
                final List<Column> pkList = table.getPrimaryKey();
                for (Column pk : pkList) { // required here (for PK's something)
                    return isHitExp(pk.getDbType(), ifValue) == !notIfValue;
                }
            }
        } else if (ifItem.equalsIgnoreCase("pk_size")) {
            if (table.hasPrimaryKey()) {
                final List<Column> pkList = table.getPrimaryKey();
                for (Column pk : pkList) { // required here (for PK's something)
                    return isHitExp(pk.getColumnSize(), ifValue) == !notIfValue;
                }
            }
        } else if (ifItem.equalsIgnoreCase("pk_dbType_with_size")) {
            if (table.hasPrimaryKey()) {
                final List<Column> pkList = table.getPrimaryKey();
                for (Column pk : pkList) { // required here (for PK's something)
                    return isHitExp(toComparingDbTypeWithSize(pk), ifValue) == !notIfValue;
                }
            }
        } else {
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown if-item: " + ifItem);
        }
        return false;
    }

    // -----------------------------------------------------
    //                                           Then Clause
    //                                           -----------
    protected void evaluateTableThenClause(DfSPolicyStatement statement, DfSPolicyResult result, Table table) {
        final String policy = toPolicy(statement);
        final DfSPolicyThenClause thenClause = statement.getThenClause();
        final String thenTheme = thenClause.getThenTheme();
        if (thenTheme != null) {
            final boolean notThenClause = thenClause.isNotThenTheme();
            final String notOr = notThenClause ? "not " : "";
            if (thenTheme.equalsIgnoreCase("bad") == !notThenClause) {
                result.violate(policy, "The table is no good: " + toTableDisp(table));
            } else if (thenTheme.contains("hasCommonColumn")) {
                if (!table.hasAllCommonColumn() == !notThenClause) {
                    result.violate(policy, "The table should " + notOr + "have common columns: " + toTableDisp(table));
                }
            } else {
                throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown then-clause: " + thenClause);
            }
        } else {
            evaluateColumnThenItemValue(statement, result, table);
        }
    }

    protected void evaluateColumnThenItemValue(DfSPolicyStatement statement, DfSPolicyResult result, Table table) {
        final List<String> violationList = statement.getThenClause().evaluate(thenPart -> {
            return doEvaluateColumnThenItemValue(statement, thenPart, table, actual -> {
                return buildViolation(table, thenPart, actual);
            });
        });
        if (!violationList.isEmpty()) {
            final String policy = toPolicy(statement);
            for (String violation : violationList) {
                result.violate(policy, violation);
            }
        }
    }

    protected String doEvaluateColumnThenItemValue(DfSPolicyStatement statement, DfSPolicyThenPart thenPart, Table table,
            Function<String, String> violationCall) {
        final String thenItem = thenPart.getThenItem();
        final String thenValue = thenPart.getThenValue();
        final boolean notThenValue = thenPart.isNotThenValue();
        if (thenItem.equalsIgnoreCase("tableName")) { // e.g. tableName is prefix:CLS_
            final String tableName = toComparingTableName(table);
            if (!isHitExp(tableName, thenValue) == !notThenValue) {
                return violationCall.apply(tableName);
            }
        } else if (thenItem.equalsIgnoreCase("alias")) { // e.g. alias is suffix:History
            final String alias = table.getAlias();
            if (!isHitExp(alias, thenValue) == !notThenValue) {
                return violationCall.apply(alias);
            }
        } else if (thenItem.equalsIgnoreCase("comment")) { // e.g. comment is contain:SEA
            final String comment = table.getComment();
            if (!isHitExp(comment, thenValue) == !notThenValue) {
                return violationCall.apply(comment);
            }
        } else if (thenItem.equalsIgnoreCase("pkName")) { // e.g. pkName is prefix:PK_
            if (table.hasPrimaryKey()) {
                final Column pk = table.getPrimaryKey().get(0); // same name if compound
                final String pkName = pk.getPrimaryKeyName();
                final String comparingValue = toConstraintComparingValue(table, thenValue);
                if (!isHitExp(pkName, comparingValue) == !notThenValue) {
                    return violationCall.apply(pkName);
                }
            }
        } else if (thenItem.equalsIgnoreCase("fkName")) { // e.g. fkName is prefix:FK_
            for (ForeignKey fk : table.getForeignKeyList()) {
                final String fkName = fk.getName();
                final String comparingValue = toConstraintComparingValue(table, thenValue);
                if (!isHitExp(fkName, comparingValue) == !notThenValue) {
                    return violationCall.apply(fkName);
                }
            }
        } else if (thenItem.equalsIgnoreCase("uniqueName")) { // e.g. uniqueName is prefix:UQ_ 
            for (Unique uq : table.getUniqueList()) {
                final String uqName = uq.getName();
                final String comparingValue = toConstraintComparingValue(table, thenValue);
                if (!isHitExp(uqName, comparingValue)) {
                    return violationCall.apply(uqName);
                }
            }
        } else if (thenItem.equalsIgnoreCase("indexName")) { // e.g. indexName is prefix:IX_ 
            for (Index ix : table.getIndexList()) {
                final String ixName = ix.getName();
                final String comparingValue = toConstraintComparingValue(table, thenValue);
                if (!isHitExp(ixName, comparingValue) == !notThenValue) {
                    return violationCall.apply(ixName);
                }
            }
        } else if (thenItem.equalsIgnoreCase("pk_columnName")) {
            if (table.hasPrimaryKey()) {
                final List<Column> pkList = table.getPrimaryKey();
                for (Column pk : pkList) {
                    final String columnName = pk.getName();
                    if (!isHitExp(columnName, thenValue) == !notThenValue) {
                        return violationCall.apply(columnName);
                    }
                }
            }
        } else if (thenItem.equalsIgnoreCase("pk_dbType") || thenItem.equalsIgnoreCase("pkDbType")) { // for compatible
            if (table.hasPrimaryKey()) {
                final List<Column> pkList = table.getPrimaryKey();
                for (Column pk : pkList) {
                    final String dbType = pk.getDbType();
                    if (!isHitExp(dbType, thenValue) == !notThenValue) {
                        return violationCall.apply(dbType);
                    }
                }
            }
        } else if (thenItem.equalsIgnoreCase("pk_size")) {
            if (table.hasPrimaryKey()) {
                final List<Column> pkList = table.getPrimaryKey();
                for (Column pk : pkList) {
                    final String size = pk.getColumnSize();
                    if (!isHitExp(size, thenValue) == !notThenValue) {
                        return violationCall.apply(size);
                    }
                }
            }
        } else if (thenItem.equalsIgnoreCase("pk_dbType_with_size")) { // e.g. char(3)
            if (table.hasPrimaryKey()) {
                final List<Column> pkList = table.getPrimaryKey();
                for (Column pk : pkList) {
                    final String dbTypeWithSize = toComparingDbTypeWithSize(pk);
                    if (!isHitExp(dbTypeWithSize, thenValue) == !notThenValue) {
                        return violationCall.apply(dbTypeWithSize);
                    }
                }
            }
        } else {
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown then-item: " + thenItem);
        }
        return null; // no violation
    }

    protected String toConstraintComparingValue(Table table, String thenValue) {
        final String tableName = toComparingTableName(table);
        String comparingValue = thenValue;
        comparingValue = Srl.replace(comparingValue, "$$table$$", tableName);
        comparingValue = Srl.replace(comparingValue, "$$Table$$", tableName);
        comparingValue = Srl.replace(comparingValue, "$$TABLE$$", tableName);
        return comparingValue;
    }

    protected String buildViolation(Table table, DfSPolicyThenPart thenPart, String actual) {
        final String thenItem = thenPart.getThenItem();
        final String thenValue = thenPart.getThenValue();
        final String notOr = thenPart.isNotThenValue() ? "not " : "";
        final String columnDisp = toTableDisp(table);
        return "The " + thenItem + " should " + notOr + "be " + thenValue + " but " + actual + ": " + columnDisp;
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected boolean isHitExp(String exp, String hint) {
        return _secretary.isHitExp(exp, hint);
    }

    protected String toComparingTableName(Table table) {
        return _secretary.toComparingTableName(table);
    }

    protected String toComparingDbTypeWithSize(Column column) {
        return _secretary.toComparingDbTypeWithSize(column);
    }

    protected String toTableDisp(Table table) {
        return _secretary.toTableDisp(table);
    }

    protected String toPolicy(DfSPolicyStatement statement) {
        return "table.statement: " + statement.getNativeExp();
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

    protected void throwSchemaPolicyCheckIllegalIfThenStatementException(DfSPolicyStatement statement, String additional) {
        _secretary.throwSchemaPolicyCheckIllegalIfThenStatementException(statement.getNativeExp(), additional);
    }
}
