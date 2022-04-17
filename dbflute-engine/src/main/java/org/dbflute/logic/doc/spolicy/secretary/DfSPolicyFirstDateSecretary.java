/*
 * Copyright 2014-2022 the original author or authors.
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

import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.exception.DfSchemaPolicyCheckIllegalFirstDateException;
import org.dbflute.exception.ParseDateExpressionFailureException;
import org.dbflute.helper.HandyDate;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement;
import org.dbflute.logic.doc.supplement.firstdate.DfFirstDateAgent;
import org.dbflute.logic.jdbc.schemadiff.DfSchemaDiff;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.1.8 (2018/5/3 Thursday)
 */
public class DfSPolicyFirstDateSecretary {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String EXAMPLE_DATE_EXPRESSION = "2018/05/03";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfFirstDateAgent _firstDateAgent;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyFirstDateSecretary(Supplier<List<DfSchemaDiff>> schemaDiffListSupplier) {
        _firstDateAgent = new DfFirstDateAgent(schemaDiffListSupplier);
    }

    // ===================================================================================
    //                                                                           Determine
    //                                                                           =========
    public boolean determineTableFirstDate(DfSPolicyStatement statement, String ifValue, boolean notIfValue, Table table) {
        return doDetermineFirstDate(statement, ifValue, notIfValue, targetDate -> {
            return isTableFirstDateAfter(table, targetDate);
        });
    }

    public boolean determineColumnFirstDate(DfSPolicyStatement statement, String ifValue, boolean notIfValue, Column column) {
        return doDetermineFirstDate(statement, ifValue, notIfValue, targetDate -> {
            return isColumnFirstDateAfter(column, targetDate);
        });
    }

    protected boolean doDetermineFirstDate(DfSPolicyStatement statement, String ifValue, boolean notIfValue, Predicate<Date> determiner) {
        if (notIfValue) { // "is not" is unsupported for firstDate
            throwSchemaPolicyCheckIllegalFirstDateException(statement, ifValue);
            return false; // unreachable
        }
        if (ifValue.startsWith("after:")) { // e.g. if firstDate is after:2018/05/03
            final String dateExp = Srl.substringFirstRear(ifValue, "after:").trim();
            if (dateExp.length() > EXAMPLE_DATE_EXPRESSION.length()) { // e.g. has time part
                throwSchemaPolicyCheckIllegalDateExpressionFormatException(statement, dateExp, null);
            }
            final Date targetDate;
            try {
                targetDate = new HandyDate(dateExp).getDate();
            } catch (ParseDateExpressionFailureException e) {
                throwSchemaPolicyCheckIllegalDateExpressionFormatException(statement, dateExp, e);
                return false; // unreachable
            }
            return determiner.test(targetDate);
        } else {
            // only "after:" supported because other patterns might not be needed
            // (and difficult logic needs because of no-existing firstDate tables)
            throwSchemaPolicyCheckIllegalFirstDateException(statement, ifValue);
            return false; // unreachable
        }
    }

    // ===================================================================================
    //                                                                           Targeting
    //                                                                           =========
    // e.g. firstDate is after:2018/05/03
    protected boolean isTableFirstDateAfter(Table table, Date targetDate) {
        return _firstDateAgent.isTableFirstDateAfter(table.getTableDbName(), targetDate);
    }

    // e.g. columnFirstDate is after:2018/05/03
    protected boolean isColumnFirstDateAfter(Column column, Date targetDate) {
        return _firstDateAgent.isColumnFirstDateAfter(column.getTable().getTableDbName(), column.getName(), targetDate);
    }

    // ===================================================================================
    //                                                                           Exception
    //                                                                           =========
    protected void throwSchemaPolicyCheckIllegalDateExpressionFormatException(DfSPolicyStatement statement, String dateExp,
            RuntimeException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Illegal format of the date expression for firstDate.");
        br.addItem("Advice");
        br.addElement("The date expression for firstDate should be e.g. 2018/05/03");
        br.addElement("(slash separator, no time part)");
        br.addElement("Confirm your schemaPolicyMap.dfprop.");
        br.addElement("For example:");
        br.addElement("  (x): after:2018/ab/cd");
        br.addElement("  (x): after:2018/05/03 12:34:56");
        br.addElement("  (o): after:2018/05/03");
        br.addItem("Statement");
        br.addElement(statement);
        br.addItem("Date Expression");
        br.addElement(dateExp);
        final String msg = br.buildExceptionMessage();
        if (cause != null) {
            throw new IllegalStateException(msg, cause);
        } else {
            throw new IllegalStateException(msg);
        }
    }

    protected void throwSchemaPolicyCheckIllegalFirstDateException(DfSPolicyStatement statement, String ifValue) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the valid prefix of firstDate for SchemaPolicyCheck.");
        br.addItem("Advice");
        br.addElement("Make sure your schemaPolicyMap.dfprop.");
        br.addElement("Only limited patterns of firstDate can be used like this:");
        br.addElement("  (o):");
        br.addElement("    if firstDate is after:2018/05/03");
        br.addElement("  (x):");
        br.addElement("    if firstDate is not after:2018/05/03");
        br.addElement("    if firstDate is before:2018/05/03");
        br.addElement("    if firstDate is 2018/05/03");
        br.addItem("Statement");
        br.addElement(statement);
        br.addItem("If-Value");
        br.addElement(ifValue);
        final String msg = br.buildExceptionMessage();
        throw new DfSchemaPolicyCheckIllegalFirstDateException(msg);
    }
}
