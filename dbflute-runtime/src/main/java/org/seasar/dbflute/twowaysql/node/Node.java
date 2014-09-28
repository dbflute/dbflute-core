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

import org.seasar.dbflute.twowaysql.context.CommandContext;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public interface Node {

    /**
     * Get the size of children.
     * @return The size of children.
     */
    int getChildSize();

    /**
     * Get the child node by the index.
     * @param index The value of index
     * @return The node instance for the corresponding child. (NotNull)
     */
    Node getChild(int index);

    /**
     * Add the child node.
     * @param node The node instance for the added child. (NotNull)
     */
    void addChild(Node node);

    /**
     * Accept the context of command.
     * @param ctx The instance of command context. (NotNull)
     */
    void accept(CommandContext ctx);
}
