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
 * The supplier of optional thing.
 * <pre>
 * Member member = entityOpt.<span style="color: #CC4747">orElseGet</span>(() -&gt; {
 *     <span style="color: #3F7E5E">// called if value NOT exists, not called if present</span>
 *     return new Member();
 * });
 * </pre>
 * @param <RESULT> The type of result of mapping.
 * @author jflute
 * @since 1.1.0 (2014/10/28 Tuesday)
 */
@FunctionalInterface
public interface OptionalThingSupplier<RESULT> {

    /**
     * Get the object from the callback process.
     * @return The result of getting. (NullAllowed: if null, returns null)
     */
    RESULT get();
}
