/*
 * Copyright 2014-2017 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.dbflute.exception.ParameterCommentNotAllowedInitialCharacterException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.twowaysql.context.CommandContext;
import org.dbflute.twowaysql.context.CommandContextCreator;
import org.dbflute.twowaysql.exception.EndCommentNotFoundException;
import org.dbflute.twowaysql.exception.ForCommentExpressionEmptyException;
import org.dbflute.twowaysql.exception.IfCommentConditionEmptyException;
import org.dbflute.twowaysql.factory.DefaultNodeAdviceFactory;
import org.dbflute.twowaysql.factory.NodeAdviceFactory;
import org.dbflute.twowaysql.factory.SqlAnalyzerFactory;
import org.dbflute.twowaysql.node.BeginNode;
import org.dbflute.twowaysql.node.BindVariableNode;
import org.dbflute.twowaysql.node.ElseNode;
import org.dbflute.twowaysql.node.EmbeddedVariableNode;
import org.dbflute.twowaysql.node.ForNode;
import org.dbflute.twowaysql.node.ForNode.LoopVariableType;
import org.dbflute.twowaysql.node.IfNode;
import org.dbflute.twowaysql.node.LoopAbstractNode;
import org.dbflute.twowaysql.node.LoopFirstNode;
import org.dbflute.twowaysql.node.LoopLastNode;
import org.dbflute.twowaysql.node.LoopNextNode;
import org.dbflute.twowaysql.node.Node;
import org.dbflute.twowaysql.node.RootNode;
import org.dbflute.twowaysql.node.SqlConnectorAdjustable;
import org.dbflute.twowaysql.node.SqlConnectorNode;
import org.dbflute.twowaysql.node.SqlPartsNode;
import org.dbflute.twowaysql.style.BoundDateDisplayStyle;
import org.dbflute.util.Srl;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class SqlAnalyzer {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final NodeAdviceFactory _defaultNodeAdviceFactory = new DefaultNodeAdviceFactory();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _specifiedSql;
    protected final boolean _blockNullParameter;
    protected final SqlTokenizer _tokenizer;
    protected boolean _overlookNativeBinding; // unused on DBFlute, for general purpose
    protected boolean _switchBindingToReplaceOnlyEmbedded; // dangerous, unused on DBFlute, for general purpose
    protected final Stack<Node> _nodeStack = new Stack<Node>();
    protected boolean _inBeginScope;
    protected List<String> _researchIfCommentList;
    protected List<String> _researchForCommentList;
    protected List<String> _researchBindVariableCommentList;
    protected List<String> _researchEmbeddedVariableCommentList;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SqlAnalyzer(String sql, boolean blockNullParameter) {
        _specifiedSql = filterAtFirst(sql);
        _blockNullParameter = blockNullParameter;
        _tokenizer = createSqlTokenizer(_specifiedSql);
    }

    protected String filterAtFirst(String sql) {
        return removeTerminalMarkAtFirst(trimSqlAtFirst(sql));
    }

    protected String trimSqlAtFirst(String sql) {
        return sql.trim();
    }

    protected String removeTerminalMarkAtFirst(String sql) {
        return sql.endsWith(";") ? sql.substring(0, sql.length() - 1) : sql;
    }

    protected SqlTokenizer createSqlTokenizer(String sql) {
        final SqlTokenizer tokenizer = newSqlTokenizer(sql);
        if (_overlookNativeBinding) {
            tokenizer.overlookNativeBinding();
        }
        return tokenizer;
    }

    protected SqlTokenizer newSqlTokenizer(String sql) {
        return new SqlTokenizer(sql);
    }

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    /**
     * [Attention] unused on DBFlute, for general purpose
     * @return this. (NotNull)
     */
    public SqlAnalyzer overlookNativeBinding() {
        _overlookNativeBinding = true;
        _tokenizer.overlookNativeBinding(); // reflect existing instance
        return this;
    }

    /**
     * [Attention] dangerous, be careful, and unused on DBFlute, basically for general purpose
     * @return this. (NotNull)
     */
    public SqlAnalyzer switchBindingToReplaceOnlyEmbedded() {
        _switchBindingToReplaceOnlyEmbedded = true;
        return this;
    }

    // ===================================================================================
    //                                                                             Analyze
    //                                                                             =======
    public Node analyze() {
        push(createRootNode()); // root node of all
        while (SqlTokenizer.EOF != _tokenizer.next()) {
            parseToken();
        }
        return pop();
    }

    protected RootNode createRootNode() {
        return new RootNode();
    }

    protected void parseToken() {
        switch (_tokenizer.getTokenType()) {
        case SqlTokenizer.SQL:
            parseSql();
            break;
        case SqlTokenizer.COMMENT:
            parseComment();
            break;
        case SqlTokenizer.ELSE:
            parseElse();
            break;
        case SqlTokenizer.BIND_VARIABLE:
            parseBindVariable();
            break;
        }
    }

    // ===================================================================================
    //                                                                           SQL Parts
    //                                                                           =========
    protected void parseSql() {
        final String sql;
        {
            String token = _tokenizer.getToken();
            if (isElseMode()) {
                token = replaceString(token, "--", "");
            }
            sql = token;
        }
        final Node node = peek();
        if (isSqlConnectorAdjustable(node)) {
            processSqlConnectorAdjustable(node, sql);
        } else {
            node.addChild(createSqlPartsNodeOutOfConnector(node, sql));
        }
    }

    protected void processSqlConnectorAdjustable(Node node, String sql) {
        final SqlTokenizer st = createSqlTokenizer(sql);
        st.skipWhitespace();
        final String skippedToken = st.skipToken(); // determination for and/or (also skip process)
        st.skipWhitespace();
        if (processSqlConnectorMark(node, sql)) { // comma, ...
            return;
        }
        if (processSqlConnectorCondition(node, st, skippedToken)) { // and/or
            return;
        }
        // is not connector
        node.addChild(createSqlPartsNodeThroughConnector(node, sql));
    }

    protected boolean processSqlConnectorMark(Node node, String sql) {
        if (doProcessSqlConnectorMark(node, sql, ",")) { // comma
            return true;
        }
        return false;
    }

    protected boolean doProcessSqlConnectorMark(Node node, String sql, String mark) {
        final String ltrimmedSql = Srl.ltrim(sql); // for mark
        if (ltrimmedSql.startsWith(mark)) { // is connector
            final String markSpace = mark + " ";
            final String realMark = ltrimmedSql.startsWith(markSpace) ? markSpace : mark;
            node.addChild(createSqlConnectorNode(node, realMark, ltrimmedSql.substring(realMark.length())));
            return true;
        }
        return false;
    }

    protected boolean processSqlConnectorCondition(Node node, SqlTokenizer st, String skippedToken) {
        if ("and".equalsIgnoreCase(skippedToken) || "or".equalsIgnoreCase(skippedToken)) { // is connector
            node.addChild(createSqlConnectorNode(node, st.getBefore(), st.getAfter()));
            return true;
        }
        return false;
    }

    protected boolean isSqlConnectorAdjustable(Node node) {
        if (node.getChildSize() > 0) {
            return false;
        }
        return (node instanceof SqlConnectorAdjustable) && !isTopBegin(node);
    }

    // ===================================================================================
    //                                                                         SQL Comment
    //                                                                         ===========
    protected void parseComment() {
        final String comment = _tokenizer.getToken();
        if (isTargetComment(comment)) { // parameter comment
            if (isBeginComment(comment)) {
                parseBegin();
            } else if (isIfComment(comment)) {
                parseIf();
            } else if (isForComment(comment)) {
                parseFor();
            } else if (isLoopVariableComment(comment)) {
                parseLoopVariable();
            } else if (isEndComment(comment)) {
                return;
            } else {
                parseCommentBindVariable();
            }
        } else if (Srl.is_NotNull_and_NotTrimmedEmpty(comment)) { // plain comment
            if (isFrequentlyMistakePattern(comment)) {
                throwParameterCommentNotAllowedInitialCharacterException(comment);
            }
            final String before = _tokenizer.getBefore();
            final String content = before.substring(before.lastIndexOf("/*"));
            peek().addChild(createSqlPartsNode(content));
        }
    }

    protected boolean isTargetComment(String comment) {
        if (Srl.is_Null_or_TrimmedEmpty(comment)) {
            return false;
        }
        if (!isSpecialInitChar(comment) && !isTargetCommentFirstChar(comment)) {
            return false;
        }
        return true;
    }

    protected boolean isSpecialInitChar(String comment) {
        return comment.startsWith(ForNode.CURRENT_VARIABLE); // except current variable from check
    }

    protected boolean isTargetCommentFirstChar(String comment) {
        return Character.isJavaIdentifierStart(comment.charAt(0));
    }

    protected boolean isFrequentlyMistakePattern(String comment) {
        return comment.startsWith(" pmb."); // e.g. /* pmb.memberName */
    }

    protected void throwParameterCommentNotAllowedInitialCharacterException(String comment) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The initial character in the parameter comment was not allowed.");
        br.addItem("Advice");
        br.addElement("The parameter comment should start with identifier.");
        br.addElement("Fix like this:");
        br.addElement("  (x) - /* pmb.memberName */");
        br.addElement("  (o) - /*pmb.memberName*/");
        br.addElement("");
        br.addElement("Or rewrite your plain comment:");
        br.addElement("  (x) - /* pmb. so ...(as plain comment) */");
        br.addElement("  (o) - /* this is pmb. so ...(as plain comment) */");
        br.addItem("Checked Comment");
        br.addElement("/*" + comment + "*/");
        br.addItem("Specified SQL");
        br.addElement(_specifiedSql);
        final String msg = br.buildExceptionMessage();
        throw new ParameterCommentNotAllowedInitialCharacterException(msg);
    }

    // ===================================================================================
    //                                                                       BEGIN Comment
    //                                                                       =============
    protected boolean isBeginComment(String comment) {
        return BeginNode.MARK.equals(comment);
    }

    protected void parseBegin() {
        final BeginNode beginNode = createBeginNode();
        try {
            _inBeginScope = true;
            peek().addChild(beginNode);
            push(beginNode);
            parseEnd();
        } finally {
            _inBeginScope = false;
        }
    }

    protected BeginNode createBeginNode() {
        return newBeginNode(_inBeginScope);
    }

    protected BeginNode newBeginNode(boolean inBeginScope) {
        return new BeginNode(inBeginScope);
    }

    protected boolean isTopBegin(Node node) {
        if (!(node instanceof BeginNode)) {
            return false;
        }
        return !((BeginNode) node).isNested();
    }

    protected boolean isNestedBegin(Node node) {
        if (!(node instanceof BeginNode)) {
            return false;
        }
        return ((BeginNode) node).isNested();
    }

    // ===================================================================================
    //                                                                          IF Comment
    //                                                                          ==========
    protected boolean isIfComment(String comment) {
        return comment.startsWith(IfNode.PREFIX);
    }

    protected void parseIf() {
        final String comment = _tokenizer.getToken();
        final String condition = comment.substring(IfNode.PREFIX.length()).trim();
        if (Srl.is_Null_or_TrimmedEmpty(condition)) {
            throwIfCommentConditionEmptyException();
        }
        final IfNode ifNode = createIfNode(condition);
        peek().addChild(ifNode);
        push(ifNode);
        parseEnd();
    }

    protected IfNode createIfNode(String expr) {
        researchIfNeeds(_researchIfCommentList, expr); // for research
        return newIfNode(expr, _specifiedSql);
    }

    protected IfNode newIfNode(String expr, String specifiedSql) {
        return new IfNode(expr, specifiedSql);
    }

    protected void throwIfCommentConditionEmptyException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The condition of IF comment was empty!");
        br.addItem("Advice");
        br.addElement("Please confirm the IF comment expression.");
        br.addElement("Your IF comment might not have a condition.");
        br.addElement("For example:");
        br.addElement("  (x) - /*IF */XXX_ID = /*pmb.xxxId*/3/*END*/");
        br.addElement("  (o) - /*IF pmb.xxxId != null*/XXX_ID = /*pmb.xxxId*/3/*END*/");
        br.addItem("IF Comment");
        br.addElement(_tokenizer.getToken());
        br.addItem("Specified SQL");
        br.addElement(_specifiedSql);
        final String msg = br.buildExceptionMessage();
        throw new IfCommentConditionEmptyException(msg);
    }

    // ===================================================================================
    //                                                                        ELSE Comment
    //                                                                        ============
    protected void parseElse() {
        final Node parent = peek();
        if (!(parent instanceof IfNode)) {
            return;
        }
        final IfNode ifNode = (IfNode) pop();
        final ElseNode elseNode = createElseNode();
        ifNode.setElseNode(elseNode);
        push(elseNode);
        _tokenizer.skipWhitespace();
    }

    protected ElseNode createElseNode() {
        return newElseNode();
    }

    protected ElseNode newElseNode() {
        return new ElseNode();
    }

    // ===================================================================================
    //                                                                         FOR Comment
    //                                                                         ===========
    protected boolean isForComment(String comment) {
        return comment.startsWith(ForNode.PREFIX);
    }

    protected void parseFor() {
        final String comment = _tokenizer.getToken();
        final String condition = comment.substring(ForNode.PREFIX.length()).trim();
        if (Srl.is_Null_or_TrimmedEmpty(condition)) {
            throwForCommentExpressionEmptyException();
        }
        final ForNode forNode = createForNode(condition);
        peek().addChild(forNode);
        push(forNode);
        parseEnd();
    }

    protected ForNode createForNode(String expr) {
        researchIfNeeds(_researchForCommentList, expr); // for research
        return newForNode(expr, _specifiedSql, getNodeAdviceFactory());
    }

    protected ForNode newForNode(String expr, String specifiedSql, NodeAdviceFactory adviceFactory) {
        return new ForNode(expr, specifiedSql, adviceFactory);
    }

    protected boolean isLoopVariableComment(String comment) {
        return comment.startsWith(LoopFirstNode.MARK) || comment.startsWith(LoopNextNode.MARK) || comment.startsWith(LoopLastNode.MARK);
    }

    protected void parseLoopVariable() { // should be in FOR comment scope
        final String comment = _tokenizer.getToken();
        final String code = Srl.substringFirstFront(comment, " ");
        if (Srl.is_Null_or_TrimmedEmpty(code)) { // no way
            String msg = "Unknown loop variable comment: " + comment;
            throw new IllegalStateException(msg);
        }
        final LoopVariableType type = LoopVariableType.codeOf(code);
        if (type == null) { // no way
            String msg = "Unknown loop variable comment: " + comment;
            throw new IllegalStateException(msg);
        }
        final String condition = comment.substring(type.name().length()).trim();
        final LoopAbstractNode loopFirstNode = createLoopFirstNode(condition, type);
        peek().addChild(loopFirstNode);
        if (Srl.count(condition, "'") < 2) {
            push(loopFirstNode);
            parseEnd();
        }
    }

    protected LoopAbstractNode createLoopFirstNode(String expr, LoopVariableType type) {
        return type.createNode(expr, _specifiedSql);
    }

    protected void throwForCommentExpressionEmptyException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The expression of FOR comment was empty!");
        br.addItem("Advice");
        br.addElement("Please confirm the FOR comment expression.");
        br.addElement("Your FOR comment might not have an expression.");
        br.addElement("For example:");
        br.addElement("  (x) - /*FOR */XXX_ID = /*#element*/3/*END*/");
        br.addElement("  (o) - /*FOR pmb.xxxList*/XXX_ID = /*#element*/3/*END*/");
        br.addItem("FOR Comment");
        br.addElement(_tokenizer.getToken());
        br.addItem("Specified SQL");
        br.addElement(_specifiedSql);
        final String msg = br.buildExceptionMessage();
        throw new ForCommentExpressionEmptyException(msg);
    }

    // ===================================================================================
    //                                                                         END Comment
    //                                                                         ===========
    protected boolean isEndComment(String content) {
        return content != null && "END".equals(content);
    }

    protected void parseEnd() {
        final int commentType = SqlTokenizer.COMMENT;
        while (SqlTokenizer.EOF != _tokenizer.next()) {
            if (_tokenizer.getTokenType() == commentType && isEndComment(_tokenizer.getToken())) {
                pop();
                return;
            }
            parseToken();
        }
        throwEndCommentNotFoundException();
    }

    protected void throwEndCommentNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The end comment was not found!");
        br.addItem("Advice");
        br.addElement("Please confirm the parameter comment logic.");
        br.addElement("It may exist the parameter comment that DOESN'T have an end comment.");
        br.addElement("For example:");
        br.addElement("  (x): /*IF pmb.xxxId != null*/XXX_ID = /*pmb.xxxId*/3");
        br.addElement("  (o): /*IF pmb.xxxId != null*/XXX_ID = /*pmb.xxxId*/3/*END*/");
        br.addItem("Specified SQL");
        br.addElement(_specifiedSql);
        final String msg = br.buildExceptionMessage();
        throw new EndCommentNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                    Bind or Embedded
    //                                                                    ================
    protected void parseBindVariable() { // for native binding, unused on DBFlute so checked later
        final String expr = _tokenizer.getToken();
        peek().addChild(createBindVariableNode(expr, null));
    }

    protected void parseCommentBindVariable() { // main binding on DBFlute
        final String expr = _tokenizer.getToken();
        final String testValue = _tokenizer.skipToken(true);
        if (expr.startsWith(EmbeddedVariableNode.PREFIX_NORMAL)) {
            if (expr.startsWith(EmbeddedVariableNode.PREFIX_REPLACE_ONLY)) { // replaceOnly
                peek().addChild(prepareReplaceOnlyEmbeddedVariableNode(expr, testValue, /*removePrefix*/true));
            } else if (expr.startsWith(EmbeddedVariableNode.PREFIX_TERMINAL_DOT)) { // terminalDot
                peek().addChild(prepareTerminalDotEmbeddedVariableNode(expr, testValue));
            } else { // normal
                peek().addChild(prepareNormalEmbeddedVariableNode(expr, testValue));
            }
        } else {
            final Node bindVariableNode;
            if (_switchBindingToReplaceOnlyEmbedded) { // false on DBFlute, for general purpose
                bindVariableNode = prepareReplaceOnlyEmbeddedVariableNode(expr, testValue, /*removePrefix*/false);
            } else { // always here on DBFlute
                bindVariableNode = createBindVariableNode(expr, testValue);
            }
            peek().addChild(bindVariableNode);
        }
    }

    // -----------------------------------------------------
    //                                     Embedded Variable
    //                                     -----------------
    protected EmbeddedVariableNode prepareReplaceOnlyEmbeddedVariableNode(String expr, String testValue, boolean removePrefix) {
        final String realExpr = removePrefix ? expr.substring(EmbeddedVariableNode.PREFIX_REPLACE_ONLY.length()) : expr;
        return createEmbeddedVariableNode(realExpr, testValue, /*replaceOnly*/true, false);
    }

    protected EmbeddedVariableNode prepareTerminalDotEmbeddedVariableNode(String expr, String testValue) {
        final String realExpr = expr.substring(EmbeddedVariableNode.PREFIX_TERMINAL_DOT.length());
        return createEmbeddedVariableNode(realExpr, testValue, false, /*terminalDot*/true);
    }

    protected EmbeddedVariableNode prepareNormalEmbeddedVariableNode(String expr, String testValue) {
        final String realExpr = expr.substring(EmbeddedVariableNode.PREFIX_NORMAL.length());
        return createEmbeddedVariableNode(realExpr, testValue, false, false);
    }

    protected EmbeddedVariableNode createEmbeddedVariableNode(String expr, String testValue, boolean replaceOnly, boolean terminalDot) {
        researchIfNeeds(_researchEmbeddedVariableCommentList, expr); // for research
        return newEmbeddedVariableNode(expr, testValue, _specifiedSql, _blockNullParameter, getNodeAdviceFactory(), replaceOnly,
                terminalDot, _overlookNativeBinding);
    }

    protected EmbeddedVariableNode newEmbeddedVariableNode(String expr, String testValue, String specifiedSql, boolean blockNullParameter,
            NodeAdviceFactory adviceFactory, boolean replaceOnly, boolean terminalDot, boolean overlookNativeBinding) {
        return new EmbeddedVariableNode(expr, testValue, specifiedSql, blockNullParameter, adviceFactory, replaceOnly, terminalDot,
                overlookNativeBinding);
    }

    // -----------------------------------------------------
    //                                         Bind Variable
    //                                         -------------
    protected BindVariableNode createBindVariableNode(String expr, String testValue) {
        researchIfNeeds(_researchBindVariableCommentList, expr); // for research
        return newBindVariableNode(expr, testValue, _specifiedSql, _blockNullParameter, getNodeAdviceFactory());
    }

    protected BindVariableNode newBindVariableNode(String expr, String testValue, String specifiedSql, boolean blockNullParameter,
            NodeAdviceFactory adviceFactory) {
        return new BindVariableNode(expr, testValue, specifiedSql, blockNullParameter, adviceFactory);
    }

    // ===================================================================================
    //                                                                        Various Node
    //                                                                        ============
    protected SqlConnectorNode createSqlConnectorNode(Node node, String connector, String sqlParts) {
        if (isNestedBegin(node)) { // basically nested if BEGIN node because checked before
            // connector adjustment of BEGIN is independent 
            return SqlConnectorNode.createSqlConnectorNodeAsIndependent(connector, sqlParts);
        } else {
            return SqlConnectorNode.createSqlConnectorNode(connector, sqlParts);
        }
    }

    protected SqlPartsNode createSqlPartsNodeOutOfConnector(Node node, String sqlParts) {
        if (isTopBegin(node)) { // top BEGIN only (nested goes 'else' statement)
            return SqlPartsNode.createSqlPartsNodeAsIndependent(sqlParts);
        } else {
            return createSqlPartsNode(sqlParts);
        }
    }

    protected SqlPartsNode createSqlPartsNodeThroughConnector(Node node, String sqlParts) {
        if (isNestedBegin(node)) { // basically nested if BEGIN node because checked before
            // connector adjustment of BEGIN is independent
            return SqlPartsNode.createSqlPartsNodeAsIndependent(sqlParts);
        } else {
            return createSqlPartsNode(sqlParts);
        }
    }

    protected SqlPartsNode createSqlPartsNode(String sqlParts) { // as plain
        return SqlPartsNode.createSqlPartsNode(sqlParts);
    }

    // ===================================================================================
    //                                                                          Node Stack
    //                                                                          ==========
    protected Node pop() {
        return (Node) _nodeStack.pop();
    }

    protected Node peek() {
        return (Node) _nodeStack.peek();
    }

    protected void push(Node node) {
        _nodeStack.push(node);
    }

    protected boolean isElseMode() {
        for (int i = 0; i < _nodeStack.size(); ++i) {
            if (_nodeStack.get(i) instanceof ElseNode) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                   Bind Value Tracer
    //                                                                   =================
    protected NodeAdviceFactory getNodeAdviceFactory() {
        return _defaultNodeAdviceFactory; // you can switch it
    }

    // ===================================================================================
    //                                                                            Research
    //                                                                            ========
    /**
     * Research IF comments. (basically for research only, NOT for execution)<br>
     * This method should be called before calling analyze(). <br>
     * The returned list is filled with IF comment after calling analyze().
     * @return The list of IF comment. (NotNull)
     */
    public List<String> researchIfComment() { // should NOT be called with execution
        final List<String> resultList = new ArrayList<String>();
        _researchIfCommentList = resultList;
        return resultList;
    }

    /**
     * Research bind variable comments. (basically for research only, NOT for execution)<br>
     * This method should be called before calling analyze(). <br>
     * The returned list is filled with bind variable comment after calling analyze().
     * @return The list of bind variable comment. (NotNull)
     */
    public List<String> researchBindVariableComment() { // should NOT be called with execution
        final List<String> resultList = new ArrayList<String>();
        _researchBindVariableCommentList = resultList;
        return resultList;
    }

    /**
     * Research embedded variable comments. (basically for research only, NOT for execution)<br>
     * This method should be called before calling analyze(). <br>
     * The returned list is filled with embedded variable comment after calling analyze().
     * @return The list of embedded variable comment. (NotNull)
     */
    public List<String> researchEmbeddedVariableComment() { // should NOT be called with execution
        final List<String> resultList = new ArrayList<String>();
        _researchEmbeddedVariableCommentList = resultList;
        return resultList;
    }

    protected void researchIfNeeds(List<String> researchList, String expr) {
        if (researchList != null) {
            researchList.add(expr);
        }
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String replaceString(String text, String fromText, String toText) {
        return Srl.replace(text, fromText, toText);
    }

    // ===================================================================================
    //                                                                          DisplaySql
    //                                                                          ==========
    public static String convertTwoWaySql2DisplaySql(SqlAnalyzerFactory factory, String twoWaySql, Object arg,
            BoundDateDisplayStyle dateDisplayStyle) {
        final String[] argNames = new String[] { "pmb" };
        final Class<?>[] argTypes = new Class<?>[] { arg.getClass() };
        final Object[] args = new Object[] { arg };
        return convertTwoWaySql2DisplaySql(factory, twoWaySql, argNames, argTypes, args, dateDisplayStyle);
    }

    public static String convertTwoWaySql2DisplaySql(SqlAnalyzerFactory factory, String twoWaySql, String[] argNames, Class<?>[] argTypes,
            Object[] args, BoundDateDisplayStyle dateDisplayStyle) {
        final CommandContext context;
        {
            final SqlAnalyzer parser = createSqlAnalyzer4DisplaySql(factory, twoWaySql);
            final Node node = parser.analyze();
            final CommandContextCreator creator = new CommandContextCreator(argNames, argTypes);
            context = creator.createCommandContext(args);
            node.accept(context);
        }
        final String preparedSql = context.getSql();
        final DisplaySqlBuilder builder = new DisplaySqlBuilder(dateDisplayStyle);
        return builder.buildDisplaySql(preparedSql, context.getBindVariables());
    }

    protected static SqlAnalyzer createSqlAnalyzer4DisplaySql(SqlAnalyzerFactory factory, String twoWaySql) {
        if (factory == null) {
            String msg = "The factory of SQL analyzer should exist.";
            throw new IllegalStateException(msg);
        }
        final boolean blockNullParameter = false;
        final SqlAnalyzer created = factory.create(twoWaySql, blockNullParameter);
        if (created != null) {
            return created;
        }
        String msg = "The factory should not return null:";
        msg = msg + " sql=" + twoWaySql + " factory=" + factory;
        throw new IllegalStateException(msg);
    }
}
