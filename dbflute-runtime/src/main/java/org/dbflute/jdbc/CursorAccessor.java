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
package org.dbflute.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The accessor of cursor. <br>
 * Basically implemented by generated Cursor classes for OutsideSql.
 * @author jflute
 * @since 1.2.6 (2022/04/17 Sunday)
 */
public interface CursorAccessor {

    /**
     * Accept the result set.
     * @param rs The cursor (result set) for the query, which has first pointer. (NotNull)
     */
    void accept(ResultSet rs);

    /**
     * Get the wrapped cursor (result set).
     * @return The instance of result set. (NotNull)
     */
    ResultSet cursor();

    /**
     * Move to next result.
     * @return Does the next result exist?
     * @throws SQLException When it fails to move the cursor to next point.
     */
    boolean next() throws SQLException;
}
