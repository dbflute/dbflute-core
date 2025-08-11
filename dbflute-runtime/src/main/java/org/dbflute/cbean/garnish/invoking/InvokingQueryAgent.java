/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.cbean.garnish.invoking;

import static org.dbflute.util.Srl.initCap;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.dbflute.cbean.ConditionQuery;
import org.dbflute.cbean.coption.ConditionOption;
import org.dbflute.cbean.coption.FromToOption;
import org.dbflute.cbean.coption.LikeSearchOption;
import org.dbflute.cbean.coption.RangeOfOption;
import org.dbflute.cbean.cvalue.ConditionValue;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.DBMetaProvider;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.exception.ConditionInvokingFailureException;
import org.dbflute.exception.IllegalConditionBeanOperationException;
import org.dbflute.helper.beans.DfBeanDesc;
import org.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfReflectionUtil;
import org.dbflute.util.DfReflectionUtil.ReflectionFailureException;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.7 split from ConditionQuery (2023/07/21 Friday at ichihara)
 */
public class InvokingQueryAgent {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ConditionQuery _rootQuery; // calls invoking, not null
    protected final DBMetaProvider _dbmetaProvider; // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public InvokingQueryAgent(ConditionQuery rootQuery, DBMetaProvider dbmetaProvider) {
        _rootQuery = rootQuery;
        _dbmetaProvider = dbmetaProvider;
    }

    // ===================================================================================
    //                                                                     Condition Value
    //                                                                     ===============
    public ConditionValue invokeValue(DBMeta dbmeta, String columnFlexibleName) {
        assertStringNotNullAndNotTrimmedEmpty("columnFlexibleName", columnFlexibleName);
        final String columnCapPropName = initCap(dbmeta.findColumnInfo(columnFlexibleName).getPropertyName());
        final String methodName = "xdfget" + columnCapPropName;
        final Method method = searchSimpleCQMethod(_rootQuery, methodName, (Class<?>[]) null);
        if (method == null) {
            throwConditionInvokingGetMethodNotFoundException(columnFlexibleName, methodName);
            return null; // unreachable
        }
        try {
            return (ConditionValue) xhelpInvokingCQMethod(_rootQuery, method, (Object[]) null);
        } catch (ReflectionFailureException e) {
            throwConditionInvokingGetReflectionFailureException(columnFlexibleName, methodName, e);
            return null; // unreachable
        }
    }

    protected void throwConditionInvokingGetMethodNotFoundException(String columnFlexibleName, String methodName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the method for getting the condition.");
        br.addItem("columnFlexibleName");
        br.addElement(columnFlexibleName);
        br.addItem("methodName");
        br.addElement(methodName);
        final String msg = br.buildExceptionMessage();
        throw new ConditionInvokingFailureException(msg);
    }

    protected void throwConditionInvokingGetReflectionFailureException(String columnFlexibleName, String methodName,
            ReflectionFailureException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to invoke the method for getting value.");
        br.addItem("columnFlexibleName");
        br.addElement(columnFlexibleName);
        br.addItem("methodName");
        br.addElement(methodName);
        final String msg = br.buildExceptionMessage();
        throw new ConditionInvokingFailureException(msg, e);
    }

    // ===================================================================================
    //                                                                           Query Set
    //                                                                           =========
    public void invokeQuery(String colName, String ckey, Object value, ConditionOption option) {
        assertStringNotNullAndNotTrimmedEmpty("columnFlexibleName", colName);
        assertStringNotNullAndNotTrimmedEmpty("conditionKeyName", ckey);
        // value, option are null allowed

        final boolean noArg = Srl.equalsIgnoreCase(ckey, "IsNull", "IsNotNull", "IsNullOrEmpty", "EmptyString");
        if (!noArg && (value == null || "".equals(value))) { // means having argument but null/empty
            // same logic as direct call, duplicate but check before reflection for performance
            if ("".equals(value) && _rootQuery.xgetSqlClause().isEmptyStringQueryAllowed()) {
                // no advance handling here in spite of NullOrEmptyQuery check
            } else { // null value or not-allowed empty string
                if (_rootQuery.xgetSqlClause().isNullOrEmptyQueryChecked()) { // as default
                    String msg = "The conditionValue is required but null or empty: column=" + colName + " value=" + value;
                    throw new IllegalConditionBeanOperationException(msg);
                } else { // e.g. when cb.ignoreNullOrEmptyQuery()
                    return; // no reflection end
                }
            }
        }
        final PropertyNameCQContainer container = xhelpExtractingPropertyNameCQContainer(colName);
        final String flexibleName = container.getFlexibleName();
        final ConditionQuery declaringCQ = container.getConditionQuery();
        final DBMeta dbmeta = _dbmetaProvider.provideDBMetaChecked(declaringCQ.asTableDbName());
        final ColumnInfo columnInfo;
        try {
            columnInfo = dbmeta.findColumnInfo(flexibleName);
        } catch (RuntimeException e) {
            throwConditionInvokingColumnFindFailureException(colName, ckey, value, option, e);
            return; // unreachable (to avoid compile error)
        }
        final String columnCapPropName = initCap(columnInfo.getPropertyName());
        final boolean rangeOf = Srl.equalsIgnoreCase(ckey, "RangeOf");
        final boolean fromTo = Srl.equalsIgnoreCase(ckey, "FromTo", "DateFromTo");
        final boolean inScope = Srl.equalsIgnoreCase(ckey, "InScope");
        if (!noArg) {
            try {
                value = columnInfo.convertToObjectNativeType(value); // convert type
            } catch (RuntimeException e) {
                throwConditionInvokingValueConvertFailureException(colName, ckey, value, option, e);
            }
        }
        final String methodName = xbuildQuerySetMethodName(ckey, columnCapPropName);
        final List<Class<?>> typeList = newArrayListSized(4);
        final Class<?> propertyType = columnInfo.getObjectNativeType();
        if (fromTo) {
            if (LocalDate.class.isAssignableFrom(propertyType)) { // #date_parade
                typeList.add(propertyType);
                typeList.add(propertyType);
            } else if (LocalDateTime.class.isAssignableFrom(propertyType)) {
                typeList.add(propertyType);
                typeList.add(propertyType);
            } else { // fixedly util.Date
                typeList.add(Date.class);
                typeList.add(Date.class);
            }
        } else if (rangeOf) {
            typeList.add(propertyType);
            typeList.add(propertyType);
        } else {
            if (!noArg) {
                final Class<?> instanceType = value.getClass();
                if (inScope && Collection.class.isAssignableFrom(instanceType)) { // double check just in case
                    typeList.add(Collection.class); // inScope's argument is fixed type
                } else {
                    typeList.add(instanceType);
                }
            }
        }
        if (option != null) { // always last argument (is implementation policy)
            typeList.add(option.getClass());
        }
        final List<Class<?>> filteredTypeList = newArrayListSized(typeList.size());
        for (Class<?> parameterType : typeList) {
            filteredTypeList.add(xfilterInvokeQueryParameterType(colName, ckey, parameterType));
        }
        final Class<?>[] argTypes = filteredTypeList.toArray(new Class<?>[filteredTypeList.size()]);
        final Method method = searchQuerySetMethod(declaringCQ, methodName, argTypes, option);
        if (method == null) {
            throwConditionInvokingSetMethodNotFoundException(colName, ckey, value, option, methodName, argTypes);
        }
        try {
            final List<Object> argList = newArrayList();
            if (fromTo || rangeOf) {
                if (!(value instanceof List<?>)) { // check type
                    throwConditionInvokingDateFromToValueInvalidException(colName, ckey, value, option, methodName, argTypes);
                }
                argList.addAll((List<?>) value);
            } else {
                if (!noArg) {
                    argList.add(value);
                }
            }
            if (option != null) {
                argList.add(option);
            }
            final List<Object> filteredArgList = newArrayListSized(argList.size());
            for (Object arg : argList) {
                filteredArgList.add(xfilterInvokeQueryParameterValue(colName, ckey, arg));
            }
            xhelpInvokingCQMethod(declaringCQ, method, filteredArgList.toArray());
        } catch (ReflectionFailureException e) {
            throwConditionInvokingSetReflectionFailureException(colName, ckey, value, option, methodName, argTypes, e);
        }
    }

    // -----------------------------------------------------
    //                                           Method Name
    //                                           -----------
    protected String xbuildQuerySetMethodName(String ckey, String columnCapPropName) {
        return "set" + columnCapPropName + "_" + initCap(ckey);
    }

    // -----------------------------------------------------
    //                                        Parameter Type
    //                                        --------------
    protected Class<?> xfilterInvokeQueryParameterType(String colName, String ckey, Class<?> parameterType) {
        return parameterType; // no filter as default (e.g. overridden by Scala to convert to immutable list)
    }

    // -----------------------------------------------------
    //                                         Method Search
    //                                         -------------
    protected Method searchQuerySetMethod(ConditionQuery declaringCQ, String methodName, Class<?>[] argTypes, ConditionOption option) {
        Method found = findCQPublicMethod(declaringCQ, methodName, argTypes);
        if (found != null) { // e.g. simple set method (without option), setSea_Equal(), setLand_GreaterThan()
            return found;
        }
        // maybe non public method here (basically option-setting method)
        // e.g. protected void setSea_LikeSearch(String, LikeSearchOption)
        // (ConditionOptionCall<LikeSearchOption> is public, LikeSearchOption is protected since 1.1.0)

        // retry for callback way of option-setting method
        if (option != null) { // e.g. LikeSearch
            // option is always last argument (is implementation policy)
            Class<?>[] retryArgType = null;
            if (isLastArgConditionOptionExtended(LikeSearchOption.class, argTypes)) {
                retryArgType = deriveRetryArgType(argTypes, LikeSearchOption.class);
            } else if (isLastArgConditionOptionExtended(RangeOfOption.class, argTypes)) {
                retryArgType = deriveRetryArgType(argTypes, RangeOfOption.class);
            } else if (isLastArgConditionOptionExtended(FromToOption.class, argTypes)) {
                retryArgType = deriveRetryArgType(argTypes, FromToOption.class);
            }
            if (retryArgType != null) {
                found = findCQWholeMethod(declaringCQ, methodName, retryArgType);
                if (found != null) {
                    return found;
                }
            }
        }

        // so next, searching all protected, private methods
        // non-cache #for_now e.g. native method of classification
        // (but cache of Class.class can be available)
        return findCQWholeMethod(declaringCQ, methodName, argTypes);
    }

    protected boolean isLastArgConditionOptionExtended(Class<?> pureOptionType, Class<?>[] argTypes) {
        if (argTypes.length == 0) {
            return false;
        }
        final Class<?> lastType = argTypes[argTypes.length - 1]; // actual type from specified option instance
        return pureOptionType.isAssignableFrom(lastType) && !pureOptionType.equals(lastType);
    }

    protected Class<?>[] deriveRetryArgType(Class<?>[] argTypes, Class<?> pureOptionType) {
        final Class<?>[] retryArgTypes = new Class<?>[argTypes.length];
        int index = 0;
        for (Class<?> argType : argTypes) {
            if (index == argTypes.length - 1) { // last loop
                retryArgTypes[index] = pureOptionType;
            } else {
                retryArgTypes[index] = argType;
            }
            ++index;
        }
        return retryArgTypes;
    }

    // -----------------------------------------------------
    //                                       Parameter Value
    //                                       ---------------
    protected Object xfilterInvokeQueryParameterValue(String colName, String ckey, Object parameterValue) {
        return parameterValue; // no filter as default (e.g. overridden by Scala to convert to immutable list)
    }

    // -----------------------------------------------------
    //                                             Exception
    //                                             ---------
    protected void throwConditionInvokingColumnFindFailureException(String columnFlexibleName, String conditionKeyName,
            Object conditionValue, ConditionOption conditionOption, RuntimeException cause) {
        final String notice = "Failed to find the column in the table.";
        doThrowConditionInvokingFailureException(notice, columnFlexibleName, conditionKeyName, conditionValue, conditionOption, null, null,
                cause);
    }

    protected void throwConditionInvokingValueConvertFailureException(String columnFlexibleName, String conditionKeyName,
            Object conditionValue, ConditionOption conditionOption, RuntimeException cause) {
        final String notice = "Failed to convert the value to property type.";
        doThrowConditionInvokingFailureException(notice, columnFlexibleName, conditionKeyName, conditionValue, conditionOption, null, null,
                cause);
    }

    protected void throwConditionInvokingSetMethodNotFoundException(String columnFlexibleName, String conditionKeyName,
            Object conditionValue, ConditionOption conditionOption, String methodName, Class<?>[] argTypes) {
        final String notice = "Not found the method for setting the condition.";
        doThrowConditionInvokingFailureException(notice, columnFlexibleName, conditionKeyName, conditionValue, conditionOption, methodName,
                argTypes, null);
    }

    protected void throwConditionInvokingDateFromToValueInvalidException(String columnFlexibleName, String conditionKeyName,
            Object conditionValue, ConditionOption conditionOption, String methodName, Class<?>[] argTypes) {
        final String notice = "The conditionValue should be List that has 2 elements, fromDate and toDate.";
        doThrowConditionInvokingFailureException(notice, columnFlexibleName, conditionKeyName, conditionValue, conditionOption, methodName,
                argTypes, null);
    }

    protected void throwConditionInvokingSetReflectionFailureException(String columnFlexibleName, String conditionKeyName,
            Object conditionValue, ConditionOption conditionOption, String methodName, Class<?>[] argTypes,
            ReflectionFailureException cause) {
        final String notice = "Failed to invoke the method for setting the condition.";
        doThrowConditionInvokingFailureException(notice, columnFlexibleName, conditionKeyName, conditionValue, conditionOption, methodName,
                argTypes, cause);
    }

    protected void doThrowConditionInvokingFailureException(String notice, String columnFlexibleName, String conditionKeyName,
            Object conditionValue, ConditionOption conditionOption, String methodName, Class<?>[] argTypes, RuntimeException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("Table");
        br.addElement(_rootQuery.asTableDbName());
        br.addItem("columnFlexibleName");
        br.addElement(columnFlexibleName);
        br.addItem("conditionKeyName");
        br.addElement(conditionKeyName);
        br.addItem("conditionValue");
        br.addElement(conditionValue);
        br.addElement(conditionValue != null ? conditionValue.getClass() : null);
        br.addItem("conditionOption");
        br.addElement(conditionOption);
        if (methodName != null) {
            final StringBuilder sb = new StringBuilder();
            if (argTypes != null) {
                int index = 0;
                for (Class<?> argType : argTypes) {
                    if (index > 0) {
                        sb.append(", ");
                    }
                    sb.append(DfTypeUtil.toClassTitle(argType));
                    ++index;
                }
            }
            br.addItem("Method");
            br.addElement(methodName + "(" + sb.toString() + ")");
        }
        final String msg = br.buildExceptionMessage();
        if (cause != null) {
            throw new ConditionInvokingFailureException(msg, cause);
        } else {
            throw new ConditionInvokingFailureException(msg);
        }
    }

    // ===================================================================================
    //                                                                            Order By
    //                                                                            ========
    public void invokeOrderBy(String columnFlexibleName, boolean isAsc) {
        assertStringNotNullAndNotTrimmedEmpty("columnFlexibleName", columnFlexibleName);
        final PropertyNameCQContainer container = xhelpExtractingPropertyNameCQContainer(columnFlexibleName);
        final String flexibleName = container.getFlexibleName();
        final ConditionQuery cq = container.getConditionQuery();
        final String ascDesc = isAsc ? "Asc" : "Desc";
        final DBMeta dbmeta = _dbmetaProvider.provideDBMetaChecked(cq.asTableDbName());
        final String columnCapPropName = initCap(dbmeta.findColumnInfo(flexibleName).getPropertyName());
        final String methodName = "addOrderBy_" + columnCapPropName + "_" + ascDesc;
        final Method method = searchSimpleCQMethod(cq, methodName, (Class<?>[]) null);
        if (method == null) {
            throwConditionInvokingOrderMethodNotFoundException(columnFlexibleName, isAsc, methodName);
        }
        try {
            xhelpInvokingCQMethod(cq, method, (Object[]) null);
        } catch (ReflectionFailureException e) {
            throwConditionInvokingOrderReflectionFailureException(columnFlexibleName, isAsc, methodName, e);
        }
    }

    protected void throwConditionInvokingOrderMethodNotFoundException(String columnFlexibleName, boolean isAsc, String methodName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the method for adding the order-by condition.");
        br.addItem("Table");
        br.addElement(_rootQuery.asTableDbName());
        br.addItem("columnFlexibleName");
        br.addElement(columnFlexibleName);
        br.addItem("isAsc");
        br.addElement(isAsc);
        br.addItem("Method");
        br.addElement(methodName);
        final String msg = br.buildExceptionMessage();
        throw new ConditionInvokingFailureException(msg);
    }

    protected void throwConditionInvokingOrderReflectionFailureException(String columnFlexibleName, boolean isAsc, String methodName,
            ReflectionFailureException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to invoke the method for setting the order-by condition.");
        br.addItem("Table");
        br.addElement(_rootQuery.asTableDbName());
        br.addItem("columnFlexibleName");
        br.addElement(columnFlexibleName);
        br.addItem("isAsc");
        br.addElement(isAsc);
        br.addItem("Method");
        br.addElement(methodName);
        final String msg = br.buildExceptionMessage();
        throw new ConditionInvokingFailureException(msg, cause);
    }

    // ===================================================================================
    //                                                                       Foreign Query
    //                                                                       =============
    // -----------------------------------------------------
    //                                    query().querySea()
    //                                    ------------------
    public ConditionQuery invokeForeignCQ(String foreignPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("foreignPropertyName", foreignPropertyName);
        final List<String> traceList = Srl.splitList(foreignPropertyName, ".");
        ConditionQuery foreignCQ = _rootQuery;
        for (String trace : traceList) {
            foreignCQ = doInvokeForeignCQ(foreignCQ, trace);
        }
        return foreignCQ;
    }

    protected ConditionQuery doInvokeForeignCQ(ConditionQuery foreignCQ, String foreignPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("foreignPropertyName", foreignPropertyName);
        final String methodName = "query" + initCap(foreignPropertyName);
        final Method method = searchSimpleCQMethod(foreignCQ, methodName, (Class<?>[]) null);
        if (method == null) {
            throwConditionInvokingForeignQueryMethodNotFoundException(foreignCQ, foreignPropertyName, methodName);
            return null; // unreachable
        }
        try {
            return (ConditionQuery) xhelpInvokingCQMethod(foreignCQ, method, (Object[]) null);
        } catch (ReflectionFailureException e) {
            throwConditionInvokingForeignQueryReflectionFailureException(foreignCQ, foreignPropertyName, methodName, e);
            return null; // unreachable
        }
    }

    protected void throwConditionInvokingForeignQueryMethodNotFoundException(ConditionQuery foreignCQ, String foreignPropertyName,
            String methodName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the method for getting a foreign condition query.");
        br.addItem("Table");
        br.addElement(_rootQuery.asTableDbName());
        br.addItem("foreignPropertyName");
        br.addElement(foreignPropertyName);
        br.addItem("Method");
        br.addElement(methodName);
        final String msg = br.buildExceptionMessage();
        throw new ConditionInvokingFailureException(msg);
    }

    protected void throwConditionInvokingForeignQueryReflectionFailureException(ConditionQuery foreignCQ, String foreignPropertyName,
            String methodName, ReflectionFailureException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to invoke the method for setting a condition(query).");
        br.addItem("Table");
        br.addElement(_rootQuery.asTableDbName());
        br.addItem("foreignPropertyName");
        br.addElement(foreignPropertyName);
        br.addItem("Method");
        br.addElement(methodName);
        final String msg = br.buildExceptionMessage();
        throw new ConditionInvokingFailureException(msg, cause);
    }

    // -----------------------------------------------------
    //                                 Foreign Determination
    //                                 ---------------------
    public boolean invokeHasForeignCQ(String foreignPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("foreignPropertyName", foreignPropertyName);
        final List<String> traceList = Srl.splitList(foreignPropertyName, ".");
        ConditionQuery foreignCQ = _rootQuery;
        final int splitLength = traceList.size();
        int index = 0;
        for (String traceName : traceList) {
            if (!doInvokeHasForeignCQ(foreignCQ, traceName)) {
                return false;
            }
            if ((index + 1) < splitLength) { // last loop
                foreignCQ = foreignCQ.invokeForeignCQ(traceName);
            }
            ++index;
        }
        return true;
    }

    protected boolean doInvokeHasForeignCQ(ConditionQuery foreignCQ, String foreignPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("foreignPropertyName", foreignPropertyName);
        final String methodName = "hasConditionQuery" + initCap(foreignPropertyName);
        final Method method = searchSimpleCQMethod(foreignCQ, methodName, (Class<?>[]) null);
        if (method == null) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Not found the method for determining a foreign condition query.");
            br.addItem("Table");
            br.addElement(_rootQuery.asTableDbName());
            br.addItem("foreignPropertyName");
            br.addElement(foreignPropertyName);
            br.addItem("methodName");
            br.addElement(methodName);
            br.addItem("ConditionQuery");
            br.addElement(DfTypeUtil.toClassTitle(foreignCQ));
            final String msg = br.buildExceptionMessage();
            throw new ConditionInvokingFailureException(msg);
        }
        try {
            return (Boolean) xhelpInvokingCQMethod(foreignCQ, method, (Object[]) null);
        } catch (ReflectionFailureException e) {
            String msg = "Failed to invoke the method for determining a condition(query):";
            msg = msg + " foreignPropertyName=" + foreignPropertyName;
            msg = msg + " methodName=" + methodName + " table=" + _rootQuery.asTableDbName();
            throw new ConditionInvokingFailureException(msg, e);
        }
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    // -----------------------------------------------------
    //                                         Property Name
    //                                         -------------
    protected PropertyNameCQContainer xhelpExtractingPropertyNameCQContainer(String chainedColumnName) {
        final String[] strings = chainedColumnName.split("\\.");
        final int length = strings.length;
        String propertyName = null;
        ConditionQuery cq = _rootQuery;
        int index = 0;
        for (String element : strings) {
            if (length == (index + 1)) { // at last loop!
                propertyName = element;
                break;
            }
            cq = cq.invokeForeignCQ(element);
            ++index;
        }
        return new PropertyNameCQContainer(propertyName, cq);
    }

    protected static class PropertyNameCQContainer {

        protected String _flexibleName;
        protected ConditionQuery _cq;

        public PropertyNameCQContainer(String flexibleName, ConditionQuery cq) {
            _flexibleName = flexibleName;
            _cq = cq;
        }

        public String getFlexibleName() {
            return _flexibleName;
        }

        public ConditionQuery getConditionQuery() {
            return _cq;
        }
    }

    // -----------------------------------------------------
    //                                         Method Search
    //                                         -------------
    protected Method searchSimpleCQMethod(ConditionQuery declaringCQ, String methodName, Class<?>[] argTypes) {
        Method found = findCQPublicMethod(declaringCQ, methodName, argTypes);
        if (found != null) { // e.g. simple set method (without option), setSea_Equal(), setLand_GreaterThan()
            return found;
        }
        // non public so searching all protected, private methods
        // non-cache #for_now (but cache of Class.class can be available)
        return findCQWholeMethod(declaringCQ, methodName, argTypes);
    }

    protected Method findCQPublicMethod(ConditionQuery declaringCQ, String methodName, Class<?>[] argTypes) {
        final Class<? extends ConditionQuery> cqType = declaringCQ.getClass();
        final DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc(cqType);
        return beanDesc.getMethodNoException(methodName, argTypes); // public only
    }

    protected Method findCQWholeMethod(ConditionQuery declaringCQ, String methodName, Class<?>[] argTypes) {
        return DfReflectionUtil.getWholeMethod(declaringCQ.getClass(), methodName, argTypes);
    }

    protected Object xhelpInvokingCQMethod(ConditionQuery cq, Method method, Object[] args) {
        return DfReflectionUtil.invokeForcedly(method, cq, args);
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected <ELEMENT> ArrayList<ELEMENT> newArrayList() {
        return DfCollectionUtil.newArrayList();
    }

    protected <ELEMENT> ArrayList<ELEMENT> newArrayListSized(int size) {
        return DfCollectionUtil.newArrayListSized(size);
    }

    /**
     * Assert that the object is not null.
     * @param variableName The check name of variable for message. (NotNull)
     * @param value The checked value. (NotNull)
     * @throws IllegalArgumentException When the argument is null.
     */
    protected void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Assert that the string is not null and not trimmed empty.
     * @param variableName The check name of variable for message. (NotNull)
     * @param value The checked value. (NotNull)
     * @throws IllegalArgumentException When the argument is null or empty.
     */
    protected void assertStringNotNullAndNotTrimmedEmpty(String variableName, String value) {
        assertObjectNotNull("variableName", variableName);
        assertObjectNotNull("value", value);
        if (value.trim().length() == 0) {
            String msg = "The value should not be empty: variableName=" + variableName + " value=" + value;
            throw new IllegalArgumentException(msg);
        }
    }
}
