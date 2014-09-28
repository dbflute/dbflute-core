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

/**
 * @author jflute
 * @since 1.0.4D (2013/06/30 Sunday)
 */
public class BehaviorInvokePathResult {

    protected final String _invokePath;
    protected final BehaviorInvokeNameResult _behaviorInvokeNameResult;
    protected final String _clientInvokeName;
    protected final String _byPassInvokeName;

    public BehaviorInvokePathResult(String invokePath, BehaviorInvokeNameResult behaviorInvokeNameResult,
            String clientInvokeName, String byPassInvokeName) {
        _invokePath = invokePath;
        _behaviorInvokeNameResult = behaviorInvokeNameResult;
        _clientInvokeName = clientInvokeName;
        _byPassInvokeName = byPassInvokeName;
    }

    /**
     * @return The invoke path for behavior, omitting behavior name. (NotNull)
     */
    public String getInvokePath() {
        return _invokePath;
    }

    /**
     * @return The result of invoke name for behavior. (NotNull: if not found, head result is null)
     */
    public BehaviorInvokeNameResult getBehaviorInvokeNameResult() {
        return _behaviorInvokeNameResult;
    }

    /**
     * @return The invoke name for client. (NotNull: if not found, return empty string)
     */
    public String getClientInvokeName() {
        return _clientInvokeName;
    }

    /**
     * @return The invoke name for by-pass. (NotNull: if not found, return empty string)
     */
    public String getByPassInvokeName() {
        return _byPassInvokeName;
    }
}
