/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.logic.doc.spolicy.secretary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.exception.DfSchemaPolicyCheckIllegalIfThenStatementException;
import org.dbflute.exception.DfSchemaPolicyCheckIllegalThenNotThemeException;
import org.dbflute.exception.DfSchemaPolicyCheckUnknownPropertyException;
import org.dbflute.exception.DfSchemaPolicyCheckUnknownThemeException;
import org.dbflute.exception.DfSchemaPolicyCheckUnknownVariableException;
import org.dbflute.exception.DfSchemaPolicyCheckViolationException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement.DfSPolicyIfClause;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement.DfSPolicyIfPart;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement.DfSPolicyThenClause;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement.DfSPolicyThenPart;
import org.dbflute.logic.doc.spolicy.result.DfSPolicyResult;
import org.dbflute.logic.doc.spolicy.result.DfSPolicyResult.DfSPolicyViolation;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 * @author subaru
 * @since 1.1.2 (2016/12/29 Thursday at higashi-ginza)
 */
public class DfSPolicyLogicalSecretary {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String EQUALS_DELIMITER = " is ";
    protected static final String NOT_PREFIX = "not ";
    protected static final String SUPPLEMENT_DELIMITER = " => ";

    // ===================================================================================
    //                                                                           Statement
    //                                                                           =========
    public DfSPolicyStatement parseStatement(String statement) {
        if (!statement.startsWith("if ")) {
            String msg = "The element of statementList should start with 'if' for SchemaPolicyCheck: " + statement;
            throw new IllegalStateException(msg);
        }
        final ScopeInfo ifScope = Srl.extractScopeFirst(statement, "if ", " then ");
        if (ifScope == null) {
            final String additional = "The statement should start with 'if' and contain 'then'.";
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, additional);
        }
        final DfSPolicyIfClause ifClause = analyzeIfClause(statement, ifScope);
        final DfSPolicyThenClause thenClause = analyzeThenClause(statement, ifScope);
        return new DfSPolicyStatement(statement, ifClause, thenClause);
    }

    // -----------------------------------------------------
    //                                             If Clause
    //                                             ---------
    protected DfSPolicyIfClause analyzeIfClause(String statement, ScopeInfo ifScope) {
        final String ifWhole = ifScope.getContent().trim();
        if (!ifWhole.contains(EQUALS_DELIMITER)) {
            final String additional = "The if-clause should contain 'is': " + ifWhole;
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, additional);
        }
        // e.g. if tableName is sea and alias is land and piari and tableName is bonvo then ...
        boolean connectedByOr = false;
        List<String> ifPartStrList = splitClauseByConnector(ifWhole, " and ");
        if (ifPartStrList.size() == 1) {
            ifPartStrList = splitClauseByConnector(ifWhole, " or ");
            if (ifPartStrList.size() >= 2) {
                connectedByOr = true;
            }
        }
        final List<DfSPolicyIfPart> ifPartList = new ArrayList<DfSPolicyIfPart>();
        for (String ifPartStr : ifPartStrList) {
            final String ifItem = Srl.substringFirstFront(ifPartStr, EQUALS_DELIMITER).trim();
            final String ifValueCandidate = Srl.substringFirstRear(ifPartStr, EQUALS_DELIMITER).trim();
            final boolean notIfValue = ifValueCandidate.startsWith(NOT_PREFIX);
            final String ifValue = notIfValue ? Srl.substringFirstRear(ifValueCandidate, NOT_PREFIX).trim() : ifValueCandidate;
            final DfSPolicyIfPart part = new DfSPolicyIfPart(ifItem, ifValue, notIfValue);
            ifPartList.add(part);
        }
        return new DfSPolicyIfClause(Collections.unmodifiableList(ifPartList), connectedByOr);
    }

    // -----------------------------------------------------
    //                                           Then Clause
    //                                           -----------
    protected DfSPolicyThenClause analyzeThenClause(String statement, ScopeInfo ifScope) {
        final String thenRear = ifScope.substringInterspaceToNext();
        final String thenWhole;
        final String supplement; // e.g. if tableName is suffix:_ID then bad => similar to column name
        if (thenRear.contains(SUPPLEMENT_DELIMITER)) {
            thenWhole = Srl.substringLastFront(thenRear, SUPPLEMENT_DELIMITER).trim();
            supplement = Srl.substringLastRear(thenRear, SUPPLEMENT_DELIMITER).trim();
        } else {
            thenWhole = thenRear;
            supplement = null;
        }
        final String thenTheme;
        final boolean notThenTheme;
        final List<DfSPolicyThenPart> thenPartList;
        final boolean connectedByOr;
        if (!thenWhole.contains(EQUALS_DELIMITER)) { // then [theme]
            final boolean startsWithNot = thenWhole.startsWith(NOT_PREFIX);
            if (startsWithNot) {
                thenTheme = Srl.substringFirstRear(thenWhole, NOT_PREFIX);
            } else {
                thenTheme = thenWhole;
            }
            notThenTheme = startsWithNot;
            thenPartList = Collections.emptyList();
            connectedByOr = false;
        } else { // then ... is ... 
            thenTheme = null;
            notThenTheme = false;
            boolean byOr = false;
            List<String> thenPartStrList = splitClauseByConnector(thenWhole, " and ");
            if (thenPartStrList.size() == 1) {
                thenPartStrList = splitClauseByConnector(thenWhole, " or ");
                if (thenPartStrList.size() >= 2) {
                    byOr = true;
                }
            }
            final List<DfSPolicyThenPart> makingPartList = new ArrayList<DfSPolicyThenPart>();
            for (String thenPartStr : thenPartStrList) {
                final String thenItem = Srl.substringFirstFront(thenPartStr, EQUALS_DELIMITER).trim();
                final String thenValueCandidate = Srl.substringFirstRear(thenPartStr, EQUALS_DELIMITER).trim();
                final boolean notThenValue = thenValueCandidate.startsWith(NOT_PREFIX);
                final String thenValue = notThenValue ? Srl.substringFirstRear(thenValueCandidate, NOT_PREFIX).trim() : thenValueCandidate;
                makingPartList.add(new DfSPolicyThenPart(thenItem, thenValue, notThenValue));
            }
            thenPartList = Collections.unmodifiableList(makingPartList);
            connectedByOr = byOr;
        }
        return new DfSPolicyThenClause(thenTheme, notThenTheme, thenPartList, connectedByOr, supplement);
    }

    protected List<String> splitClauseByConnector(String clause, String connector) {
        // e.g. tableName is sea and alias is land and piari and tableName is bonvo
        // e.g. tableName is sea and alias is land or piari and tableName is bonvo
        // e.g. tableName is sea or alias is land or piari or tableName is bonvo
        // e.g. tableName is sea or alias is land and piari or tableName is bonvo
        final List<String> partElementList = Srl.splitListTrimmed(clause, connector);
        final List<String> partElementSummaryList = new ArrayList<String>();
        final StringBuilder connectedSb = new StringBuilder();
        for (String partElement : partElementList) {
            if (partElement.contains(EQUALS_DELIMITER)) { // e.g. tableName is sea
                if (connectedSb.length() > 0) { // e.g. alias is land
                    partElementSummaryList.add(connectedSb.toString()); // add previous part
                    connectedSb.setLength(0);
                }
            }
            if (connectedSb.length() > 0) {
                connectedSb.append(connector);
            }
            connectedSb.append(partElement);
        }
        if (connectedSb.length() > 0) { // e.g. tableName is bonvo
            partElementSummaryList.add(connectedSb.toString()); // add last part
        }
        return partElementSummaryList;
    }

    // ===================================================================================
    //                                                                           Hit Logic
    //                                                                           =========
    public boolean isHitExp(DfSPolicyStatement statement, String exp, String hint) {
        return determineHitBy(statement, exp, hint);
    }

    protected boolean determineHitBy(DfSPolicyStatement statement, String name, String hint) { // hint is e.g. prefix:MEMBER
        if (name == null) {
            return false;
        }
        if ("$$ALL$$".equalsIgnoreCase(hint)) { // e.g. tableName is $$ALL$$
            return true;
        }
        checkUnknownVariable(statement, name, hint); // should be after $$ALL$$ process
        if (hint.contains(" and ")) { // e.g. tableName is prefix:MEMBER and suffix:_HISTORY
            final List<String> elementHintList = Srl.splitListTrimmed(hint, " and ");
            for (String elementHint : elementHintList) {
                if (!DfNameHintUtil.isHitByTheHint(name, elementHint)) {
                    return false;
                }
            }
            return true;
        } else if (hint.contains(" or ")) { // e.g. tableName is prefix:MEMBER or suffix:_HISTORY
            final List<String> elementHintList = Srl.splitListTrimmed(hint, " or ");
            for (String elementHint : elementHintList) {
                if (DfNameHintUtil.isHitByTheHint(name, elementHint)) {
                    return true;
                }
            }
            return false;
        } else {
            return DfNameHintUtil.isHitByTheHint(name, hint);
        }
    }

    protected void checkUnknownVariable(DfSPolicyStatement statement, String name, String hint) {
        if (Srl.count(hint, "$$") >= 2) {
            ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Unknown variable in the SchemaPolicyCheck statement.");
            br.addItem("Advice");
            br.addElement("Confirm your statement in schemaPolicyMap.dfprop");
            br.addElement("and see the official document.");
            br.addElement("You can use only reserved variables.");
            br.addElement("For example:");
            br.addElement("  (o):");
            br.addElement("    columnName is $$ALL$$");
            br.addElement("    fkName is prefix:FK_$$table$$");
            br.addElement("    alias is not $$columnName$$");
            br.addElement("    alias is not $$comment$$");
            br.addElement("  (x):");
            br.addElement("    columnName is $$sea$$");
            br.addElement("    columnName is $$land$$");
            br.addElement("    columnName is $$piari$$");
            br.addElement("    columnName is $$bonvo$$");
            br.addElement("    columnName is $$dstore$$");
            br.addItem("Statement");
            br.addElement(statement.getNativeExp());
            br.addItem("Current Data");
            br.addElement(name);
            br.addItem("Unresolved Keyword");
            br.addElement(hint);
            final String msg = br.buildExceptionMessage();
            throw new DfSchemaPolicyCheckUnknownVariableException(msg);
        }
    }

    // ===================================================================================
    //                                                            Conversion for Comparing
    //                                                            ========================
    public String toComparingTableName(Table table) {
        // use SQL name because DB name may be controlled
        // (and use resource name to be without schema prefix)
        return table.getResourceNameForSqlName();
    }

    public String toComparingColumnName(Column column) {
        return column.getResourceNameForSqlName(); // same reason as table name
    }

    public String toComparingDbTypeWithSize(Column column) {
        return column.getDbType() + "(" + column.getColumnSize() + ")";
    }

    // ===================================================================================
    //                                                              Conversion for Display
    //                                                              ======================
    public String toTableDisp(Table table) {
        final StringBuilder sb = new StringBuilder();
        sb.append(table.getAliasExpression());
        sb.append(table.getTableDispName());
        if (table.hasComment()) {
            sb.append(" // ").append(Srl.cut(table.getComment(), 10, "..."));
        }
        return sb.toString();
    }

    public String toColumnDisp(Column column) { // e.g. (Sea.Sea Name)SEA.SEA_NAME VARCHAR(100) (NotNull) 
        final Table table = column.getTable();
        final StringBuilder sb = new StringBuilder();
        if (table.hasAlias()) {
            sb.append("(");
            sb.append(table.getAlias());
            if (column.hasAlias()) {
                sb.append(".");
                sb.append(column.getAlias());
            }
            sb.append(")");
        }
        sb.append(table.getTableDispName());
        sb.append(".").append(column.getName());
        sb.append(" ").append(column.hasDbType() ? column.getDbType() : "(unknownType)");
        if (column.hasColumnSize()) {
            sb.append("(").append(column.getColumnSize()).append(")");
        }
        sb.append(" ").append(column.isNotNull() ? "(NotNull)" : "(NullAllowed)");
        if (column.hasComment()) {
            sb.append(" // ").append(Srl.cut(column.getComment(), 10, "..."));
        }
        return sb.toString();
    }

    // ===================================================================================
    //                                                                       Pattern Logic
    //                                                                       =============
    public boolean isNotIdentityIfPureIDPK(Table table) {
        if (table.hasPrimaryKey() && table.hasSinglePrimaryKey()) {
            final Column pk = table.getPrimaryKeyAsOne();
            return !pk.isForeignKey() && Srl.endsWithIgnoreCase(pk.getName(), "ID") && !pk.isIdentity();
        } else {
            return false;
        }
    }

    public boolean isNotSequenceIfPureIDPK(Table table) {
        if (table.hasPrimaryKey() && table.hasSinglePrimaryKey()) {
            final Column pk = table.getPrimaryKeyAsOne();
            return !pk.isForeignKey() && Srl.endsWithIgnoreCase(pk.getName(), "ID") && !pk.isSequence();
        } else {
            return false;
        }
    }

    // ===================================================================================
    //                                                                 Violation Exception
    //                                                                 ===================
    public String buildSchemaPolicyCheckViolationMessage(DfSPolicyResult result) { // independent for SchemaHTML display
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The schema policy has been violated.");
        br.addItem("Advice");
        br.addElement("Make sure your violating schema (ERD and DDL).");
        br.addElement("You can see violations on this message or SchemaHTML.");
        br.addElement("And after that, execute renewal (or regenerate) again.");
        br.addElement("(tips: The schema policy is on schemaPolicyMap.dfprop)");
        // unneeded because of too big, already info, also violation with definition by jflute (2019/01/27)
        //br.addItem("Schema Policy");
        //br.addElement(buildPolicyExp(policyMap));
        br.addItem("Violation");
        final Map<String, List<DfSPolicyViolation>> violationMap = result.getViolationMap();
        int policyIndex = 0;
        for (Entry<String, List<DfSPolicyViolation>> entry : violationMap.entrySet()) {
            if (policyIndex > 0) { // second or more
                br.addElement(""); // empty line
            }
            br.addElement(entry.getKey());
            final List<DfSPolicyViolation> violationList = entry.getValue();
            int violationIndex = 0;
            for (DfSPolicyViolation violation : violationList) {
                final boolean beforeLastLoop = violationIndex < violationList.size() - 1;
                br.addElement((beforeLastLoop ? " |-" : " +-") + violation.getMessage());
                ++violationIndex;
            }
            ++policyIndex;
        }
        return br.buildExceptionMessage();
    }

    public void throwSchemaPolicyCheckViolationException(String violationMessage) {
        throw new DfSchemaPolicyCheckViolationException(violationMessage);
    }

    public String buildPolicyExp(Map<String, Object> policyMap) {
        final StringBuilder policySb = new StringBuilder();
        policyMap.forEach((key, value) -> {
            if (key.equals("tableMap")) {
                setupTableColumnMapDisp(policySb, key, value);
            } else if (key.equals("columnMap")) {
                setupTableColumnMapDisp(policySb, key, value);
            } else {
                policySb.append("\n").append(key).append(": ").append(value);
            }
        });
        return Srl.ltrim(policySb.toString());
    }

    protected void setupTableColumnMapDisp(StringBuilder policySb, String mapTitle, Object mapObj) {
        policySb.append("\n").append(mapTitle).append(":");
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) mapObj;
        map.forEach((key, value) -> {
            if (key.equals("statementList")) {
                policySb.append("\n  " + key + ":");
                @SuppressWarnings("unchecked")
                final List<String> statementList = (List<String>) value;
                for (Object statement : statementList) {
                    policySb.append("\n    " + statement);
                }
            } else {
                policySb.append("\n  " + key + ": " + value);
            }
        });
    }

    // ===================================================================================
    //                                                                  Settings Exception
    //                                                                  ==================
    public void throwSchemaPolicyCheckUnknownThemeException(String theme, String targetType) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown theme for SchemaPolicyCheck.");
        br.addItem("Advice");
        br.addElement("Make sure your schemaPolicyMap.dfprop.");
        br.addElement("For example, you can use following themes:");
        br.addElement(" Table  : hasPK, upperCaseBasis, lowerCaseBasis, identityIfPureIDPK, ...");
        br.addElement(" Column : upperCaseBasis, lowerCaseBasis, ...");
        br.addItem("Target");
        br.addElement(targetType);
        br.addItem("Unknown Theme");
        br.addElement(theme);
        final String msg = br.buildExceptionMessage();
        throw new DfSchemaPolicyCheckUnknownThemeException(msg);
    }

    public void throwSchemaPolicyCheckUnknownPropertyException(String property) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown property for SchemaPolicyCheck.");
        br.addItem("Advice");
        br.addElement("Make sure your schemaPolicyMap.dfprop.");
        br.addItem("Unknown Property");
        br.addElement(property);
        final String msg = br.buildExceptionMessage();
        throw new DfSchemaPolicyCheckUnknownPropertyException(msg);
    }

    public void throwSchemaPolicyCheckIllegalIfThenStatementException(String nativeStatement, String additional) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Illegal if-then statement for SchemaPolicyCheck.");
        br.addItem("Advice");
        br.addElement("Make sure your schemaPolicyMap.dfprop.");
        br.addElement("If-then statement should be like this:");
        br.addElement(" if [if-clause] then [then-clause]");
        br.addElement("");
        br.addElement("To be precise:");
        br.addElement(" if [if-item] is [if-value] then [then-item]");
        br.addElement("  or ");
        br.addElement(" if [if-item] is [if-value] then [then-item] is [then-value]");
        br.addElement("");
        br.addElement("For example:");
        br.addElement("  (o): if columnName is suffix:_FLAG then bad");
        br.addElement("  (o): if columnName is suffix:_FLG then notNull");
        br.addElement("  (o): if columnName is suffix:_FLG then dbType is INTEGER");
        br.addElement("");
        br.addElement(additional);
        br.addItem("Statement");
        br.addElement(nativeStatement);
        final String msg = br.buildExceptionMessage();
        throw new DfSchemaPolicyCheckIllegalIfThenStatementException(msg);
    }

    public void throwSchemaPolicyCheckIllegalThenNotThemeException(String nativeStatement, String theme) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Illegal then-not theme for SchemaPolicyCheck.");
        br.addItem("Advice");
        br.addElement("Make sure your schemaPolicyMap.dfprop.");
        br.addElement("then-not is prohibited with the specified theme");
        br.addItem("Theme");
        br.addElement(theme);
        br.addItem("Statement");
        br.addElement(nativeStatement);
        final String msg = br.buildExceptionMessage();
        throw new DfSchemaPolicyCheckIllegalThenNotThemeException(msg);
    }
}
