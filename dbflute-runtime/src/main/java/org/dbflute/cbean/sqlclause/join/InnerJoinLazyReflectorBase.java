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
package org.dbflute.cbean.sqlclause.join;

/**
 * @author jflute
 * @since 0.9.9.0A (2011/07/27 Wednesday)
 */
public abstract class InnerJoinLazyReflectorBase implements InnerJoinLazyReflector {

    protected final InnerJoinNoWaySpeaker _noWaySpeaker;

    public InnerJoinLazyReflectorBase(InnerJoinNoWaySpeaker noWaySpeaker) {
        _noWaySpeaker = noWaySpeaker;
    }

    /**
     * {@inheritDoc}
     */
    public void reflect() {
        if (_noWaySpeaker == null || !_noWaySpeaker.isNoWayInner()) {
            doReflect();
        }
    }

    /**
     * Reflect inner-join to the corresponding join info lazily.
     */
    protected abstract void doReflect();
}
