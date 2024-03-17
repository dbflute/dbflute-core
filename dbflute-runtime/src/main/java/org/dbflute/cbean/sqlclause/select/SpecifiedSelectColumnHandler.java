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
package org.dbflute.cbean.sqlclause.select;

import org.dbflute.cbean.dream.SpecifiedColumn;

/**
 * @author jflute
 * @since 1.0.4K (2013/09/07 Saturday)
 */
public interface SpecifiedSelectColumnHandler {

    /**
     * Handle the specified column.
     * @param tableAliasName The alias name of table. (NotNull)
     * @param specifiedColumn The info about column specification. (NotNull)
     */
    void handle(String tableAliasName, SpecifiedColumn specifiedColumn);
}
