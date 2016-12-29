/*
 * Copyright 2014-2016 the original author or authors.
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
import org.apache.torque.engine.database.model.Table;
import org.dbflute.exception.DfSchemaPolicyCheckIllegalIfThenStatementException;
import org.dbflute.exception.DfSchemaPolicyCheckUnknownPropertyException;
import org.dbflute.exception.DfSchemaPolicyCheckUnknownThemeException;
import org.dbflute.exception.DfSchemaPolicyCheckViolationException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 * @since 1.1.2 (2016/12/29 Thursday at higashi-ginza)
 */
public class DfSchemaPolicyMiscSecretary {

    // ===================================================================================
    //                                                                           Statement
    //                                                                           =========
    public DfSchemaPolicyIfClause extractIfClause(String statement) {
        if (!statement.startsWith("if ")) {
            String msg = "The element of statementList should start with 'if' for SchemaPolicyCheck: " + statement;
            throw new IllegalStateException(msg);
        }
        final ScopeInfo ifClauseScope = Srl.extractScopeFirst(statement, "if ", " then ");
        if (ifClauseScope == null) {
            final String additional = "The statement should start with 'if' and contain 'then'.";
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, additional);
        }
        final String ifClause = ifClauseScope.getContent().trim();
        if (!ifClause.contains(" is ")) {
            final String additional = "The if-clause should contain 'is': " + ifClause;
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, additional);
        }
        final String ifItem = Srl.substringFirstFront(ifClause, " is ").trim();
        final String ifValue = Srl.substringFirstRear(ifClause, " is ").trim();
        final String thenClause = ifClauseScope.substringInterspaceToNext();
        return new DfSchemaPolicyIfClause(ifItem, ifValue, thenClause);
    }

    public static class DfSchemaPolicyIfClause {

        protected final String _ifItem;
        protected final String _ifValue;
        protected final String _thenClause;

        public DfSchemaPolicyIfClause(String ifItem, String ifValue, String thenClause) {
            this._ifItem = ifItem;
            this._ifValue = ifValue;
            this._thenClause = thenClause;
        }

        public String getIfItem() {
            return _ifItem;
        }

        public String getIfValue() {
            return _ifValue;
        }

        public String getThenClause() {
            return _thenClause;
        }
    }

    // ===================================================================================
    //                                                                           Hit Logic
    //                                                                           =========
    public boolean isHitTable(String columnName, String hint) {
        return determineHitBy(columnName, hint);
    }

    public boolean isHitColumn(String columnName, String hint) {
        return determineHitBy(columnName, hint);
    }

    public boolean isHitExp(String exp, String hint) {
        return determineHitBy(exp, hint);
    }

    protected boolean determineHitBy(String name, String hint) {
        if (name == null) {
            return false;
        }
        if ("$$ALL$$".equalsIgnoreCase(hint)) {
            return true;
        } else if (hint.contains(" and ")) {
            final List<String> elementHintList = Srl.splitListTrimmed(hint, " and ");
            for (String elementHint : elementHintList) {
                if (!DfNameHintUtil.isHitByTheHint(name, elementHint)) {
                    return false;
                }
            }
            return true;
        } else if (hint.contains(" or ")) {
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

    // ===================================================================================
    //                                                                             Compare
    //                                                                             =======
    public String buildCaseComparingTableName(Table table) {
        // use SQL name because DB name may be controlled
        // (and use resource name to be without schema prefix)
        return table.getResourceNameForSqlName();
    }

    public String buildCaseComparingColumnName(Column column) {
        return column.getResourceNameForSqlName(); // same reason as table name
    }

    // ===================================================================================
    //                                                                             Display
    //                                                                             =======
    public String toTableDisp(Table table) {
        return table.getAliasExpression() + table.getTableDbName();
    }

    public String toColumnDisp(Column column) {
        final String notNull = column.isNotNull() ? "*" : "";
        final String dbType = column.hasDbType() ? column.getDbType() : "(unknownType)";
        final String size = column.hasColumnSize() ? "(" + column.getColumnSize() + ")" : "";
        return notNull + column.getTable().getTableDbName() + "." + column.getName() + " " + dbType + size;
    }

    // ===================================================================================
    //                                                                 Violation Exception
    //                                                                 ===================
    public void throwSchemaPolicyCheckViolationException(Map<String, Object> policyMap, List<String> vioList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The schema policy has been violated.");
        br.addItem("Advice");
        br.addElement("Make sure your violating schema (ERD and DDL).");
        br.addElement("And after that, execute renewal (or regenerate) again.");
        br.addElement("(tips: The schema policy is on schemaPolicyMap.dfprop)");
        br.addItem("Schema Policy");
        br.addElement(buildPolicyExp(policyMap));
        br.addItem("Violation");
        for (String vio : vioList) {
            br.addElement(vio);
        }
        final String msg = br.buildExceptionMessage();
        throw new DfSchemaPolicyCheckViolationException(msg, vioList);
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
        br.addElement("You can use following themes:");
        br.addElement(" Table  : hasPK, upperCaseBasis, lowerCaseBasis, identityIfPureIDPK");
        br.addElement(" Column : upperCaseBasis, lowerCaseBasis");
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

    public void throwSchemaPolicyCheckIllegalIfThenStatementException(String statement, String additional) {
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
        br.addElement(statement);
        final String msg = br.buildExceptionMessage();
        throw new DfSchemaPolicyCheckIllegalIfThenStatementException(msg);
    }
}
