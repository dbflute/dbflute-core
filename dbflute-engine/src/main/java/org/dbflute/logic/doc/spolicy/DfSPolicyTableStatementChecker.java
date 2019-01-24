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
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyFirstDateSecretary;
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyLogicalSecretary;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.1.2 (2016/12/29 Thursday at higashi-ginza)
 */
public class DfSPolicyTableStatementChecker {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String ADDITIONAL_SUFFIX = "(additional)";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSPolicyFirstDateSecretary _firstDateSecretary;
    protected final DfSPolicyLogicalSecretary _logicalSecretary;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyTableStatementChecker(DfSPolicyFirstDateSecretary firstDateSecretary, DfSPolicyLogicalSecretary logicalSecretary) {
        _firstDateSecretary = firstDateSecretary;
        _logicalSecretary = logicalSecretary;
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
    //                                                                           If Clause
    //                                                                           =========
    // -----------------------------------------------------
    //                                              Evaluate
    //                                              --------
    // e.g.
    //  if tableName is suffix:_ID then bad
    //  if tableName is suffix:_HISTORY then pkDbType is bigint
    protected void evaluateTableIfClause(DfSPolicyStatement statement, DfSPolicyResult result, Table table) {
        if (statement.getIfClause().evaluate(ifPart -> isIfTrue(statement, ifPart, table))) {
            evaluateTableThenClause(statement, result, table);
        }
    }

    // -----------------------------------------------------
    //                                         If Item-Value
    //                                         -------------
    protected boolean isIfTrue(DfSPolicyStatement statement, DfSPolicyIfPart ifPart, Table table) {
        final String ifItem = ifPart.getIfItem();
        final String ifValue = ifPart.getIfValue();
        final boolean notIfValue = ifPart.isNotIfValue();
        if (ifItem.equalsIgnoreCase("tableName")) { // if tableName is ...
            final String comparingTableName = toComparingTableName(table);
            final String comparingIfValue = toTableNameComparingIfValue(table, ifValue);
            return isHitExp(statement, comparingTableName, comparingIfValue) == !notIfValue;
        } else if (ifItem.equalsIgnoreCase("alias")) { // if alias is ...
            final String comparingIfValue = toAliasComparingIfValue(table, ifValue);
            return isHitExp(statement, table.getAlias(), comparingIfValue) == !notIfValue;
        } else if (ifItem.equalsIgnoreCase("firstDate")) { // if firstDate is after:2018/05/03
            return determineFirstDateIfValue(statement, ifValue, notIfValue, table);
        } else if (ifItem.equalsIgnoreCase("pk_columnName")) {
            return determinePkSomethingIfValue(statement, table, ifValue, notIfValue, pk -> toComparingColumnName(pk));
        } else if (ifItem.equalsIgnoreCase("pk_dbType") || ifItem.equalsIgnoreCase("pkDbType")) { // for compatible
            return determinePkSomethingIfValue(statement, table, ifValue, notIfValue, pk -> pk.getDbType());
        } else if (ifItem.equalsIgnoreCase("pk_size")) {
            return determinePkSomethingIfValue(statement, table, ifValue, notIfValue, pk -> pk.getColumnSize());
        } else if (ifItem.equalsIgnoreCase("pk_dbType_with_size")) {
            return determinePkSomethingIfValue(statement, table, ifValue, notIfValue, pk -> toComparingDbTypeWithSize(pk));
        } else {
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown if-item: " + ifItem);
        }
        return false;
    }

    protected boolean determineFirstDateIfValue(DfSPolicyStatement statement, String ifValue, boolean notIfValue, Table table) {
        return _firstDateSecretary.determineTableFirstDate(statement, ifValue, notIfValue, table);
    }

    protected boolean determinePkSomethingIfValue(DfSPolicyStatement statement, Table table, String ifValue, boolean notIfValue,
            Function<Column, String> valueProvider) {
        if (table.hasPrimaryKey()) {
            final List<Column> pkList = table.getPrimaryKey();
            for (Column pk : pkList) { // required here (for PK's something)
                return isHitExp(statement, valueProvider.apply(pk), ifValue) == !notIfValue;
            }
        }
        return false;
    }

    protected String toTableNameComparingIfValue(Table table, String ifValue) {
        return convertToTableNameComparingValue(table, ifValue);
    }

    protected String toAliasComparingIfValue(Table table, String ifValue) {
        return convertToAliasComparingValue(table, ifValue);
    }

    // ===================================================================================
    //                                                                         Then Clause
    //                                                                         ===========
    // -----------------------------------------------------
    //                                              Evaluate
    //                                              --------
    protected void evaluateTableThenClause(DfSPolicyStatement statement, DfSPolicyResult result, Table table) {
        final String thenTheme = statement.getThenClause().getThenTheme();
        if (thenTheme != null) {
            evaluateTableThenTheme(statement, result, table);
        } else {
            evaluateTableThenItemValue(statement, result, table);
        }
    }

    // -----------------------------------------------------
    //                                            Then Theme
    //                                            ----------
    protected void evaluateTableThenTheme(DfSPolicyStatement statement, DfSPolicyResult result, Table table) {
        final String policy = toPolicy(statement);
        final DfSPolicyThenClause thenClause = statement.getThenClause();
        final String thenTheme = thenClause.getThenTheme(); // already not null here
        final boolean notThenClause = thenClause.isNotThenTheme();
        final String notOr = notThenClause ? "not " : "";
        if (thenTheme.equalsIgnoreCase("bad")) {
            if (notThenClause) {
                throwSchemaPolicyCheckIllegalThenNotThemeException(statement, "bad");
            } else {
                result.violate(policy, "The table is no good: " + toTableDisp(table));
            }
        } else if (thenTheme.contains("hasPK")) {
            if (!table.hasPrimaryKey() == !notThenClause) {
                result.violate(policy, "The table should " + notOr + "have primary key: " + toTableDisp(table));
            }
        } else if (thenTheme.contains("upperCaseBasis")) {
            if (Srl.isLowerCaseAny(toComparingTableName(table)) == !notThenClause) {
                result.violate(policy, "The table name should " + notOr + "be on upper case basis: " + toTableDisp(table));
            }
        } else if (thenTheme.contains("lowerCaseBasis")) {
            if (Srl.isUpperCaseAny(toComparingTableName(table)) == !notThenClause) {
                result.violate(policy, "The table name should " + notOr + "be on lower case basis: " + toTableDisp(table));
            }
        } else if (thenTheme.contains("identityIfPureIDPK")) {
            if (_logicalSecretary.isNotIdentityIfPureIDPK(table) == !notThenClause) {
                result.violate(policy, "The primary key should " + notOr + "be identity: " + toTableDisp(table));
            }
        } else if (thenTheme.contains("sequenceIfPureIDPK")) {
            if (_logicalSecretary.isNotSequenceIfPureIDPK(table) == !notThenClause) {
                result.violate(policy, "The primary key should " + notOr + "be sequence: " + toTableDisp(table));
            }
        } else if (thenTheme.contains("hasCommonColumn")) {
            if (!table.hasAllCommonColumn() == !notThenClause) {
                result.violate(policy, "The table should " + notOr + "have common columns: " + toTableDisp(table));
            }
        } else if (thenTheme.contains("hasAlias")) {
            if (!table.hasAlias() == !notThenClause) {
                result.violate(policy, "The table should " + notOr + "have table alias: " + toTableDisp(table));
            }
        } else if (thenTheme.contains("hasComment")) {
            if (!table.hasComment() == !notThenClause) {
                result.violate(policy, "The table should " + notOr + "have table comment: " + toTableDisp(table));
            }
        } else {
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown then-clause: " + thenClause);
        }
    }

    // -----------------------------------------------------
    //                                       Then Item-Value
    //                                       ---------------
    protected void evaluateTableThenItemValue(DfSPolicyStatement statement, DfSPolicyResult result, Table table) {
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
            if (!isHitExp(statement, tableName, toTableNameComparingThenValue(table, thenValue)) == !notThenValue) {
                return violationCall.apply(tableName);
            }
        } else if (thenItem.equalsIgnoreCase("alias")) { // e.g. alias is suffix:History
            final String alias = table.getAlias();
            if (!isHitExp(statement, alias, toAliasComparingThenValue(table, thenValue)) == !notThenValue) {
                return violationCall.apply(alias);
            }
        } else if (thenItem.equalsIgnoreCase("comment")) { // e.g. comment is contain:SEA
            final String comment = table.getComment();
            if (!isHitExp(statement, comment, thenValue) == !notThenValue) {
                return violationCall.apply(comment);
            }
        } else if (thenItem.equalsIgnoreCase("pkName")) { // e.g. pkName is prefix:PK_
            if (table.hasPrimaryKey()) {
                final Column pk = table.getPrimaryKey().get(0); // same name if compound
                final String pkName = pk.getPrimaryKeyName();
                final String comparingThenValue = toConstraintNameComparingThenValue(table, thenValue);
                if (!isHitExp(statement, pkName, comparingThenValue) == !notThenValue) {
                    final String disp = pkName + (pk.isAdditionalPrimaryKey() ? ADDITIONAL_SUFFIX : "");
                    return violationCall.apply(disp);
                }
            }
        } else if (thenItem.equalsIgnoreCase("fkName")) { // e.g. fkName is prefix:FK_
            for (ForeignKey fk : table.getForeignKeyList()) {
                final String fkName = fk.getName();
                final String comparingThenValue = toConstraintNameComparingThenValue(table, thenValue);
                if (!isHitExp(statement, fkName, comparingThenValue) == !notThenValue) {
                    final String disp = fkName + (fk.isAdditionalForeignKey() ? ADDITIONAL_SUFFIX : "");
                    return violationCall.apply(disp);
                }
            }
        } else if (thenItem.equalsIgnoreCase("uniqueName")) { // e.g. uniqueName is prefix:UQ_ 
            for (Unique uq : table.getUniqueList()) {
                final String uqName = uq.getName();
                final String comparingThenValue = toConstraintNameComparingThenValue(table, thenValue);
                if (!isHitExp(statement, uqName, comparingThenValue)) {
                    final String disp = uqName + (uq.isAdditional() ? ADDITIONAL_SUFFIX : "");
                    return violationCall.apply(disp);
                }
            }
        } else if (thenItem.equalsIgnoreCase("indexName")) { // e.g. indexName is prefix:IX_ 
            for (Index ix : table.getIndexList()) {
                final String ixName = ix.getName();
                final String comparingThenValue = toConstraintNameComparingThenValue(table, thenValue);
                if (!isHitExp(statement, ixName, comparingThenValue) == !notThenValue) {
                    return violationCall.apply(ixName);
                }
            }
        } else if (thenItem.equalsIgnoreCase("pk_columnName")) {
            return determinePkSomethingThenValue(statement, table, violationCall, thenValue, notThenValue, pk -> toComparingColumnName(pk));
        } else if (thenItem.equalsIgnoreCase("pk_dbType") || thenItem.equalsIgnoreCase("pkDbType")) { // for compatible
            return determinePkSomethingThenValue(statement, table, violationCall, thenValue, notThenValue, pk -> pk.getDbType());
        } else if (thenItem.equalsIgnoreCase("pk_size")) {
            return determinePkSomethingThenValue(statement, table, violationCall, thenValue, notThenValue, pk -> pk.getColumnSize());
        } else if (thenItem.equalsIgnoreCase("pk_dbType_with_size")) { // e.g. char(3)
            return determinePkSomethingThenValue(statement, table, violationCall, thenValue, notThenValue,
                    pk -> toComparingDbTypeWithSize(pk));
        } else {
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown then-item: " + thenItem);
        }
        return null; // no violation
    }

    protected String determinePkSomethingThenValue(DfSPolicyStatement statement, Table table, Function<String, String> violationCall,
            String thenValue, boolean notThenValue, Function<Column, String> valueProvider) {
        if (table.hasPrimaryKey()) {
            final List<Column> pkList = table.getPrimaryKey();
            for (Column pk : pkList) {
                final String comparingValue = valueProvider.apply(pk);
                if (!isHitExp(statement, comparingValue, thenValue) == !notThenValue) {
                    return violationCall.apply(comparingValue);
                }
            }
        }
        return null;
    }

    protected String toTableNameComparingThenValue(Table table, String thenValue) {
        return convertToTableNameComparingValue(table, thenValue);
    }

    protected String toAliasComparingThenValue(Table table, String thenValue) {
        return convertToAliasComparingValue(table, thenValue);
    }

    protected String toConstraintNameComparingThenValue(Table table, String thenValue) {
        return convertToConstraintNameComparingValue(table, thenValue);
    }

    protected String buildViolation(Table table, DfSPolicyThenPart thenPart, String actual) {
        // change message to be simple, because it can be debugged by only statement display by jflute (2018/05/22)
        //final String thenItem = thenPart.getThenItem();
        //final String thenValue = thenPart.getThenValue();
        //final String notOr = thenPart.isNotThenValue() ? "not " : "";
        final String columnDisp = toTableDisp(table);
        return "but " + actual + ": " + columnDisp;
        //return "The " + thenItem + " should " + notOr + "be " + thenValue + " but " + actual + ": " + columnDisp;
    }

    // ===================================================================================
    //                                                                     Comparing Value
    //                                                                     ===============
    // *The "(df:emptyXxx)" is dummy value for $$variable$$ of no-value not to hit unrelated value
    protected String convertToTableNameComparingValue(Table table, String yourValue) { // @since 1.1.9
        String comparingValue = yourValue;
        {
            final String comment = table.hasComment() ? table.getComment() : "(df:emptyComment)";
            comparingValue = replaceComparingValue(comparingValue, "comment", comment); // @since 1.1.9
        }
        return comparingValue;
    }

    protected String convertToAliasComparingValue(Table table, String yourValue) { // @since 1.1.9
        String comparingValue = yourValue;
        final String tableName = toComparingTableName(table);
        {
            comparingValue = replaceComparingValue(comparingValue, "tableName", tableName, /*suppressUpper*/true); // @since 1.1.9
            comparingValue = replaceComparingValue(comparingValue, "table", tableName); // facade style, @since 1.1.9
        }
        {
            final String comment = table.hasComment() ? table.getComment() : "(df:emptyComment)";
            comparingValue = replaceComparingValue(comparingValue, "comment", comment); // @since 1.1.9
        }
        return comparingValue;
    }

    protected String convertToConstraintNameComparingValue(Table table, String thenValue) {
        final String tableName = toComparingTableName(table);
        String comparingValue = thenValue;
        comparingValue = replaceComparingValue(comparingValue, "tableName", tableName, /*suppressUpper*/true); // @since 1.1.9
        comparingValue = replaceComparingValue(comparingValue, "table", tableName); // facade style, @since first
        return comparingValue;
    }

    protected String replaceComparingValue(String comparingValue, String variableName, String targetStr) {
        return replaceComparingValue(comparingValue, variableName, targetStr, false);
    }

    protected String replaceComparingValue(String comparingValue, String variableName, String targetStr, boolean suppressUpper) {
        String filtered = comparingValue;
        filtered = Srl.replace(filtered, "$$" + variableName + "$$", targetStr); // e.g. $$table$$
        filtered = Srl.replace(filtered, "$$" + Srl.initCap(variableName) + "$$", targetStr); // e.g. $$Table$$
        if (!suppressUpper) {
            filtered = Srl.replace(filtered, "$$" + variableName.toUpperCase() + "$$", targetStr); // e.g. $$TABLE$$
        }
        return filtered;
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected boolean isHitExp(DfSPolicyStatement statement, String exp, String hint) {
        return _logicalSecretary.isHitExp(statement, exp, hint);
    }

    protected String toComparingTableName(Table table) {
        return _logicalSecretary.toComparingTableName(table); // e.g. MEMBER
    }

    protected String toComparingColumnName(Column column) {
        return _logicalSecretary.toComparingColumnName(column); // e.g. MEMBER_NAME
    }

    protected String toComparingDbTypeWithSize(Column column) {
        return _logicalSecretary.toComparingDbTypeWithSize(column);
    }

    protected String toTableDisp(Table table) {
        return _logicalSecretary.toTableDisp(table);
    }

    protected String toPolicy(DfSPolicyStatement statement) {
        return "table.statement: " + statement.getNativeExp();
    }

    // ===================================================================================
    //                                                                           Exception
    //                                                                           =========
    protected void throwSchemaPolicyCheckUnknownThemeException(String theme, String targetType) {
        _logicalSecretary.throwSchemaPolicyCheckUnknownThemeException(theme, targetType);
    }

    protected void throwSchemaPolicyCheckUnknownPropertyException(String property) {
        _logicalSecretary.throwSchemaPolicyCheckUnknownPropertyException(property);
    }

    protected void throwSchemaPolicyCheckIllegalIfThenStatementException(DfSPolicyStatement statement, String additional) {
        _logicalSecretary.throwSchemaPolicyCheckIllegalIfThenStatementException(statement.getNativeExp(), additional);
    }

    protected void throwSchemaPolicyCheckIllegalThenNotThemeException(DfSPolicyStatement statement, String theme) {
        _logicalSecretary.throwSchemaPolicyCheckIllegalThenNotThemeException(statement.getNativeExp(), theme);
    }
}
