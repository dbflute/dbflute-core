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
package org.seasar.dbflute.logic.sql2entity.analyzer;

import java.util.Map;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.9.6 (2009/10/26 Monday)
 */
public class DfSql2EntityMarkAnalyzerTest extends PlainTestCase {

    // ===================================================================================
    //                                                                   Title/Description
    //                                                                   =================
    public void test_getDescription_basic() {
        // ## Arrange ##
        DfSql2EntityMarkAnalyzer analyzer = new DfSql2EntityMarkAnalyzer();
        String descriptionMark = DfSql2EntityMarkAnalyzer.DESCRIPTION_MARK;
        String sql = "/*" + descriptionMark + "\n foo \n*/ select from";

        // ## Act ##
        String description = analyzer.getDescription(sql);

        // ## Assert ##
        assertEquals(" foo", description);
    }

    // ===================================================================================
    //                                                                 SelectColumnComment
    //                                                                 ===================
    public void test_getSelectColumnCommentMap_basic() {
        // ## Arrange ##
        DfSql2EntityMarkAnalyzer analyzer = new DfSql2EntityMarkAnalyzer();
        StringBuilder sb = new StringBuilder();
        sb.append("select FOO as FOO_ID -- // Comment1");
        sb.append(ln()).append(" , foo.BAR_NAME -- // Comment2");
        sb.append(ln()).append(" , BAZ_DATE -- // Comment3");

        // ## Act ##
        Map<String, String> commentMap = analyzer.getSelectColumnCommentMap(sb.toString());

        // ## Assert ##
        log(commentMap);
        assertEquals("Comment1", commentMap.get("FOO_ID"));
        assertEquals("Comment2", commentMap.get("BAR_NAME"));
        assertEquals("Comment3", commentMap.get("BAZ_DATE"));
    }

    public void test_getSelectColumnCommentMap_irregular() {
        // ## Arrange ##
        DfSql2EntityMarkAnalyzer analyzer = new DfSql2EntityMarkAnalyzer();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT FOO_ID -- //Comment1");
        sb.append(ln()).append(" , foo.BAR_NAME -- abc //Comment2");
        sb.append(ln()).append(" BAZ_DATE -- // Comment3");
        sb.append(ln()).append(" QUX_DATE, -- // Comment4");
        sb.append(ln()).append(" , 0 as QUUX_DATE -- // Comment5");
        sb.append(ln()).append(" max(foo) as CORGE_DATE, -- // Comment6");

        // ## Act ##
        Map<String, String> commentMap = analyzer.getSelectColumnCommentMap(sb.toString());

        // ## Assert ##
        log(commentMap);
        assertEquals("Comment1", commentMap.get("FOO_ID"));
        assertEquals("Comment2", commentMap.get("BAR_NAME"));
        assertEquals("Comment3", commentMap.get("BAZ_DATE"));
        assertEquals("Comment4", commentMap.get("QUX_DATE"));
        assertEquals("Comment5", commentMap.get("QUUX_DATE"));
        assertEquals("Comment6", commentMap.get("CORGE_DATE"));
    }

    public void test_getSelectColumnCommentMap_real() {
        // ## Arrange ##
        DfSql2EntityMarkAnalyzer analyzer = new DfSql2EntityMarkAnalyzer();
        StringBuilder sb = new StringBuilder();
        sb.append("select member.MEMBER_ID");
        sb.append(ln()).append(" , member.MEMBER_NAME");
        sb.append(ln()).append(" , member.BIRTHDATE -- // select column comment here (no as)");
        sb.append(ln()).append(
                " , member.FORMALIZED_DATETIME as FORMALIZED_DATETIME -- // select column comment here (using as)");
        sb.append(ln()).append(" , member.MEMBER_STATUS_CODE -- for Classification Test of Sql2Entity");
        sb.append(ln()).append(" , memberStatus.MEMBER_STATUS_NAME");
        sb.append(ln()).append(" , memberStatus.DISPLAY_ORDER as STATUS_DISPLAY_ORDER -- for Alias Name Test");
        sb.append(ln()).append(" , 0 as DUMMY_FLG -- // for Classification Test of Sql2Entity");
        sb.append(ln()).append(" , 0 as DUMMY_NOFLG -- // for Classification Test of Sql2Entity");

        // ## Act ##
        Map<String, String> commentMap = analyzer.getSelectColumnCommentMap(sb.toString());

        // ## Assert ##
        log(commentMap);
        assertEquals("select column comment here (no as)", commentMap.get("BIRTHDATE"));
        assertEquals("select column comment here (using as)", commentMap.get("FORMALIZED_DATETIME"));
        assertEquals("for Classification Test of Sql2Entity", commentMap.get("DUMMY_FLG"));
        assertEquals("for Classification Test of Sql2Entity", commentMap.get("DUMMY_NOFLG"));
    }
}
