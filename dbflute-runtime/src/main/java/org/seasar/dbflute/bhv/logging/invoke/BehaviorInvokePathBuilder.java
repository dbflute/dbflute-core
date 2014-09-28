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

import java.util.Arrays;
import java.util.List;

import org.seasar.dbflute.helper.stacktrace.InvokeNameResult;

/**
 * @author jflute
 * @since 1.0.4D (2013/06/30 Sunday)
 */
public class BehaviorInvokePathBuilder {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String OMIT_MARK = "...";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String[] _clientNames;
    protected final String[] _byPassNames;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public BehaviorInvokePathBuilder(String[] clientNames, String[] byPassNames) {
        _clientNames = clientNames;
        _byPassNames = byPassNames;
    }

    // ===================================================================================
    //                                                                    Build InvokePath
    //                                                                    ================
    public BehaviorInvokePathResult buildInvokePath(StackTraceElement[] stackTrace,
            BehaviorInvokeNameResult behaviorInvokeNameResult) {
        final InvokeNameResult behaviorHeadResult = behaviorInvokeNameResult.getInvokeNameHeadResult();
        final int bhvNextIndex = behaviorHeadResult != null ? behaviorHeadResult.getNextStartIndex() : -1;

        // extract client result
        final ClientInvokeNameResult clinentInvokeNameResult = extractClientInvoke(stackTrace, bhvNextIndex);
        final List<InvokeNameResult> clientInvokeNameResultList = clinentInvokeNameResult.getInvokeNameResultList();
        final InvokeNameResult headClientResult = findHeadInvokeResult(clientInvokeNameResultList);

        // extract by-pass result
        final int clientFirstIndex = headClientResult != null ? headClientResult.getFoundFirstIndex() : -1;
        final int byPassLoopSize = clientFirstIndex - bhvNextIndex;
        final ByPassInvokeNameResult byPassInvokeNameResult = extractByPassInvoke(stackTrace, bhvNextIndex,
                byPassLoopSize);
        final List<InvokeNameResult> byPassResultList = byPassInvokeNameResult.getInvokeNameResultList();
        final InvokeNameResult headByPassResult = findHeadInvokeResult(byPassResultList);

        if (headClientResult == null && headByPassResult == null) { // when both are not found
            return null;
        }
        final boolean useTestShortName;
        if (isClientResultMainExists(clientInvokeNameResultList)) {
            useTestShortName = true;
        } else {
            useTestShortName = headClientResult != null && headByPassResult != null;
        }

        final String clientInvokeName = buildInvokeName(headClientResult, useTestShortName);
        final String byPassInvokeName = buildInvokeName(headByPassResult, useTestShortName);

        final StringBuilder sb = new StringBuilder();
        sb.append(clientInvokeName);
        sb.append(findTailInvokeName(clientInvokeNameResultList, useTestShortName));
        sb.append(byPassInvokeName);
        sb.append(findTailInvokeName(byPassResultList, useTestShortName));
        sb.append(OMIT_MARK); // fixed specification (used as replace-key later e.g. in error message)
        final String invokePath = sb.toString();

        return new BehaviorInvokePathResult(invokePath, behaviorInvokeNameResult, clientInvokeName, byPassInvokeName);
    }

    // ===================================================================================
    //                                                                       Client Invoke
    //                                                                       =============
    protected ClientInvokeNameResult extractClientInvoke(StackTraceElement[] stackTrace, int startIndex) {
        final List<String> suffixList = Arrays.asList(_clientNames);
        final ClientInvokeNameExtractor extractor = createClientInvokeNameExtractor(suffixList, startIndex);
        return extractor.extractClientInvoke(stackTrace);
    }

    protected ClientInvokeNameExtractor createClientInvokeNameExtractor(List<String> suffixList, int startIndex) {
        return new ClientInvokeNameExtractor(suffixList, startIndex);
    }

    // ===================================================================================
    //                                                                       ByPass Invoke
    //                                                                       =============
    protected ByPassInvokeNameResult extractByPassInvoke(StackTraceElement[] stackTrace, int startIndex, int loopSize) {
        final List<String> suffixList = Arrays.asList(_byPassNames);
        final ByPassInvokeNameExtractor extractor = createByPassInvokeNameExtractor(suffixList, startIndex, loopSize);
        return extractor.extractByPassInvoke(stackTrace);
    }

    protected ByPassInvokeNameExtractor createByPassInvokeNameExtractor(List<String> suffixList, int startIndex,
            int loopSize) {
        return new ByPassInvokeNameExtractor(suffixList, startIndex, loopSize);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected boolean isClientResultMainExists(List<InvokeNameResult> clientResultList) {
        boolean mainExists = false;
        for (InvokeNameResult invokeNameResult : clientResultList) {
            if (!invokeNameResult.hasTestSuffix()) {
                mainExists = true;
                break;
            }
        }
        return mainExists;
    }

    protected InvokeNameResult findHeadInvokeResult(List<InvokeNameResult> resultList) {
        if (!resultList.isEmpty()) {
            // The latest element is the very head invoking.
            return resultList.get(resultList.size() - 1);
        }
        return null;
    }

    protected String buildInvokeName(InvokeNameResult invokeNameResult, boolean useTestShortName) {
        return invokeNameResult != null ? invokeNameResult.buildInvokeName(useTestShortName) : "";
    }

    protected String findTailInvokeName(List<InvokeNameResult> resultList, boolean hasBoth) {
        if (resultList.size() > 1) {
            return resultList.get(0).buildInvokeName(hasBoth);
        }
        return "";
    }
}
