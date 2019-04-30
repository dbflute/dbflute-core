/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.dbway;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * The DB-way of Unknown.
 * @author jflute
 */
public class WayOfUnknown implements DBWay, Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
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
        return null;
    }

    // ===================================================================================
    //                                                                         SQL Support
    //                                                                         ===========
    public boolean isBlockCommentSupported() {
        return true; // as default for unknown
    }

    public boolean isLineCommentSupported() {
        return true; // as default for unknown
    }

    // ===================================================================================
    //                                                                        JDBC Support
    //                                                                        ============
    public boolean isScrollableCursorSupported() {
        return true; // as default for unknown
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
    public OnQueryStringConnector getStringConnector() {
        return STANDARD_STRING_CONNECTOR;
    }

    // ===================================================================================
    //                                                                   SQLException Info
    //                                                                   =================
    public boolean isUniqueConstraintException(String sqlState, Integer errorCode) {
        return false;
    }
}
