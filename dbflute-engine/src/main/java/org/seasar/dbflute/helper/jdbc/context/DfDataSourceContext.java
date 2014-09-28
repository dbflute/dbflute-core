/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.helper.jdbc.context;

import javax.sql.DataSource;

/**
 * @author jflute
 */
public class DfDataSourceContext {

    /** The thread-local for this. */
    private static final ThreadLocal<DataSource> _threadLocal = new ThreadLocal<DataSource>();

    /**
     * Get DataSource on thread.
     * @return DataSource. (NullAllowed)
     */
    public static DataSource getDataSource() {
        return (DataSource) _threadLocal.get();
    }

    /**
     * Set DataSource on thread.
     * @param dataSource DataSource. (NotNull)
     */
    public static void setDataSource(DataSource dataSource) {
        if (dataSource == null) {
            String msg = "The argument[dataSource] must not be null.";
            throw new IllegalArgumentException(msg);
        }
        _threadLocal.set(dataSource);
    }

    /**
     * Is existing DataSource on thread?
     * @return The determination, true or false.
     */
    public static boolean isExistDataSource() {
        return (_threadLocal.get() != null);
    }

    /**
     * Clear DataSource on thread.
     */
    public static void clearDataSource() {
        _threadLocal.set(null);
    }
}
