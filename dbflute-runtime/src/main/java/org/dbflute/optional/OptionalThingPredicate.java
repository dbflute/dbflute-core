/*
 * Copyright 2014-2019 the original author or authors.
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
 * The predicate of optional value.
 * <pre>
 * entityOpt.<span style="color: #CC4747">filter</span>(member -&gt; {
 *     <span style="color: #3F7E5E">// called if value exists, not called if not present</span>
 *     return member.getMemberId() % 2 == 0;
 * }).<span style="color: #994747">ifPresent</span>(...);
 * </pre>
 * @param <OBJ> The type of wrapped object in optional object.
 * @author jflute
 * @since 1.0.5G (2014/05/26 Monday)
 */
@FunctionalInterface
public interface OptionalThingPredicate<OBJ> {

    /**
     * Test the object in the optional object.
     * @param obj The wrapped object in the optional object. (NotNull)
     * @return The determination, true or false.
     */
    boolean test(OBJ obj);
}
