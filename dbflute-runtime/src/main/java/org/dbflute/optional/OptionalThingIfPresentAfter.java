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
package org.dbflute.optional;

/**
 * The consumer of optional value.
 * <pre>
 * opt.<span style="color: #CC4747">ifPresent</span>(member -&gt; {
 *     <span style="color: #3F7E5E">// called if value exists, or not called</span>
 *     ... = member.getMemberName();
 * }).<span style="color: #CC4747">orElse</span>(() -&gt; {
 *     <span style="color: #3F7E5E">// *here: called if no value</span>
 * });
 * </pre>
 * @author jflute
 * @since 1.1.0 (2014/10/05 Monday)
 */
@FunctionalInterface
public interface OptionalThingIfPresentAfter {

    /**
     * Execute the process when the thing is none.
     * <pre>
     * opt.<span style="color: #994747">ifPresent</span>(member -&gt; {
     *     <span style="color: #3F7E5E">// called if value exists, or not called</span>
     *     ... = member.getMemberName();
     * }).<span style="color: #CC4747">orElse</span>(() -&gt; {
     *     <span style="color: #3F7E5E">// *here: called if no value</span>
     * });
     * </pre>
     * @param noArgLambda The callback to run the process when the thing is none. (NotNull)
     */
    void orElse(OptionalThingNoneProcessor noArgLambda);
}
