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
package org.dbflute.cbean.coption;

import org.dbflute.jdbc.StatementConfig;

/**
 * The callback interface of statement configuration.
 * @param <CONF> The type of statement configuration to be set up.
 * @author jflute
 * @since 1.1.0 (2014/11/05 Wednesday)
 */
@FunctionalInterface
public interface StatementConfigCall<CONF extends StatementConfig> {

    /**
     * @param conf The configuration of JDBC statement to be set up. (NotNull)
     */
    void callback(CONF conf);
}
