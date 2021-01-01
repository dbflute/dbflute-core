/*
 * Copyright 2014-2021 the original author or authors.
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
 * The function of optional thing.
 * <pre>
 * entityOpt.<span style="color: #CC4747">map</span>(member -&gt; {
 *     <span style="color: #3F7E5E">// called if value exists, not called if not present</span>
 *     return new MemberWebBean(member);
 * }).<span style="color: #994747">ifPresent</span>(...);
 * </pre>
 * @param <OBJ> The type of wrapped object in optional thing.
 * @param <RESULT> The type of result of mapping.
 * @author jflute
 * @since 1.0.5F (2014/05/10 Saturday)
 */
@FunctionalInterface
public interface OptionalThingFunction<OBJ, RESULT> {

    /**
     * Apply the object in the optional thing.
     * @param obj The wrapped object in the optional thing. (NotNull)
     * @return The result of mapping. (NullAllowed: if null, map() returns empty optional thing)
     */
    RESULT apply(OBJ obj);
}
