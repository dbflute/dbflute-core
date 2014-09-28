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
package org.seasar.dbflute.logic.sql2entity.pmbean;

import java.util.List;

import org.seasar.dbflute.resource.DBFluteSystem;

/**
 * @author jflute
 * @since 0.9.8.4 (2011/05/28 Friday)
 */
public class DfPmbPropertyOptionComment {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String OPTION_PREFIX = "comment(";
    public static final String OPTION_SUFFIX = ")";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfPmbMetaData _pmbMetaData;
    protected final String _propertyName;
    protected final DfPmbPropertyOptionFinder _pmbMetaDataPropertyOptionFinder;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfPmbPropertyOptionComment(DfPmbMetaData pmbMetaData, String propertyName,
            DfPmbPropertyOptionFinder pmbMetaDataPropertyOptionFinder) {
        _pmbMetaData = pmbMetaData;
        _propertyName = propertyName;
        _pmbMetaDataPropertyOptionFinder = pmbMetaDataPropertyOptionFinder;
    }

    // ===================================================================================
    //                                                                      Classification
    //                                                                      ==============
    public boolean hasPmbMetaDataPropertyOptionComment() {
        return extractCommentFromOption(_propertyName) != null;
    }

    protected String extractCommentFromOption(String propertyName) {
        final String pmbMetaDataPropertyOption = getPmbMetaDataPropertyOption();
        if (pmbMetaDataPropertyOption == null) {
            return null;
        }
        String option = pmbMetaDataPropertyOption.trim();
        {
            if (option.trim().length() == 0) {
                return null;
            }
            final List<String> splitOption = splitOption(option);
            String firstOption = null;
            for (String element : splitOption) {
                if (element.startsWith(OPTION_PREFIX) && element.endsWith(OPTION_SUFFIX)) {
                    firstOption = element;
                    break;
                }
            }
            if (firstOption == null) {
                return null;
            }
            option = firstOption;
        }
        final int commentIdx = OPTION_PREFIX.length();
        final int commentEndIdx = option.length() - OPTION_SUFFIX.length();
        try {
            return option.substring(commentIdx, commentEndIdx);
        } catch (StringIndexOutOfBoundsException e) {
            String msg = "Look at the message below:" + ln();
            msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * " + ln();
            msg = msg + "IndexOutOfBounds ocurred:" + ln();
            msg = msg + " " + _pmbMetaData.getClassName() + " " + _propertyName;
            msg = msg + ":" + option + ln();
            msg = msg + "{" + option + "}.substring(" + commentIdx + ", " + commentEndIdx + ")" + ln();
            msg = msg + "* * * * * * * * * */";
            throw new IllegalStateException(msg, e);
        }
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected String getPmbMetaDataPropertyOption() {
        return _pmbMetaDataPropertyOptionFinder.findPmbMetaDataPropertyOption(_propertyName);
    }

    protected List<String> splitOption(String option) {
        return DfPmbPropertyOptionFinder.splitOption(option);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }
}
