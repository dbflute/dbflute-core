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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The DB-way of PostgreSQL.
 * @author jflute
 */
public class WayOfPostgreSQL implements DBWay, Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                        Sequence Way
    //                                                                        ============
    public String buildSequenceNextValSql(String sequenceName) {
        return "select nextval ('" + sequenceName + "')";
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
        return "23505".equals(sqlState);
    }

    // ===================================================================================
    //                                                                Extension Definition
    //                                                                ====================
    public enum OperandOfLikeSearch implements ExtensionOperand {
        BASIC("like"), CASE_INSENSITIVE("ilike"), FULL_TEXT_SEARCH("%%"), OLD_FULL_TEXT_SEARCH("@@");
        private static final Map<String, OperandOfLikeSearch> _codeValueMap = new HashMap<String, OperandOfLikeSearch>();
        static {
            for (OperandOfLikeSearch value : values()) {
                _codeValueMap.put(value.code().toLowerCase(), value);
            }
        }
        private String _code;

        private OperandOfLikeSearch(String code) {
            _code = code;
        }

        public String code() {
            return _code;
        }

        public static OperandOfLikeSearch codeOf(Object code) {
            if (code == null) {
                return null;
            }
            return _codeValueMap.get(code.toString().toLowerCase());
        }

        public String operand() {
            return _code;
        }
    }
}
