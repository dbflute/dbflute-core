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
package org.seasar.dbflute.optional;

/**
 * The consumer of optional value.
 * <pre>
 * opt.<span style="color: #DD4747">ifPresent</span>(member -&gt; {
 *     <span style="color: #3F7E5E">// called if value exists, not called if not present</span>
 *     ... = member.getMemberName();
 * });
 * opt.<span style="color: #DD4747">required</span>(member -&gt; {
 *     <span style="color: #3F7E5E">// called if value exists, or exception if not present</span>
 *     ... = member.getMemberName();
 * });
 * </pre>
 * @param <OBJ> The type of wrapped object in optional object.
 * @author jflute
 * @since 1.0.5F (2014/05/10 Saturday)
 */
public interface OptionalObjectConsumer<OBJ> {

    /**
     * Accept the value in the optional object.
     * <pre>
     * when caller is <span style="color: #DD4747">ifPresent()</span>:
     *   called if value exists, not called if not present
     *
     * when caller is <span style="color: #DD4747">required()</span>:
     *   called if value exists, or exception if not present
     * </pre>
     * @param obj The wrapped object in the optional object. (NotNull)
     */
    void accept(OBJ obj);
}
