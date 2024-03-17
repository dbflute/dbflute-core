/*
 * Copyright 2014-2024 the original author or authors.
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

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.dream.SpecifiedColumn;
import org.dbflute.exception.ConditionInvokingFailureException;
import org.dbflute.helper.beans.DfBeanDesc;
import org.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.dbflute.util.DfReflectionUtil;
import org.dbflute.util.DfReflectionUtil.ReflectionFailureException;

/**
 * @author jflute
 * @since 1.2.7 split from ConditionBean (2023/07/21 Friday at ichihara)
 */
public class InvokingCBeanAgent {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ConditionBean _rootBean; // calls invoking, not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public InvokingCBeanAgent(ConditionBean rootBean) {
        _rootBean = rootBean;
    }

    // ===================================================================================
    //                                                                     Condition Value
    //                                                                     ===============
    public void invokeSetupSelect(String foreignPropertyNamePath) {
        assertStringNotNullAndNotTrimmedEmpty("foreignPropertyNamePath", foreignPropertyNamePath);
        final String delimiter = ".";
        Object currentObj = _rootBean;
        String remainder = foreignPropertyNamePath;
        int count = 0;
        boolean last = false;
        while (true) {
            final int deimiterIndex = remainder.indexOf(delimiter);
            final String propertyName;
            if (deimiterIndex < 0) {
                propertyName = remainder;
                last = true;
            } else {
                propertyName = remainder.substring(0, deimiterIndex);
                remainder = remainder.substring(deimiterIndex + delimiter.length(), remainder.length());
            }
            final Class<?> targetType = currentObj.getClass();
            final String methodName = (count == 0 ? "setupSelect_" : "with") + initCap(propertyName);
            final Method method = xhelpGettingCBChainMethod(targetType, methodName, (Class<?>[]) null);
            if (method == null) {
                String msg = "Not found the method for setupSelect:";
                msg = msg + " table=" + _rootBean.asTableDbName();
                msg = msg + " foreignPropertyNamePath=" + foreignPropertyNamePath;
                msg = msg + " targetType=" + targetType + " methodName=" + methodName;
                throw new ConditionInvokingFailureException(msg);
            }
            try {
                currentObj = DfReflectionUtil.invoke(method, currentObj, (Object[]) null);
            } catch (ReflectionFailureException e) {
                String msg = "Failed to invoke the method:";
                msg = msg + " table=" + _rootBean.asTableDbName();
                msg = msg + " foreignPropertyNamePath=" + foreignPropertyNamePath;
                msg = msg + " targetType=" + targetType + " methodName=" + methodName;
                throw new ConditionInvokingFailureException(msg, e);
            }
            ++count;
            if (last) {
                break;
            }
        }
    }

    // ===================================================================================
    //                                                                             Specify
    //                                                                             =======
    public SpecifiedColumn invokeSpecifyColumn(Object localSp, String columnPropertyPath) {
        assertObjectNotNull("localSp", localSp);
        assertStringNotNullAndNotTrimmedEmpty("columnPropertyPath", columnPropertyPath);
        final String delimiter = ".";
        Object currentObj = localSp;
        String remainder = columnPropertyPath;
        boolean last = false;
        while (true) {
            final int deimiterIndex = remainder.indexOf(delimiter);
            final String propertyName;
            if (deimiterIndex < 0) {
                propertyName = remainder; // hard to get relation DB meta so plain name
                last = true;
            } else {
                propertyName = remainder.substring(0, deimiterIndex);
                remainder = remainder.substring(deimiterIndex + delimiter.length(), remainder.length());
            }
            final Class<?> targetType = currentObj.getClass();
            final String methodName = (last ? "column" : "specify") + initCap(propertyName);
            final Method method = xhelpGettingCBChainMethod(targetType, methodName, (Class<?>[]) null);
            if (method == null) {
                String msg = "Not found the method for SpecifyColumn:";
                msg = msg + " table=" + _rootBean.asTableDbName();
                msg = msg + " columnPropertyPath=" + columnPropertyPath + " targetType=" + targetType + " methodName=" + methodName;
                throw new ConditionInvokingFailureException(msg);
            }
            try {
                currentObj = DfReflectionUtil.invoke(method, currentObj, (Object[]) null);
            } catch (ReflectionFailureException e) {
                String msg = "Failed to invoke the method:";
                msg = msg + " table=" + _rootBean.asTableDbName();
                msg = msg + " columnPropertyPath=" + columnPropertyPath + " targetType=" + targetType + " methodName=" + methodName;
                throw new ConditionInvokingFailureException(msg, e);
            }
            if (last) {
                break;
            }
        }
        return (SpecifiedColumn) currentObj;
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected Method xhelpGettingCBChainMethod(Class<?> type, String methodName, Class<?>[] argTypes) {
        final DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc(type);
        return beanDesc.getMethodNoException(methodName, argTypes);
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
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
