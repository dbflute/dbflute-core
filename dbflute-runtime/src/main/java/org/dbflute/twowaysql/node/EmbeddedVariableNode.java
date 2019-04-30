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
package org.dbflute.twowaysql.node;

import java.lang.reflect.Array;
import java.util.Collection;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.twowaysql.SqlAnalyzer;
import org.dbflute.twowaysql.context.CommandContext;
import org.dbflute.twowaysql.context.CommandContextCreator;
import org.dbflute.twowaysql.exception.EmbeddedVariableCommentContainsBindSymbolException;
import org.dbflute.twowaysql.factory.NodeAdviceFactory;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class EmbeddedVariableNode extends VariableNode {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String PREFIX_NORMAL = "$";
    public static final String PREFIX_REPLACE_ONLY = "$$";
    public static final String PREFIX_TERMINAL_DOT = "$.";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final boolean _replaceOnly;
    protected final boolean _terminalDot;
    protected final boolean _overlookNativeBinding; // treats native binding as plain question mark on SQL

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public EmbeddedVariableNode(String expression, String testValue, String specifiedSql, boolean blockNullParameter,
            NodeAdviceFactory nodeAdviceFactory, boolean replaceOnly, boolean terminalDot, boolean overlookNativeBinding) {
        super(expression, testValue, specifiedSql, blockNullParameter, nodeAdviceFactory);
        _replaceOnly = replaceOnly;
        _terminalDot = terminalDot;
        _overlookNativeBinding = overlookNativeBinding;
    }

    // ===================================================================================
    //                                                                              Accept
    //                                                                              ======
    @Override
    protected void doProcess(CommandContext ctx, BoundValue boundValue, LoopInfo loopInfo) {
        final Object finalValue = boundValue.getTargetValue();
        final Class<?> finalType = boundValue.getTargetType();
        if (isInScope()) {
            if (finalValue == null) { // in-scope does not allow null value
                throwBindOrEmbeddedCommentParameterNullValueException(boundValue);
            }
            if (Collection.class.isAssignableFrom(finalType)) {
                embedArray(ctx, ((Collection<?>) finalValue).toArray());
            } else if (finalType.isArray()) {
                embedArray(ctx, finalValue);
            } else {
                throwBindOrEmbeddedCommentInScopeNotListException(boundValue);
            }
        } else {
            if (finalValue == null) {
                ctx.addSql("null");
            } else if (!(finalValue instanceof String)) {
                final String embeddedValue = finalValue.toString();
                if (isQuotedScalar()) { // basically for condition value
                    ctx.addSql(quote(embeddedValue));
                } else { // basically for cannot-bound condition (for example, paging)
                    ctx.addSql(embeddedValue);
                }
            } else {
                // string type here
                final String embeddedStr = (String) finalValue;
                assertNotContainBindSymbol(embeddedStr);
                if (isQuotedScalar()) { // basically for condition value
                    ctx.addSql(quote(embeddedStr));
                    if (isAcceptableLikeSearch(loopInfo)) {
                        setupRearOption(ctx, boundValue);
                    }
                } else {
                    final Object firstValue = boundValue.getFirstValue();
                    final Class<?> firstType = boundValue.getFirstType();
                    final boolean bound = processDynamicBinding(ctx, firstValue, firstType, embeddedStr);
                    if (!bound) {
                        ctx.addSql(embeddedStr);
                    }
                }
            }
        }
        if (_testValue != null) {
            if (_replaceOnly) { // e.g. select ... from /*$$pmb.schema*/MEMBER
                // actually the test value is not test value
                // but a part of SQL statement here
                ctx.addSql(_testValue);
            } else if (_terminalDot) { // e.g. select ... from /*$$pmb.schema*/dev.MEMBER
                // the real test value is until a dot character
                ctx.addSql("." + Srl.substringFirstRear(_testValue, "."));
            }
        }
    }

    protected void embedArray(CommandContext ctx, Object array) {
        if (array == null) {
            return;
        }
        final int length = Array.getLength(array);
        if (length == 0) {
            throwBindOrEmbeddedCommentParameterEmptyListException();
        }
        final boolean quotedInScope = isQuotedInScope();
        ctx.addSql("(");
        int validCount = 0;
        for (int i = 0; i < length; ++i) {
            final Object currentElement = Array.get(array, i);
            if (currentElement != null) {
                if (validCount > 0) {
                    ctx.addSql(", ");
                }
                final String currentStr = currentElement.toString();
                assertNotContainBindSymbol(currentStr);
                if (quotedInScope) {
                    ctx.addSql(quote(currentStr));
                } else {
                    ctx.addSql(currentStr);
                }
                ++validCount;
            }
        }
        if (validCount == 0) {
            throwBindOrEmbeddedCommentParameterNullOnlyListException();
        }
        ctx.addSql(")");
    }

    protected void assertNotContainBindSymbol(String value) {
        if (_overlookNativeBinding) { // for general purpose e.g. MailFlute (to avoid question mark headache)
            return;
        }
        if (containsBindSymbol(value)) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("The value of embedded comment contained bind symbols.");
            br.addItem("Advice");
            br.addElement("The value of embedded comment should not contain bind symbols.");
            br.addElement("For example, a question mark '?'.");
            br.addItem("Comment Expression");
            br.addElement(_expression);
            br.addItem("Embedded Value");
            br.addElement(value);
            final String msg = br.buildExceptionMessage();
            throw new EmbeddedVariableCommentContainsBindSymbolException(msg);
        }
    }

    protected boolean containsBindSymbol(String value) {
        return value.indexOf("?") > -1;
    }

    protected String quote(String value) {
        return "'" + value + "'";
    }

    protected boolean isQuotedScalar() {
        if (_testValue == null) {
            return false;
        }
        return Srl.count(_testValue, "'") > 1 && _testValue.startsWith("'") && _testValue.endsWith("'");
    }

    protected boolean isQuotedInScope() {
        if (!isInScope()) {
            return false;
        }
        return Srl.count(_testValue, "'") > 1;
    }

    protected boolean processDynamicBinding(CommandContext ctx, Object firstValue, Class<?> firstType, String embeddedString) {
        final ScopeInfo first = Srl.extractScopeFirst(embeddedString, "/*", "*/");
        if (first == null) {
            return false;
        }
        // unsupported general purpose options in dynamic for now, e.g. MailFlute, may be almost unneeded
        final SqlAnalyzer analyzer = new SqlAnalyzer(embeddedString, _blockNullParameter);
        final Node rootNode = analyzer.analyze();
        final CommandContextCreator creator = new CommandContextCreator(new String[] { "pmb" }, new Class<?>[] { firstType });
        final CommandContext rootCtx = creator.createCommandContext(new Object[] { firstValue });
        rootNode.accept(rootCtx);
        final String sql = rootCtx.getSql();
        ctx.addSql(sql, rootCtx.getBindVariables(), rootCtx.getBindVariableTypes());
        return true;
    }

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    @Override
    protected ParameterCommentType getCommentType() {
        return ParameterCommentType.EMBEDDED;
    }
}
