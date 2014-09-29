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

import org.dbflute.bhv.core.context.ResourceContext;
import org.dbflute.cbean.coption.LikeSearchOption;
import org.dbflute.dbway.DBWay;
import org.dbflute.twowaysql.node.BoundValueTracer;
import org.dbflute.twowaysql.node.FilteringBindOption;
import org.dbflute.twowaysql.node.ParameterCommentType;

/**
 * @author jflute
 * @since 1.1.0 (2014/09/29 Monday)
 */
public class MelodicBoundValueTracer extends BoundValueTracer {

    public MelodicBoundValueTracer(List<String> nameList, String expression, String specifiedSql,
            ParameterCommentType commentType) {
        super(nameList, expression, specifiedSql, commentType);
    }

    @Override
    protected void adjustLikeSearchDBWay(FilteringBindOption option) {
        if (option == null) {
            return;
        }
        final DBWay dbway = ResourceContext.currentDBDef().dbway();
        if (option instanceof LikeSearchOption) {
            ((LikeSearchOption) option).acceptOriginalWildCardList(dbway.getOriginalWildCardList());
        }
        option.acceptStringConnector(dbway.getStringConnector());
    }
}
