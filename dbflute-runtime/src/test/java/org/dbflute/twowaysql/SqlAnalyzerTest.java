/*
 * Copyright 2014-2015 the original author or authors.
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
        return pmb;
    }

    protected CommandContext prepareCtx(SimpleMapPmb<Object> pmb, Node node) {
        CommandContextCreator creator = new CommandContextCreator(new String[] { "pmb" }, new Class<?>[] { pmb.getClass() });
        CommandContext ctx = creator.createCommandContext(new Object[] { pmb });
        node.accept(ctx);
        return ctx;
    }
}
