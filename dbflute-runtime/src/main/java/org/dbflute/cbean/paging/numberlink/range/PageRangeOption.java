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
package org.dbflute.cbean.paging.numberlink.range;

import java.io.Serializable;

import org.dbflute.cbean.paging.numberlink.PageNumberLinkOption;

/**
 * The option of page range.
 * @author jflute
 */
public class PageRangeOption implements PageNumberLinkOption, Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected int _pageRangeSize;
    protected boolean _fillLimit;

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
        sb.append("pageRangeSize=").append(_pageRangeSize);
        sb.append(", fillLimit=").append(_fillLimit);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the size of page range.
     * @return The size of page range.
     */
    public int getPageRangeSize() {
        return _pageRangeSize;
    }

    /**
     * Set the size of page range.
     * <pre>
     * e.g. range-size=5, current-page=8
     * PageRangeBean pageRange = page.pageRange(op -> op.<span style="color: #CC4747">rangeSize</span>(5));
     * List&lt;Integer&gt; numberList = pageRange.createPageNumberList();
     *
     * <span style="color: #3F7E5E">//  8 / 23 pages (453 records)</span>
     * <span style="color: #3F7E5E">// previous</span> <span style="color: #CC4747">3 4 5 6 7 8 9 10 11 12 13</span> <span style="color: #3F7E5E">next</span>
     * </pre>
     * @param pageRangeSize The size of page range.
     * @return this. (NotNull)
     */
    public PageRangeOption rangeSize(int pageRangeSize) {
        _pageRangeSize = pageRangeSize;
        return this;
    }

    /**
     * Is fill-limit valid?
     * @return The determination, true or false.
     */
    public boolean isFillLimit() {
        return _fillLimit;
    }

    /**
     * Set fill-limit option.
     * <pre>
     * e.g. range-size=5, current-page=8
     * PageRangeBean pageRange = page.pageRange(op -> op.rangeSize(5).<span style="color: #CC4747">fillLimit()</span>);
     * List&lt;Integer&gt; numberList = pageRange.createPageNumberList();
     * 
     * <span style="color: #3F7E5E">//  8 / 23 pages (453 records)</span>
     * <span style="color: #3F7E5E">// previous</span> <span style="color: #CC4747">3 4 5 6 7 8 9 10 11 12 13</span> <span style="color: #3F7E5E">next</span>
     * 
     * <span style="color: #3F7E5E">// e.g. fillLimit=true, current-page=3</span>
     * <span style="color: #3F7E5E">//  3 / 23 pages (453 records)</span>
     * <span style="color: #3F7E5E">//</span> <span style="color: #CC4747">1 2 3 4 5 6 7 8 9 10 11</span> <span style="color: #3F7E5E">next</span>
     * </pre>
     * @return this. (NotNull)
     */
    public PageRangeOption fillLimit() {
        _fillLimit = true;
        return this;
    }
}
