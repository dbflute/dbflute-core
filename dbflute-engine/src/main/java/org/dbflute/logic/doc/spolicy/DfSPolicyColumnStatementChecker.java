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
import java.util.function.Predicate;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement.DfSPolicyIfPart;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement.DfSPolicyThenClause;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement.DfSPolicyThenPart;
import org.dbflute.logic.doc.spolicy.result.DfSPolicyResult;
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyCrossSecretary;
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyFirstDateSecretary;
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyLogicalSecretary;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.1.2 (2016/12/29 Thursday at higashi-ginza)
 */
public class DfSPolicyColumnStatementChecker {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSPolicyCrossSecretary _crossDeterminer;
    protected final DfSPolicyFirstDateSecretary _firstDateDeterminer;
    protected final DfSPolicyLogicalSecretary _logicalSecretary;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyColumnStatementChecker(DfSPolicyCrossSecretary crossDeterminer, DfSPolicyFirstDateSecretary firstDateDeterminer,
            DfSPolicyLogicalSecretary logicalSecretary) {
        _crossDeterminer = crossDeterminer;
        _firstDateDeterminer = firstDateDeterminer;
        _logicalSecretary = logicalSecretary;
    }

    // ===================================================================================
    //                                                                    Column Statement
    //                                                                    ================
    public void checkColumnStatement(List<DfSPolicyStatement> statementList, DfSPolicyResult result, Column column) {
        for (DfSPolicyStatement statement : statementList) {
            evaluateColumnIfClause(statement, result, column);
        }
    }

    // ===================================================================================
    //                                                                           If Clause
    //                                                                           =========
    // -----------------------------------------------------
    //                                              Evaluate
    //                                              --------
    // e.g.
    //  if columnName is suffix:_FLG then notNull
    //  if columnName is suffix:_FLG then dbType is integer
    protected void evaluateColumnIfClause(DfSPolicyStatement statement, DfSPolicyResult result, Column column) {
        if (statement.getIfClause().evaluate(ifPart -> isIfTrue(statement, ifPart, column))) {
            evaluateColumnThenClause(statement, result, column);
        }
    }

    // -----------------------------------------------------
    //                                         If Item-Value
    //                                         -------------
    protected boolean isIfTrue(DfSPolicyStatement statement, DfSPolicyIfPart ifPart, Column column) {
        final String ifItem = ifPart.getIfItem();
        final String ifValue = ifPart.getIfValue();
        final boolean notIfValue = ifPart.isNotIfValue();
        if (ifItem.equalsIgnoreCase("tableName")) { // if tableName is ...
            return isHitExp(toComparingTableName(column), ifValue) == !notIfValue;
        } else if (ifItem.equalsIgnoreCase("column")) { // if column is ...
            if ("notNull".equalsIgnoreCase(ifValue)) {
                return column.isNotNull() == !notIfValue;
            } else if ("identity".equalsIgnoreCase(ifValue)) {
                return column.isAutoIncrement() == !notIfValue;
            } else if ("pk".equalsIgnoreCase(ifValue)) {
                return column.isPrimaryKey() == !notIfValue;
            } else if ("singlePK".equalsIgnoreCase(ifValue)) {
                return (column.isPrimaryKey() && !column.isTwoOrMoreColumnPrimaryKey()) == !notIfValue;
            } else if ("fk".equalsIgnoreCase(ifValue)) {
                return column.isForeignKey() == !notIfValue;
            } else if ("unique".equalsIgnoreCase(ifValue)) {
                return column.isUnique() == !notIfValue;
            } else if ("index".equalsIgnoreCase(ifValue)) {
                return column.hasIndex() == !notIfValue;
            } else if ("classification".equalsIgnoreCase(ifValue)) {
                return column.hasClassification() == !notIfValue;
            } else {
                throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown if-value: " + ifValue);
            }
        } else if (ifItem.equalsIgnoreCase("columnName")) { // if columnName is ...
            return isHitExp(toComparingColumnName(column), toColumnNameComparingIfValue(column, ifValue)) == !notIfValue;
        } else if (ifItem.equalsIgnoreCase("tableColumnName")) { // if tableColumnName is ...
            return isHitExp(toComparingTableColumnName(column), toColumnNameComparingIfValue(column, ifValue)) == !notIfValue;
        } else if (ifItem.equalsIgnoreCase("alias")) { // if alias is ...
            return isHitExp(column.getAlias(), toAliasComparingIfValue(column, ifValue)) == !notIfValue;
        } else if (ifItem.equalsIgnoreCase("dbType")) { // if dbType is ...
            if (column.hasDbType()) { // just in case
                return isHitExp(column.getDbType(), ifValue) == !notIfValue;
            }
        } else if (ifItem.equalsIgnoreCase("size")) { // if size is ...
            if (column.hasColumnSize()) { // just in case
                return isHitExp(column.getColumnSize(), ifValue) == !notIfValue;
            }
        } else if (ifItem.equalsIgnoreCase("dbType_with_size")) { // e.g. if dbType_with_size is char(3)
            if (column.hasDbType() && column.hasColumnSize()) { // just in case
                final String exp = toComparingDbTypeWithSize(column);
                return isHitExp(exp, ifValue) == !notIfValue;
            }
        } else if (ifItem.equalsIgnoreCase("firstDate")) { // if firstDate is after:2018/05/03
            return determineFirstDate(statement, ifValue, notIfValue, column);
        } else {
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown if-item: " + ifItem);
        }
        return false;
    }

    protected String toColumnNameComparingIfValue(Column column, String ifValue) {
        return convertToColumnNameComparingValue(column, ifValue);
    }

    protected String toAliasComparingIfValue(Column column, String ifValue) {
        return convertToAliasComparingValue(column, ifValue);
    }

    protected boolean determineFirstDate(DfSPolicyStatement statement, String ifValue, boolean notIfValue, Column column) {
        return _firstDateDeterminer.determineColumnFirstDate(statement, ifValue, notIfValue, column);
    }

    // ===================================================================================
    //                                                                         Then Clause
    //                                                                         ===========
    // -----------------------------------------------------
    //                                              Evaluate
    //                                              --------
    protected void evaluateColumnThenClause(DfSPolicyStatement statement, DfSPolicyResult result, Column column) {
        if (statement.getThenClause().getThenTheme() != null) {
            evaluateColumnThenTheme(statement, result, column);
        } else {
            evaluateColumnThenItemValue(statement, result, column);
        }
    }

    // -----------------------------------------------------
    //                                            Then Theme
    //                                            ----------
    protected void evaluateColumnThenTheme(DfSPolicyStatement statement, DfSPolicyResult result, Column column) {
        final String policy = toPolicy(statement);
        final DfSPolicyThenClause thenClause = statement.getThenClause();
        final String thenTheme = thenClause.getThenTheme(); // already not null here
        final boolean notThenClause = thenClause.isNotThenTheme();
        final String notOr = notThenClause ? "not " : "";
        if (thenTheme.equalsIgnoreCase("bad") == !notThenClause) {
            result.violate(policy, "The column is no good: " + toColumnDisp(column));
        } else if (thenTheme.equalsIgnoreCase("notNull")) {
            if (!column.isNotNull() == !notThenClause) {
                result.violate(policy, "The column should " + notOr + "be not-null: " + toColumnDisp(column));
            }
        } else if (thenTheme.equalsIgnoreCase("identity")) {
            if (!column.isAutoIncrement() == !notThenClause) {
                result.violate(policy, "The column should " + notOr + "be identity (auto increment): " + toColumnDisp(column));
            }
        } else if (thenTheme.equalsIgnoreCase("pk")) {
            if (!column.isPrimaryKey() == !notThenClause) {
                result.violate(policy, "The column should " + notOr + "be primary key: " + toColumnDisp(column));
            }
        } else if (thenTheme.equalsIgnoreCase("fk")) {
            if (!column.isForeignKey() == !notThenClause) {
                result.violate(policy, "The column should " + notOr + "be foreign key: " + toColumnDisp(column));
            }
        } else if (thenTheme.equalsIgnoreCase("unique")) {
            if (!column.isUnique() == !notThenClause) {
                result.violate(policy, "The column should " + notOr + "be unique: " + toColumnDisp(column));
            }
        } else if (thenTheme.equalsIgnoreCase("index")) {
            if (!column.hasIndex() == !notThenClause) {
                result.violate(policy, "The column should " + notOr + "have index: " + toColumnDisp(column));
            }
        } else if (thenTheme.equalsIgnoreCase("classification")) {
            if (!column.hasClassification() == !notThenClause) {
                result.violate(policy, "The column should " + notOr + "be classification: " + toColumnDisp(column));
            }
        } else if (thenTheme.equalsIgnoreCase("upperCaseBasis")) {
            if (Srl.isLowerCaseAny(toComparingColumnName(column)) == !notThenClause) {
                result.violate(policy, "The column name should " + notOr + "be on upper case basis: " + toColumnDisp(column));
            }
        } else if (thenTheme.equalsIgnoreCase("lowerCaseBasis")) {
            if (Srl.isUpperCaseAny(toComparingColumnName(column)) == !notThenClause) {
                result.violate(policy, "The column name should " + notOr + "be on lower case basis: " + toColumnDisp(column));
            }
        } else if (thenTheme.equalsIgnoreCase("hasAlias")) {
            if (!column.hasAlias() == !notThenClause) {
                result.violate(policy, "The column should " + notOr + "have column alias: " + toColumnDisp(column));
            }
        } else if (thenTheme.equalsIgnoreCase("hasComment")) {
            if (!column.hasComment() == !notThenClause) {
                result.violate(policy, "The column should " + notOr + "have column comment: " + toColumnDisp(column));
            }
        } else if (thenTheme.equalsIgnoreCase("sameColumnAliasIfSameColumnName")) {
            final String vio = _crossDeterminer.determineSameColumnAliasIfSameColumnName(column, prepareYourTargeting(statement));
            if (vio != null) {
                result.violate(policy, vio);
            }
        } else if (thenTheme.equalsIgnoreCase("sameColumnDbTypeIfSameColumnName")) {
            final String vio = _crossDeterminer.determineSameColumnDbTypeIfSameColumnName(column, prepareYourTargeting(statement));
            if (vio != null) {
                result.violate(policy, vio);
            }
        } else if (thenTheme.equalsIgnoreCase("sameColumnSizeIfSameColumnName")) {
            final String vio = _crossDeterminer.determineSameColumnSizeIfSameColumnName(column, prepareYourTargeting(statement));
            if (vio != null) {
                result.violate(policy, vio);
            }
        } else if (thenTheme.equalsIgnoreCase("sameColumnNameIfSameColumnAlias")) {
            final String vio = _crossDeterminer.determineSameColumnNameIfSameColumnAlias(column, prepareYourTargeting(statement));
            if (vio != null) {
                result.violate(policy, vio);
            }
        } else {
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown then-clause: " + thenClause);
        }
    }

    protected Predicate<Column> prepareYourTargeting(DfSPolicyStatement statement) { // to evaluate same condition to your column
        return your -> {
            return statement.getIfClause().evaluate(ifPart -> isIfTrue(statement, ifPart, your));
        };
    }

    // -----------------------------------------------------
    //                                       Then Item-Value
    //                                       ---------------
    protected void evaluateColumnThenItemValue(DfSPolicyStatement statement, DfSPolicyResult result, Column column) {
        final List<String> violationList = statement.getThenClause().evaluate(thenPart -> {
            return doEvaluateColumnThenItemValue(statement, thenPart, column, actual -> {
                return buildViolation(column, thenPart, actual);
            });
        });
        if (!violationList.isEmpty()) {
            final String policy = toPolicy(statement);
            for (String violation : violationList) {
                result.violate(policy, violation);
            }
        }
    }

    protected String doEvaluateColumnThenItemValue(DfSPolicyStatement statement, DfSPolicyThenPart thenPart, Column column,
            Function<String, String> violationCall) {
        // *can arrange more but don't be hard for now
        final String thenItem = thenPart.getThenItem();
        final String thenValue = thenPart.getThenValue();
        final boolean notThenValue = thenPart.isNotThenValue();
        if (thenItem.equalsIgnoreCase("tableName")) { // e.g. tableName is prefix:CLS_
            final String tableName = toComparingTableName(column);
            if (!isHitExp(tableName, thenValue) == !notThenValue) {
                return violationCall.apply(tableName);
            }
        } else if (thenItem.equalsIgnoreCase("column")) { // e.g. column is notNull
            if ("notNull".equalsIgnoreCase(thenValue)) {
                final boolean determination = column.isNotNull();
                if (!determination == !notThenValue) {
                    return violationCall.apply(String.valueOf(determination));
                }
            } else if ("identity".equalsIgnoreCase(thenValue)) {
                final boolean determination = column.isAutoIncrement();
                if (!determination == !notThenValue) {
                    return violationCall.apply(String.valueOf(determination));
                }
            } else if ("pk".equalsIgnoreCase(thenValue)) {
                final boolean determination = column.isPrimaryKey();
                if (!determination == !notThenValue) {
                    return violationCall.apply(String.valueOf(determination));
                }
            } else if ("singlePK".equalsIgnoreCase(thenValue)) {
                final boolean determination = column.isPrimaryKey() && !column.isTwoOrMoreColumnPrimaryKey();
                if (!determination == !notThenValue) {
                    return violationCall.apply(String.valueOf(determination));
                }
            } else if ("fk".equalsIgnoreCase(thenValue)) {
                final boolean determination = column.isForeignKey();
                if (!determination == !notThenValue) {
                    return violationCall.apply(String.valueOf(determination));
                }
            } else if ("unique".equalsIgnoreCase(thenValue)) {
                final boolean determination = column.isUnique();
                if (!determination == !notThenValue) {
                    return violationCall.apply(String.valueOf(determination));
                }
            } else if ("index".equalsIgnoreCase(thenValue)) {
                final boolean determination = column.hasIndex();
                if (!determination == !notThenValue) {
                    return violationCall.apply(String.valueOf(determination));
                }
            } else if ("classification".equalsIgnoreCase(thenValue)) {
                final boolean determination = column.hasClassification();
                if (!determination == !notThenValue) {
                    return violationCall.apply(String.valueOf(determination));
                }
            } else {
                throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown then-value: " + thenValue);
            }
        } else if (thenItem.equalsIgnoreCase("columnName")) { // e.g. columnName is suffix:_ID
            final String columnName = toComparingColumnName(column);
            if (!isHitExp(columnName, toColumnNameComparingThenValue(column, thenValue)) == !notThenValue) {
                return violationCall.apply(columnName);
            }
        } else if (thenItem.equalsIgnoreCase("tableColumnName")) { // e.g. tableColumnName is contain:R.M
            final String tableColumnName = toComparingTableColumnName(column);
            if (!isHitExp(tableColumnName, toColumnNameComparingThenValue(column, thenValue)) == !notThenValue) {
                return violationCall.apply(tableColumnName);
            }
        } else if (thenItem.equalsIgnoreCase("alias")) { // e.g. alias is suffix:ID
            final String alias = column.getAlias();
            if (!isHitExp(alias, toAliasComparingThenValue(column, thenValue)) == !notThenValue) {
                return violationCall.apply(alias);
            }
        } else if (thenItem.equalsIgnoreCase("dbType")) { // e.g. dbType is integer
            final String dbType = column.getDbType();
            if (!isHitExp(dbType, thenValue) == !notThenValue) {
                return violationCall.apply(dbType);
            }
        } else if (thenItem.equalsIgnoreCase("size")) { // e.g. size is 200
            final String size = column.getColumnSize(); // String expression #for_now
            if (!isHitExp(size, thenValue) == !notThenValue) {
                return violationCall.apply(size);
            }
        } else if (thenItem.equalsIgnoreCase("dbType_with_size")) { // e.g. dbType_with_size is char(3)
            final String dbTypeWithSize = toComparingDbTypeWithSize(column);
            if (!isHitExp(dbTypeWithSize, thenValue) == !notThenValue) {
                return violationCall.apply(dbTypeWithSize);
            }
        } else if (thenItem.equalsIgnoreCase("comment")) { // e.g. comment is contain:SEA
            final String comment = column.getComment();
            if (!isHitExp(comment, thenValue) == !notThenValue) {
                return violationCall.apply(comment);
            }
        } else {
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown then-item: " + thenItem);
        }
        return null; // no violation
    }

    protected String toColumnNameComparingThenValue(Column column, String thenValue) {
        return convertToColumnNameComparingValue(column, thenValue);
    }

    protected String toAliasComparingThenValue(Column column, String thenValue) {
        return convertToAliasComparingValue(column, thenValue);
    }

    protected String buildViolation(Column column, DfSPolicyThenPart thenPart, String actual) {
        // change message to be simple, because it can be debugged by only statement display by jflute (2018/05/22)
        //final String thenItem = thenPart.getThenItem();
        //final String thenValue = thenPart.getThenValue();
        //final String notOr = thenPart.isNotThenValue() ? "not " : "";
        final String columnDisp = toColumnDisp(column);
        return "but " + actual + ": " + columnDisp;
        //return "The " + thenItem + " should " + notOr + "be " + thenValue + " but " + actual + ": " + columnDisp;
    }

    // ===================================================================================
    //                                                                     Comparing Value
    //                                                                     ===============
    // *The "(df:emptyXxx)" is dummy value for $$variable$$ of no-value not to hit unrelated value
    protected String convertToColumnNameComparingValue(Column column, String yourValue) { // @since 1.1.8
        String comparingValue = yourValue;
        {
            final String tableName = toComparingTableName(column); // e.g. columnName is $$table$$_ID
            comparingValue = replaceComparingValue(comparingValue, "tableName", tableName, /*suppressUpper*/true); // @since 1.1.9
            comparingValue = replaceComparingValue(comparingValue, "table", tableName); // old style, @since 1.1.8
        }
        {
            final String comment = column.hasComment() ? column.getComment() : "(df:emptyComment)";
            comparingValue = replaceComparingValue(comparingValue, "comment", comment); // @since 1.1.9
        }
        return comparingValue;
    }

    protected String convertToAliasComparingValue(Column column, String yourValue) {
        String comparingValue = yourValue;
        {
            final String columnDbName = column.getName();
            comparingValue = replaceComparingValue(comparingValue, "columnName", columnDbName, /*suppressUpper*/true); // @since 1.1.9
        }
        {
            final Table table = column.getTable();
            final String tableAlias = table.hasAlias() ? table.getAlias() : "(df:emptyTableAlias)";
            comparingValue = replaceComparingValue(comparingValue, "tableAlias", tableAlias, /*suppressUpper*/true); // @since 1.1.9
        }
        {
            final String comment = column.hasComment() ? column.getComment() : "(df:emptyComment)";
            comparingValue = replaceComparingValue(comparingValue, "comment", comment); // @since 1.1.9
        }
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
    protected boolean isHitExp(String exp, String hint) {
        return _logicalSecretary.isHitExp(exp, hint);
    }

    protected String toComparingTableName(Column column) {
        return _logicalSecretary.toComparingTableName(column.getTable()); // e.g. MEMBER
    }

    protected String toComparingColumnName(Column column) {
        return _logicalSecretary.toComparingColumnName(column); // e.g. MEMBER_NAME
    }

    protected String toComparingTableColumnName(Column column) {
        final String tableName = toComparingTableName(column);
        final String columnName = toComparingColumnName(column);
        return tableName + "." + columnName; // e.g. MEMBER.MEMBER_NAME
    }

    protected String toComparingDbTypeWithSize(Column column) {
        return _logicalSecretary.toComparingDbTypeWithSize(column);
    }

    protected String toColumnDisp(Column column) {
        return _logicalSecretary.toColumnDisp(column);
    }

    protected String toPolicy(DfSPolicyStatement statement) {
        return "column.statement: " + statement.getNativeExp();
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
}
