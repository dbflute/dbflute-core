/*
 * Copyright 2014-2014 the original author or authors.
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
 * The processor of optional thing when none.
 * <pre>
 * opt.ifPresent(member -&gt; {
 *     <span style="color: #3F7E5E">// called if value exists, not called if not present</span>
 *     ... = member.getMemberName();
 * }).<span style="color: #DD4747">orElse</span>(() -&gt; {
 *     <span style="color: #3F7E5E">// *here: called if value does not exists</span>
 * });
 * </pre>
 * @author jflute
 * @since 1.1.0 (2014/10/05 Sunday)
 */
public interface OptionalThingNoneProcessor {

    /**
     * Fire the process when the value is not present.
     */
    void process();
}
