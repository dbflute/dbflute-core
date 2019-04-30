/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.twowaysql;

import org.dbflute.exception.CommentTerminatorNotFoundException;
import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 * @since 0.9.5 (2009/04/08 Wednesday)
 */
public class SqlTokenizerTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                                Skip
    //                                                                                ====
    public void test_skipToken() {
        // ## Arrange ##
        String sql = "/*IF pmb.memberName != null*/and member.MEMBER_NAME = 'TEST'/*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        tokenizer.next();
        tokenizer.skipWhitespace();
        String skippedToken = tokenizer.skipToken();
        tokenizer.skipWhitespace();

        // ## Assert ##
        log("skippedToken : " + skippedToken);
        log("before       : " + tokenizer.getBefore());
        log("after        : " + tokenizer.getAfter());
        assertEquals("and", skippedToken);
    }

    public void test_skipToken_integerTestValue() {
        // ## Arrange ##
        String sql = "/*foo*/123/*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        tokenizer.next();
        String skippedToken = tokenizer.skipToken();

        // ## Assert ##
        log("skippedToken : " + skippedToken);
        assertEquals("123", skippedToken);
    }

    public void test_skipToken_stringTestValue() {
        // ## Arrange ##
        String sql = "/*foo*/'2001-12-15'/*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        tokenizer.next();
        String skippedToken = tokenizer.skipToken();

        // ## Assert ##
        log("skippedToken : " + skippedToken);
        assertEquals("'2001-12-15'", skippedToken);
    }

    public void test_skipToken_nonTestValue_dateLiteralPrefix() {
        // ## Arrange ##
        String sql = "/*foo*/date '2001-12-15'/*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        tokenizer.next();
        String skippedToken = tokenizer.skipToken();

        // ## Assert ##
        log("skippedToken : " + skippedToken);
        assertEquals("date", skippedToken);
    }

    public void test_skipToken_testValue_dateLiteralPrefix() {
        // ## Arrange ##
        String sql = "/*foo*/date '2001-12-15'/*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        tokenizer.next();
        String skippedToken = tokenizer.skipToken(true);

        // ## Assert ##
        log("skippedToken : " + skippedToken);
        assertEquals("date '2001-12-15'", skippedToken);
    }

    public void test_skipToken_testValue_timestampLiteralPrefix() {
        // ## Arrange ##
        String sql = "/*foo*/timestamp '2001-12-15 12:34:56.123'/*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        tokenizer.next();
        String skippedToken = tokenizer.skipToken(true);

        // ## Assert ##
        log("skippedToken : " + skippedToken);
        assertEquals("timestamp '2001-12-15 12:34:56.123'", skippedToken);
    }

    public void test_skipWhitespace() {
        // ## Arrange ##
        String sql = "/*IF pmb.memberName != null*/ and member.MEMBER_NAME = 'TEST'/*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        tokenizer.next();
        tokenizer.skipWhitespace();
        String skippedToken = tokenizer.skipToken();
        tokenizer.skipWhitespace();

        // ## Assert ##
        log("skippedToken : " + skippedToken);
        log("before       : " + tokenizer.getBefore());
        log("after        : " + tokenizer.getAfter());
        assertEquals("and", skippedToken);
    }

    public void test_extractDateLiteralPrefix_date() {
        // ## Arrange ##
        String sql = "foo/*bar*/date '2009-10-29";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        String prefix = tokenizer.extractDateLiteralPrefix(true, sql, "foo/*bar*/".length());

        // ## Assert ##
        assertEquals("date ", prefix);
    }

    public void test_extractDateLiteralPrefix_dateNonSpace() {
        // ## Arrange ##
        String sql = "foo/*bar*/date'2009-10-29";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        String prefix = tokenizer.extractDateLiteralPrefix(true, sql, "foo/*bar*/".length());

        // ## Assert ##
        assertEquals("date", prefix);
    }

    public void test_extractDateLiteralPrefix_timestamp() {
        // ## Arrange ##
        String sql = "foo/*bar*/timestamp '2009-10-29";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        String prefix = tokenizer.extractDateLiteralPrefix(true, sql, "foo/*bar*/".length());

        // ## Assert ##
        assertEquals("timestamp ", prefix);
    }

    public void test_extractDateLiteralPrefix_timestampNonSpace() {
        // ## Arrange ##
        String sql = "foo/*bar*/timestamp'2009-10-29";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        String prefix = tokenizer.extractDateLiteralPrefix(true, sql, "foo/*bar*/".length());

        // ## Assert ##
        assertEquals("timestamp", prefix);
    }

    public void test_extractDateLiteralPrefix_dateUpperCase() {
        // ## Arrange ##
        String sql = "foo/*bar*/DATE '2009-10-29";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        String prefix = tokenizer.extractDateLiteralPrefix(true, sql, "foo/*bar*/".length());

        // ## Assert ##
        assertEquals("DATE ", prefix);
    }

    public void test_extractDateLiteralPrefix_timestampUpperCase() {
        // ## Arrange ##
        String sql = "foo/*bar*/TIMESTAMP '2009-10-29";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        String prefix = tokenizer.extractDateLiteralPrefix(true, sql, "foo/*bar*/".length());

        // ## Assert ##
        assertEquals("TIMESTAMP ", prefix);
    }

    public void test_extractDateLiteralPrefix_nonTarget() {
        // ## Arrange ##
        SqlTokenizer tokenizer = new SqlTokenizer(null);

        // ## Act & Assert ##
        int l = "foo/*bar*/".length();
        assertEquals(null, tokenizer.extractDateLiteralPrefix(true, "foo/*bar*/'2009-10-29", l));
        assertEquals(null, tokenizer.extractDateLiteralPrefix(true, "foo/*bar*/23", l));
        assertEquals(null, tokenizer.extractDateLiteralPrefix(true, "foo/*bar*/abc", l));
        assertEquals(null, tokenizer.extractDateLiteralPrefix(true, "foo/*bar*/date foo", l));
        assertEquals(null, tokenizer.extractDateLiteralPrefix(true, "foo/*bar*/timestamp bar", l));
    }

    // ===================================================================================
    //                                                                          Show Token
    //                                                                          ==========
    public void test_show_next_with_BEGIN_comment() {
        String sql = "select * from MEMBER";
        sql = sql + " /*BEGIN*/";
        sql = sql + " where";
        sql = sql + "   /*IF pmb.memberId != null*/member.MEMBER_ID = 3/*END*/";
        sql = sql + "   /*IF pmb.memberName != null*/and member.MEMBER_NAME = 'TEST'/*END*/";
        sql = sql + " /*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        log("01: " + tokenizer._token);
        tokenizer.next();
        log("02: " + tokenizer._token);
        tokenizer.next();
        log("03: " + tokenizer._token);
        tokenizer.next();
        log("04: " + tokenizer._token);
        tokenizer.next();
        log("05: " + tokenizer._token);
        tokenizer.next();
        log("06: " + tokenizer._token);
        tokenizer.next();
        log("07: " + tokenizer._token);
        tokenizer.next();
        log("08: " + tokenizer._token);
        tokenizer.next();
        log("09: " + tokenizer._token);
        tokenizer.next();
        log("10: " + tokenizer._token);
        tokenizer.next();
        log("11: " + tokenizer._token);
        tokenizer.next();
        log("12: " + tokenizer._token);
        tokenizer.next();
        log("13: " + tokenizer._token);
        tokenizer.next();
        log("14: " + tokenizer._token);
    }

    public void test_show_next_without_BEGIN_comment() {
        String sql = "select * from MEMBER";
        sql = sql + " where";
        sql = sql + "   /*IF pmb.memberId != null*/member.MEMBER_ID = 3/*END*/";
        sql = sql + "   /*IF pmb.memberName != null*/and member.MEMBER_NAME = 'TEST'/*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        log("01: " + tokenizer._token);
        tokenizer.next();
        log("02: " + tokenizer._token);
        tokenizer.next();
        log("03: " + tokenizer._token);
        tokenizer.next();
        log("04: " + tokenizer._token);
        tokenizer.next();
        log("05: " + tokenizer._token);
        tokenizer.next();
        log("06: " + tokenizer._token);
        tokenizer.next();
        log("07: " + tokenizer._token);
        tokenizer.next();
        log("08: " + tokenizer._token);
        tokenizer.next();
        log("09: " + tokenizer._token);
        tokenizer.next();
        log("10: " + tokenizer._token);
        tokenizer.next();
        log("11: " + tokenizer._token);
        tokenizer.next();
        log("12: " + tokenizer._token);
        tokenizer.next();
        log("13: " + tokenizer._token);
        tokenizer.next();
        log("14: " + tokenizer._token);
    }

    // ===================================================================================
    //                                                                           Exception
    //                                                                           =========
    public void test_commentEndNotFound() {
        // ## Arrange ##
        String sql = "/*IF pmb.memberName != null*/and member.MEMBER_NAME = 'TEST'/*END";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        try {
            while (SqlTokenizer.EOF != tokenizer.next()) {}

            // ## Assert ##
            fail();
        } catch (CommentTerminatorNotFoundException e) {
            // OK
            log(e.getMessage());
        }
    }
}
