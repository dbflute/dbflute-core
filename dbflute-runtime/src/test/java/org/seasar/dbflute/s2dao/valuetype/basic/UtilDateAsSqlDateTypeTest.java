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
package org.seasar.dbflute.s2dao.valuetype.basic;

import java.util.Date;

import junit.framework.TestCase;

import org.seasar.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 0.9.6.3 (2009/12/15 Tuesday)
 */
public class UtilDateAsSqlDateTypeTest extends TestCase {

    public void test_toUtilDate() {
        // ## Arrange ##
        UtilDateAsSqlDateType type = new UtilDateAsSqlDateType();
        java.sql.Date sqlDate = DfTypeUtil.toSqlDate(DfTypeUtil.toDate("2009/12/13"));

        // ## Act ##
        java.util.Date utilDate = type.toUtilDate(sqlDate);

        // ## Assert ##
        assertFalse(utilDate instanceof java.sql.Date);
        assertEquals("2009/12/13", DfTypeUtil.toString(utilDate, "yyyy/MM/dd"));
    }

    public void test_toSqlDate() {
        // ## Arrange ##
        UtilDateAsSqlDateType type = new UtilDateAsSqlDateType();
        Date utilDate = DfTypeUtil.toDate("2009/12/13");

        // ## Act ##
        java.sql.Date sqlDate = type.toSqlDate(utilDate);

        // ## Assert ##
        assertEquals("2009/12/13", DfTypeUtil.toString(sqlDate, "yyyy/MM/dd"));
    }
}
