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

import org.seasar.dbflute.helper.stacktrace.InvokeNameExtractingResource;
import org.seasar.dbflute.helper.stacktrace.InvokeNameResult;

/**
 * @author jflute
 * @since 1.0.4D (2013/06/30 Sunday)
 */
public class ClientInvokeNameExtractor {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<String> _suffixList;
    protected final int _startIndex;
    protected final InvokeNameExtractingCoinLogic _coinLogic = createInvokeNameExtractingCoinLogic();

    protected InvokeNameExtractingCoinLogic createInvokeNameExtractingCoinLogic() {
        return new InvokeNameExtractingCoinLogic();
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ClientInvokeNameExtractor(List<String> suffixList, int startIndex) {
        _suffixList = suffixList;
        _startIndex = startIndex;
    }

    // ===================================================================================
    //                                                                      Extract Client
    //                                                                      ==============
    public ClientInvokeNameResult extractClientInvoke(StackTraceElement[] stackTrace) {
        final InvokeNameExtractingResource resource = new InvokeNameExtractingResource() {
            public boolean isTargetElement(String className, String methodName) {
                return isClassNameEndsWith(className, _suffixList);
            }

            public String filterSimpleClassName(String simpleClassName) {
                return simpleClassName;
            }

            public boolean isUseAdditionalInfo() {
                return true;
            }

            public int getStartIndex() {
                return _startIndex;
            }

            public int getLoopSize() {
                return getInvocationExtractingMaxLoopSize();
            }
        };
        final List<InvokeNameResult> invokeNameResultList = extractInvokeName(resource, stackTrace);
        return new ClientInvokeNameResult(invokeNameResultList);
    }

    protected int getInvocationExtractingMaxLoopSize() {
        return 20;
    }

    // ===================================================================================
    //                                                                 Extract Invoke Name
    //                                                                 ===================
    /**
     * @param resource the call-back resource for invoke-name-extracting. (NotNull)
     * @param stackTrace Stack log. (NotNull)
     * @return The list of result of invoke name. (NotNull: If not found, returns empty string.)
     */
    protected List<InvokeNameResult> extractInvokeName(InvokeNameExtractingResource resource,
            StackTraceElement[] stackTrace) {
        return _coinLogic.extractInvokeName(resource, stackTrace);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected boolean isClassNameEndsWith(String className, List<String> suffixList) {
        return _coinLogic.isClassNameEndsWith(className, suffixList);
    }

    protected boolean isClassNameContains(String className, List<String> keywordList) {
        return _coinLogic.isClassNameContains(className, keywordList);
    }
}
