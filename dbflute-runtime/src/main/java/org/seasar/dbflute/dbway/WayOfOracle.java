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
import java.util.Arrays;
import java.util.List;

/**
 * The DB-way of Oracle.
 * @author jflute
 */
public class WayOfOracle implements DBWay, Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Serial version UID. (Default) */
    private static final long serialVersionUID = 1L;

    protected static final List<String> ORIGNAL_WILD_CARD_LIST = Arrays.asList("\uff05", "\uff3f");

    // ===================================================================================
    //                                                                        Sequence Way
    //                                                                        ============
    public String buildSequenceNextValSql(String sequenceName) {
        return "select " + sequenceName + ".nextval from dual";
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
        return true;
    }

    public boolean isLineCommentSupported() {
        return true;
    }

    // ===================================================================================
    //                                                                        JDBC Support
    //                                                                        ============
    public boolean isScrollableCursorSupported() {
        return true;
    }

    // ===================================================================================
    //                                                                 LikeSearch WildCard
    //                                                                 ===================
    public List<String> getOriginalWildCardList() {
        return ORIGNAL_WILD_CARD_LIST;
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
        return errorCode != null && errorCode == 1;
    }
}
