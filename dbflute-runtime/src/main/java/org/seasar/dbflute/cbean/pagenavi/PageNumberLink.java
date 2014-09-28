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

import java.io.Serializable;

/**
 * The basic DTO of page number link.
 * <pre>
 * page.setPageRangeSize(5);
 * List&lt;PageNumberLink&gt; linkList = page.pageRange().<span style="color: #DD4747">buildPageNumberLinkList</span>(new PageNumberLinkSetupper&lt;PageNumberLink&gt;() {
 *     public PageNumberLink setup(int pageNumberElement, boolean current) {
 *         String href = buildPagingHref(pageNumberElement); <span style="color: #3F7E5E">// for paging navigation links</span>
 *         return new PageNumberLink().initialize(pageNumber, current, href);
 *     }
 * });
 * </pre>
 * @author jflute
 */
public class PageNumberLink implements Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Serial version UID. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The element of page number. */
    protected int _pageNumberElement;

    /** Is the page number for current page? */
    protected boolean _current;

    /** The 'href' string corresponding to the page number. */
    protected String _pageNumberLinkHref;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor. <br />
     * You can initialize attributes by initialize() after this creation.
     */
    public PageNumberLink() {
    }

    // ===================================================================================
    //                                                                         Initializer
    //                                                                         ===========
    /**
     * Initialize basic attributes.
     * @param pageNumberElement The element of page number.
     * @param current Is the page number for current page?
     * @param pageNumberLinkHref The 'href' string corresponding to the page number. (NullAllowed)
     * @return this. (NotNull)
     */
    public PageNumberLink initialize(int pageNumberElement, boolean current, String pageNumberLinkHref) {
        setPageNumberElement(pageNumberElement);
        setCurrent(current);
        setPageNumberLinkHref(pageNumberLinkHref);
        return this;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    /**
     * @return The view string of all attribute values. (NotNull)
     */
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("pageNumberElement=").append(_pageNumberElement);
        sb.append(", pageNumberLinkHref=").append(_pageNumberLinkHref);
        sb.append(", current=").append(_current);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public int getPageNumberElement() {
        return _pageNumberElement;
    }

    public void setPageNumberElement(int pageNumberElement) {
        this._pageNumberElement = pageNumberElement;
    }

    public boolean isCurrent() {
        return _current;
    }

    public void setCurrent(boolean current) {
        this._current = current;
    }

    public String getPageNumberLinkHref() {
        return _pageNumberLinkHref;
    }

    public void setPageNumberLinkHref(String pageNumberLinkHref) {
        this._pageNumberLinkHref = pageNumberLinkHref;
    }
}
