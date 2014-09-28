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
package org.seasar.dbflute.twowaysql.node;

import java.util.List;

import org.seasar.dbflute.twowaysql.context.CommandContext;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public abstract class AbstractNode implements Node {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<Node> _childList = DfCollectionUtil.newArrayList();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public AbstractNode() {
    }

    // ===================================================================================
    //                                                                      Child Handling
    //                                                                      ==============
    public int getChildSize() {
        return _childList.size();
    }

    public Node getChild(int index) {
        return (Node) _childList.get(index);
    }

    public void addChild(Node node) {
        _childList.add(node);
    }

    protected boolean isBeginChildAndValidSql(CommandContext ctx, String sql) {
        return ctx.isBeginChild() && Srl.is_NotNull_and_NotTrimmedEmpty(sql);
    }
}
