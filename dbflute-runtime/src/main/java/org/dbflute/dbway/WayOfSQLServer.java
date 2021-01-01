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
package org.dbflute.dbway;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * The DB-way of SQLServer.
 * @author jflute
 */
public class WayOfSQLServer implements DBWay, Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    protected static final List<String> _originalWildCardList = Arrays.asList("[", "]");

    // ===================================================================================
    //                                                                        Sequence Way
    //                                                                        ============
    public String buildSequenceNextValSql(String sequenceName) {
        return "select next value for " + sequenceName; // supported since SQLServer2012
    }

    // ===================================================================================
    //                                                                       Identity Info
    //                                                                       =============
    public String getIdentitySelectSql() {
        return "select @@identity";
    }

    public String buildIdentityDisableSql(String tableSqlName) {
        return buildIdentityOnOffSql(tableSqlName, true);
    }

    public String buildIdentityEnableSql(String tableSqlName) {
        return buildIdentityOnOffSql(tableSqlName, false);
    }

    protected String buildIdentityOnOffSql(String tableSqlName, boolean insertOn) {
        return "set identity_insert " + tableSqlName + " " + (insertOn ? "on" : "off");
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
        return _originalWildCardList;
    }

    // ===================================================================================
    //                                                                    String Connector
    //                                                                    ================
    public OnQueryStringConnector getStringConnector() {
        return PLUS_STRING_CONNECTOR;
    }

    // ===================================================================================
    //                                                                   SQLException Info
    //                                                                   =================
    public boolean isUniqueConstraintException(String sqlState, Integer errorCode) {
        return errorCode != null && errorCode == 2627;
    }
}
