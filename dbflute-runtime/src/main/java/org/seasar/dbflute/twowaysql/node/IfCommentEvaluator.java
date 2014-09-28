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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.beans.DfBeanDesc;
import org.seasar.dbflute.helper.beans.DfPropertyDesc;
import org.seasar.dbflute.helper.beans.exception.DfBeanIllegalPropertyException;
import org.seasar.dbflute.helper.beans.exception.DfBeanMethodNotFoundException;
import org.seasar.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.twowaysql.exception.IfCommentDifferentTypeComparisonException;
import org.seasar.dbflute.twowaysql.exception.IfCommentEmptyExpressionException;
import org.seasar.dbflute.twowaysql.exception.IfCommentIllegalParameterBeanSpecificationException;
import org.seasar.dbflute.twowaysql.exception.IfCommentListIndexNotNumberException;
import org.seasar.dbflute.twowaysql.exception.IfCommentListIndexOutOfBoundsException;
import org.seasar.dbflute.twowaysql.exception.IfCommentMethodInvocationFailureException;
import org.seasar.dbflute.twowaysql.exception.IfCommentNotBooleanResultException;
import org.seasar.dbflute.twowaysql.exception.IfCommentNotFoundMethodException;
import org.seasar.dbflute.twowaysql.exception.IfCommentNotFoundPropertyException;
import org.seasar.dbflute.twowaysql.exception.IfCommentNullPointerException;
import org.seasar.dbflute.twowaysql.exception.IfCommentPropertyReadFailureException;
import org.seasar.dbflute.twowaysql.exception.IfCommentUnsupportedExpressionException;
import org.seasar.dbflute.twowaysql.exception.IfCommentUnsupportedTypeComparisonException;
import org.seasar.dbflute.twowaysql.pmbean.MapParameterBean;
import org.seasar.dbflute.util.DfReflectionUtil;
import org.seasar.dbflute.util.DfReflectionUtil.ReflectionFailureException;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.DfTypeUtil.ParseTimestampException;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class IfCommentEvaluator {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String AND = " && ";
    protected static final String OR = " || ";
    protected static final String EQUAL = " == ";
    protected static final String NOT_EQUAL = " != ";
    protected static final String GREATER_THAN = " > ";
    protected static final String LESS_THAN = " < ";
    protected static final String GREATER_EQUAL = " >= ";
    protected static final String LESS_EQUAL = " <= ";
    protected static final String BOOLEAN_NOT = "!";
    protected static final String METHOD_SUFFIX = "()";

    protected static final String[] CONNECTORS = new String[] { AND.trim(), OR.trim() };
    protected static final String[] OPERANDS = new String[] { EQUAL.trim(), NOT_EQUAL.trim(), GREATER_THAN.trim(),
            LESS_THAN.trim(), GREATER_EQUAL.trim(), LESS_EQUAL.trim() };

    public static String[] getConnectors() {
        return CONNECTORS;
    }

    public static String[] getOperands() {
        return OPERANDS;
    }

    public static boolean isConnector(String target) {
        return Srl.equalsPlain(target, CONNECTORS);
    }

    public static boolean isOperand(String target) {
        return Srl.equalsPlain(target, OPERANDS);
    }

    public static boolean isBooleanNotStatement(String target) {
        return Srl.startsWith(target, BOOLEAN_NOT);
    }

    public static boolean isMethodStatement(String target) {
        return Srl.endsWith(target, METHOD_SUFFIX);
    }

    public static String substringBooleanNotRear(String target) {
        return isBooleanNotStatement(target) ? Srl.substringFirstRear(target, BOOLEAN_NOT) : target;
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ParameterFinder _finder;
    protected final String _expression;
    protected final String _specifiedSql;
    protected final LoopInfo _loopInfo;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public IfCommentEvaluator(ParameterFinder finder, String expression, String specifiedSql, LoopInfo loopInfo) {
        this._finder = finder;
        this._expression = expression != null ? expression.trim() : null;
        this._specifiedSql = specifiedSql;
        this._loopInfo = loopInfo;
    }

    // ===================================================================================
    //                                                                              Accept
    //                                                                              ======
    public boolean evaluate() {
        assertExpression();
        if (_expression.contains(AND)) {
            final List<String> splitList = splitList(_expression, AND);
            for (String booleanClause : splitList) {
                final boolean result = evaluateBooleanClause(booleanClause);
                if (!result) {
                    return false;
                }
            }
            return true;
        } else if (_expression.contains(OR)) {
            final List<String> splitList = splitList(_expression, OR);
            for (String booleanClause : splitList) {
                final boolean result = evaluateBooleanClause(booleanClause);
                if (result) {
                    return true;
                }
            }
            return false;
        } else {
            return evaluateBooleanClause(_expression);
        }
    }

    public void assertExpression() {
        if (_expression == null || _expression.trim().length() == 0) {
            throwIfCommentEmptyExpressionException();
        }
        {
            String filtered = Srl.replace(_expression, "()", "");
            filtered = Srl.replace(filtered, ".get(", "");
            if (filtered.contains("(")) {
                throwIfCommentUnsupportedExpressionException();
            }
        }
        if (_expression.contains(AND) && _expression.contains(OR)) {
            throwIfCommentUnsupportedExpressionException();
        }
        if (_expression.contains(" = ") || _expression.contains(" <> ")) {
            throwIfCommentUnsupportedExpressionException();
        }
        if (_expression.contains("\"")) {
            throwIfCommentUnsupportedExpressionException();
        }
    }

    protected boolean evaluateBooleanClause(final String booleanClause) {
        if (booleanClause.contains(EQUAL)) {
            return evaluateCompareClause(booleanClause, EQUAL, new OperandEvaluator() {
                public boolean evaluate(Object leftResult, Object rightResult) {
                    if (leftResult instanceof Number && rightResult instanceof Number) {
                        leftResult = new BigDecimal(leftResult.toString());
                        rightResult = new BigDecimal(rightResult.toString());
                    }
                    assertCompareType(leftResult, rightResult, booleanClause);
                    return leftResult != null ? leftResult.equals(rightResult) : rightResult == null;
                }
            });
        } else if (booleanClause.contains(NOT_EQUAL)) {
            return evaluateCompareClause(booleanClause, NOT_EQUAL, new OperandEvaluator() {
                public boolean evaluate(Object leftResult, Object rightResult) {
                    if (leftResult instanceof Number && rightResult instanceof Number) {
                        leftResult = new BigDecimal(leftResult.toString());
                        rightResult = new BigDecimal(rightResult.toString());
                    }
                    assertCompareType(leftResult, rightResult, booleanClause);
                    return leftResult != null ? !leftResult.equals(rightResult) : rightResult != null;
                }
            });
        } else if (booleanClause.contains(GREATER_THAN)) {
            return evaluateCompareClause(booleanClause, GREATER_THAN, new OperandEvaluator() {
                public boolean evaluate(Object leftResult, Object rightResult) {
                    if (leftResult == null) {
                        return false;
                    }
                    if (rightResult == null) {
                        return true;
                    }
                    return compareLeftRight(leftResult, rightResult, new ComparaDeterminer() {
                        public boolean compare(int compareResult) {
                            return compareResult > 0;
                        }
                    }, booleanClause);
                }
            });
        } else if (booleanClause.contains(LESS_THAN)) {
            return evaluateCompareClause(booleanClause, LESS_THAN, new OperandEvaluator() {
                public boolean evaluate(Object leftResult, Object rightResult) {
                    if (leftResult == null) {
                        return true;
                    }
                    if (rightResult == null) {
                        return false;
                    }
                    return compareLeftRight(leftResult, rightResult, new ComparaDeterminer() {
                        public boolean compare(int compareResult) {
                            return compareResult < 0;
                        }
                    }, booleanClause);
                }
            });
        } else if (booleanClause.contains(GREATER_EQUAL)) {
            return evaluateCompareClause(booleanClause, GREATER_EQUAL, new OperandEvaluator() {
                public boolean evaluate(Object leftResult, Object rightResult) {
                    if (leftResult == null) {
                        return rightResult == null;
                    }
                    if (rightResult == null) {
                        return true;
                    }
                    return compareLeftRight(leftResult, rightResult, new ComparaDeterminer() {
                        public boolean compare(int compareResult) {
                            return compareResult >= 0;
                        }
                    }, booleanClause);
                }
            });
        } else if (booleanClause.contains(LESS_EQUAL)) {
            return evaluateCompareClause(booleanClause, LESS_EQUAL, new OperandEvaluator() {
                public boolean evaluate(Object leftResult, Object rightResult) {
                    if (leftResult == null) {
                        return true;
                    }
                    if (rightResult == null) {
                        return false;
                    }
                    return compareLeftRight(leftResult, rightResult, new ComparaDeterminer() {
                        public boolean compare(int compareResult) {
                            return compareResult <= 0;
                        }
                    }, booleanClause);
                }
            });
        } else {
            return evaluateStandAloneValue(booleanClause);
        }
    }

    protected boolean compareLeftRight(Object leftResult, Object rightResult, ComparaDeterminer determiner,
            String booleanClause) {
        assertCompareType(leftResult, rightResult, booleanClause);
        if (leftResult instanceof Date) {
            final Date leftDate = (Date) leftResult;
            final Date rightDate = (Date) rightResult;
            return determiner.compare(leftDate.compareTo(rightDate));
        } else if (leftResult instanceof Number) {
            final Number leftNumber = (Number) leftResult;
            final BigDecimal leftDecimal = new BigDecimal(leftNumber.toString());
            final Number rightNumber = (Number) rightResult;
            final BigDecimal rightDecimal = new BigDecimal(rightNumber.toString());
            return determiner.compare(leftDecimal.compareTo(rightDecimal));
        } else {
            throwIfCommentUnsupportedTypeComparisonException(leftResult, rightResult, booleanClause);
            return false; // unreachable
        }
    }

    protected void assertCompareType(Object leftResult, Object rightResult, String booleanClause) {
        if (leftResult != null && rightResult != null && leftResult instanceof Date) {
            if (!(rightResult instanceof Date)) {
                throwIfCommentDifferentTypeComparisonException(leftResult, rightResult, booleanClause);
            }
        } else if (leftResult != null && rightResult != null && leftResult instanceof Number) {
            if (!(rightResult instanceof Number)) {
                throwIfCommentDifferentTypeComparisonException(leftResult, rightResult, booleanClause);
            }
        }
    }

    protected static interface ComparaDeterminer {
        boolean compare(int compareResult);
    }

    protected boolean evaluateCompareClause(String booleanClause, String operand, OperandEvaluator evaluator) {
        final String left = booleanClause.substring(0, booleanClause.indexOf(operand)).trim();
        final String right = booleanClause.substring(booleanClause.indexOf(operand) + operand.length()).trim();
        final Object leftResult = evaluateComparePiece(left, null);
        final Object rightResult = evaluateComparePiece(right, null);
        return evaluator.evaluate(leftResult, rightResult);
    }

    protected static interface OperandEvaluator {
        boolean evaluate(Object leftResult, Object rightRight);
    }

    protected Object evaluateComparePiece(String piece, Object leftResult) {
        piece = piece.trim();
        if (!startsWithParameterBean(piece)) {
            if ("null".equalsIgnoreCase(piece)) {
                return null;
            }
            if ("true".equalsIgnoreCase(piece)) {
                return true;
            }
            if ("false".equalsIgnoreCase(piece)) {
                return false;
            }
            final String quote = "'";
            final int qlen = "'".length();
            if (piece.startsWith(quote) && piece.endsWith(quote)) {
                return piece.substring(qlen, piece.length() - qlen);
            }
            final String dateMark = "date ";
            if (piece.toLowerCase().startsWith(dateMark)) {
                final String rearValue = piece.substring(dateMark.length()).trim();
                if (rearValue.startsWith(quote) && rearValue.endsWith(quote)) {
                    final String literal = rearValue.substring(qlen, rearValue.length() - qlen).trim();
                    try {
                        return DfTypeUtil.toTimestamp(literal);
                    } catch (ParseTimestampException ignored) {
                    }
                }
            }
            try {
                return DfTypeUtil.toBigDecimal(piece);
            } catch (NumberFormatException ignored) {
            }
        }
        final List<String> propertyList = new ArrayList<String>();
        String preProperty = setupPropertyList(piece, propertyList);
        Object baseObject = findBaseObject(preProperty);
        for (String property : propertyList) {
            baseObject = processOneProperty(baseObject, preProperty, property);
            preProperty = property;
        }
        return baseObject;
    }

    protected boolean evaluateStandAloneValue(String piece) {
        piece = piece.trim();
        boolean not = false;
        if (piece.startsWith(BOOLEAN_NOT)) {
            not = true;
            piece = piece.substring(BOOLEAN_NOT.length());
        }
        if (!startsWithParameterBean(piece)) {
            if ("true".equalsIgnoreCase(piece)) {
                return not ? false : true;
            }
            if ("false".equalsIgnoreCase(piece)) {
                return not ? true : false;
            }
        }
        final List<String> propertyList = new ArrayList<String>();
        String preProperty = setupPropertyList(piece, propertyList);
        Object baseObject = findBaseObject(preProperty);
        for (String property : propertyList) {
            baseObject = processOneProperty(baseObject, preProperty, property);
            preProperty = property;
        }
        if (baseObject == null) {
            throwIfCommentNotBooleanResultException();
        }
        final boolean result = Boolean.valueOf(baseObject.toString());
        return not ? !result : result;
    }

    protected boolean startsWithParameterBean(String piece) {
        return piece.startsWith("pmb");
    }

    /**
     * @param piece The piece of condition. (NotNull)
     * @param propertyList The list of property except first property. (NotNull, FirstEmpty)
     * @return The first property. (NotNull)
     */
    protected String setupPropertyList(String piece, List<String> propertyList) {
        final List<String> splitList = splitList(piece, ".");
        String firstName = null;
        for (int i = 0; i < splitList.size(); i++) {
            final String token = splitList.get(i);
            if (i == 0) {
                assertFirstName(token);
                firstName = token;
                continue;
            }
            propertyList.add(token);
        }
        return firstName;
    }

    protected void assertFirstName(String firstName) {
        if (isLoopCurrentVariable(firstName)) {
            return;
        }
        if (NodeUtil.isCurrentVariableOutOfScope(firstName, isInLoop())) {
            throwLoopCurrentVariableOutOfForCommentException();
        }
        final Object firstArg = _finder.find(firstName); // get from plain context
        if (NodeUtil.isWrongParameterBeanName(firstName, firstArg)) {
            throwIfCommentIllegalParameterBeanSpecificationException();
        }
    }

    protected void throwLoopCurrentVariableOutOfForCommentException() {
        NodeUtil.throwLoopCurrentVariableOutOfForCommentException(_expression, _specifiedSql);
    }

    protected Object processOneProperty(Object baseObject, String firstProperty, String property) {
        if (baseObject == null) {
            throwIfCommentNullPointerException(firstProperty);
        }
        final DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc(baseObject.getClass());
        if (beanDesc.hasPropertyDesc(property)) { // main case
            final DfPropertyDesc propertyDesc = beanDesc.getPropertyDesc(property);
            try {
                return propertyDesc.getValue(baseObject);
            } catch (DfBeanIllegalPropertyException e) {
                throwIfCommentPropertyReadFailureException(baseObject, propertyDesc.getPropertyName(), e);
                return null; // unreachable
            }
        }
        if (property.endsWith(METHOD_SUFFIX)) { // sub-main case
            final String methodName = property.substring(0, property.length() - METHOD_SUFFIX.length());
            try {
                final Method method = beanDesc.getMethod(methodName);
                return DfReflectionUtil.invoke(method, baseObject, (Object[]) null);
            } catch (DfBeanMethodNotFoundException e) {
                throwIfCommentNotFoundMethodException(baseObject, methodName);
                return null; // unreachable
            } catch (ReflectionFailureException e) {
                throwIfCommentMethodInvocationFailureException(baseObject, methodName, e);
                return null; // unreachable
            }
        }
        if (MapParameterBean.class.isInstance(baseObject)) { // used by union-query internally
            // if the key does not exist, it does not process
            // (different specification with Map)
            final Map<?, ?> map = ((MapParameterBean<?>) baseObject).getParameterMap();
            if (map.containsKey(property)) {
                return map.get(property);
            }
        }
        if (Map.class.isInstance(baseObject)) {
            // if the key does not exist, treated same as a null value
            final Map<?, ?> map = (Map<?, ?>) baseObject;
            return map.get(property);
        }
        if (List.class.isInstance(baseObject)) {
            if (property.startsWith("get(") && property.endsWith(")")) {
                final List<?> list = (List<?>) baseObject;
                final String exp = Srl.extractScopeFirst(property, "get(", ")").getContent();
                try {
                    final Integer index = DfTypeUtil.toInteger(exp);
                    return list.get(index);
                } catch (NumberFormatException e) {
                    throwIfCommentListIndexNotNumberException(list, exp, e);
                    return null; // unreachable
                } catch (IndexOutOfBoundsException e) {
                    throwIfCommentListIndexOutOfBoundsException(list, exp, e);
                    return null; // unreachable
                }
            }
        }
        throwIfCommentNotFoundPropertyException(baseObject, property);
        return null; // unreachable
    }

    // ===================================================================================
    //                                                                         Base Object
    //                                                                         ===========
    protected Object findBaseObject(String firstName) {
        if (isLoopCurrentVariable(firstName)) {
            return _loopInfo.getCurrentParameter();
        } else {
            return _finder.find(firstName);
        }
    }

    // ===================================================================================
    //                                                                           Loop Info
    //                                                                           =========
    protected boolean isInLoop() {
        return _loopInfo != null;
    }

    protected boolean isLoopCurrentVariable(String firstName) {
        return isInLoop() && ForNode.CURRENT_VARIABLE.equals(firstName);
    }

    // ===================================================================================
    //                                                                           Exception
    //                                                                           =========
    protected void throwIfCommentEmptyExpressionException() {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The IF comment expression was empty!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "Please confirm your IF comment." + ln();
        msg = msg + "For example, wrong and correct IF comment is as below:" + ln();
        msg = msg + "  /- - - - - - - - - - - - - - - - - - - - - - - - - - " + ln();
        msg = msg + "  (x) - /*IF */" + ln();
        msg = msg + "  (o) - /*IF pmb.memberId != null*/" + ln();
        msg = msg + "  - - - - - - - - - -/" + ln();
        msg = msg + ln();
        msg = msg + "[IF Comment Expression]" + ln() + _expression + ln();
        msg = msg + ln();
        msg = msg + "[Specified ParameterBean]" + ln() + getDisplayParameterBean() + ln();
        msg = msg + ln();
        msg = msg + "[Specified SQL]" + ln() + _specifiedSql + ln();
        msg = msg + "* * * * * * * * * */";
        throw new IfCommentEmptyExpressionException(msg);
    }

    protected void throwIfCommentUnsupportedExpressionException() {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The IF comment expression was unsupported!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "Please confirm your unsupported IF comment." + ln();
        msg = msg + "For example, unsupported examples:" + ln();
        msg = msg + "  (x:andOr) - /*IF (pmb.fooId != null || pmb.barId != null) && pmb.fooName != null*/" + ln();
        msg = msg + "  (x:argsMethod) - /*IF pmb.buildFooId(123)*/" + ln();
        msg = msg + "  (x:stringLiteral) - /*IF pmb.fooName == 'Pixy' || pmb.fooName == \"Pixy\"*/" + ln();
        msg = msg + "  (x:singleEqual) - /*IF pmb.fooId = null*/ --> /*IF pmb.fooId == null*/" + ln();
        msg = msg + "  (x:anotherNot) - /*IF pmb.fooId <> null*/ --> /*IF pmb.fooId != null*/" + ln();
        msg = msg + "  (x:doubleQuotation) - /*IF pmb.fooName == \"Pixy\"*/ --> /*IF pmb.fooName == 'Pixy'*/" + ln();
        msg = msg + "  " + ln();
        msg = msg + "If you want to write a complex condition, write an ExParameterBean property." + ln();
        msg = msg + "And use the property in IF comment." + ln();
        msg = msg + "For example, ExParameterBean original property:" + ln();
        msg = msg + "  e.g. ExParameterBean (your original property)" + ln();
        msg = msg + "  /- - - - - - - - - - - - - - - - - - - - - - - - - - " + ln();
        msg = msg + "  public boolean isOriginalMemberProperty() {" + ln();
        msg = msg + "      return (getMemberId() != null || getBirthdate() != null) && getMemberName() != null);"
                + ln();
        msg = msg + "  }" + ln();
        msg = msg + "  - - - - - - - - - -/" + ln();
        msg = msg + "  " + ln();
        msg = msg + "  e.g. IF comment" + ln();
        msg = msg + "  /- - - - - - - - - - - - - - - - - - - - - - - - - - " + ln();
        msg = msg + "  /*IF pmb.originalMemberProperty*/" + ln();
        msg = msg + "  - - - - - - - - - -/" + ln();
        msg = msg + ln();
        msg = msg + "[IF Comment Expression]" + ln() + _expression + ln();
        msg = msg + ln();
        msg = msg + "[Specified ParameterBean]" + ln() + getDisplayParameterBean() + ln();
        msg = msg + ln();
        msg = msg + "[Specified SQL]" + ln() + _specifiedSql + ln();
        msg = msg + "* * * * * * * * * */";
        throw new IfCommentUnsupportedExpressionException(msg);
    }

    public void throwIfCommentIllegalParameterBeanSpecificationException() {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The IF comment had the illegal parameter-bean specification!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "Please confirm your IF comment." + ln();
        msg = msg + "For example, wrong and correct IF comment is as below:" + ln();
        msg = msg + "  (x) - /*IF pmb,memberId != null*/" + ln();
        msg = msg + "  (x) - /*IF p mb.memberId != null*/" + ln();
        msg = msg + "  (x) - /*IF pmb:memberId != null*/" + ln();
        msg = msg + "  (x) - /*IF pnb.memberId != null*/" + ln();
        msg = msg + "  (o) - /*IF pmb.memberId != null*/" + ln();
        msg = msg + ln();
        msg = msg + "[IF Comment Expression]" + ln() + _expression + ln();
        msg = msg + ln();
        msg = msg + "[Specified ParameterBean]" + ln() + getDisplayParameterBean() + ln();
        msg = msg + ln();
        msg = msg + "[Specified SQL]" + ln() + _specifiedSql + ln();
        msg = msg + "* * * * * * * * * */";
        throw new IfCommentIllegalParameterBeanSpecificationException(msg);
    }

    protected void throwIfCommentPropertyReadFailureException(Object baseObject, String propertyName,
            DfBeanIllegalPropertyException e) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("Failed to read the property on the IF comment!");
        br.addItem("Advice");
        br.addElement("Please confirm your IF comment properties.");
        br.addElement("(readable? accessbile? and so on...)");
        br.addItem("IF Comment");
        br.addElement(_expression);
        br.addItem("Illegal Property");
        br.addElement(DfTypeUtil.toClassTitle(baseObject) + "." + propertyName);
        br.addItem("Exception Message");
        br.addElement(e.getClass());
        br.addElement(e.getMessage());
        final Throwable cause = e.getCause();
        if (cause != null) { // basically DfBeanIllegalPropertyException has its cause
            br.addElement(cause.getClass());
            br.addElement(cause.getMessage());
            final Throwable nextCause = cause.getCause();
            if (nextCause != null) {
                br.addElement(nextCause.getClass());
                br.addElement(nextCause.getMessage());
            }
        }
        br.addItem("Specified ParameterBean");
        br.addElement(getDisplayParameterBean());
        br.addItem("Specified SQL");
        br.addElement(_specifiedSql);
        final String msg = br.buildExceptionMessage();
        throw new IfCommentPropertyReadFailureException(msg, e);
    }

    protected void throwIfCommentNotFoundMethodException(Object baseObject, String notFoundMethod) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The method on the IF comment was not found!");
        br.addItem("Advice");
        br.addElement("Please confirm your IF comment properties.");
        br.addElement("For example, wrong and correct IF comment is as below:");
        br.addElement("  /- - - - - - - - - - - - - - - - - - - - - - - - - - ");
        br.addElement("  (x) - /*IF pmb.getMemborWame() != null*/");
        br.addElement("  (o) - /*IF pmb.getMemberName() != null*/");
        br.addElement("  - - - - - - - - - -/");
        br.addItem("IF Comment");
        br.addElement(_expression);
        br.addItem("NotFound Method");
        br.addElement(DfTypeUtil.toClassTitle(baseObject) + "." + notFoundMethod + "()");
        br.addItem("Specified ParameterBean");
        br.addElement(getDisplayParameterBean());
        br.addItem("Specified SQL");
        br.addElement(_specifiedSql);
        final String msg = br.buildExceptionMessage();
        throw new IfCommentNotFoundMethodException(msg);
    }

    protected void throwIfCommentMethodInvocationFailureException(Object baseObject, String methodName,
            ReflectionFailureException e) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("Failed to invoke the method on the IF comment!");
        br.addItem("Advice");
        br.addElement("Please confirm the method implementation on your comment.");
        br.addItem("IF Comment");
        br.addElement(_expression);
        br.addItem("Failure Method");
        br.addElement(DfTypeUtil.toClassTitle(baseObject) + "." + methodName + "()");
        br.addItem("Exception Message");
        br.addElement(e.getMessage());
        br.addItem("Specified ParameterBean");
        br.addElement(getDisplayParameterBean());
        br.addItem("Specified SQL");
        br.addElement(_specifiedSql);
        final String msg = br.buildExceptionMessage();
        throw new IfCommentMethodInvocationFailureException(msg, e);
    }

    protected void throwIfCommentNotFoundPropertyException(Object baseObject, String notFoundProperty) {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The IF comment property was not found!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "Please confirm your IF comment properties." + ln();
        msg = msg + "For example, wrong and correct IF comment is as below:" + ln();
        msg = msg + "  /- - - - - - - - - - - - - - - - - - - - - - - - - - " + ln();
        msg = msg + "  (x) - /*IF pmb.memderBame != null*/" + ln();
        msg = msg + "  (o) - /*IF pmb.memberName != null*/" + ln();
        msg = msg + "  - - - - - - - - - -/" + ln();
        msg = msg + ln();
        msg = msg + "[IF Comment Expression]" + ln() + _expression + ln();
        msg = msg + ln();
        msg = msg + "[not found Property]" + ln();
        msg = msg + (baseObject != null ? DfTypeUtil.toClassTitle(baseObject) + "." : "");
        msg = msg + notFoundProperty + ln();
        msg = msg + ln();
        msg = msg + "[Specified ParameterBean]" + ln() + getDisplayParameterBean() + ln();
        msg = msg + ln();
        msg = msg + "[Specified SQL]" + ln() + _specifiedSql + ln();
        msg = msg + "* * * * * * * * * */";
        throw new IfCommentNotFoundPropertyException(msg);
    }

    protected void throwIfCommentNullPointerException(String nullProperty) {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The IF comment met the null pointer!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "Please confirm your IF comment and its property values." + ln();
        msg = msg + ln();
        msg = msg + "[IF Comment Expression]" + ln() + _expression + ln();
        msg = msg + ln();
        msg = msg + "[Null Property]" + ln() + nullProperty + ln();
        msg = msg + ln();
        msg = msg + "[Specified ParameterBean]" + ln() + getDisplayParameterBean() + ln();
        msg = msg + ln();
        msg = msg + "[Specified SQL]" + ln() + _specifiedSql + ln();
        msg = msg + "* * * * * * * * * */";
        throw new IfCommentNullPointerException(msg);
    }

    protected void throwIfCommentDifferentTypeComparisonException(Object left, Object right, String booleanClause) {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The IF comment had the different type comparison!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "Please confirm your IF comment property types." + ln();
        msg = msg + "If the left type is Number, the right type should be Number." + ln();
        msg = msg + "If the left type is Date, the right type should be Date." + ln();
        msg = msg + ln();
        msg = msg + "[IF Comment Expression]" + ln() + _expression + ln();
        msg = msg + ln();
        msg = msg + "[Target Boolean Clause]" + ln() + booleanClause + ln();
        msg = msg + ln();
        msg = msg + "[Left]" + ln() + (left != null ? left.getClass() : "null") + ln();
        msg = msg + " --> " + left + ln();
        msg = msg + ln();
        msg = msg + "[Right]" + ln() + (right != null ? right.getClass() : "null") + ln();
        msg = msg + " --> " + right + ln();
        msg = msg + ln();
        msg = msg + "[Specified ParameterBean]" + ln() + getDisplayParameterBean() + ln();
        msg = msg + ln();
        msg = msg + "[Specified SQL]" + ln() + _specifiedSql + ln();
        msg = msg + "* * * * * * * * * */";
        throw new IfCommentDifferentTypeComparisonException(msg);
    }

    protected void throwIfCommentUnsupportedTypeComparisonException(Object left, Object right, String booleanClause) {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The IF comment had the different type comparison!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "Please confirm your IF comment property types." + ln();
        msg = msg + "For example, String type is unsupported at comparison(>, <, >=, <=)." + ln();
        msg = msg + "Number and Date are only supported." + ln();
        msg = msg + ln();
        msg = msg + "[IF Comment Expression]" + ln() + _expression + ln();
        msg = msg + ln();
        msg = msg + "[Target Boolean Clause]" + ln() + booleanClause + ln();
        msg = msg + ln();
        msg = msg + "[Left]" + ln() + (left != null ? left.getClass() : "null") + ln();
        msg = msg + " --> " + left + ln();
        msg = msg + ln();
        msg = msg + "[Right]" + ln() + (right != null ? right.getClass() : "null") + ln();
        msg = msg + " --> " + right + ln();
        msg = msg + ln();
        msg = msg + "[Specified ParameterBean]" + ln() + getDisplayParameterBean() + ln();
        msg = msg + ln();
        msg = msg + "[Specified SQL]" + ln() + _specifiedSql + ln();
        msg = msg + "* * * * * * * * * */";
        throw new IfCommentUnsupportedTypeComparisonException(msg);
    }

    protected void throwIfCommentNotBooleanResultException() {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The IF comment was not boolean!");
        br.addItem("Advice");
        br.addElement("Please confirm your IF comment property.");
        br.addElement("IF-statement result should be boolean type.");
        br.addElement("(and also the result should not be null)");
        br.addItem("IF Comment");
        br.addElement(_expression);
        br.addItem("Specified ParameterBean");
        br.addElement(getDisplayParameterBean());
        br.addItem("Specified SQL");
        br.addElement(_specifiedSql);
        final String msg = br.buildExceptionMessage();
        throw new IfCommentNotBooleanResultException(msg);
    }

    protected void throwIfCommentListIndexNotNumberException(List<?> list, String notNumberIndex,
            NumberFormatException e) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The list index on the IF comment was not number!");
        br.addItem("Advice");
        br.addElement("Please confirm the index on your comment.");
        br.addItem("IF Comment");
        br.addElement(_expression);
        br.addItem("Target List");
        br.addElement(list);
        br.addItem("NotNumber Index");
        br.addElement(notNumberIndex);
        br.addItem("NumberFormatException");
        br.addElement(e.getMessage());
        br.addItem("Specified ParameterBean");
        br.addElement(getDisplayParameterBean());
        br.addItem("Specified SQL");
        br.addElement(_specifiedSql);
        final String msg = br.buildExceptionMessage();
        throw new IfCommentListIndexNotNumberException(msg, e);
    }

    protected void throwIfCommentListIndexOutOfBoundsException(List<?> list, String numberIndex,
            IndexOutOfBoundsException e) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The list index on the IF comment was out of bounds!");
        br.addItem("Advice");
        br.addElement("Please confirm the index on your comment.");
        br.addItem("IF Comment");
        br.addElement(_expression);
        br.addItem("Target List");
        br.addElement(list);
        br.addItem("OutOfBounds Index");
        br.addElement(numberIndex);
        br.addItem("IndexOutOfBoundsException");
        br.addElement(e.getMessage());
        br.addItem("Specified ParameterBean");
        br.addElement(getDisplayParameterBean());
        br.addItem("Specified SQL");
        br.addElement(_specifiedSql);
        final String msg = br.buildExceptionMessage();
        throw new IfCommentListIndexOutOfBoundsException(msg, e);
    }

    protected Object getDisplayParameterBean() {
        // basically these exceptions are for debug,
        // so it can show the values of parameter-bean
        return findBaseObject("pmb");
    }

    // ===================================================================================
    //                                                                    Exception Helper
    //                                                                    ================
    protected ExceptionMessageBuilder createExceptionMessageBuilder() {
        return new ExceptionMessageBuilder();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }

    protected List<String> splitList(String str, String delimiter) {
        return Srl.splitList(str, delimiter);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getExpression() {
        return _expression;
    }
}
