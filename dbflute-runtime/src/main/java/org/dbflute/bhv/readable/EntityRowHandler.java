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
package org.dbflute.bhv.readable;

/**
 * The handler of entity row.
 * @param <ENTITY> The type of entity.
 * @author jflute
 */
@FunctionalInterface
public interface EntityRowHandler<ENTITY> {

    /**
     * Handle entity as row.
     * @param entity The entity as row. (NotNull)
     */
    void handle(ENTITY entity);

    /**
     * Does it break the cursor? (skip next rows?)<br>
     * You can skip next records by your overriding. <br>
     * (cannot skip the first record, this determination is after one row handling)
     * <pre>
     * <span style="color: #0000C0">memberBhv</span>.<span style="color: #CC4747">selectCursor</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.query().set...
     * }, new EntityRowHandler&lt;Member&gt;() { <span style="color: #3F7E5E">// not lambda for override</span>
     *     private boolean <span style="color: #0000C0">breakCursor</span>;
     *     public void handle(Member <span style="color: #553000">member</span>) {
     *         ...
     *         if (...) { <span style="color: #3F7E5E">// means the final row</span>
     *             <span style="color: #0000C0">breakCursor</span> = true; <span style="color: #3F7E5E">// skip the next records</span>
     *         }
     *     }
     *     public boolean <span style="color: #CC4747">isBreakCursor</span>() {
     *         return <span style="color: #0000C0">breakCursor<span>;
     *     }
     * });
     * </pre>
     * @return The determination, true or false.
     */
    default boolean isBreakCursor() {
        return false;
    }
}
