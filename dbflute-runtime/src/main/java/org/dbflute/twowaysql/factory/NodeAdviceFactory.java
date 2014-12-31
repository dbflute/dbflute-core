/*
 * Copyright 2014-2015 the original author or authors.
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
package org.dbflute.twowaysql.factory;

import java.util.List;

import org.dbflute.twowaysql.node.BoundValueTracer;
import org.dbflute.twowaysql.node.FilteringBindOption;
import org.dbflute.twowaysql.node.ParameterCommentType;

/**
 * @author jflute
 * @since 1.1.0 (2014/09/29 Monday)
 */
public interface NodeAdviceFactory {

    String INLOOP_LIKE_PREFIX = "likePrefix";
    String INLOOP_LIKE_SUFFIX = "likeSuffix";
    String INLOOP_LIKE_CONTAIN = "likeContain";

    /**
     * @param nameList The list of name as path element for the value. (NotNull)
     * @param expression The expression for the value. (NotNull)
     * @param specifiedSql The specified SQL string for the value. (NotNull)
     * @param commentType The type of comment corresponding the value. (NotNull)
     * @return The new-created set-upper of the value and type. (NotNull)
     */
    BoundValueTracer createBoundValueTracer(List<String> nameList, String expression, String specifiedSql, ParameterCommentType commentType);

    /**
     * @param mightBeLikeDirection It might be the direction of like-search, e.g. likePrefix (NotNull)
     * @return The new-created option of like-search. (NullAllowed: if null, in-loop like-search unsupported)
     */
    FilteringBindOption createInLoopLikeSearchOption(String mightBeLikeDirection);
}
