/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute.twowaysql.node;

import org.dbflute.dbway.OnQueryStringConnector;

/**
 * @author jflute
 * @since 1.1.0 (2014/09/29 Monday)
 */
public interface FilteringBindOption {

    /**
     * @param value The filtered value. (NullAllowed: if null, returns null)
     * @return The generated real value on SQL expression. (NullAllowed: when the value is null)
     */
    String generateRealValue(String value);

    /**
     * @return The part of SQL expression as rear. (NotNull, EmptyAllowed)
     */
    String getRearOption();

    /**
     * @param stringConnector The connector of string on query. (NotNull)
     * @return this. (NotNull)
     */
    FilteringBindOption acceptStringConnector(OnQueryStringConnector stringConnector);
}