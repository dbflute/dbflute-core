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
package org.seasar.dbflute.bhv.logging.invoke;

import java.util.List;

import org.seasar.dbflute.helper.stacktrace.InvokeNameResult;

/**
 * @author jflute
 * @since 1.0.4D (2013/06/30 Sunday)
 */
public class BehaviorInvokeNameResult {

    protected final String _invocationExp;
    protected final String _invocationExpNoMethodSuffix;
    protected final InvokeNameResult _invokeNameHeadResult;
    protected final List<InvokeNameResult> _invokeNameResultList;

    public BehaviorInvokeNameResult(String invocationExp, String invocationExpNoMethodSuffix,
            InvokeNameResult invokeNameHeadResult, List<InvokeNameResult> invokeNameResultList) {
        _invocationExp = invocationExp;
        _invocationExpNoMethodSuffix = invocationExpNoMethodSuffix;
        _invokeNameHeadResult = invokeNameHeadResult;
        _invokeNameResultList = invokeNameResultList;
    }

    /**
     * @return The string expression of invocation for behavior. (NotNull)
     */
    public String getInvocationExp() {
        return _invocationExp;
    }

    /**
     * @return The string expression of invocation for behavior, no method suffix. (NotNull)
     */
    public String getInvocationExpNoMethodSuffix() {
        return _invocationExpNoMethodSuffix;
    }

    /**
     * @return The head result of invoke name for behavior. (NullAllowed: if null, means not found)
     */
    public InvokeNameResult getInvokeNameHeadResult() {
        return _invokeNameHeadResult;
    }

    /**
     * @return The list of invoke name result for behavior. (NotNull, EmptyAllowed: if empty, means not found)
     */
    public List<InvokeNameResult> getInvokeNameResultList() {
        return _invokeNameResultList;
    }
}
