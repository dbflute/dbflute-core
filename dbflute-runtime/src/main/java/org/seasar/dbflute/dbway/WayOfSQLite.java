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
package org.seasar.dbflute.dbway;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * The DB-way of SQLite.
 * @author jflute
 */
public class WayOfSQLite implements DBWay, Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Serial version UID. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                        Sequence Way
    //                                                                        ============
    public String buildSequenceNextValSql(String sequenceName) {
        return null;
    }

    // ===================================================================================
    //                                                                       Identity Info
    //                                                                       =============
    public String getIdentitySelectSql() {
        return "select last_insert_rowid()";
    }

    // ===================================================================================
    //                                                                         SQL Support
    //                                                                         ===========
    public boolean isBlockCommentSupported() {
        return true;
    }

    public boolean isLineCommentSupported() {
        return true;
    }

    // ===================================================================================
    //                                                                        JDBC Support
    //                                                                        ============
    public boolean isScrollableCursorSupported() {
        return false;
    }

    // ===================================================================================
    //                                                                 LikeSearch WildCard
    //                                                                 ===================
    @SuppressWarnings("unchecked")
    public List<String> getOriginalWildCardList() {
        return Collections.EMPTY_LIST;
    }

    // ===================================================================================
    //                                                                    String Connector
    //                                                                    ================
    public StringConnector getStringConnector() {
        return STANDARD_STRING_CONNECTOR;
    }

    // ===================================================================================
    //                                                                   SQLException Info
    //                                                                   =================
    public boolean isUniqueConstraintException(String sqlState, Integer errorCode) {
        return false; // sqlState is null and errorCode is 0 always...
    }
}
