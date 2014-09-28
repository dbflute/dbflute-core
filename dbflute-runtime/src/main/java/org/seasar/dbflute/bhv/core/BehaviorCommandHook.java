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
package org.seasar.dbflute.bhv.core;

/**
 * The hook interface of behavior commands for call-back. <br />
 * You can hook before-and-finally of all behavior commands. <br />
 * The hook methods may be called by nested process
 * so you should pay attention to it when you implements this.
 * @author jflute
 */
public interface BehaviorCommandHook {

    /**
     * Hook it before the command execution.
     * @param meta The meta information of the behavior command. (NotNull)
     */
    void hookBefore(BehaviorCommandMeta meta);

    /**
     * Hook it in finally clause after the command execution.
     * @param meta The meta information of the behavior command. (NotNull)
     * @param cause The exception if the behavior command was failed. (NullAllowed)
     */
    void hookFinally(BehaviorCommandMeta meta, RuntimeException cause);
}
