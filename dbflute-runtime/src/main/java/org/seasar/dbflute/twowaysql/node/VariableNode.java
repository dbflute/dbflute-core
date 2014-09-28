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
package org.seasar.dbflute.twowaysql.node;

import java.util.List;

import org.seasar.dbflute.cbean.coption.LikeSearchOption;
import org.seasar.dbflute.twowaysql.context.CommandContext;
import org.seasar.dbflute.twowaysql.node.ValueAndTypeSetupper.CommentType;
import org.seasar.dbflute.util.Srl;

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

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public VariableNode(String expression, String testValue, String specifiedSql, boolean blockNullParameter) {
        if (expression.contains(":")) {
            this._expression = Srl.substringFirstFront(expression, ":").trim();
            this._optionDef = Srl.substringFirstRear(expression, ":").trim();
        } else {
            this._expression = expression;
            this._optionDef = null;
        }
        this._testValue = testValue;
        this._nameList = Srl.splitList(_expression, ".");
        this._specifiedSql = specifiedSql;
        this._blockNullParameter = blockNullParameter;
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

    protected void doAccept(CommandContext ctx, Object firstValue, Class<?> firstType, LoopInfo loopInfo,
            boolean inheritLoop) {
        assertInLoopOnlyOptionInLoop(loopInfo);
        final ValueAndType valueAndType = new ValueAndType();
        valueAndType.setFirstValue(firstValue);
        valueAndType.setFirstType(firstType);
        setupValueAndType(valueAndType);
        processLikeSearch(valueAndType, loopInfo, inheritLoop);
        if (_blockNullParameter && valueAndType.getTargetValue() == null) {
            throwBindOrEmbeddedCommentParameterNullValueException(valueAndType);
        }
        doProcess(ctx, valueAndType, loopInfo);
    }

    protected abstract void doProcess(CommandContext ctx, ValueAndType valueAndType, LoopInfo loopInfo);

    protected void assertFirstNameAsNormal(CommandContext ctx, String firstName) {
        if (NodeUtil.isCurrentVariableOutOfScope(firstName, false)) {
            throwLoopCurrentVariableOutOfForCommentException();
        }
        if (NodeUtil.isWrongParameterBeanName(firstName, ctx)) {
            throwBindOrEmbeddedCommentIllegalParameterBeanSpecificationException();
        }
    }

    protected void throwLoopCurrentVariableOutOfForCommentException() {
        NodeUtil.throwLoopCurrentVariableOutOfForCommentException(_expression, _specifiedSql);
    }

    protected void setupValueAndType(ValueAndType valueAndType) {
        final CommentType type = getCommentType();
        final ValueAndTypeSetupper setuper = new ValueAndTypeSetupper(_nameList, _expression, _specifiedSql, type);
        setuper.setupValueAndType(valueAndType);
    }

    protected abstract CommentType getCommentType();

    // ===================================================================================
    //                                                                   LikeSearch Helper
    //                                                                   =================
    protected void processLikeSearch(ValueAndType valueAndType, LoopInfo loopInfo, boolean inheritLoop) {
        if (!isAcceptableLikeSearch(loopInfo)) {
            return;
        }
        final LikeSearchOption inLoopForcedLikeSearchOption = getInLoopForcedLikeSearchOption(loopInfo);
        if (inLoopForcedLikeSearchOption != null) { // forced option
            valueAndType.setLikeSearchOption(inLoopForcedLikeSearchOption);
        } else {
            if (inheritLoop) {
                valueAndType.inheritLikeSearchOptionIfNeeds(loopInfo);
            }
        }
        valueAndType.filterValueByOptionIfNeeds();
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

    protected LikeSearchOption getInLoopForcedLikeSearchOption(LoopInfo loopInfo) {
        if (Srl.is_NotNull_and_NotTrimmedEmpty(_optionDef)) {
            final List<String> optionList = Srl.splitListTrimmed(_optionDef, "|");
            for (String option : optionList) {
                if (option.equals(INLOOP_OPTION_LIKE_PREFIX)) {
                    return new LikeSearchOption().likePrefix();
                } else if (option.equals(INLOOP_OPTION_LIKE_SUFFIX)) {
                    return new LikeSearchOption().likeSuffix();
                } else if (option.equals(INLOOP_OPTION_LIKE_CONTAIN)) {
                    return new LikeSearchOption().likeContain();
                }
            }
        }
        return null;
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

    protected void setupRearOption(CommandContext ctx, ValueAndType valueAndType) { // for sub-class
        final String rearOption = valueAndType.buildRearOptionOnSql();
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
    protected void throwBindOrEmbeddedCommentParameterNullValueException(ValueAndType valueAndType) {
        final Class<?> targetType = valueAndType.getTargetType();
        NodeUtil.throwBindOrEmbeddedCommentParameterNullValueException(_expression, targetType, _specifiedSql, isBind());
    }

    protected void throwBindOrEmbeddedCommentInScopeNotListException(ValueAndType valueAndType) {
        final Class<?> targetType = valueAndType.getTargetType();
        NodeUtil.throwBindOrEmbeddedCommentInScopeNotListException(_expression, targetType, _specifiedSql, isBind());
    }

    protected void throwBindOrEmbeddedCommentIllegalParameterBeanSpecificationException() {
        NodeUtil.throwBindOrEmbeddedCommentIllegalParameterBeanSpecificationException(_expression, _specifiedSql,
                isBind());
    }

    protected void throwBindOrEmbeddedCommentParameterEmptyListException() {
        NodeUtil.throwBindOrEmbeddedCommentParameterEmptyListException(_expression, _specifiedSql, isBind());
    }

    protected void throwBindOrEmbeddedCommentParameterNullOnlyListException() {
        NodeUtil.throwBindOrEmbeddedCommentParameterNullOnlyListException(_expression, _specifiedSql, isBind());
    }

    protected void throwInLoopOptionOutOfLoopException() {
        NodeUtil.throwInLoopOptionOutOfLoopException(_expression, _specifiedSql, _optionDef);
    }

    protected boolean isBind() {
        return getCommentType().equals(CommentType.BIND);
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
