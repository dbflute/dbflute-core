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
package org.dbflute.cbean.paging.numberlink.group;

import java.io.Serializable;

import org.dbflute.cbean.paging.numberlink.PageNumberLinkOption;

/**
 * The option of page group.
 * @author jflute
 */
public class PageGroupOption implements PageNumberLinkOption, Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected int _pageGroupSize;

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    /**
     * @return The view string of all attribute values. (NotNull)
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(" pageGroupSize=").append(_pageGroupSize);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the size of page group.
     * @return The size of page group.
     */
    public int getPageGroupSize() {
        return _pageGroupSize;
    }

    /**
     * Set the size of page group.
     * <pre>
     * e.g. group-size=10, current-page=8
     * PageGroupBean pageGroup = page.pageGroup(op -> op.<span style="color: #DD4747">groupSize</span>(3));
     * List&lt;Integer&gt; numberList = pageGroup.createPageNumberList();
     *
     * <span style="color: #3F7E5E">//  8 / 23 pages (453 records)</span>
     * <span style="color: #3F7E5E">// previous</span> <span style="color: #DD4747">1 2 3 4 5 6 7 8 9 10</span> <span style="color: #3F7E5E">next</span>
     * </pre>
     * @param pageGroupSize The size of page group.
     * @return this. (NotNull)
     */
    public PageGroupOption groupSize(int pageGroupSize) {
        _pageGroupSize = pageGroupSize;
        return this;
    }
}
