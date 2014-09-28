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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The DB-way of MySQL.
 * @author jflute
 */
public class WayOfMySQL implements DBWay, Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Serial version UID. (Default) */
    private static final long serialVersionUID = 1L;

    protected static final StringConnector ORIGINAL_STRING_CONNECTOR = new StringConnector() {
        public String connect(Object... elements) {
            final StringBuilder sb = new StringBuilder();
            sb.append("concat(");
            int index = 0;
            for (Object element : elements) {
                if (index > 0) {
                    sb.append(", ");
                }
                sb.append(element);
                ++index;
            }
            sb.append(")");
            return sb.toString();
        }
    };

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
        return "SELECT LAST_INSERT_ID()";
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
    public StringConnector getStringConnector() {
        return ORIGINAL_STRING_CONNECTOR;
    }

    // ===================================================================================
    //                                                                   SQLException Info
    //                                                                   =================
    public boolean isUniqueConstraintException(String sqlState, Integer errorCode) {
        return errorCode != null && errorCode == 1062;
    }

    // ===================================================================================
    //                                                                     ENUM Definition
    //                                                                     ===============
    public enum FullTextSearchModifier {
        InBooleanMode("in boolean mode") //
        , InNaturalLanguageMode("in natural language mode") //
        , InNaturalLanguageModeWithQueryExpansion("in natural language mode with query expansion") //
        , WithQueryExpansion("with query expansion");
        private static final Map<String, FullTextSearchModifier> _codeValueMap = new HashMap<String, FullTextSearchModifier>();
        static {
            for (FullTextSearchModifier value : values()) {
                _codeValueMap.put(value.code().toLowerCase(), value);
            }
        }
        private String _code;

        private FullTextSearchModifier(String code) {
            _code = code;
        }

        public String code() {
            return _code;
        }

        public static FullTextSearchModifier codeOf(Object code) {
            if (code == null) {
                return null;
            }
            return _codeValueMap.get(code.toString().toLowerCase());
        }
    }
}
