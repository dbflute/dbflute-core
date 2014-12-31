/*
 * Copyright 2014-2015 the original author or authors.
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
package org.dbflute.properties.initializer;

/**
 * @author jflute
 * @since 1.1.0 (2014/10/22 Wednesday)
 */
public class DfAdditionalForeignKeyOption {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _fixedCondition;
    protected String _fixedSuffix;
    protected boolean _fixedInline;
    protected boolean _fixedReferrer;
    protected boolean _fixedOnlyJoin;
    protected String _comment;
    protected boolean _suppressJoin;
    protected boolean _suppressSubQuery;
    protected String _deprecated;

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "additionalFK:{" + _fixedSuffix + ", " + _comment + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getFixedCondition() {
        return _fixedCondition;
    }

    public void setFixedCondition(String fixedCondition) {
        _fixedCondition = fixedCondition;
    }

    public String getFixedSuffix() {
        return _fixedSuffix;
    }

    public void setFixedSuffix(String fixedSuffix) {
        _fixedSuffix = fixedSuffix;
    }

    public boolean isFixedInline() {
        return _fixedInline;
    }

    public void setFixedInline(boolean fixedInline) {
        _fixedInline = fixedInline;
    }

    public boolean isFixedReferrer() {
        return _fixedReferrer;
    }

    public void setFixedReferrer(boolean fixedReferrer) {
        _fixedReferrer = fixedReferrer;
    }

    public boolean isFixedOnlyJoin() {
        return _fixedOnlyJoin;
    }

    public void setFixedOnlyJoin(boolean fixedOnlyJoin) {
        _fixedOnlyJoin = fixedOnlyJoin;
    }

    public String getComment() {
        return _comment;
    }

    public void setComment(String comment) {
        _comment = comment;
    }

    public boolean isSuppressJoin() {
        return _suppressJoin;
    }

    public void setSuppressJoin(boolean suppressJoin) {
        _suppressJoin = suppressJoin;
    }

    public boolean isSuppressSubQuery() {
        return _suppressSubQuery;
    }

    public void setSuppressSubQuery(boolean suppressSubQuery) {
        _suppressSubQuery = suppressSubQuery;
    }

    public String getDeprecated() {
        return _deprecated;
    }

    public void setDeprecated(String deprecated) {
        _deprecated = deprecated;
    }
}
