/*
 * Copyright 2014-2014 the original author or authors.
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
package org.dbflute.bhv.core.melodicsql;

import java.util.List;

import org.dbflute.cbean.coption.LikeSearchOption;
import org.dbflute.twowaysql.factory.NodeAdviceFactory;
import org.dbflute.twowaysql.node.BoundValueTracer;
import org.dbflute.twowaysql.node.FilteringBindOption;
import org.dbflute.twowaysql.node.ParameterCommentType;

/**
 * @author jflute
 * @since 1.1.0 (2014/09/29 Monday)
 */
public class MelodicNodeAdviceFactory implements NodeAdviceFactory {

    @Override
    public BoundValueTracer createBoundValueTracer(List<String> nameList, String expression, String specifiedSql,
            ParameterCommentType commentType) {
        return newMelodicBoundValueTracer(nameList, expression, specifiedSql, commentType);
    }

    protected MelodicBoundValueTracer newMelodicBoundValueTracer(List<String> nameList, String expression,
            String specifiedSql, ParameterCommentType commentType) {
        return new MelodicBoundValueTracer(nameList, expression, specifiedSql, commentType);
    }

    @Override
    public FilteringBindOption createInLoopLikeSearchOption(String likeDirection) {
        final LikeSearchOption option = newLikeSearchOption();
        if (likeDirection.equals(INLOOP_LIKE_PREFIX)) {
            return option.likePrefix();
        } else if (likeDirection.equals(INLOOP_LIKE_SUFFIX)) {
            return option.likeSuffix();
        } else if (likeDirection.equals(INLOOP_LIKE_CONTAIN)) {
            return option.likeContain();
        } else { // basically no way
            String msg = "Unknown direction of like-search: " + likeDirection;
            throw new IllegalStateException(msg);
        }
    }

    protected LikeSearchOption newLikeSearchOption() {
        return new LikeSearchOption();
    }
}
