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

import java.lang.reflect.Array;
import java.util.Collection;

import org.seasar.dbflute.twowaysql.context.CommandContext;
import org.seasar.dbflute.twowaysql.node.ValueAndTypeSetupper.CommentType;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class BindVariableNode extends VariableNode {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public BindVariableNode(String expression, String testValue, String specifiedSql, boolean blockNullParameter) {
        super(expression, testValue, specifiedSql, blockNullParameter);
    }

    // ===================================================================================
    //                                                                              Accept
    //                                                                              ======
    @Override
    protected void doProcess(CommandContext ctx, ValueAndType valueAndType, LoopInfo loopInfo) {
        final Object finalValue = valueAndType.getTargetValue();
        final Class<?> finalType = valueAndType.getTargetType();
        if (isInScope()) {
            if (finalValue == null) { // in-scope does not allow null value
                throwBindOrEmbeddedCommentParameterNullValueException(valueAndType);
            }
            if (Collection.class.isAssignableFrom(finalType)) {
                bindArray(ctx, ((Collection<?>) finalValue).toArray());
            } else if (finalType.isArray()) {
                bindArray(ctx, finalValue);
            } else {
                throwBindOrEmbeddedCommentInScopeNotListException(valueAndType);
            }
        } else {
            ctx.addSql("?", finalValue, finalType); // if null, bind as null
            if (isAcceptableLikeSearch(loopInfo)) {
                setupRearOption(ctx, valueAndType);
            }
        }
    }

    protected void bindArray(CommandContext ctx, Object array) {
        if (array == null) {
            return;
        }
        final int length = Array.getLength(array);
        if (length == 0) {
            throwBindOrEmbeddedCommentParameterEmptyListException();
        }
        Class<?> clazz = null;
        for (int i = 0; i < length; ++i) {
            final Object currentElement = Array.get(array, i);
            if (currentElement != null) {
                clazz = currentElement.getClass();
                break;
            }
        }
        if (clazz == null) {
            throwBindOrEmbeddedCommentParameterNullOnlyListException();
        }
        ctx.addSql("(");
        int validCount = 0;
        for (int i = 0; i < length; ++i) {
            final Object currentElement = Array.get(array, i);
            if (currentElement != null) {
                if (validCount > 0) {
                    ctx.addSql(", ");
                }
                ctx.addSql("?", currentElement, clazz);
                ++validCount;
            }
        }
        ctx.addSql(")");
    }

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    @Override
    protected CommentType getCommentType() {
        return CommentType.BIND;
    }
}
