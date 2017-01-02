/*
 * Copyright 2014-2017 the original author or authors.
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

    protected MelodicBoundValueTracer newMelodicBoundValueTracer(List<String> nameList, String expression, String specifiedSql,
            ParameterCommentType commentType) {
        return new MelodicBoundValueTracer(nameList, expression, specifiedSql, commentType);
    }

    @Override
    public FilteringBindOption prepareInLoopLikeSearchOption(String mightBeLikeDirection) {
        final LikeSearchOption option = newLikeSearchOption();
        if (mightBeLikeDirection.equals(INLOOP_LIKE_PREFIX)) {
            return option.likePrefix();
        } else if (mightBeLikeDirection.equals(INLOOP_LIKE_SUFFIX)) {
            return option.likeSuffix();
        } else if (mightBeLikeDirection.equals(INLOOP_LIKE_CONTAIN)) {
            return option.likeContain();
        } else { // other options e.g. cls(...)
            return null;
        }
    }

    protected LikeSearchOption newLikeSearchOption() {
        return new LikeSearchOption();
    }
}
