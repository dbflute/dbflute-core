/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.twowaysql.node;

import org.dbflute.twowaysql.node.ForNode.LoopVariableType;

/**
 * @author jflute
 */
public class LoopFirstNode extends LoopAbstractNode implements SqlConnectorAdjustable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String MARK = "FIRST";

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public LoopFirstNode(String expression, String specifiedSql) {
        super(expression, specifiedSql);
    }

    // ===================================================================================
    //                                                                              Accept
    //                                                                              ======
    @Override
    protected LoopVariableType getLoopVariableType() {
        return LoopVariableType.FIRST;
    }

    @Override
    protected boolean isValid(int loopSize, int loopIndex) {
        return loopIndex == 0;
    }
}