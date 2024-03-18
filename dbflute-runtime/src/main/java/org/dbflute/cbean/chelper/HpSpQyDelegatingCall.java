/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.cbean.chelper;

import org.dbflute.cbean.ConditionQuery;

/**
 * @param <CQ> The type of condition-query.
 * @author jflute
 * @since 1.1.0 (2014/10/24 Friday)
 */
public class HpSpQyDelegatingCall<CQ extends ConditionQuery> implements HpSpQyCall<CQ> {

    protected final HpSpQyHas<CQ> _has;
    protected final HpSpQyQy<CQ> _qy;

    public HpSpQyDelegatingCall(HpSpQyHas<CQ> has, HpSpQyQy<CQ> qy) {
        _has = has;
        _qy = qy;
    }

    /** {@inheritDoc} */
    public boolean has() {
        return _has.has();
    }

    /** {@inheritDoc} */
    public CQ qy() {
        return _qy.qy();
    }
}
