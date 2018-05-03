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
package org.dbflute.logic.doc.spolicy.secretary;

import java.util.List;

import org.dbflute.unit.EngineTestCase;

/**
 * @author jflute
 * @since 1.1.2 (2017/1/3 Tuesday)
 */
public class DfSPolicyMiscSecretaryTest extends EngineTestCase {

    // ===================================================================================
    //                                                            splitClauseByConnector()
    //                                                            ========================
    public void test_splitClauseByConnector_and_and() {
        // ## Arrange ##
        DfSPolicyLogicalSecretary secretary = new DfSPolicyLogicalSecretary();

        // ## Act ##
        List<String> list =
                secretary.splitClauseByConnector("tableName is sea and alias is land and piari and tableName is bonvo", " and ");

        // ## Assert ##
        assertHasAnyElement(list);
        for (String element : list) {
            log(element);
        }
        assertEquals("tableName is sea", list.get(0));
        assertEquals("alias is land and piari", list.get(1));
        assertEquals("tableName is bonvo", list.get(2));
    }

    public void test_splitClauseByConnector_and_or() {
        // ## Arrange ##
        DfSPolicyLogicalSecretary secretary = new DfSPolicyLogicalSecretary();

        // ## Act ##
        List<String> list = secretary.splitClauseByConnector("tableName is sea and alias is land or piari and tableName is bonvo", " and ");

        // ## Assert ##
        assertHasAnyElement(list);
        for (String element : list) {
            log(element);
        }
        assertEquals("tableName is sea", list.get(0));
        assertEquals("alias is land or piari", list.get(1));
        assertEquals("tableName is bonvo", list.get(2));
    }

    public void test_splitClauseByConnector_or_or() {
        // ## Arrange ##
        DfSPolicyLogicalSecretary secretary = new DfSPolicyLogicalSecretary();

        // ## Act ##
        List<String> list = secretary.splitClauseByConnector("tableName is sea or alias is land or piari or tableName is bonvo", " or ");

        // ## Assert ##
        assertHasAnyElement(list);
        for (String element : list) {
            log(element);
        }
        assertEquals("tableName is sea", list.get(0));
        assertEquals("alias is land or piari", list.get(1));
        assertEquals("tableName is bonvo", list.get(2));
    }

    public void test_splitClauseByConnector_or_and() {
        // ## Arrange ##
        DfSPolicyLogicalSecretary secretary = new DfSPolicyLogicalSecretary();

        // ## Act ##
        List<String> list = secretary.splitClauseByConnector("tableName is sea or alias is land and piari or tableName is bonvo", " or ");

        // ## Assert ##
        assertHasAnyElement(list);
        for (String element : list) {
            log(element);
        }
        assertEquals("tableName is sea", list.get(0));
        assertEquals("alias is land and piari", list.get(1));
        assertEquals("tableName is bonvo", list.get(2));
    }
}
