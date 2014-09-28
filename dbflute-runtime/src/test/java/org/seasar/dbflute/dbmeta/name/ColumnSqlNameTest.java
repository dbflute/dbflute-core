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
package org.seasar.dbflute.dbmeta.name;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.9.9.1B (2011/10/20 Thursday)
 */
public class ColumnSqlNameTest extends PlainTestCase {

    public void test_hasIrregularChar_basic() throws Exception {
        assertFalse(new ColumnSqlName("MEMBER_ID").hasIrregularChar());
        assertFalse(new ColumnSqlName("MEMBER_NAME").hasIrregularChar());
        assertFalse(new ColumnSqlName("MEMBER_ACCOUNT").hasIrregularChar());
        assertFalse(new ColumnSqlName("BIRTHDATE").hasIrregularChar());
        assertFalse(new ColumnSqlName("FORMALIZED_DATETIME").hasIrregularChar());
        assertFalse(new ColumnSqlName("PAYMENT_COMPLETE_FLG").hasIrregularChar());
        assertFalse(new ColumnSqlName("REGISTER_DATETIME").hasIrregularChar());
        assertFalse(new ColumnSqlName("UPDATE_USER").hasIrregularChar());
        assertFalse(new ColumnSqlName("member_id").hasIrregularChar());
        assertFalse(new ColumnSqlName("member_name").hasIrregularChar());
        assertFalse(new ColumnSqlName("member_account").hasIrregularChar());
        assertFalse(new ColumnSqlName("birthdate").hasIrregularChar());
        assertFalse(new ColumnSqlName("formalized_datetime").hasIrregularChar());
        assertFalse(new ColumnSqlName("payment_complete_flg").hasIrregularChar());
        assertFalse(new ColumnSqlName("register_datetime").hasIrregularChar());
        assertFalse(new ColumnSqlName("update_user").hasIrregularChar());
        assertFalse(new ColumnSqlName("memberId").hasIrregularChar());
        assertFalse(new ColumnSqlName("memberName").hasIrregularChar());
        assertFalse(new ColumnSqlName("member9Account3").hasIrregularChar());
        assertFalse(new ColumnSqlName("m0e2m304b516e728r9A0ccount").hasIrregularChar());
    }

    public void test_hasIrregularChar_quoted() throws Exception {
        assertTrue(new ColumnSqlName("\"MEMBER_ID\"").hasIrregularChar());
        assertTrue(new ColumnSqlName("'MEMBER_NAME'").hasIrregularChar());
        assertTrue(new ColumnSqlName("[MEMBER_ACCOUNT]").hasIrregularChar());
        assertTrue(new ColumnSqlName("\"BIRTHDATE").hasIrregularChar());
        assertTrue(new ColumnSqlName("FORMALIZED_DATETIME'").hasIrregularChar());
        assertTrue(new ColumnSqlName("PAYMENT_COMPLETE_FLG]").hasIrregularChar());
    }

    public void test_hasIrregularChar_mark() throws Exception {
        assertTrue(new ColumnSqlName("MEMBER$_ID").hasIrregularChar());
        assertTrue(new ColumnSqlName("MEMBER-NAME").hasIrregularChar());
        assertTrue(new ColumnSqlName("MEMB$ER_ACCOUNT").hasIrregularChar());
        assertTrue(new ColumnSqlName("BIRTHD#ATE").hasIrregularChar());
        assertTrue(new ColumnSqlName("FORMALIZED_DATE&TIME").hasIrregularChar());
        assertTrue(new ColumnSqlName("PAYMENT_COMP)LETE_FLG").hasIrregularChar());
        assertTrue(new ColumnSqlName("REGISTER_DATETIME!").hasIrregularChar());
        assertTrue(new ColumnSqlName("(UPDATE_USER").hasIrregularChar());
        assertTrue(new ColumnSqlName("member id").hasIrregularChar());
    }

    public void test_hasIrregularChar_doubleByte() throws Exception {
        assertTrue(new ColumnSqlName("MEMBER\uff05_ID").hasIrregularChar());
        assertTrue(new ColumnSqlName("MEMBER\uff06_NAME").hasIrregularChar());
        assertTrue(new ColumnSqlName("MEMBER\uff10_NAME").hasIrregularChar());
    }
}
