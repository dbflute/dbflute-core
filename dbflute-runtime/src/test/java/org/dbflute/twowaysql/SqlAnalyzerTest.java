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
package org.dbflute.twowaysql;

import org.dbflute.twowaysql.context.CommandContext;
import org.dbflute.twowaysql.context.CommandContextCreator;
import org.dbflute.twowaysql.exception.BindVariableCommentIllegalParameterBeanSpecificationException;
import org.dbflute.twowaysql.node.Node;
import org.dbflute.twowaysql.node.SqlPartsNode;
import org.dbflute.twowaysql.pmbean.SimpleMapPmb;
import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 * @since 0.9.5 (2009/04/08 Wednesday)
 */
public class SqlAnalyzerTest extends RuntimeTestCase {

    // *detail tests for analyze() are moved to node tests
    // ===================================================================================
    //                                                                          IF Comment
    //                                                                          ==========
    public void test_analyze_IF_keepLine_if_true() {
        // ## Arrange ##
        SimpleMapPmb<Object> pmb = preparePmb();
        StringBuilder sb = new StringBuilder();
        sb.append("select *");
        sb.append(ln());
        sb.append(ln()).append("/*IF pmb.sea*/");
        sb.append(ln()).append("mystic");
        sb.append(ln()).append("/*END*/");
        sb.append(ln()).append("/*IF pmb.land*/oneman/*END*/");
        sb.append(ln()).append("/*$pmb.iks*/");
        String twoway = sb.toString();
        SqlAnalyzer analyzer = new SqlAnalyzer(twoway, false);

        // ## Act ##
        Node node = analyzer.analyze();

        // ## Assert ##
        CommandContext ctx = prepareCtx(pmb, node);
        String sql = ctx.getSql();
        log(ln() + sql);
        assertEquals("select *\n\n\nmystic\n\n\namba", sql);
        assertEquals(0, ctx.getBindVariables().length);
    }

    public void test_analyze_IF_keepLine_if_false() {
        // ## Arrange ##
        SimpleMapPmb<Object> pmb = preparePmb();
        StringBuilder sb = new StringBuilder();
        sb.append("select *");
        sb.append(ln());
        sb.append(ln()).append("/*IF pmb.sea*/mystic/*END*/");
        sb.append(ln()).append("/*IF pmb.land*/");
        sb.append(ln()).append("oneman");
        sb.append(ln()).append("/*END*/");
        sb.append(ln()).append("/*$pmb.iks*/");
        String twoway = sb.toString();
        SqlAnalyzer analyzer = new SqlAnalyzer(twoway, false);

        // ## Act ##
        Node node = analyzer.analyze();

        // ## Assert ##
        CommandContext ctx = prepareCtx(pmb, node);
        String sql = ctx.getSql();
        log(ln() + sql);
        assertEquals("select *\n\nmystic\n\namba", sql);
        assertEquals(0, ctx.getBindVariables().length);
    }

    // ===================================================================================
    //                                                                         FOR Comment
    //                                                                         ===========
    public void test_analyze_FOR_keepLine_if_true() {
        // ## Arrange ##
        SimpleMapPmb<Object> pmb = preparePmb();
        StringBuilder sb = new StringBuilder();
        sb.append("select *");
        sb.append(ln());
        sb.append(ln()).append("/*FOR pmb.dstore*/");
        sb.append(ln()).append("/*$#current*/mystic");
        sb.append(ln()).append("/*END*/");
        sb.append(ln()).append("/*FOR pmb.dstore*/oneman/*END*/");
        sb.append(ln()).append("/*$pmb.iks*/");
        String twoway = sb.toString();
        SqlAnalyzer analyzer = new SqlAnalyzer(twoway, false);

        // ## Act ##
        Node node = analyzer.analyze();

        // ## Assert ##
        CommandContext ctx = prepareCtx(pmb, node);
        String sql = ctx.getSql();
        log(ln() + sql);
        assertEquals("select *\n\n\nuni\n\ncity\n\nonemanoneman\namba", sql);
        assertEquals(0, ctx.getBindVariables().length);
    }

    public void test_analyze_FOR_keepLine_if_false() {
        // ## Arrange ##
        StringBuilder sb = new StringBuilder();
        sb.append("select *");
        sb.append(ln());
        sb.append(ln()).append("/*FOR pmb.bonvo*/");
        sb.append(ln()).append("/*$#current*/mystic");
        sb.append(ln()).append("/*END*/");
        sb.append(ln()).append("/*FOR pmb.bonvo*/oneman/*END*/");
        sb.append(ln()).append("/*$pmb.iks*/");
        String twoway = sb.toString();
        SqlAnalyzer analyzer = new SqlAnalyzer(twoway, false);

        // ## Act ##
        Node node = analyzer.analyze();

        // ## Assert ##
        SimpleMapPmb<Object> pmb = preparePmb();
        CommandContext ctx = prepareCtx(pmb, node);
        String sql = ctx.getSql();
        log(ln() + sql);
        assertEquals("select *\n\n\n\namba", sql);
        assertEquals(0, ctx.getBindVariables().length);
    }

    public void test_analyze_FOR_loopVariable() {
        // ## Arrange ##
        StringBuilder sb = new StringBuilder();
        sb.append("select *");
        sb.append(ln());
        sb.append(ln()).append("/*FOR pmb.dstore*//*FIRST*/han/*END*/");
        sb.append(ln()).append("/*$#current*/mystic");
        sb.append(ln()).append("/*LAST*/gar/*END*//*END*/");
        sb.append(ln()).append("/*FOR pmb.bonvo*/oneman/*END*/");
        sb.append(ln()).append("/*$pmb.iks*/");
        String twoway = sb.toString();
        SqlAnalyzer analyzer = new SqlAnalyzer(twoway, false);

        // ## Act ##
        Node node = analyzer.analyze();

        // ## Assert ##
        SimpleMapPmb<Object> pmb = preparePmb();
        CommandContext ctx = prepareCtx(pmb, node);
        String sql = ctx.getSql();
        log(ln() + sql);
        assertEquals("select *\n\nhan\nuni\n\ncity\ngar\n\namba", sql);
        assertEquals(0, ctx.getBindVariables().length);
    }

    // ===================================================================================
    //                                                                BindVariable Comment
    //                                                                ====================
    public void test_analyze_bindVariable_basic() {
        // ## Arrange ##
        String twoway = "/*BEGIN*/where";
        twoway = twoway + " /*IF pmb.sea*/member.MEMBER_ID = /*pmb.iks*/'abc'/*END*/";
        twoway = twoway + " /*IF pmb.land*/and member.MEMBER_NAME = /*pmb.iks*/'stu'/*END*/";
        twoway = twoway + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(twoway, false);

        // ## Act ##
        Node node = analyzer.analyze();

        // ## Assert ##
        SimpleMapPmb<Object> pmb = preparePmb();
        CommandContext ctx = prepareCtx(pmb, node);
        String sql = ctx.getSql();
        log(ln() + sql);
        String expected = "where member.MEMBER_ID = ? ";
        assertEquals(expected, sql);
        assertEquals(1, ctx.getBindVariables().length);
        assertEquals("amba", ctx.getBindVariables()[0]);
    }

    public void test_analyze_bindVariable_nonSwitchToEmbedded_meansNormal() {
        // ## Arrange ##
        String twoway = "/*BEGIN*/where";
        twoway = twoway + " /*IF pmb.sea*/member.MEMBER_ID = /*pmb.iks*/'abc'/*END*/";
        twoway = twoway + " /*IF pmb.land*/and member.MEMBER_NAME = /*pmb.iks*/'stu'/*END*/";
        twoway = twoway + " /*FOR pmb.dstore*/";
        twoway = twoway + "   /*#current*/mystic";
        twoway = twoway + " /*END*/";
        twoway = twoway + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(twoway, false);

        // ## Act ##
        Node node = analyzer.analyze();

        // ## Assert ##
        SimpleMapPmb<Object> pmb = preparePmb();
        CommandContext ctx = prepareCtx(pmb, node);
        String sql = ctx.getSql();
        log(ln() + sql);
        String expected = "where member.MEMBER_ID = ?     ?    ? ";
        assertEquals(expected, sql);
        assertEquals(3, ctx.getBindVariables().length);
    }

    public void test_analyze_bindVariable_switchToEmbedded_dangerous() {
        // ## Arrange ##
        String twoway = "/*BEGIN*/where";
        twoway = twoway + " /*IF pmb.sea*/member.MEMBER_ID = /*pmb.iks*/'abc'/*END*/";
        twoway = twoway + " /*IF pmb.land*/and member.MEMBER_NAME = /*pmb.iks*/'stu'/*END*/";
        twoway = twoway + " /*FOR pmb.dstore*/";
        twoway = twoway + "   /*#current*/mystic";
        twoway = twoway + " /*END*/";
        twoway = twoway + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(twoway, false).switchBindingToReplaceOnlyEmbedded();

        // ## Act ##
        Node node = analyzer.analyze();

        // ## Assert ##
        SimpleMapPmb<Object> pmb = preparePmb();
        CommandContext ctx = prepareCtx(pmb, node);
        String sql = ctx.getSql();
        log(ln() + sql);
        String expected = "where member.MEMBER_ID = 'amba''abc'     unimystic    citymystic ";
        assertEquals(expected, sql);
        assertEquals(0, ctx.getBindVariables().length);
    }

    // ===================================================================================
    //                                                                      Native Binding
    //                                                                      ==============
    public void test_analyze_nativeBinding_cannotUse_inSql() {
        // ## Arrange ##
        SqlAnalyzer analyzer = new SqlAnalyzer("select ?", false);

        // ## Act ##
        Node node = analyzer.analyze();

        // ## Assert ##
        SimpleMapPmb<Object> pmb = preparePmb();
        assertException(BindVariableCommentIllegalParameterBeanSpecificationException.class, () -> prepareCtx(pmb, node));
    }

    public void test_analyze_nativeBinding_keep_inBlockComment() {
        // ## Arrange ##
        SqlAnalyzer analyzer = new SqlAnalyzer("select *\n/* sea? */\nfrom ...", false);

        // ## Act ##
        Node node = analyzer.analyze();

        // ## Assert ##
        prepareCtx(preparePmb(), node); // expects no exception
    }

    public void test_analyze_nativeBinding_keep_inLineComment() {
        // ## Arrange ##
        SqlAnalyzer analyzer = new SqlAnalyzer("select *\n-- sea?\nfrom ...", false);

        // ## Act ##
        Node node = analyzer.analyze();

        // ## Assert ##
        // exception but replace to 'Q' in other process actually
        SimpleMapPmb<Object> pmb = preparePmb();
        assertException(BindVariableCommentIllegalParameterBeanSpecificationException.class, () -> prepareCtx(pmb, node));
    }

    public void test_analyze_nativeBinding_overlook() {
        // ## Arrange ##
        SqlAnalyzer analyzer = new SqlAnalyzer("select ?", false).overlookNativeBinding();

        // ## Act ##
        Node node = analyzer.analyze();

        // ## Assert ##
        SimpleMapPmb<Object> pmb = preparePmb();
        CommandContext ctx = prepareCtx(pmb, node);
        String sql = ctx.getSql();
        assertEquals("select ?", sql);
        assertEquals(0, ctx.getBindVariables().length);
    }

    // ===================================================================================
    //                                                                            SQL Node
    //                                                                            ========
    public void test_createSqlNode() {
        // ## Arrange ##
        SqlAnalyzer analyzer = new SqlAnalyzer("foobar", false);

        // ## Act ##
        SqlPartsNode node = analyzer.createSqlPartsNode("foo");

        // ## Assert ##
        assertEquals("foo", node.getSqlParts());
    }

    // ===================================================================================
    //                                                                         Test Helper
    //                                                                         ===========
    protected SimpleMapPmb<Object> preparePmb() {
        SimpleMapPmb<Object> pmb = new SimpleMapPmb<Object>();
        pmb.addParameter("sea", true);
        pmb.addParameter("land", false);
        pmb.addParameter("iks", "amba");
        pmb.addParameter("dstore", newArrayList("uni", "city"));
        pmb.addParameter("bonvo", newArrayList());
        return pmb;
    }

    protected CommandContext prepareCtx(SimpleMapPmb<Object> pmb, Node node) {
        CommandContextCreator creator = new CommandContextCreator(new String[] { "pmb" }, new Class<?>[] { pmb.getClass() });
        CommandContext ctx = creator.createCommandContext(new Object[] { pmb });
        node.accept(ctx);
        return ctx;
    }
}
