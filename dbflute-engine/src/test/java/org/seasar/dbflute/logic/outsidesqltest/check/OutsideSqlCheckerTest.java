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
package org.seasar.dbflute.logic.outsidesqltest.check;

import org.seasar.dbflute.exception.DfCustomizeEntityMarkInvalidException;
import org.seasar.dbflute.exception.DfParameterBeanMarkInvalidException;
import org.seasar.dbflute.logic.outsidesqltest.DfOutsideSqlChecker;
import org.seasar.dbflute.twowaysql.exception.EndCommentNotFoundException;
import org.seasar.dbflute.twowaysql.exception.IfCommentConditionEmptyException;
import org.seasar.dbflute.twowaysql.exception.IfCommentUnsupportedExpressionException;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.9.6 (2009/10/25 Sunday)
 */
public class OutsideSqlCheckerTest extends PlainTestCase {

    public void test_check_basic() {
        // ## Arrange ##
        DfOutsideSqlChecker ker = new DfOutsideSqlChecker();
        String fn = "test.sql";

        // ## Act & Assert ##
        ker.check(fn, "-- #df:entity#\n-- !df:pmb!\nfoo /*IF pmb.memberId != null*/bar/*END*/");
        ker.check(fn, "-- #df:entity#\n-- !df:pmb!\nfoo /*IF pmb.memberId != null && pmb.existsPurchase*/bar/*END*/");
        ker.check(fn, "-- #df:entity#\nfoo /*IF pmb.getMemberId() != null || pmb.isExistsPurchase()*/bar/*END*/");
        ker.check(fn, "-- !df:pmb!\nfoo /*IF pmb.memberName == 'abc'*/bar/*END*/");
    }

    public void test_check_customizeEntity() {
        // ## Arrange ##
        DfOutsideSqlChecker ker = new DfOutsideSqlChecker();
        String fn = "test.sql";

        // ## Act ##
        try {
            ker.check(fn, "-- #df;entity#\n-- !df:pmb!\nfoo /*IF pmb.memberId != null*/bar/*END*/");

            // ## Assert ##
            fail();
        } catch (DfCustomizeEntityMarkInvalidException e) {
            // OK
            log(e.getMessage());
        }
        // ## Act ##
        try {
            ker.check(fn, "-- #df:pmb#\n-- !df:pmb!\nfoo /*IF pmb.memberId != null*/bar/*END*/");

            // ## Assert ##
            fail();
        } catch (DfCustomizeEntityMarkInvalidException e) {
            // OK
            log(e.getMessage());
        }
        // ## Act ##
        try {
            ker.check(fn, "-- #df:emtity#\n-- !df:pmb!\nfoo /*IF pmb.memberId != null*/bar/*END*/");

            // ## Assert ##
            fail();
        } catch (DfCustomizeEntityMarkInvalidException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_check_parameterBean() {
        // ## Arrange ##
        DfOutsideSqlChecker ker = new DfOutsideSqlChecker();
        String fn = "test.sql";

        // ## Act ##
        try {
            ker.check(fn, "-- #df:entity#\n-- !df;pmb!\nfoo /*IF pmb.memberId != null*/bar/*END*/");

            // ## Assert ##
            fail();
        } catch (DfParameterBeanMarkInvalidException e) {
            // OK
            log(e.getMessage());
        }
        // ## Act ##
        try {
            ker.check(fn, "-- #df:entity#\n-- !df:entity!\nfoo /*IF pmb.memberId != null*/bar/*END*/");

            // ## Assert ##
            fail();
        } catch (DfParameterBeanMarkInvalidException e) {
            // OK
            log(e.getMessage());
        }
        // ## Act ##
        try {
            ker.check(fn, "-- #df:entity#\n-- !df:pnb!\nfoo /*IF pmb.memberId != null*/bar/*END*/");

            // ## Assert ##
            fail();
        } catch (DfParameterBeanMarkInvalidException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_check_parameterComment() {
        // ## Arrange ##
        DfOutsideSqlChecker ker = new DfOutsideSqlChecker();
        String fn = "test.sql";

        // ## Act ##
        try {
            ker.check(fn, "-- #df:entity#\n-- !df:pmb!\nfoo /*IF pmb.memberId != null*/bar");

            // ## Assert ##
            fail();
        } catch (EndCommentNotFoundException e) {
            // OK
            log(e.getMessage());
        }
        // ## Act ##
        try {
            ker.check(fn, "-- #df:entity#\n-- !df:pmb!\nfoo /*IF */bar/*END*/");

            // ## Assert ##
            fail();
        } catch (IfCommentConditionEmptyException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_check_ifCommentExpression_basic() {
        // ## Arrange ##
        DfOutsideSqlChecker ker = new DfOutsideSqlChecker();
        String fn = "test.sql";

        // ## Act & Assert ##
        ker.check(fn, "/*IF pmb.memberId != null && pmb.memberName != null*/bar/*END*/");
        ker.check(fn, "/*IF pmb.memberId != null || pmb.memberName != null*/bar/*END*/");
        ker.check(fn, "/*IF pmb.getMemberId() != null || pmb.memberName != null*/bar/*END*/");
    }

    public void test_check_ifCommentExpression_unsupported() {
        // ## Arrange ##
        DfOutsideSqlChecker ker = new DfOutsideSqlChecker();
        String fn = "test.sql";

        // ## Act & Assert ##
        try {
            ker.check(fn, "/*IF (pmb.memberId != null && pmb.memberName != null) || pmb.exists*/bar/*END*/");
        } catch (IfCommentUnsupportedExpressionException e) {
            // OK
            log(e.getMessage());
        }
        try {
            ker.check(fn, "/*IF pmb.memberId != null && pmb.memberName != null || pmb.exists*/bar/*END*/");
        } catch (IfCommentUnsupportedExpressionException e) {
            // OK
            log(e.getMessage());
        }
        try {
            ker.check(fn, "/*IF pmb.memberId = null && pmb.memberName != null*/bar/*END*/");
        } catch (IfCommentUnsupportedExpressionException e) {
            // OK
            log(e.getMessage());
        }
        try {
            ker.check(fn, "/*IF pmb.memberId <> null && pmb.memberName != null*/bar/*END*/");
        } catch (IfCommentUnsupportedExpressionException e) {
            // OK
            log(e.getMessage());
        }
        try {
            ker.check(fn, "/*IF pmb.memberName == \"abc\"*/bar/*END*/");
        } catch (IfCommentUnsupportedExpressionException e) {
            // OK
            log(e.getMessage());
        }
    }
}
