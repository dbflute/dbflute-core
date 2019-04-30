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
package org.dbflute.cbean.paging;

import org.dbflute.jdbc.FetchBean;

/**
 * The bean for fetch narrowing.
 * @author jflute
 */
public interface FetchNarrowingBean extends FetchBean {

    /**
     * Get fetch start index.
     * @return Fetch start index.
     */
    int getFetchNarrowingSkipStartIndex();

    /**
     * Get fetch size.
     * @return Fetch size.
     */
    int getFetchNarrowingLoopCount();

    /**
     * Is fetch start index supported?
     * @return The determination, true or false.
     */
    boolean isFetchNarrowingSkipStartIndexEffective();

    /**
     * Is fetch size supported?
     * @return The determination, true or false.
     */
    boolean isFetchNarrowingLoopCountEffective();

    /**
     * Is fetch-narrowing effective?
     * @return The determination, true or false.
     */
    boolean isFetchNarrowingEffective();

    /**
     * Disable fetch narrowing. Only checking safety result size is valid. {INTERNAL METHOD}
     */
    void xdisableFetchNarrowing();

    /**
     * Enable ignored fetch narrowing. {INTERNAL METHOD}
     */
    void xenableIgnoredFetchNarrowing();
}
