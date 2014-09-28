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
package org.seasar.dbflute.cbean.pagenavi;

/**
 * The set-upper of page number link.
 * <pre>
 * page.setPageRangeSize(5);
 * List&lt;PageNumberLink&gt; linkList = page.pageRange().<span style="color: #DD4747">buildPageNumberLinkList</span>(new PageNumberLinkSetupper&lt;PageNumberLink&gt;() {
 *     public PageNumberLink setup(int pageNumberElement, boolean current) {
 *         String href = buildPagingHref(pageNumberElement); <span style="color: #3F7E5E">// for paging navigation links</span>
 *         return new PageNumberLink().initialize(pageNumberElement, current, href);
 *     }
 * });
 * </pre>
 * @param <LINK> The type of link.
 * @author jflute
 */
public interface PageNumberLinkSetupper<LINK extends PageNumberLink> {

    /**
     * Set up page number link.
     * @param pageNumberElement Page number element.
     * @param current Is current page?
     * @return Page number link. (NotNull)
     */
    LINK setup(int pageNumberElement, boolean current);
}
