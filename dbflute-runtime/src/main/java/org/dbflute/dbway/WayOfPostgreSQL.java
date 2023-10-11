/*
 * Copyright 2014-2023 the original author or authors.
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

import org.dbflute.dbway.topic.ExtensionOperand;
import org.dbflute.dbway.topic.OnQueryStringConnector;
import org.dbflute.optional.OptionalThing;

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
        /** normal */
        BASIC("like")

        /** like-search with ignoring case (like is case-sensitive on PostgreSQL) */
        , CASE_INSENSITIVE("ilike")

        /** MeCab+textsearch–ja */
        , MECAB_TEXTSEARCH_JA_FULL_TEXT_SEARCH("@@") // since 1.2.7

        // #for_now jflute other operands "&~", "&@~" are unsupported, consider when feedback (2023/07/20)
        // because maybe I needs to study PGroonga more to adjust framework design for it
        /** PGroonga as basic operand. */
        , PGROONGA_BASIC_FULL_TEXT_SEARCH("&@") // since 1.2.7

        // #hope jflute rename to concrete name or remove (2023/07/21)
        /** new Ludia+Senna */
        , FULL_TEXT_SEARCH("%%") // traditional but default to keep compatible

        /** old Ludia+Senna */
        ,OLD_FULL_TEXT_SEARCH("@@") // traditional and may be unused
        ;

        private static final Map<String, OperandOfLikeSearch> _codeValueMap = new HashMap<String, OperandOfLikeSearch>();
        static {
            for (OperandOfLikeSearch value : values()) {
                _codeValueMap.put(value.code().toLowerCase(), value);
            }
        }
        private final String _code;

        private OperandOfLikeSearch(String code) {
            _code = code;
        }

        public String code() {
            return _code;
        }

        public static OptionalThing<OperandOfLikeSearch> of(Object code) {
            if (code == null) {
                return OptionalThing.ofNullable(null, () -> {
                    throw new IllegalArgumentException("The argument 'code' should not be null.");
                });
            }
            if (code instanceof OperandOfLikeSearch) {
                return OptionalThing.of((OperandOfLikeSearch) code);
            }
            if (code instanceof OptionalThing<?>) {
                return of(((OptionalThing<?>) code).orElse(null));
            }
            final OperandOfLikeSearch operand = _codeValueMap.get(code.toString().toLowerCase());
            return OptionalThing.ofNullable(operand, () -> {
                throw new IllegalStateException("Not found the operand by the code: " + code);
            });
        }

        @Deprecated
        public static OperandOfLikeSearch codeOf(Object code) {
            return of(code).orElse(null);
        }

        public String operand() {
            return _code;
        }
    }
}
