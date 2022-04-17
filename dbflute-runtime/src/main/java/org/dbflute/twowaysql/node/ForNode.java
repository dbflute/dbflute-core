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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.optional.OptionalThing;
import org.dbflute.twowaysql.context.CommandContext;
import org.dbflute.twowaysql.exception.ForCommentIllegalParameterBeanSpecificationException;
import org.dbflute.twowaysql.exception.ForCommentParameterNotListException;
import org.dbflute.twowaysql.factory.NodeAdviceFactory;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;

/**
 * The node for FOR (loop). <br>
 * FOR comment is evaluated before analyzing nodes,
 * so it is not related to container node.
 * @author jflute
 */
public class ForNode extends ScopeNode implements SqlConnectorAdjustable, LoopAcceptable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String PREFIX = "FOR ";
    public static final String CURRENT_VARIABLE = "#current";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _expression;
    protected final List<String> _nameList;
    protected final String _specifiedSql;
    protected final NodeAdviceFactory _nodeAdviceFactory;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ForNode(String expression, String specifiedSql, NodeAdviceFactory nodeAdviceFactory) {
        _expression = expression;
        _nameList = Srl.splitList(expression, ".");
        _specifiedSql = specifiedSql;
        _nodeAdviceFactory = nodeAdviceFactory;
    }

    // ===================================================================================
    //                                                                              Accept
    //                                                                              ======
    public void accept(CommandContext ctx) {
        doAccept(ctx, null);
    }

    public void accept(CommandContext ctx, LoopInfo loopInfo) {
        final String firstName = _nameList.get(0);
        if (firstName.equals(ForNode.CURRENT_VARIABLE)) { // use loop element
            final Object parameter = loopInfo.getCurrentParameter();
            final Class<?> parameterType = loopInfo.getCurrentParameterType();
            doAccept(ctx, parameter, parameterType, loopInfo, true);
        } else { // normal
            doAccept(ctx, loopInfo);
        }
    }

    public void doAccept(CommandContext ctx, LoopInfo parentLoop) {
        final String firstName = _nameList.get(0);
        assertFirstNameAsNormal(ctx, firstName);
        final Object value = ctx.getArg(firstName);
        final Class<?> clazz = ctx.getArgType(firstName);
        doAccept(ctx, value, clazz, parentLoop, false);
    }

    public void doAccept(CommandContext ctx, Object firstValue, Class<?> firstType, LoopInfo parentLoop, boolean inheritLoop) {
        if (firstValue == null) {
            return; // if base object is null, do nothing at FOR comment
        }
        final BoundValue boundValue = new BoundValue();
        boundValue.setFirstValue(firstValue);
        boundValue.setFirstType(firstType);
        setupBoundValue(boundValue);
        if (inheritLoop) {
            boundValue.inheritLikeSearchOptionIfNeeds(parentLoop);
        }
        final Object targetValue = boundValue.getTargetValue();
        if (targetValue == null) {
            return; // if target value is null, do nothing at FOR comment
        }
        assertParameterList(targetValue);
        final List<?> parameterList = (List<?>) targetValue;
        final int loopSize = parameterList.size();
        final LoopInfo loopInfo = new LoopInfo();
        loopInfo.setParentLoop(parentLoop);
        loopInfo.setExpression(_expression);
        loopInfo.setSpecifiedSql(_specifiedSql);
        loopInfo.setParameterList(parameterList);
        loopInfo.setLoopSize(loopSize);
        loopInfo.setFilteringBindOption(boundValue.getFilteringBindOption());
        for (int loopIndex = 0; loopIndex < loopSize; loopIndex++) {
            loopInfo.setLoopIndex(loopIndex);
            processAcceptingChildren(ctx, loopInfo);
        }
        if (loopSize > 0) {
            ctx.setEnabled(true);
        }
    }

    protected void assertFirstNameAsNormal(CommandContext ctx, String firstName) {
        if (NodeChecker.isCurrentVariableOutOfScope(firstName, false)) {
            throwLoopCurrentVariableOutOfForCommentException();
        }
        if (NodeChecker.isWrongParameterBeanName(firstName, ctx)) {
            throwForCommentIllegalParameterBeanSpecificationException();
        }
    }

    protected void throwLoopCurrentVariableOutOfForCommentException() {
        NodeChecker.throwLoopCurrentVariableOutOfForCommentException(_expression, _specifiedSql);
    }

    protected void throwForCommentIllegalParameterBeanSpecificationException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The FOR comment had the illegal parameter-bean specification!");
        br.addItem("Advice");
        br.addElement("Please confirm your FOR comment.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*FOR pmb,memberId*/");
        br.addElement("    /*FOR p mb,memberId*/");
        br.addElement("    /*FOR pmb:memberId*/");
        br.addElement("    /*FOR pmb,memberId*/");
        br.addElement("  (o):");
        br.addElement("    /*FOR pmb.memberId*/");
        br.addItem("FOR Comment Expression");
        br.addElement(_expression);
        // *debug to this exception does not need contents of the parameter-bean
        //  (and for security to application data)
        //br.addItem("ParameterBean");
        //br.addElement(pmb);
        br.addItem("Specified SQL");
        br.addElement(_specifiedSql);
        final String msg = br.buildExceptionMessage();
        throw new ForCommentIllegalParameterBeanSpecificationException(msg);
    }

    protected void setupBoundValue(BoundValue boundValue) {
        final ParameterCommentType type = ParameterCommentType.FORCOMMENT;
        final BoundValueTracer tracer = createBoundValueTracer(type);
        tracer.trace(boundValue);
    }

    protected BoundValueTracer createBoundValueTracer(final ParameterCommentType type) {
        return _nodeAdviceFactory.createBoundValueTracer(_nameList, _expression, _specifiedSql, type);
    }

    protected void assertParameterList(Object targetValue) {
        if (!List.class.isInstance(targetValue)) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("The parameter for FOR coment was not list.");
            br.addItem("FOR Comment Expression");
            br.addElement(_expression);
            br.addItem("Parameter");
            br.addElement(targetValue.getClass());
            br.addElement(targetValue);
            br.addItem("Specified SQL");
            br.addElement(_specifiedSql);
            String msg = br.buildExceptionMessage();
            throw new ForCommentParameterNotListException(msg);
        }
    }

    // ===================================================================================
    //                                                                       Loop Variable
    //                                                                       =============
    public enum LoopVariableType {
        FIRST("first", new LoopVariableNodeFactory() {
            public LoopAbstractNode create(String expression, String specifiedSql) {
                return new LoopFirstNode(expression, specifiedSql);
            }
        }), NEXT("next", new LoopVariableNodeFactory() {
            public LoopAbstractNode create(String expression, String specifiedSql) {
                return new LoopNextNode(expression, specifiedSql);
            }
        }), LAST("last", new LoopVariableNodeFactory() {
            public LoopAbstractNode create(String expression, String specifiedSql) {
                return new LoopLastNode(expression, specifiedSql);
            }
        });

        private static final Map<String, LoopVariableType> _codeValueMap = new HashMap<String, LoopVariableType>();
        static {
            for (LoopVariableType value : values()) {
                _codeValueMap.put(value.code().toLowerCase(), value);
            }
        }
        private String _code;
        private LoopVariableNodeFactory _factory;

        private LoopVariableType(String code, LoopVariableNodeFactory factory) {
            _code = code;
            _factory = factory;
        }

        public String code() {
            return _code;
        }

        public static OptionalThing<LoopVariableType> of(Object code) {
            if (code == null) {
                return OptionalThing.ofNullable(null, () -> {
                    throw new IllegalArgumentException("The argument 'code' should not be null.");
                });
            }
            if (code instanceof LoopVariableType) {
                return OptionalThing.of((LoopVariableType) code);
            }
            if (code instanceof OptionalThing<?>) {
                return of(((OptionalThing<?>) code).orElse(null));
            }
            final LoopVariableType modifier = _codeValueMap.get(code.toString().toLowerCase());
            return OptionalThing.ofNullable(modifier, () -> {
                throw new IllegalStateException("Not found the type by the code: " + code);
            });
        }

        @Deprecated
        public static LoopVariableType codeOf(Object code) {
            return of(code).orElse(null);
        }

        public LoopAbstractNode createNode(String expression, String specifiedSql) {
            return _factory.create(expression, specifiedSql);
        }
    }

    public interface LoopVariableNodeFactory {
        LoopAbstractNode create(String expression, String specifiedSql);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":{" + _expression + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getExpression() {
        return _expression;
    }
}
