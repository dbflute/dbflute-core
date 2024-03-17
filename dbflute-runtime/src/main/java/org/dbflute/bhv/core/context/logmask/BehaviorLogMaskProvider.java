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
package org.dbflute.bhv.core.context.logmask;

import org.dbflute.bhv.exception.SQLExceptionHandler.SQLExceptionDisplaySqlMaskMan;
import org.dbflute.exception.EntityAlreadyUpdatedException.AlreadyUpdatedBeanMaskMan;

/**
 * @author jflute
 * @since 1.2.7 (2023/07/09 Sunday at roppongi japanese)
 */
public interface BehaviorLogMaskProvider {

    /**
     * @return The callback to mark EntityAlreadyUpdatedException bean. (NullAllowed: if null, no mask)
     */
    default AlreadyUpdatedBeanMaskMan provideAlreadyUpdatedBeanMaskMan() {
        return null; // as default
    }

    /**
     * @return The callback to mark SQLException displaySQl. (NullAllowed: if null, no mask)
     */
    default SQLExceptionDisplaySqlMaskMan provideSQLExceptionDisplaySqlMaskMan() {
        return null; // as default
    }
}
