/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.bhv.core;

/**
 * The interface of SQL execution. <br>
 * This is basically reused on executing so it's thread safe.
 * @author jflute
 */
public interface SqlExecution {

    /**
     * Execute SQL.
     * @param args The arguments for SQL.
     * @return The execution result. (NullAllowed: depends on an execution)
     */
    Object execute(Object[] args);
}
