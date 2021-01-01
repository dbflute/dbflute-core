/*
 * Copyright 2014-2021 the original author or authors.
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

import java.util.List;

import org.dbflute.twowaysql.context.CommandContext;
import org.dbflute.twowaysql.factory.NodeAdviceFactory;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public abstract class VariableNode extends AbstractNode implements LoopAcceptable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String INLOOP_OPTION_LIKE_PREFIX = "likePrefix";
    protected static final String INLOOP_OPTION_LIKE_SUFFIX = "likeSuffix";
    protected static final String INLOOP_OPTION_LIKE_CONTAIN = "likeContain";
    protected static final String INLOOP_OPTION_NOT_LIKE = "notLike";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _expression;
    protected final String _testValue;
    protected final String _optionDef;
    protected final List<String> _nameList;
    protected final String _specifiedSql;
    protected final boolean _blockNullParameter;
    protected final NodeAdviceFactory _nodeAdviceFactory;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public VariableNode(String expression, String testValue, String specifiedSql, boolean blockNullParameter,
            NodeAdviceFactory nodeAdviceFactory) {
        if (expression.contains(":")) {
            _expression = Srl.substringFirstFront(expression, ":").trim();
            _optionDef = Srl.substringFirstRear(expression, ":").trim();
        } else {
            _expression = expression;
            _optionDef = null;
        }
        _testValue = testValue;
        _nameList = Srl.splitList(_expression, ".");
        _specifiedSql = specifiedSql;
        _blockNullParameter = blockNullParameter;
        _nodeAdviceFactory = nodeAdviceFactory;
    }

    // ===================================================================================
    //                                                                              Accept
    //                                                                              ======
    public void accept(CommandContext ctx) {
        doAccept(ctx, null);
    }

    public void accept(CommandContext ctx, LoopInfo loopInfo) { // for FOR comment
        final String firstName = _nameList.get(0);
        if (firstName.equals(ForNode.CURRENT_VARIABLE)) { // use loop element
            final Object parameter = loopInfo.getCurrentParameter();
            final Class<?> parameterType = loopInfo.getCurrentParameterType();
            doAccept(ctx, parameter, parameterType, loopInfo, true);
        } else { // normal
            doAccept(ctx, loopInfo);
        }
    }

    protected void doAccept(CommandContext ctx, LoopInfo loopInfo) {
        final String firstName = _nameList.get(0);
        assertFirstNameAsNormal(ctx, firstName);
        final Object firstValue = ctx.getArg(firstName);
        final Class<?> firstType = ctx.getArgType(firstName);
        doAccept(ctx, firstValue, firstType, loopInfo, false);
    }

    protected void doAccept(CommandContext ctx, Object firstValue, Class<?> firstType, LoopInfo loopInfo, boolean inheritLoop) {
        assertInLoopOnlyOptionInLoop(loopInfo);
        final BoundValue boundValue = new BoundValue();
        boundValue.setFirstValue(firstValue);
        boundValue.setFirstType(firstType);
        setupBoundValue(boundValue);
        processLikeSearch(boundValue, loopInfo, inheritLoop);
        if (_blockNullParameter && boundValue.getTargetValue() == null) {
            throwBindOrEmbeddedCommentParameterNullValueException(boundValue);
        }
        doProcess(ctx, boundValue, loopInfo);
    }

    protected abstract void doProcess(CommandContext ctx, BoundValue boundValue, LoopInfo loopInfo);

    protected void assertFirstNameAsNormal(CommandContext ctx, String firstName) {
        if (NodeChecker.isCurrentVariableOutOfScope(firstName, false)) {
            throwLoopCurrentVariableOutOfForCommentException();
        }
        if (NodeChecker.isWrongParameterBeanName(firstName, ctx)) {
            throwBindOrEmbeddedCommentIllegalParameterBeanSpecificationException();
        }
    }

    protected void throwLoopCurrentVariableOutOfForCommentException() {
        NodeChecker.throwLoopCurrentVariableOutOfForCommentException(_expression, _specifiedSql);
    }

    protected void setupBoundValue(BoundValue boundValue) {
        final ParameterCommentType commentType = getCommentType();
        final BoundValueTracer tracer = createBoundValueTracer(commentType);
        tracer.trace(boundValue);
    }

    protected BoundValueTracer createBoundValueTracer(ParameterCommentType commentType) {
        return _nodeAdviceFactory.createBoundValueTracer(_nameList, _expression, _specifiedSql, commentType);
    }

    protected abstract ParameterCommentType getCommentType();

    // ===================================================================================
    //                                                                   LikeSearch Helper
    //                                                                   =================
    protected void processLikeSearch(BoundValue boundValue, LoopInfo loopInfo, boolean inheritLoop) {
        if (!isAcceptableLikeSearch(loopInfo)) {
            return;
        }
        final FilteringBindOption inLoopForcedLikeSearchOption = findInLoopForcedLikeSearchOption(loopInfo);
        if (inLoopForcedLikeSearchOption != null) { // forced option
            boundValue.setFilteringBindOption(inLoopForcedLikeSearchOption);
        } else {
            if (inheritLoop) {
                boundValue.inheritLikeSearchOptionIfNeeds(loopInfo);
            }
        }
        boundValue.filterValueByOptionIfNeeds();
    }

    protected boolean isAcceptableLikeSearch(LoopInfo loopInfo) {
        if (loopInfo != null && Srl.is_NotNull_and_NotTrimmedEmpty(_optionDef)) {
            final List<String> optionList = Srl.splitListTrimmed(_optionDef, "|");
            if (optionList.contains(INLOOP_OPTION_NOT_LIKE)) {
                return false; // specified not-like in loop
            }
        }
        return true; // basically true
    }

    protected FilteringBindOption findInLoopForcedLikeSearchOption(LoopInfo loopInfo) {
        if (Srl.is_NotNull_and_NotTrimmedEmpty(_optionDef)) {
            final List<String> optionList = Srl.splitListTrimmed(_optionDef, "|");
            for (String option : optionList) {
                return prepareInLoopLikeSearchOption(option);
            }
        }
        return null;
    }

    protected FilteringBindOption prepareInLoopLikeSearchOption(String likeDirection) {
        return _nodeAdviceFactory.prepareInLoopLikeSearchOption(likeDirection);
    }

    protected void assertInLoopOnlyOptionInLoop(LoopInfo loopInfo) {
        if (loopInfo == null && Srl.is_NotNull_and_NotTrimmedEmpty(_optionDef)) {
            final String onlyInLoop = INLOOP_OPTION_NOT_LIKE;
            final List<String> optionList = Srl.splitListTrimmed(_optionDef, "|");
            for (String option : optionList) {
                if (onlyInLoop.equals(option)) {
                    // means 'notLike' is specified at out of loop
                    throwInLoopOptionOutOfLoopException();
                }
            }
        }
    }

    protected void setupRearOption(CommandContext ctx, BoundValue boundValue) { // for sub-class
        final String rearOption = boundValue.buildRearOptionOnSql();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(rearOption)) {
            ctx.addSql(rearOption);
        }
    }

    // ===================================================================================
    //                                                                      InScope Helper
    //                                                                      ==============
    protected boolean isInScope() {
        if (_testValue == null) {
            return false;
        }
        return _testValue.startsWith("(") && _testValue.endsWith(")");
    }

    // ===================================================================================
    //                                                                           Exception
    //                                                                           =========
    protected void throwBindOrEmbeddedCommentParameterNullValueException(BoundValue boundValue) {
        final Class<?> targetType = boundValue.getTargetType();
        NodeChecker.throwBindOrEmbeddedCommentParameterNullValueException(_expression, targetType, _specifiedSql, isBind());
    }

    protected void throwBindOrEmbeddedCommentInScopeNotListException(BoundValue boundValue) {
        final Class<?> targetType = boundValue.getTargetType();
        NodeChecker.throwBindOrEmbeddedCommentInScopeNotListException(_expression, targetType, _specifiedSql, isBind());
    }

    protected void throwBindOrEmbeddedCommentIllegalParameterBeanSpecificationException() {
        NodeChecker.throwBindOrEmbeddedCommentIllegalParameterBeanSpecificationException(_expression, _specifiedSql, isBind());
    }

    protected void throwBindOrEmbeddedCommentParameterEmptyListException() {
        NodeChecker.throwBindOrEmbeddedCommentParameterEmptyListException(_expression, _specifiedSql, isBind());
    }

    protected void throwBindOrEmbeddedCommentParameterNullOnlyListException() {
        NodeChecker.throwBindOrEmbeddedCommentParameterNullOnlyListException(_expression, _specifiedSql, isBind());
    }

    protected void throwInLoopOptionOutOfLoopException() {
        NodeChecker.throwInLoopOptionOutOfLoopException(_expression, _specifiedSql, _optionDef);
    }

    protected boolean isBind() {
        return getCommentType().equals(ParameterCommentType.BIND);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getExpression() {
        return _expression;
    }

    public String getTestValue() {
        return _testValue;
    }

    public String getOptionDef() {
        return _optionDef;
    }

    public boolean isBlockNullParameter() {
        return _blockNullParameter;
    }
}
